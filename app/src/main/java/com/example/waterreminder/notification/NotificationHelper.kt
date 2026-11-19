package com.example.waterreminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.waterreminder.MainActivity
import com.example.waterreminder.R
import com.example.waterreminder.receiver.ReminderBroadcastReceiver

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "water_reminder_sound_vibrate_v3"
        private const val CHANNEL_SOUND_ONLY = "water_reminder_sound_v3"
        private const val CHANNEL_VIBRATE_ONLY = "water_reminder_vibrate_v3"
        private const val CHANNEL_SILENT = "water_reminder_silent_v3"
        const val CHANNEL_NAME = "Hydration Reminders"
        const val NOTIFICATION_ID = 1001

        const val ACTION_DRINK = "com.example.waterreminder.ACTION_DRINK"
        const val ACTION_SNOOZE = "com.example.waterreminder.ACTION_SNOOZE"
        const val ACTION_SKIP = "com.example.waterreminder.ACTION_SKIP"
        const val EXTRA_AMOUNT_ML = "extra_amount_ml"
        const val EXTRA_SHOW_REMINDER_BOT = "extra_show_reminder_bot"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fun channel(id: String, name: String, sound: Boolean, vibration: Boolean) =
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Delivers scheduled water break reminders"
                    enableVibration(vibration)
                    setShowBadge(true)
                    if (!sound) setSound(null, null)
                }

            notificationManager.createNotificationChannels(
                listOf(
                    channel(CHANNEL_ID, CHANNEL_NAME, sound = true, vibration = true),
                    channel(CHANNEL_SOUND_ONLY, "$CHANNEL_NAME (sound)", sound = true, vibration = false),
                    channel(CHANNEL_VIBRATE_ONLY, "$CHANNEL_NAME (vibrate)", sound = false, vibration = true),
                    channel(CHANNEL_SILENT, "$CHANNEL_NAME (silent)", sound = false, vibration = false)
                )
            )
        }
    }

    fun showReminderNotification(
        userName: String = "",
        quickAddAmountMl: Int = 250,
        soundEnabled: Boolean = true,
        vibrateEnabled: Boolean = true
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val nameGreeting = if (userName.isNotBlank()) " $userName" else ""
        val titleText = "Time to Hydrate$nameGreeting!"
        val messageText = "Take a moment to drink water and maintain your healthy hydration habit!"

        // Tap notification -> Open MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SHOW_REMINDER_BOT, true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Drink
        val drinkIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_DRINK
            putExtra(EXTRA_AMOUNT_ML, quickAddAmountMl)
        }
        val drinkPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            drinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze
        val snoozeIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Skip
        val skipIntent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_SKIP
        }
        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when {
            soundEnabled && vibrateEnabled -> CHANNEL_ID
            soundEnabled -> CHANNEL_SOUND_ONLY
            vibrateEnabled -> CHANNEL_VIBRATE_ONLY
            else -> CHANNEL_SILENT
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_water)
            .setContentTitle(titleText)
            .setContentText(messageText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(messageText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, "Drink +${quickAddAmountMl}ml", drinkPendingIntent)
            .addAction(0, "Snooze", snoozePendingIntent)
            .addAction(0, "Skip", skipPendingIntent)

        if (!vibrateEnabled) {
            builder.setVibrate(longArrayOf(0L))
        }

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
