package com.example.waterreminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.ScheduleRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.domain.model.ActivityMode
import com.example.waterreminder.domain.model.HydrationSummary
import com.example.waterreminder.domain.model.LogSource
import com.example.waterreminder.domain.model.UserPreferences
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.scheduler.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class HomeUiState(
    val greeting: String = "Welcome",
    val todayDateFormatted: String = "",
    val hydrationSummary: HydrationSummary = HydrationSummary(
        date = LocalDate.now().toString(),
        consumedMl = 0,
        goalMl = 3500,
        workConsumedMl = 0,
        workGoalMl = 2450,
        outsideWorkConsumedMl = 0,
        outsideWorkGoalMl = 1050
    ),
    val userPreferences: UserPreferences = UserPreferences(),
    val nextReminderFormatted: String = "No upcoming reminders",
    val motivationalMessage: String = "Start your day with a fresh glass of water!",
    val recentlyDeletedLog: WaterLog? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val waterRepository: WaterRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val todayStr = LocalDate.now().toString()
    private val _recentlyDeletedLog = MutableStateFlow<WaterLog?>(null)
    private val _nextReminderFormatted = MutableStateFlow("No upcoming reminders")

    val uiState: StateFlow<HomeUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        _recentlyDeletedLog,
        _nextReminderFormatted
    ) { prefs, deletedLog, nextReminder -> Triple(prefs, deletedLog, nextReminder) }
        .flatMapLatest { (prefs, deletedLog, nextReminder) ->
            waterRepository.getHydrationSummary(
                date = todayStr,
                defaultGoalMl = prefs.dailyGoalMl,
                workAllocationPercent = prefs.workAllocationPercent,
                workStartTime = prefs.workStartTime,
                workEndTime = prefs.workEndTime
            ).map { summary ->
                val hour = LocalTime.now().hour
                val greetingTime = when {
                    hour in 5..11 -> "Good morning"
                    hour in 12..16 -> "Good afternoon"
                    hour in 17..21 -> "Good evening"
                    else -> "Good night"
                }
                val nameStr = if (prefs.userName.isNotBlank()) ", ${prefs.userName}" else ""

                HomeUiState(
                    greeting = "$greetingTime$nameStr",
                    todayDateFormatted = LocalDate.now().format(
                        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
                    ),
                    hydrationSummary = summary,
                    userPreferences = prefs,
                    nextReminderFormatted = nextReminder,
                    motivationalMessage = calculateMotivationalMessage(summary.progressPercentage),
                    recentlyDeletedLog = deletedLog
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    init {
        viewModelScope.launch { updateNextReminder() }
    }

    fun snoozeNextReminder() {
        viewModelScope.launch {
            val snoozeMinutes = scheduleRepository.getActiveSchedulesSync()
                .firstOrNull()
                ?.snoozeMinutes
                ?: 15
            reminderScheduler.scheduleNextReminder(
                snoozedUntilMs = System.currentTimeMillis() + snoozeMinutes * 60_000L
            )
            updateNextReminder()
        }
    }

    fun addWater(amountMl: Int, source: LogSource = LogSource.MANUAL, note: String = "") {
        viewModelScope.launch {
            val log = WaterLog(
                amountMl = amountMl,
                timestamp = System.currentTimeMillis(),
                source = source,
                note = note
            )
            waterRepository.addWaterLog(log, todayStr)
            updateNextReminder()
        }
    }

    fun updateWaterLog(log: WaterLog, amountMl: Int, note: String) {
        viewModelScope.launch {
            val updated = log.copy(amountMl = amountMl, note = note)
            waterRepository.updateWaterLog(updated, todayStr)
            updateNextReminder()
        }
    }

    fun deleteWaterLog(log: WaterLog) {
        viewModelScope.launch {
            _recentlyDeletedLog.value = log
            waterRepository.deleteWaterLog(log, todayStr)
            updateNextReminder()
        }
    }

    fun undoDeleteWaterLog() {
        val deleted = _recentlyDeletedLog.value ?: return
        viewModelScope.launch {
            waterRepository.addWaterLog(deleted, todayStr)
            _recentlyDeletedLog.value = null
            updateNextReminder()
        }
    }

    private suspend fun updateNextReminder() {
        val nextTimeMs = reminderScheduler.scheduleNextReminder()
        _nextReminderFormatted.value = nextTimeMs?.let { timestamp ->
            Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("EEE, h:mm a"))
        } ?: "No upcoming reminders"
    }

    private fun calculateMotivationalMessage(progressPercent: Float): String {
        return when {
            progressPercent >= 100f -> "Daily hydration goal completed! Awesome job!"
            progressPercent >= 75f -> "Almost at your goal! Keep sipping!"
            progressPercent >= 50f -> "Halfway there! Stay hydrated during work!"
            progressPercent >= 25f -> "Good start! Keep up the momentum!"
            else -> "Start your day with a fresh glass of water!"
        }
    }

    class Factory(
        private val waterRepo: WaterRepository,
        private val scheduleRepo: ScheduleRepository,
        private val prefsRepo: UserPreferencesRepository,
        private val scheduler: ReminderScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(waterRepo, scheduleRepo, prefsRepo, scheduler) as T
        }
    }
}
