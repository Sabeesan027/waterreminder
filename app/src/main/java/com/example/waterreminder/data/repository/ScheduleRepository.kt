package com.example.waterreminder.data.repository

import com.example.waterreminder.data.local.dao.BreakPeriodDao
import com.example.waterreminder.data.local.dao.ReminderScheduleDao
import com.example.waterreminder.data.local.dao.ReminderTimeDao
import com.example.waterreminder.data.local.entity.BreakPeriodEntity
import com.example.waterreminder.data.local.entity.ReminderScheduleEntity
import com.example.waterreminder.data.local.entity.ReminderTimeEntity
import com.example.waterreminder.domain.model.BreakPeriod
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.domain.model.ReminderTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScheduleRepository(
    private val scheduleDao: ReminderScheduleDao,
    private val timeDao: ReminderTimeDao,
    private val breakDao: BreakPeriodDao
) {

    fun getAllSchedules(): Flow<List<ReminderSchedule>> {
        return scheduleDao.getAllSchedules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAllSchedulesSync(): List<ReminderSchedule> {
        return scheduleDao.getAllSchedulesSync().map { it.toDomain() }
    }

    suspend fun getActiveSchedulesSync(): List<ReminderSchedule> {
        return scheduleDao.getActiveSchedulesSync().map { it.toDomain() }
    }

    suspend fun getScheduleById(id: Long): ReminderSchedule? {
        return scheduleDao.getScheduleById(id)?.toDomain()
    }

    fun getScheduleByIdFlow(id: Long): Flow<ReminderSchedule?> {
        return scheduleDao.getScheduleByIdFlow(id).map { it?.toDomain() }
    }

    suspend fun saveSchedule(schedule: ReminderSchedule): Long {
        val entity = ReminderScheduleEntity.fromDomain(schedule)
        return if (schedule.id == 0L) {
            scheduleDao.insertSchedule(entity)
        } else {
            scheduleDao.updateSchedule(entity)
            schedule.id
        }
    }

    suspend fun deleteSchedule(schedule: ReminderSchedule) {
        scheduleDao.deleteSchedule(ReminderScheduleEntity.fromDomain(schedule))
    }

    // Reminder Times
    fun getTimesForSchedule(scheduleId: Long): Flow<List<ReminderTime>> {
        return timeDao.getTimesForSchedule(scheduleId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getTimesForScheduleSync(scheduleId: Long): List<ReminderTime> {
        return timeDao.getTimesForScheduleSync(scheduleId).map { it.toDomain() }
    }

    suspend fun saveTime(time: ReminderTime): Long {
        val entity = ReminderTimeEntity.fromDomain(time)
        return if (time.id == 0L) {
            timeDao.insertTime(entity)
        } else {
            timeDao.updateTime(entity)
            time.id
        }
    }

    suspend fun deleteTimeById(id: Long) {
        timeDao.deleteTimeById(id)
    }

    // Break Periods
    fun getBreaksForSchedule(scheduleId: Long): Flow<List<BreakPeriod>> {
        return breakDao.getBreaksForSchedule(scheduleId).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getBreaksForScheduleSync(scheduleId: Long): List<BreakPeriod> {
        return breakDao.getBreaksForScheduleSync(scheduleId).map { it.toDomain() }
    }

    suspend fun saveBreak(breakPeriod: BreakPeriod): Long {
        val entity = BreakPeriodEntity.fromDomain(breakPeriod)
        return if (breakPeriod.id == 0L) {
            breakDao.insertBreak(entity)
        } else {
            breakDao.updateBreak(entity)
            breakPeriod.id
        }
    }

    suspend fun deleteBreakById(id: Long) {
        breakDao.deleteBreakById(id)
    }
}
