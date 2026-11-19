package com.example.waterreminder.scheduler

import com.example.waterreminder.domain.model.BreakPeriod
import com.example.waterreminder.domain.model.HydrationSummary
import com.example.waterreminder.domain.model.ReminderMode
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.domain.model.ReminderTime
import com.example.waterreminder.domain.model.UserPreferences
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReminderEngine {

    /**
     * Calculates the next reminder epoch milliseconds from the given state.
     * Returns null if no active schedule yields a valid future slot.
     */
    fun calculateNextReminderTime(
        now: LocalDateTime,
        schedules: List<ReminderSchedule>,
        timesMap: Map<Long, List<ReminderTime>>, // scheduleId -> specific times
        breaksMap: Map<Long, List<BreakPeriod>>, // scheduleId -> break periods
        userPreferences: UserPreferences,
        lastLogTimestampMs: Long?,
        summaryToday: HydrationSummary?,
        snoozedUntilMs: Long? = null
    ): Long? {
        if (!userPreferences.remindersEnabled) return null

        // If snooze is active and in future, respect snooze time
        val nowMs = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (snoozedUntilMs != null && snoozedUntilMs > nowMs) {
            return snoozedUntilMs
        }

        val candidateTimesMs = mutableListOf<Long>()

        for (schedule in schedules) {
            if (!schedule.isEnabled) continue

            // If goal is reached and schedule suppresses after goal, skip interval/specific times calculation
            if (schedule.suppressAfterGoalReached && summaryToday?.isGoalReached == true) {
                continue
            }

            val times = calculateCandidateTimesForSchedule(
                now = now,
                schedule = schedule,
                specificTimes = timesMap[schedule.id] ?: emptyList(),
                breakPeriods = breaksMap[schedule.id] ?: emptyList(),
                userPreferences = userPreferences,
                lastLogTimestampMs = lastLogTimestampMs
            )
            candidateTimesMs.addAll(times)
        }

        val futureCandidates = candidateTimesMs.filter { it > nowMs }.sorted()
        return futureCandidates.firstOrNull()
    }

    private fun calculateCandidateTimesForSchedule(
        now: LocalDateTime,
        schedule: ReminderSchedule,
        specificTimes: List<ReminderTime>,
        breakPeriods: List<BreakPeriod>,
        userPreferences: UserPreferences,
        lastLogTimestampMs: Long?
    ): List<Long> {
        val candidates = mutableListOf<Long>()
        val today = now.toLocalDate()

        // Check today and the next 7 days to find eligible future slots
        for (dayOffset in 0..7) {
            val checkDate = today.plusDays(dayOffset.toLong())
            val dayOfWeekValue = checkDate.dayOfWeek.value // 1=Mon, 7=Sun

            if (!schedule.daysOfWeek.contains(dayOfWeekValue)) {
                continue
            }

            val startTime = parseTime(schedule.startTime, LocalTime.of(8, 30))
            val endTime = parseTime(schedule.endTime, LocalTime.of(17, 30))

            val wakeTime = parseTime(userPreferences.wakeUpTime, LocalTime.of(7, 0))
            val sleepTime = parseTime(userPreferences.sleepTime, LocalTime.of(23, 0))

            val daySlots = mutableListOf<LocalTime>()

            if (schedule.mode == ReminderMode.INTERVAL) {
                val intervalMinutes = schedule.intervalMinutes.coerceAtLeast(15)
                var slot = startTime

                if (startTime.isBefore(endTime) || startTime == endTime) {
                    while (!slot.isAfter(endTime)) {
                        daySlots.add(slot)
                        slot = slot.plusMinutes(intervalMinutes.toLong())
                        if (intervalMinutes <= 0) break
                    }
                } else {
                    // Overnight window e.g. 22:00 to 06:00
                    var current = startTime
                    val nextDay = checkDate.plusDays(1)
                    var currentDt = LocalDateTime.of(checkDate, current)
                    val endDt = LocalDateTime.of(nextDay, endTime)
                    while (!currentDt.isAfter(endDt)) {
                        if (currentDt.toLocalDate() == checkDate) {
                            daySlots.add(currentDt.toLocalTime())
                        }
                        currentDt = currentDt.plusMinutes(intervalMinutes.toLong())
                    }
                }
            } else {
                // SPECIFIC_TIMES mode
                specificTimes.filter { it.isEnabled }.forEach { st ->
                    parseTimeOrNull(st.time)?.let { daySlots.add(it) }
                }
            }

            for (slotTime in daySlots) {
                val slotDateTime = LocalDateTime.of(checkDate, slotTime)
                val slotMs = slotDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // 1. Check if within sleep hours
                if (isTimeInInterval(slotTime, sleepTime, wakeTime)) {
                    continue
                }

                // 2. Check if within enabled break periods
                val activeBreaks = breakPeriods.filter { it.isEnabled }
                val isInBreak = activeBreaks.any { bp ->
                    val bStart = parseTime(bp.startTime, LocalTime.of(12, 30))
                    val bEnd = parseTime(bp.endTime, LocalTime.of(13, 30))
                    isTimeInInterval(slotTime, bStart, bEnd)
                }
                if (isInBreak) continue

                // 3. Check recent drink suppression window
                if (lastLogTimestampMs != null) {
                    val suppressionMs = userPreferences.recentDrinkSuppressionMinutes * 60 * 1000L
                    if (slotMs >= lastLogTimestampMs && slotMs - lastLogTimestampMs < suppressionMs) {
                        continue
                    }
                }

                candidates.add(slotMs)
            }
        }

        return candidates
    }

    private fun parseTime(timeStr: String, default: LocalTime): LocalTime {
        return try {
            LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            default
        }
    }

    private fun parseTimeOrNull(timeStr: String): LocalTime? {
        return try {
            LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            null
        }
    }

    private fun isTimeInInterval(time: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (start.isBefore(end) || start == end) {
            !time.isBefore(start) && !time.isAfter(end)
        } else {
            // Overnight window e.g. 23:00 to 07:00
            !time.isBefore(start) || !time.isAfter(end)
        }
    }
}
