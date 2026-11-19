package com.example.waterreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_events")
data class ReminderEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // TRIGGERED, DELIVERED, SNOOZED, SKIPPED, DRANK
    val scheduledTimeMs: Long
)
