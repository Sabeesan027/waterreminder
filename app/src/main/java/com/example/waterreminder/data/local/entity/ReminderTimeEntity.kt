package com.example.waterreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.waterreminder.domain.model.ReminderTime

@Entity(
    tableName = "reminder_times",
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
data class ReminderTimeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val time: String,
    val isEnabled: Boolean
) {
    fun toDomain(): ReminderTime = ReminderTime(
        id = id,
        scheduleId = scheduleId,
        time = time,
        isEnabled = isEnabled
    )

    companion object {
        fun fromDomain(rt: ReminderTime): ReminderTimeEntity = ReminderTimeEntity(
            id = rt.id,
            scheduleId = rt.scheduleId,
            time = rt.time,
            isEnabled = rt.isEnabled
        )
    }
}
