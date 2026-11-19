package com.example.waterreminder.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.ScheduleRepository
import com.example.waterreminder.domain.model.BreakPeriod
import com.example.waterreminder.domain.model.ReminderMode
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.domain.model.ReminderTime
import com.example.waterreminder.scheduler.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RemindersUiState(
    val schedules: List<ReminderSchedule> = emptyList(),
    val activeSchedule: ReminderSchedule? = null,
    val specificTimes: List<ReminderTime> = emptyList(),
    val breakPeriods: List<BreakPeriod> = emptyList(),
    val masterRemindersEnabled: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _selectedScheduleId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<RemindersUiState> = combine(
        scheduleRepository.getAllSchedules(),
        _selectedScheduleId,
        userPreferencesRepository.userPreferencesFlow
    ) { schedules, selectedId, prefs ->
        Triple(schedules, selectedId, prefs)
    }.flatMapLatest { (schedules, selectedId, prefs) ->
        val active = schedules.find { it.id == selectedId }
            ?: schedules.find { it.isEnabled }
            ?: schedules.firstOrNull()

        if (active == null) {
            flowOf(RemindersUiState(masterRemindersEnabled = prefs.remindersEnabled))
        } else {
            val timesFlow = scheduleRepository.getTimesForSchedule(active.id)
            val breaksFlow = scheduleRepository.getBreaksForSchedule(active.id)

            combine(timesFlow, breaksFlow) { times, breaks ->
                RemindersUiState(
                    schedules = schedules,
                    activeSchedule = active,
                    specificTimes = times,
                    breakPeriods = breaks,
                    masterRemindersEnabled = prefs.remindersEnabled
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RemindersUiState()
    )

    fun selectSchedule(scheduleId: Long) {
        _selectedScheduleId.value = scheduleId
    }

    fun toggleMasterReminders(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateRemindersEnabled(enabled)
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun updateSchedule(schedule: ReminderSchedule) {
        viewModelScope.launch {
            scheduleRepository.saveSchedule(schedule)
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun deleteSchedule(schedule: ReminderSchedule) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule)
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun toggleDay(dayValue: Int) {
        val current = uiState.value.activeSchedule ?: return
        val days = current.daysOfWeek.toMutableSet()
        if (days.contains(dayValue)) days.remove(dayValue) else days.add(dayValue)
        val updated = current.copy(daysOfWeek = if (days.isEmpty()) setOf(1) else days)
        updateSchedule(updated)
    }

    fun setScheduleMode(mode: ReminderMode) {
        val current = uiState.value.activeSchedule ?: return
        updateSchedule(current.copy(mode = mode))
    }

    fun setIntervalMinutes(minutes: Int) {
        val current = uiState.value.activeSchedule ?: return
        updateSchedule(current.copy(intervalMinutes = minutes))
    }

    fun setSnoozeMinutes(minutes: Int) {
        val current = uiState.value.activeSchedule ?: return
        updateSchedule(current.copy(snoozeMinutes = minutes))
    }

    // Specific Times
    fun addSpecificTime(timeStr: String) {
        val current = uiState.value.activeSchedule ?: return
        viewModelScope.launch {
            val existing = scheduleRepository.getTimesForScheduleSync(current.id)
            if (existing.none { it.time == timeStr }) {
                scheduleRepository.saveTime(
                    ReminderTime(scheduleId = current.id, time = timeStr, isEnabled = true)
                )
                reminderScheduler.scheduleNextReminder()
            }
        }
    }

    fun deleteSpecificTime(timeId: Long) {
        viewModelScope.launch {
            scheduleRepository.deleteTimeById(timeId)
            reminderScheduler.scheduleNextReminder()
        }
    }

    // Break Periods
    fun addBreakPeriod(name: String, startTime: String, endTime: String) {
        val current = uiState.value.activeSchedule ?: return
        viewModelScope.launch {
            scheduleRepository.saveBreak(
                BreakPeriod(
                    scheduleId = current.id,
                    name = name,
                    startTime = startTime,
                    endTime = endTime,
                    isEnabled = true
                )
            )
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun toggleBreakPeriod(breakPeriod: BreakPeriod) {
        viewModelScope.launch {
            scheduleRepository.saveBreak(breakPeriod.copy(isEnabled = !breakPeriod.isEnabled))
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun deleteBreakPeriod(breakId: Long) {
        viewModelScope.launch {
            scheduleRepository.deleteBreakById(breakId)
            reminderScheduler.scheduleNextReminder()
        }
    }

    class Factory(
        private val scheduleRepo: ScheduleRepository,
        private val prefsRepo: UserPreferencesRepository,
        private val scheduler: ReminderScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RemindersViewModel(scheduleRepo, prefsRepo, scheduler) as T
        }
    }
}
