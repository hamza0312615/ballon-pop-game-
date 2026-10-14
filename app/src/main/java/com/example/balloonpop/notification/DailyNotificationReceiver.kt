package com.example.balloonpop.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Re-schedule alarm on phone reboot
                DailyNotificationHelper.scheduleDailyNotification(context)
            }
            DailyNotificationHelper.ACTION_DAILY_ALARM -> {
                // Trigger once-a-day notification
                DailyNotificationHelper.showNotification(context, isTest = false)
            }
        }
    }
}
