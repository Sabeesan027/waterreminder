package com.example.waterreminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.waterreminder.data.local.dao.ActivityModeHistoryDao
import com.example.waterreminder.data.local.dao.BreakPeriodDao
import com.example.waterreminder.data.local.dao.DailyGoalDao
import com.example.waterreminder.data.local.dao.ReminderEventDao
import com.example.waterreminder.data.local.dao.ReminderScheduleDao
import com.example.waterreminder.data.local.dao.ReminderTimeDao
import com.example.waterreminder.data.local.dao.WaterLogDao
import com.example.waterreminder.data.local.entity.ActivityModeHistoryEntity
import com.example.waterreminder.data.local.entity.BreakPeriodEntity
import com.example.waterreminder.data.local.entity.DailyGoalEntity
import com.example.waterreminder.data.local.entity.ReminderEventEntity
import com.example.waterreminder.data.local.entity.ReminderScheduleEntity
import com.example.waterreminder.data.local.entity.ReminderTimeEntity
import com.example.waterreminder.data.local.entity.WaterLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WaterLogEntity::class,
        DailyGoalEntity::class,
        ReminderScheduleEntity::class,
        ReminderTimeEntity::class,
        BreakPeriodEntity::class,
        ReminderEventEntity::class,
        ActivityModeHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun waterLogDao(): WaterLogDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun reminderScheduleDao(): ReminderScheduleDao
    abstract fun reminderTimeDao(): ReminderTimeDao
    abstract fun breakPeriodDao(): BreakPeriodDao
    abstract fun reminderEventDao(): ReminderEventDao
    abstract fun activityModeHistoryDao(): ActivityModeHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "water_reminder.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                // Pre-populate default schedule
                val defaultScheduleId = db.reminderScheduleDao().insertSchedule(
                    ReminderScheduleEntity(
                        name = "Office Schedule",
                        isEnabled = true,
                        daysOfWeekCsv = "1,2,3,4,5",
                        startTime = "08:30",
                        endTime = "17:30",
                        mode = "INTERVAL",
                        intervalMinutes = 60,
                        snoozeMinutes = 15,
                        suppressAfterGoalReached = true
                    )
                )

                // Add default Lunch Break
                db.breakPeriodDao().insertBreak(
                    BreakPeriodEntity(
                        scheduleId = defaultScheduleId,
                        name = "Lunch Break",
                        startTime = "12:30",
                        endTime = "13:30",
                        isEnabled = true
                    )
                )

                // Add Weekend Schedule
                db.reminderScheduleDao().insertSchedule(
                    ReminderScheduleEntity(
                        name = "Weekend Schedule",
                        isEnabled = false,
                        daysOfWeekCsv = "6,7",
                        startTime = "09:30",
                        endTime = "19:00",
                        mode = "INTERVAL",
                        intervalMinutes = 90,
                        snoozeMinutes = 15,
                        suppressAfterGoalReached = true
                    )
                )
            }
        }
    }
}
