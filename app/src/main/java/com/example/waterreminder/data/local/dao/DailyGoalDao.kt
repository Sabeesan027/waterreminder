package com.example.waterreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waterreminder.data.local.entity.DailyGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyGoalDao {

    @Query("SELECT * FROM daily_goals WHERE date = :date LIMIT 1")
    fun getGoalForDate(date: String): Flow<DailyGoalEntity?>

    @Query("SELECT * FROM daily_goals WHERE date = :date LIMIT 1")
    suspend fun getGoalForDateSync(date: String): DailyGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGoal(goal: DailyGoalEntity): Long

    @Query("DELETE FROM daily_goals")
    suspend fun clearAllGoals(): Int
}
