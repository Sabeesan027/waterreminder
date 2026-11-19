package com.example.waterreminder.domain.model

data class UserPreferences(
    val isOnboardingCompleted: Boolean = false,
    val userName: String = "Sabin",
    val unit: String = "ml", // "ml" or "oz"
    val dailyGoalMl: Int = 3500,
    val quickAddMl: Int = 250,
    val workAllocationPercent: Int = 70,
    val wakeUpTime: String = "07:00",
    val sleepTime: String = "23:00",
    val workStartTime: String = "08:30",
    val workEndTime: String = "17:30",
    val activityMode: ActivityMode = ActivityMode.OFFICE_AC,
    val remindersEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val darkThemeMode: String = "DARK",
    val recentDrinkSuppressionMinutes: Int = 30
)
