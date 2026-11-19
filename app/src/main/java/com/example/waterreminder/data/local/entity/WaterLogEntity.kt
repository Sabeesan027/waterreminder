package com.example.waterreminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.waterreminder.domain.model.LogSource
import com.example.waterreminder.domain.model.WaterLog

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long,
    val date: String, // "YYYY-MM-DD" for fast indexing
    val source: String,
    val note: String
) {
    fun toDomain(): WaterLog = WaterLog(
        id = id,
        amountMl = amountMl,
        timestamp = timestamp,
        source = try { LogSource.valueOf(source) } catch (e: Exception) { LogSource.MANUAL },
        note = note
    )

    companion object {
        fun fromDomain(log: WaterLog, date: String): WaterLogEntity = WaterLogEntity(
            id = log.id,
            amountMl = log.amountMl,
            timestamp = log.timestamp,
            date = date,
            source = log.source.name,
            note = log.note
        )
    }
}
