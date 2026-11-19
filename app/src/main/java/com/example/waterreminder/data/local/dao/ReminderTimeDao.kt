package com.example.waterreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.waterreminder.data.local.entity.ReminderTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderTimeDao {

    @Query("SELECT * FROM reminder_times WHERE scheduleId = :scheduleId ORDER BY time ASC")
    fun getTimesForSchedule(scheduleId: Long): Flow<List<ReminderTimeEntity>>

    @Query("SELECT * FROM reminder_times WHERE scheduleId = :scheduleId ORDER BY time ASC")
    suspend fun getTimesForScheduleSync(scheduleId: Long): List<ReminderTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTime(time: ReminderTimeEntity): Long

    @Update
    suspend fun updateTime(time: ReminderTimeEntity): Int

    @Delete
    suspend fun deleteTime(time: ReminderTimeEntity): Int

    @Query("DELETE FROM reminder_times WHERE id = :id")
    suspend fun deleteTimeById(id: Long): Int
}
