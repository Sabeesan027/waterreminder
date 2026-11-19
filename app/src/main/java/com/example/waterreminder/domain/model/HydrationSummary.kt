package com.example.waterreminder.domain.model

data class HydrationSummary(
    val date: String,
    val consumedMl: Int,
    val goalMl: Int,
    val workConsumedMl: Int,
    val workGoalMl: Int,
    val outsideWorkConsumedMl: Int,
    val outsideWorkGoalMl: Int,
    val logs: List<WaterLog> = emptyList()
) {
    val progressPercentage: Float
        get() = if (goalMl > 0) (consumedMl.toFloat() / goalMl * 100f).coerceAtMost(100f) else 0f

    val workProgressPercentage: Float
        get() = if (workGoalMl > 0) (workConsumedMl.toFloat() / workGoalMl * 100f).coerceAtMost(100f) else 0f

    val remainingMl: Int
        get() = (goalMl - consumedMl).coerceAtLeast(0)

    val isGoalReached: Boolean
        get() = consumedMl >= goalMl
}
