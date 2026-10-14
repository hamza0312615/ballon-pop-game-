package com.example.balloonpop.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.balloonpop.MainActivity
import com.example.balloonpop.R
import java.util.Calendar

object DailyNotificationHelper {
    const val CHANNEL_ID = "daily_balloon_rewards_channel"
    const val CHANNEL_NAME = "Daily Game Bonus & Streak Reminders"
    const val NOTIFICATION_ID = 7701
    const val ACTION_DAILY_ALARM = "com.example.balloonpop.ACTION_DAILY_NOTIFICATION"
    private const val PREFS_NAME = "balloon_pop_notification_prefs"
    private const val KEY_NOTIFS_ENABLED = "daily_notifs_enabled"

    private val DAILY_MESSAGES = listOf(
        Pair("🎡 Free Daily Spin Ready!", "Your lucky wheel spin is waiting! Win up to 1,500 coins and 25 gems today!"),
        Pair("🎈 Keep Your Daily Streak Alive!", "Don't lose your streak bonus! Pop balloons now to claim awesome rewards!"),
        Pair("⚡ New Daily Quests Available!", "Fresh quests are active! Complete them to earn massive coin and diamond bonuses!"),
        Pair("💥 Sky Challenge Awaits!", "Can you beat the 15-second balloon rush? Jump in and pop your way to a new high score!")
    )

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders for free spins, rewards, and balloon popping challenges"
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun isNotificationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFS_ENABLED, true)
    }

    fun setNotificationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFS_ENABLED, enabled).apply()
        if (enabled) {
            scheduleDailyNotification(context)
        } else {
            cancelDailyNotification(context)
        }
    }

    /**
     * Schedules a daily recurring notification once per day at 12:00 PM noon
     * (or next day noon if already passed).
     */
    fun scheduleDailyNotification(context: Context) {
        if (!isNotificationEnabled(context)) return

        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
            action = ACTION_DAILY_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If 12:00 PM already passed today, schedule for tomorrow noon
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            // Fallback for strict alarm restrictions
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelDailyNotification(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyNotificationReceiver::class.java).apply {
            action = ACTION_DAILY_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Posts the notification to the system notification drawer.
     */
    fun showNotification(context: Context, isTest: Boolean = false) {
        createNotificationChannel(context)

        // Select a message
        val dayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) % DAILY_MESSAGES.size
        val (title, text) = if (isTest) {
            Pair("🎈 Daily Reminder Active!", "Daily notification is working perfectly! You'll receive rewards once a day.")
        } else {
            DAILY_MESSAGES[dayIndex]
        }

        // Tap opens MainActivity directly into the game
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet on Android 13+
        }
    }
}
