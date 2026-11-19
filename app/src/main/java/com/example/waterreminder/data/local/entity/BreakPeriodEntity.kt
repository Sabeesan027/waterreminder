package com.example.waterreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.waterreminder.domain.model.BreakPeriod

@Entity(
    tableName = "break_periods",
    foreignKeys = [
        ForeignKey(
            entity = ReminderScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["scheduleId"])]
)
data class BreakPeriodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val name: String,
    val startTime: String,
    val endTime: String,
    val isEnabled: Boolean
) {
    fun toDomain(): BreakPeriod = BreakPeriod(
        id = id,
        scheduleId = scheduleId,
        name = name,
        startTime = startTime,
        endTime = endTime,
        isEnabled = isEnabled
    )

    companion object {
        fun fromDomain(bp: BreakPeriod): BreakPeriodEntity = BreakPeriodEntity(
            id = bp.id,
            scheduleId = bp.scheduleId,
            name = bp.name,
            startTime = bp.startTime,
            endTime = bp.endTime,
            isEnabled = bp.isEnabled
        )
    }
}
