package com.example.waterreminder.domain.model

enum class ActivityMode(val displayName: String, val description: String, val defaultIntervalMinutes: Int) {
    OFFICE_AC(
        displayName = "Office / AC",
        description = "Optimized for desk workers in air-conditioned environments with gentle reminders and break suppression.",
        defaultIntervalMinutes = 60
    ),
    NORMAL(
        displayName = "Normal Activity",
        description = "Standard hydration tracking for daily routine and moderate movement.",
        defaultIntervalMinutes = 45
    ),
    OUTDOOR_ACTIVE(
        displayName = "Outdoor / Active",
        description = "Designed for high temperature, tropical climates (e.g., Sri Lanka) or active outdoor schedules with frequent hydration prompts.",
        defaultIntervalMinutes = 30
    )
}
