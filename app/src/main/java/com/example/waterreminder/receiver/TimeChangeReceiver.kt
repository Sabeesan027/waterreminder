package com.example.waterreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.waterreminder.data.local.AppDatabase
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.ScheduleRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIME_CHANGED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
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
                    val scheduler = ReminderScheduler(context, scheduleRepo, waterRepo, prefsRepo)

                    scheduler.scheduleNextReminder()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
