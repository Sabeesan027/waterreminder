package com.example.waterreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.waterreminder.data.local.entity.ActivityModeHistoryEntity

@Dao
interface ActivityModeHistoryDao {

    @Insert
    suspend fun insertHistory(entry: ActivityModeHistoryEntity): Long

    @Query("SELECT * FROM activity_mode_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMode(): ActivityModeHistoryEntity?
}
