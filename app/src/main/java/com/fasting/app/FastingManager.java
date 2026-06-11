package com.fasting.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 断食/进食状态管理器（单例）
 * 管理16小时断食 + 8小时进食的循环计时
 */
public class FastingManager {

    private static final String PREF_NAME = "fasting_prefs";
    private static FastingManager instance;

    // 状态常量
    public enum State { IDLE, FASTING, EATING }

    // 时长（毫秒）
    public static final long FASTING_DURATION_MS = 16L * 60 * 60 * 1000; // 16小时
    public static final long EATING_DURATION_MS = 8L * 60 * 60 * 1000;   // 8小时

    // SharedPreferences 键
    private static final String KEY_STATE = "state";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_COMPLETED_CYCLES = "completed_cycles";
    private static final String KEY_LAST_END_TIME = "last_end_time";

    private final SharedPreferences prefs;
    private final Context context;

    private FastingManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized FastingManager getInstance(Context context) {
        if (instance == null) {
            instance = new FastingManager(context);
        }
        return instance;
    }

    // ==================== 状态管理 ====================

    /** 获取当前状态 */
    public State getState() {
        String stateStr = prefs.getString(KEY_STATE, State.IDLE.name());
        try {
            return State.valueOf(stateStr);
        } catch (IllegalArgumentException e) {
            return State.IDLE;
        }
    }

    /** 设置状态并持久化 */
    private void setState(State state) {
        prefs.edit().putString(KEY_STATE, state.name()).apply();
    }

    /** 获取当前周期开始时间 */
    public long getStartTime() {
        return prefs.getLong(KEY_START_TIME, 0);
    }

    /** 设置开始时间 */
    private void setStartTime(long time) {
        prefs.edit().putLong(KEY_START_TIME, time).apply();
    }

    // ==================== 操作 ====================

    /**
     * 开始断食（从 IDLE 或 EATING 结束后调用）
     */
    public void startFasting() {
        long now = System.currentTimeMillis();
        setState(State.FASTING);
        setStartTime(now);

        // 预约断食结束通知
        NotificationHelper.scheduleNotification(
                context,
                context.getString(R.string.fasting_end_title),
                context.getString(R.string.fasting_end_text),
                now + FASTING_DURATION_MS,
                1001
        );
    }

    /**
     * 开始进食窗口（断食结束后自动调用）
     */
    public void startEating() {
        long now = System.currentTimeMillis();
        setState(State.EATING);
        setStartTime(now);

        // 预约进食结束通知
        NotificationHelper.scheduleNotification(
                context,
                context.getString(R.string.eating_end_title),
                context.getString(R.string.eating_end_text),
                now + EATING_DURATION_MS,
                1002
        );
    }

    /**
     * 重置 — 回到 IDLE 状态（从断食开始界面）
     */
    public void reset() {
        setState(State.IDLE);
        setStartTime(0);

        // 取消所有预约通知
        NotificationHelper.cancelAll(context);
    }

    // ==================== 计时计算 ====================

    /** 获取当前阶段的总时长 */
    public long getTotalDuration() {
        State state = getState();
        if (state == State.FASTING) return FASTING_DURATION_MS;
        if (state == State.EATING) return EATING_DURATION_MS;
        return 0;
    }

    /** 获取已过去的时间（毫秒） */
    public long getElapsedMillis() {
        State state = getState();
        if (state == State.IDLE) return 0;

        long elapsed = System.currentTimeMillis() - getStartTime();
        long total = getTotalDuration();
        return Math.min(elapsed, total);
    }

    /** 获取剩余时间（毫秒） */
    public long getRemainingMillis() {
        State state = getState();
        if (state == State.IDLE) return 0;

        long remaining = getTotalDuration() - getElapsedMillis();
        return Math.max(0, remaining);
    }

    /** 获取进度 (0.0 ~ 1.0) */
    public float getProgress() {
        State state = getState();
        if (state == State.IDLE) return 0f;

        long total = getTotalDuration();
        if (total == 0) return 0f;
        return Math.min(1f, (float) getElapsedMillis() / total);
    }

    // ==================== 自动切换检测 ====================

    /**
     * 检查是否需要自动切换状态
     * 应在 onResume 或定时器中调用
     * @return 是否发生了状态切换
     */
    public boolean checkAutoTransition() {
        State state = getState();
        if (state == State.IDLE) return false;

        long remaining = getRemainingMillis();
        if (remaining <= 0) {
            // 时间到了，切换到下一个状态
            if (state == State.FASTING) {
                // 断食结束 → 开始进食
                startEating();
                return true;
            } else if (state == State.EATING) {
                // 进食结束 → 完成一个完整周期，自动开始新断食
                incrementCompletedCycles();
                startFasting();
                return true;
            }
        }
        return false;
    }

    // ==================== 完成周期计数 ====================

    /** 获取成功完成的轻断食天数（完整 16+8 周期数） */
    public int getCompletedCycles() {
        return prefs.getInt(KEY_COMPLETED_CYCLES, 0);
    }

    /** 完成一个周期（进食结束后调用） */
    private void incrementCompletedCycles() {
        int current = getCompletedCycles();
        prefs.edit().putInt(KEY_COMPLETED_CYCLES, current + 1).apply();
    }

    // ==================== 格式化 ====================

    /** 将毫秒格式化为 HH:MM:SS */
    public static String formatDuration(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /** 获取当前状态的中文标签 */
    public String getStateLabel() {
        State state = getState();
        switch (state) {
            case FASTING:
                return "断食中";
            case EATING:
                return "进食窗口";
            default:
                return "准备开始";
        }
    }

    /** 获取当前状态的倒计时标签 */
    public String getCountdownLabel() {
        State state = getState();
        switch (state) {
            case FASTING:
                return "剩余断食时间";
            case EATING:
                return "剩余进食时间";
            default:
                return "";
        }
    }
}
