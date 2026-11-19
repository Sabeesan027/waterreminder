package com.example.waterreminder.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.ScheduleRepository
import com.example.waterreminder.domain.model.ActivityMode
import com.example.waterreminder.domain.model.BreakPeriod
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.domain.model.UserPreferences
import com.example.waterreminder.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentPage: Int = 0,
    val userName: String = "",
    val unit: String = "ml",
    val dailyGoalMl: Int = 3500,
    val quickAddMl: Int = 250,
    val wakeUpTime: String = "07:00",
    val sleepTime: String = "23:00",
    val workStartTime: String = "08:30",
    val workEndTime: String = "17:30",
    val lunchStart: String = "12:30",
    val lunchEnd: String = "13:30",
    val activityMode: ActivityMode = ActivityMode.OFFICE_AC,
    val remindersEnabled: Boolean = true,
    val workingDays: Set<Int> = setOf(1, 2, 3, 4, 5), // Mon-Fri
    val reminderIntervalMinutes: Int = 60,
    val snoozeMinutes: Int = 15,
    val suppressDuringBreaks: Boolean = true
)

class OnboardingViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val scheduleRepository: ScheduleRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextPage() {
        _uiState.update { it.copy(currentPage = (it.currentPage + 1).coerceAtMost(4)) }
    }

    fun previousPage() {
        _uiState.update { it.copy(currentPage = (it.currentPage - 1).coerceAtLeast(0)) }
    }

    fun updateName(name: String) { _uiState.update { it.copy(userName = name) } }
    fun updateUnit(unit: String) { _uiState.update { it.copy(unit = unit) } }
    fun updateGoal(goalMl: Int) { _uiState.update { it.copy(dailyGoalMl = goalMl) } }
    fun updateQuickAdd(amountMl: Int) { _uiState.update { it.copy(quickAddMl = amountMl) } }
    fun updateWakeUpTime(time: String) { _uiState.update { it.copy(wakeUpTime = time) } }
    fun updateSleepTime(time: String) { _uiState.update { it.copy(sleepTime = time) } }
    fun updateWorkStart(time: String) { _uiState.update { it.copy(workStartTime = time) } }
    fun updateWorkEnd(time: String) { _uiState.update { it.copy(workEndTime = time) } }
    fun updateLunchStart(time: String) { _uiState.update { it.copy(lunchStart = time) } }
    fun updateLunchEnd(time: String) { _uiState.update { it.copy(lunchEnd = time) } }
    fun updateActivityMode(mode: ActivityMode) { _uiState.update { it.copy(activityMode = mode) } }
    fun updateRemindersEnabled(enabled: Boolean) { _uiState.update { it.copy(remindersEnabled = enabled) } }
    fun toggleWorkingDay(day: Int) {
        _uiState.update { state ->
            val newDays = state.workingDays.toMutableSet()
            if (newDays.contains(day)) newDays.remove(day) else newDays.add(day)
            state.copy(workingDays = if (newDays.isEmpty()) setOf(1) else newDays)
        }
    }
    fun updateInterval(minutes: Int) { _uiState.update { it.copy(reminderIntervalMinutes = minutes) } }
    fun updateSnooze(minutes: Int) { _uiState.update { it.copy(snoozeMinutes = minutes) } }
    fun updateWorkingDays(days: Set<Int>) {
        _uiState.update { it.copy(workingDays = days.ifEmpty { setOf(1) }) }
    }
    fun updateSuppressDuringBreaks(enabled: Boolean) {
        _uiState.update { it.copy(suppressDuringBreaks = enabled) }
    }

    fun skipOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(true)
            userPreferencesRepository.updateRemindersEnabled(false)
            reminderScheduler.cancelReminder()
            onFinished()
        }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val currentPrefs = userPreferencesRepository.userPreferencesFlow.first()

            val updatedPrefs = UserPreferences(
                isOnboardingCompleted = true,
                userName = state.userName.trim().ifBlank { currentPrefs.userName },
                unit = state.unit,
                dailyGoalMl = state.dailyGoalMl,
                quickAddMl = state.quickAddMl,
                workAllocationPercent = currentPrefs.workAllocationPercent,
                wakeUpTime = state.wakeUpTime,
                sleepTime = state.sleepTime,
                workStartTime = state.workStartTime,
                workEndTime = state.workEndTime,
                activityMode = state.activityMode,
                remindersEnabled = state.remindersEnabled,
                soundEnabled = currentPrefs.soundEnabled,
                vibrateEnabled = currentPrefs.vibrateEnabled,
                darkThemeMode = currentPrefs.darkThemeMode,
                recentDrinkSuppressionMinutes = currentPrefs.recentDrinkSuppressionMinutes
            )

            userPreferencesRepository.updatePreferences(updatedPrefs)

            // Save active default schedule in DB
            val activeSchedules = scheduleRepository.getActiveSchedulesSync()
            val primarySchedule = activeSchedules.firstOrNull() ?: ReminderSchedule()

            val newSchedule = primarySchedule.copy(
                name = "${state.activityMode.displayName} Schedule",
                isEnabled = state.remindersEnabled,
                daysOfWeek = state.workingDays,
                startTime = state.workStartTime,
                endTime = state.workEndTime,
                intervalMinutes = state.reminderIntervalMinutes,
                snoozeMinutes = state.snoozeMinutes
            )
            val scheduleId = scheduleRepository.saveSchedule(newSchedule)

            if (state.suppressDuringBreaks &&
                scheduleRepository.getBreaksForScheduleSync(scheduleId).isEmpty()
            ) {
                scheduleRepository.saveBreak(
                    BreakPeriod(
                        scheduleId = scheduleId,
                        name = "Lunch Break",
                        startTime = state.lunchStart,
                        endTime = state.lunchEnd
                    )
                )
            }

            reminderScheduler.scheduleNextReminder()
            onFinished()
        }
    }

    class Factory(
        private val prefsRepo: UserPreferencesRepository,
        private val scheduleRepo: ScheduleRepository,
        private val scheduler: ReminderScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(prefsRepo, scheduleRepo, scheduler) as T
        }
    }
}
