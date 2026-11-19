package com.example.waterreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.waterreminder.data.local.entity.ReminderEventEntity

@Dao
interface ReminderEventDao {

    @Insert
    suspend fun insertEvent(event: ReminderEventEntity): Long

    @Query("SELECT * FROM reminder_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEvents(limit: Int): List<ReminderEventEntity>
}
