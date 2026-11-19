package com.example.waterreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waterreminder.WaterReminderApplication
import com.example.waterreminder.data.local.AppDatabase
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.ScheduleRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.domain.model.LogSource
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.notification.NotificationHelper
import com.example.waterreminder.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val waterRepo = WaterRepository(db.waterLogDao(), db.dailyGoalDao())
                val scheduleRepo = ScheduleRepository(
                    db.reminderScheduleDao(),
                    db.reminderTimeDao(),
                    db.breakPeriodDao()
                )
                val prefsRepo = UserPreferencesRepository(context)
                val notificationHelper = NotificationHelper(context)
                val scheduler = ReminderScheduler(context, scheduleRepo, waterRepo, prefsRepo)

                val userPrefs = prefsRepo.userPreferencesFlow.first()

                when (intent.action) {
                    ReminderScheduler.ACTION_REMINDER_TRIGGER -> {
                        if (userPrefs.remindersEnabled) {
                            (context.applicationContext as? WaterReminderApplication)?.showReminderBot()
                            notificationHelper.showReminderNotification(
                                userName = userPrefs.userName,
                                quickAddAmountMl = userPrefs.quickAddMl,
                                soundEnabled = userPrefs.soundEnabled,
                                vibrateEnabled = userPrefs.vibrateEnabled
                            )
                        }
                        scheduler.scheduleNextReminder()
                    }

                    NotificationHelper.ACTION_DRINK -> {
                        notificationHelper.dismissNotification()
                        val amountMl = intent.getIntExtra(
                            NotificationHelper.EXTRA_AMOUNT_ML,
                            userPrefs.quickAddMl
                        )
                        val date = LocalDate.now().toString()
                        val log = WaterLog(
                            amountMl = amountMl,
                            timestamp = System.currentTimeMillis(),
                            source = LogSource.NOTIFICATION
                        )
                        waterRepo.addWaterLog(log, date)
                        scheduler.scheduleNextReminder()
                    }

                    NotificationHelper.ACTION_SNOOZE -> {
                        notificationHelper.dismissNotification()
                        val defaultSchedule = scheduleRepo.getActiveSchedulesSync().firstOrNull()
                        val snoozeMinutes = defaultSchedule?.snoozeMinutes ?: 15
                        val snoozeUntilMs = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
                        scheduler.scheduleNextReminder(snoozedUntilMs = snoozeUntilMs)
                    }

                    NotificationHelper.ACTION_SKIP -> {
                        notificationHelper.dismissNotification()
                        scheduler.scheduleNextReminder()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
