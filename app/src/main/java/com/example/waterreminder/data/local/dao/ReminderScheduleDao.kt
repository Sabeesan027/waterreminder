package com.example.waterreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.waterreminder.data.local.entity.ReminderScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderScheduleDao {

    @Query("SELECT * FROM reminder_schedules ORDER BY id ASC")
    fun getAllSchedules(): Flow<List<ReminderScheduleEntity>>

    @Query("SELECT * FROM reminder_schedules ORDER BY id ASC")
    suspend fun getAllSchedulesSync(): List<ReminderScheduleEntity>

    @Query("SELECT * FROM reminder_schedules WHERE isEnabled = 1 ORDER BY id ASC")
    suspend fun getActiveSchedulesSync(): List<ReminderScheduleEntity>

    @Query("SELECT * FROM reminder_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ReminderScheduleEntity?

    @Query("SELECT * FROM reminder_schedules WHERE id = :id LIMIT 1")
    fun getScheduleByIdFlow(id: Long): Flow<ReminderScheduleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ReminderScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ReminderScheduleEntity): Int

    @Delete
    suspend fun deleteSchedule(schedule: ReminderScheduleEntity): Int
}
