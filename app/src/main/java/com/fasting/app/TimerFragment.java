package com.fasting.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * 计时页面 — 显示圆形倒计时
 */
public class TimerFragment extends Fragment {

    private CircularProgressView progressView;
    private TextView stateLabel;
    private TextView countdownLabel;
    private Button btnStart;
    private Button btnReset;

    private FastingManager manager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tickRunnable;
    private boolean isRunning = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        // 绑定视图
        progressView = view.findViewById(R.id.circular_progress);
        stateLabel = view.findViewById(R.id.tv_state_label);
        countdownLabel = view.findViewById(R.id.tv_countdown_label);
        btnStart = view.findViewById(R.id.btn_start);
        btnReset = view.findViewById(R.id.btn_reset);

        // 初始化管理器
        manager = FastingManager.getInstance(requireContext());

        // 按钮事件
        btnStart.setOnClickListener(v -> {
            manager.startFasting();
            updateUI();
            startTicking();
        });

        btnReset.setOnClickListener(v -> {
            manager.reset();
            updateUI();
            stopTicking();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 检查是否有自动切换
        manager.checkAutoTransition();
        updateUI();

        FastingManager.State state = manager.getState();
        if (state != FastingManager.State.IDLE) {
            startTicking();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTicking();
    }

    private void updateUI() {
        FastingManager.State state = manager.getState();

        if (state == FastingManager.State.IDLE) {
            // 初始状态
            progressView.setProgressImmediate(0f);
            progressView.setTimeText("16:00:00");
            progressView.setLabelText("准备开始");
            progressView.setProgressColor(ContextCompat.getColor(requireContext(), R.color.fasting_color));
            progressView.setLabelColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

            stateLabel.setText("16:8 轻断食");
            stateLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            countdownLabel.setText("点击开始，进入16小时断食");
            countdownLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

            btnStart.setVisibility(View.VISIBLE);
            btnStart.setText("开始断食");
            btnStart.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.fasting_color));
        } else {
            // 进行中
            btnStart.setVisibility(View.GONE);

            if (state == FastingManager.State.FASTING) {
                progressView.setProgressColor(ContextCompat.getColor(requireContext(), R.color.fasting_color));
                progressView.setLabelColor(ContextCompat.getColor(requireContext(), R.color.fasting_color));
                stateLabel.setText("断食中");
                stateLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.fasting_color));
                countdownLabel.setText("剩余断食时间");
                countdownLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.fasting_color));
            } else {
                progressView.setProgressColor(ContextCompat.getColor(requireContext(), R.color.eating_color));
                progressView.setLabelColor(ContextCompat.getColor(requireContext(), R.color.eating_color));
                stateLabel.setText("进食窗口");
                stateLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.eating_color));
                countdownLabel.setText("剩余进食时间");
                countdownLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.eating_color));
            }

            updateTimerDisplay();
        }
    }

    private void updateTimerDisplay() {
        FastingManager.State state = manager.getState();
        if (state == FastingManager.State.IDLE) return;

        long remaining = manager.getRemainingMillis();
        float progress = manager.getProgress();

        progressView.setProgress(progress);
        progressView.setTimeText(FastingManager.formatDuration(remaining));
        progressView.setLabelText(manager.getStateLabel());
    }

    private void startTicking() {
        if (isRunning) return;
        isRunning = true;

        tickRunnable = new Runnable() {
            @Override
            public void run() {
                // 检查自动切换
                boolean transitioned = manager.checkAutoTransition();
                if (transitioned) {
                    updateUI();
                    // 通知 StatsFragment 刷新
                    notifyStatsRefresh();
                }

                FastingManager.State state = manager.getState();
                if (state == FastingManager.State.IDLE) {
                    stopTicking();
                    updateUI();
                    return;
                }

                updateTimerDisplay();
                handler.postDelayed(this, 1000); // 每秒刷新
            }
        };
        handler.post(tickRunnable);
    }

    private void stopTicking() {
        isRunning = false;
        if (tickRunnable != null) {
            handler.removeCallbacks(tickRunnable);
        }
    }

    private void notifyStatsRefresh() {
        // 通过 Activity 通知统计页面刷新
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onCycleCompleted();
        }
    }
}
