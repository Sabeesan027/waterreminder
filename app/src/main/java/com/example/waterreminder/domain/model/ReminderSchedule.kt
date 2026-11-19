package com.example.waterreminder.domain.model

enum class ReminderMode {
    INTERVAL,
    SPECIFIC_TIMES
}

data class ReminderSchedule(
    val id: Long = 0,
    val name: String = "Office Schedule",
    val isEnabled: Boolean = true,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5), // 1=Mon, 7=Sun (java.time DayOfWeek values)
    val startTime: String = "08:30", // HH:mm 24-hr format
    val endTime: String = "17:30", // HH:mm 24-hr format
    val mode: ReminderMode = ReminderMode.INTERVAL,
    val intervalMinutes: Int = 60,
    val snoozeMinutes: Int = 15,
    val suppressAfterGoalReached: Boolean = true
) {
    fun isDaySelected(dayOfWeekValue: Int): Boolean = daysOfWeek.contains(dayOfWeekValue)
}

data class ReminderTime(
    val id: Long = 0,
    val scheduleId: Long,
    val time: String, // HH:mm 24-hr format
    val isEnabled: Boolean = true
)

data class BreakPeriod(
    val id: Long = 0,
    val scheduleId: Long,
    val name: String = "Lunch Break",
    val startTime: String = "12:30",
    val endTime: String = "13:30",
    val isEnabled: Boolean = true
)
