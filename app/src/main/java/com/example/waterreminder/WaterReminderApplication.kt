package com.example.waterreminder

import android.app.Application
import com.example.waterreminder.data.local.AppDatabase
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.ScheduleRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WaterReminderApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var waterRepository: WaterRepository
        private set
    lateinit var scheduleRepository: ScheduleRepository
        private set
    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set
    lateinit var reminderScheduler: ReminderScheduler
        private set

    private val _reminderBotVisible = MutableStateFlow(false)
    val reminderBotVisible = _reminderBotVisible.asStateFlow()

    fun showReminderBot() {
        _reminderBotVisible.value = true
    }

    fun dismissReminderBot() {
        _reminderBotVisible.value = false
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        waterRepository = WaterRepository(database.waterLogDao(), database.dailyGoalDao())
        scheduleRepository = ScheduleRepository(
            database.reminderScheduleDao(),
            database.reminderTimeDao(),
            database.breakPeriodDao()
        )
        userPreferencesRepository = UserPreferencesRepository(this)
        reminderScheduler = ReminderScheduler(
            this,
            scheduleRepository,
            waterRepository,
            userPreferencesRepository
        )

        // Schedule initial reminder in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderScheduler.scheduleNextReminder()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
