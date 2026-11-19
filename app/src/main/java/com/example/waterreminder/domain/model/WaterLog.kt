package com.example.waterreminder.domain.model

enum class LogSource {
    MANUAL,
    NOTIFICATION,
    WIDGET,
    QUICK_ADD
}

data class WaterLog(
    val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val source: LogSource = LogSource.MANUAL,
    val note: String = ""
)
