package com.example.waterreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.waterreminder.domain.model.ReminderMode
import com.example.waterreminder.domain.model.ReminderSchedule

@Entity(tableName = "reminder_schedules")
data class ReminderScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isEnabled: Boolean,
    val daysOfWeekCsv: String, // e.g. "1,2,3,4,5"
    val startTime: String,
    val endTime: String,
    val mode: String,
    val intervalMinutes: Int,
    val snoozeMinutes: Int,
    val suppressAfterGoalReached: Boolean
) {
    fun toDomain(): ReminderSchedule {
        val days = daysOfWeekCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
        return ReminderSchedule(
            id = id,
            name = name,
            isEnabled = isEnabled,
            daysOfWeek = if (days.isEmpty()) setOf(1, 2, 3, 4, 5) else days,
            startTime = startTime,
            endTime = endTime,
            mode = try { ReminderMode.valueOf(mode) } catch (e: Exception) { ReminderMode.INTERVAL },
            intervalMinutes = intervalMinutes,
            snoozeMinutes = snoozeMinutes,
            suppressAfterGoalReached = suppressAfterGoalReached
        )
    }

    companion object {
        fun fromDomain(schedule: ReminderSchedule): ReminderScheduleEntity = ReminderScheduleEntity(
            id = schedule.id,
            name = schedule.name,
            isEnabled = schedule.isEnabled,
            daysOfWeekCsv = schedule.daysOfWeek.sorted().joinToString(","),
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            mode = schedule.mode.name,
            intervalMinutes = schedule.intervalMinutes,
            snoozeMinutes = schedule.snoozeMinutes,
            suppressAfterGoalReached = schedule.suppressAfterGoalReached
        )
    }
}
