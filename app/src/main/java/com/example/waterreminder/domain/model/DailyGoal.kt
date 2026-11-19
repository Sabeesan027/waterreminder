package com.example.waterreminder.domain.model

data class DailyGoal(
    val date: String, // "YYYY-MM-DD"
    val goalMl: Int = 3500,
    val workAllocationPercent: Int = 70
) {
    val workGoalMl: Int
        get() = (goalMl * (workAllocationPercent / 100.0)).toInt()

    val outsideWorkGoalMl: Int
        get() = goalMl - workGoalMl
}
