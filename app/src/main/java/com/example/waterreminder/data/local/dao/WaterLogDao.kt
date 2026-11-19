package com.example.waterreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.waterreminder.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getLogsForDate(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getLogsForDateSync(date: String): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    fun getLogsForRange(startDate: String, endDate: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    suspend fun getAllLogsSync(): List<WaterLogEntity>

    @Query("SELECT * FROM water_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): WaterLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterLogEntity): Long

    @Update
    suspend fun updateLog(log: WaterLogEntity): Int

    @Delete
    suspend fun deleteLog(log: WaterLogEntity): Int

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long): Int

    @Query("DELETE FROM water_logs")
    suspend fun clearAllLogs(): Int

    @Query("SELECT MAX(timestamp) FROM water_logs")
    suspend fun getLastLogTimestamp(): Long?
}
