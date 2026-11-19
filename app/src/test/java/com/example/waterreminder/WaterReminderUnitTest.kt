package com.example.waterreminder

import com.example.waterreminder.domain.model.ActivityMode
import com.example.waterreminder.domain.model.BreakPeriod
import com.example.waterreminder.domain.model.DailyGoal
import com.example.waterreminder.domain.model.HydrationSummary
import com.example.waterreminder.domain.model.ReminderMode
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.domain.model.ReminderTime
import com.example.waterreminder.domain.model.UserPreferences
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.scheduler.ReminderEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class WaterReminderUnitTest {

    @Test
    fun testDailyGoalWorkAllocationCalculation() {
        val dailyGoal = DailyGoal(
            date = "2025-01-01",
            goalMl = 3500,
            workAllocationPercent = 70
        )

        assertEquals(2450, dailyGoal.workGoalMl)
        assertEquals(1050, dailyGoal.outsideWorkGoalMl)
    }

    @Test
    fun testHydrationSummaryProgressCalculation() {
        val summary = HydrationSummary(
            date = "2025-01-01",
            consumedMl = 1750,
            goalMl = 3500,
            workConsumedMl = 1200,
            workGoalMl = 2450,
            outsideWorkConsumedMl = 550,
            outsideWorkGoalMl = 1050
        )

        assertEquals(50f, summary.progressPercentage, 0.01f)
        assertEquals(1750, summary.remainingMl)
        assertEquals(false, summary.isGoalReached)
    }

    @Test
    fun testReminderEngineCalculatesNextSlot() {
        val engine = ReminderEngine()
        val now = LocalDateTime.of(2025, 1, 6, 8, 0) // Monday 8:00 AM

        val schedule = ReminderSchedule(
            id = 1L,
            name = "Office Schedule",
            isEnabled = true,
            daysOfWeek = setOf(1, 2, 3, 4, 5), // Mon-Fri
            startTime = "08:30",
            endTime = "17:30",
            mode = ReminderMode.INTERVAL,
            intervalMinutes = 60,
            snoozeMinutes = 15,
            suppressAfterGoalReached = true
        )

        val userPrefs = UserPreferences(
            remindersEnabled = true,
            wakeUpTime = "07:00",
            sleepTime = "23:00",
            recentDrinkSuppressionMinutes = 30
        )

        val nextTimeMs = engine.calculateNextReminderTime(
            now = now,
            schedules = listOf(schedule),
            timesMap = emptyMap(),
            breaksMap = emptyMap(),
            userPreferences = userPrefs,
            lastLogTimestampMs = null,
            summaryToday = null
        )

        assertNotNull(nextTimeMs)
        val nextDateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(nextTimeMs!!),
            ZoneId.systemDefault()
        )

        // Should pick 08:30 AM
        assertEquals(8, nextDateTime.hour)
        assertEquals(30, nextDateTime.minute)
    }

    @Test
    fun testReminderEngineSuppressesDuringBreak() {
        val engine = ReminderEngine()
        val now = LocalDateTime.of(2025, 1, 6, 12, 15) // Monday 12:15 PM

        val schedule = ReminderSchedule(
            id = 1L,
            isEnabled = true,
            daysOfWeek = setOf(1),
            startTime = "08:30",
            endTime = "17:30",
            mode = ReminderMode.INTERVAL,
            intervalMinutes = 30
        )

        val lunchBreak = BreakPeriod(
            id = 1L,
            scheduleId = 1L,
            name = "Lunch Break",
            startTime = "12:30",
            endTime = "13:30",
            isEnabled = true
        )

        val userPrefs = UserPreferences(remindersEnabled = true)

        val nextTimeMs = engine.calculateNextReminderTime(
            now = now,
            schedules = listOf(schedule),
            timesMap = emptyMap(),
            breaksMap = mapOf(1L to listOf(lunchBreak)),
            userPreferences = userPrefs,
            lastLogTimestampMs = null,
            summaryToday = null
        )

        assertNotNull(nextTimeMs)
        val nextDateTime = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(nextTimeMs!!),
            ZoneId.systemDefault()
        )

        // 12:30 and 13:00 are during break, so next eligible slot is 13:30 or 14:00
        assertTrue(nextDateTime.hour >= 13)
    }
}
