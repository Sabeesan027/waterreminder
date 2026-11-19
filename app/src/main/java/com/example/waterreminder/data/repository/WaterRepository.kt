package com.example.waterreminder.data.repository

import com.example.waterreminder.data.local.dao.DailyGoalDao
import com.example.waterreminder.data.local.dao.WaterLogDao
import com.example.waterreminder.data.local.entity.DailyGoalEntity
import com.example.waterreminder.data.local.entity.WaterLogEntity
import com.example.waterreminder.domain.model.DailyGoal
import com.example.waterreminder.domain.model.HydrationSummary
import com.example.waterreminder.domain.model.WaterLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class WaterRepository(
    private val waterLogDao: WaterLogDao,
    private val dailyGoalDao: DailyGoalDao
) {

    fun getLogsForDate(date: String): Flow<List<WaterLog>> {
        return waterLogDao.getLogsForDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getLogsForDateSync(date: String): List<WaterLog> {
        return waterLogDao.getLogsForDateSync(date).map { it.toDomain() }
    }

    fun getHydrationSummary(
        date: String,
        defaultGoalMl: Int,
        workAllocationPercent: Int,
        workStartTime: String,
        workEndTime: String
    ): Flow<HydrationSummary> {
        val logsFlow = waterLogDao.getLogsForDate(date)
        val goalFlow = dailyGoalDao.getGoalForDate(date)

        return combine(logsFlow, goalFlow) { entities, goalEntity ->
            val logs = entities.map { it.toDomain() }
            val goalMl = goalEntity?.goalMl ?: defaultGoalMl
            val allocPercent = goalEntity?.workAllocationPercent ?: workAllocationPercent

            val workGoalMl = (goalMl * (allocPercent / 100.0)).toInt()
            val outsideWorkGoalMl = goalMl - workGoalMl

            val workStart = parseTime(workStartTime, LocalTime.of(8, 30))
            val workEnd = parseTime(workEndTime, LocalTime.of(17, 30))

            var consumedTotal = 0
            var workConsumed = 0
            var outsideWorkConsumed = 0

            logs.forEach { log ->
                consumedTotal += log.amountMl
                val logTime = java.time.Instant.ofEpochMilli(log.timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalTime()
                if (isTimeInInterval(logTime, workStart, workEnd)) {
                    workConsumed += log.amountMl
                } else {
                    outsideWorkConsumed += log.amountMl
                }
            }

            HydrationSummary(
                date = date,
                consumedMl = consumedTotal,
                goalMl = goalMl,
                workConsumedMl = workConsumed,
                workGoalMl = workGoalMl,
                outsideWorkConsumedMl = outsideWorkConsumed,
                outsideWorkGoalMl = outsideWorkGoalMl,
                logs = logs
            )
        }
    }

    suspend fun addWaterLog(log: WaterLog, date: String): Long {
        return waterLogDao.insertLog(WaterLogEntity.fromDomain(log, date))
    }

    suspend fun updateWaterLog(log: WaterLog, date: String) {
        waterLogDao.updateLog(WaterLogEntity.fromDomain(log, date))
    }

    suspend fun deleteWaterLog(log: WaterLog, date: String) {
        waterLogDao.deleteLog(WaterLogEntity.fromDomain(log, date))
    }

    suspend fun deleteWaterLogById(id: Long) {
        waterLogDao.deleteLogById(id)
    }

    suspend fun clearAllLogs() {
        waterLogDao.clearAllLogs()
    }

    suspend fun getLastLogTimestamp(): Long? {
        return waterLogDao.getLastLogTimestamp()
    }

    suspend fun setDailyGoal(goal: DailyGoal) {
        dailyGoalDao.setGoal(DailyGoalEntity.fromDomain(goal))
    }

    fun getLogsForRange(startDate: String, endDate: String): Flow<List<WaterLog>> {
        return waterLogDao.getLogsForRange(startDate, endDate).map { list ->
            list.map { it.toDomain() }
        }
    }

    private fun parseTime(timeStr: String, default: LocalTime): LocalTime {
        return try {
            LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            default
        }
    }

    private fun isTimeInInterval(time: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (start.isBefore(end) || start == end) {
            !time.isBefore(start) && !time.isAfter(end)
        } else {
            // Overnight window e.g. 22:00 to 06:00
            !time.isBefore(start) || !time.isAfter(end)
        }
    }
}
