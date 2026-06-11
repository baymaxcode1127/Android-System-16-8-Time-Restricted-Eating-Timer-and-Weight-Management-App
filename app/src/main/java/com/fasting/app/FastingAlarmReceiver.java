package com.fasting.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 闹钟广播接收器 — 到时弹出通知
 */
public class FastingAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        int notificationId = intent.getIntExtra("notification_id", 0);

        if (title == null) title = "断食提醒";
        if (text == null) text = "";

        NotificationHelper.sendNotification(context, title, text, notificationId);
    }
}
