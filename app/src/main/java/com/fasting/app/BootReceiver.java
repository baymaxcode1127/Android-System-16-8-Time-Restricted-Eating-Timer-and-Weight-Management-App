package com.fasting.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机广播接收器 — 设备重启后重新预约未完成的闹钟
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        FastingManager manager = FastingManager.getInstance(context);
        FastingManager.State state = manager.getState();

        if (state == FastingManager.State.IDLE) return;

        long remaining = manager.getRemainingMillis();
        long triggerTime = System.currentTimeMillis() + remaining;

        if (state == FastingManager.State.FASTING) {
            NotificationHelper.scheduleNotification(
                    context,
                    context.getString(R.string.fasting_end_title),
                    context.getString(R.string.fasting_end_text),
                    triggerTime,
                    1001
            );
        } else if (state == FastingManager.State.EATING) {
            NotificationHelper.scheduleNotification(
                    context,
                    context.getString(R.string.eating_end_title),
                    context.getString(R.string.eating_end_text),
                    triggerTime,
                    1002
            );
        }
    }
}
