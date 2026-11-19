package com.example.waterreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.waterreminder.domain.model.DailyGoal

@Entity(tableName = "daily_goals")
data class DailyGoalEntity(
    @PrimaryKey
    val date: String, // "YYYY-MM-DD"
    val goalMl: Int,
    val workAllocationPercent: Int
) {
    fun toDomain(): DailyGoal = DailyGoal(
        date = date,
        goalMl = goalMl,
        workAllocationPercent = workAllocationPercent
    )

    companion object {
        fun fromDomain(goal: DailyGoal): DailyGoalEntity = DailyGoalEntity(
            date = goal.date,
            goalMl = goal.goalMl,
            workAllocationPercent = goal.workAllocationPercent
        )
    }
}
