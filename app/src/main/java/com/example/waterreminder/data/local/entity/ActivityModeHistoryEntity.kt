package com.example.waterreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_mode_history")
data class ActivityModeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String,
    val timestamp: Long = System.currentTimeMillis()
)
