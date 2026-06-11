package com.fasting.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

/**
 * 通知辅助类 — 管理通知渠道和闹钟预约
 */
public class NotificationHelper {

    private static final String TAG = "FastingNotif";
    private static final String CHANNEL_ID = "fasting_channel";

    /**
     * 创建通知渠道（Android 8.0+ 必须）
     */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "断食提醒",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("断食和进食时间结束提醒");
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 发送即时通知
     */
    public static void sendNotification(Context context, String title, String text, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 200, 300});

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }

    /**
     * 获取兼容的 PendingIntent flags
     */
    private static int getPendingIntentFlags() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 31) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }

    /**
     * 预约未来某个时间的通知（使用 AlarmManager）
     */
    public static void scheduleNotification(Context context, String title, String text,
                                            long triggerTimeMillis, int requestCode) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, FastingAlarmReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("text", text);
            intent.putExtra("notification_id", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    getPendingIntentFlags()
            );

            // 尝试使用精确闹钟，失败则回退到普通闹钟
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeMillis,
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeMillis,
                            pendingIntent
                    );
                }
            } catch (SecurityException e) {
                // SCHEDULE_EXACT_ALARM 权限被拒绝（Android 12+），回退到不精确闹钟
                Log.w(TAG, "精确闹钟权限不足，使用普通闹钟: " + e.getMessage());
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                );
            }
        } catch (Exception e) {
            // 捕获所有异常，确保 App 不会崩溃
            Log.e(TAG, "预约通知失败: " + e.getMessage(), e);
        }
    }

    /**
     * 取消所有预约通知
     */
    public static void cancelAll(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            for (int requestCode : new int[]{1001, 1002}) {
                Intent intent = new Intent(context, FastingAlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        getPendingIntentFlags()
                );
                alarmManager.cancel(pendingIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "取消通知失败: " + e.getMessage(), e);
        }
    }
}
