package com.example.waterreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.waterreminder.data.local.entity.BreakPeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BreakPeriodDao {

    @Query("SELECT * FROM break_periods WHERE scheduleId = :scheduleId ORDER BY startTime ASC")
    fun getBreaksForSchedule(scheduleId: Long): Flow<List<BreakPeriodEntity>>

    @Query("SELECT * FROM break_periods WHERE scheduleId = :scheduleId ORDER BY startTime ASC")
    suspend fun getBreaksForScheduleSync(scheduleId: Long): List<BreakPeriodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreak(breakPeriod: BreakPeriodEntity): Long

    @Update
    suspend fun updateBreak(breakPeriod: BreakPeriodEntity): Int

    @Delete
    suspend fun deleteBreak(breakPeriod: BreakPeriodEntity): Int

    @Query("DELETE FROM break_periods WHERE id = :id")
    suspend fun deleteBreakById(id: Long): Int
}
