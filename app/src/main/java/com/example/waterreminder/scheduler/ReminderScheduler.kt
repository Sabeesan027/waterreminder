package com.example.waterreminder.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.ScheduleRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.receiver.ReminderBroadcastReceiver
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime

class ReminderScheduler(
    private val context: Context,
    private val scheduleRepository: ScheduleRepository,
    private val waterRepository: WaterRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val reminderEngine = ReminderEngine()

    companion object {
        const val ACTION_REMINDER_TRIGGER = "com.example.waterreminder.ACTION_REMINDER_TRIGGER"
        const val REQUEST_CODE_REMINDER = 2001
    }

    suspend fun scheduleNextReminder(snoozedUntilMs: Long? = null): Long? {
        val userPrefs = userPreferencesRepository.userPreferencesFlow.first()
        if (!userPrefs.remindersEnabled) {
            userPreferencesRepository.setSnoozedUntilMs(null)
            cancelReminder()
            return null
        }

        if (snoozedUntilMs != null) {
            userPreferencesRepository.setSnoozedUntilMs(snoozedUntilMs)
        }
        var effectiveSnooze = snoozedUntilMs ?: userPreferencesRepository.getSnoozedUntilMs()
        if (effectiveSnooze != null && effectiveSnooze <= System.currentTimeMillis()) {
            userPreferencesRepository.setSnoozedUntilMs(null)
            effectiveSnooze = null
        }

        val activeSchedules = scheduleRepository.getActiveSchedulesSync()
        if (activeSchedules.isEmpty() && effectiveSnooze == null) {
            cancelReminder()
            return null
        }

        val timesMap = activeSchedules.associate { s ->
            s.id to scheduleRepository.getTimesForScheduleSync(s.id)
        }
        val breaksMap = activeSchedules.associate { s ->
            s.id to scheduleRepository.getBreaksForScheduleSync(s.id)
        }

        val todayStr = LocalDate.now().toString()
        val summaryToday = waterRepository.getHydrationSummary(
            date = todayStr,
            defaultGoalMl = userPrefs.dailyGoalMl,
            workAllocationPercent = userPrefs.workAllocationPercent,
            workStartTime = userPrefs.workStartTime,
            workEndTime = userPrefs.workEndTime
        ).first()

        val lastLogTimestampMs = waterRepository.getLastLogTimestamp()

        val nextTimeMs = reminderEngine.calculateNextReminderTime(
            now = LocalDateTime.now(),
            schedules = activeSchedules,
            timesMap = timesMap,
            breaksMap = breaksMap,
            userPreferences = userPrefs,
            lastLogTimestampMs = lastLogTimestampMs,
            summaryToday = summaryToday,
            snoozedUntilMs = effectiveSnooze
        )

        if (nextTimeMs != null) {
            setAlarm(nextTimeMs)
        } else {
            cancelReminder()
        }

        return nextTimeMs
    }

    private fun setAlarm(triggerAtMs: Long) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMs,
                    pendingIntent
                )
            } catch (e: Exception) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMs,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMs,
                pendingIntent
            )
        }
    }

    fun cancelReminder() {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            action = ACTION_REMINDER_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
