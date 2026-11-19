package com.example.waterreminder.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.domain.model.ActivityMode
import com.example.waterreminder.domain.model.UserPreferences
import com.example.waterreminder.scheduler.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val waterRepository: WaterRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateName(name: String) {
        viewModelScope.launch {
            val current = userPreferences.value
            userPreferencesRepository.updatePreferences(current.copy(userName = name.trim()))
        }
    }

    fun updateDailyGoal(goalMl: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateDailyGoal(goalMl)
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun updateWorkAllocation(percent: Int) {
        viewModelScope.launch {
            val current = userPreferences.value
            userPreferencesRepository.updatePreferences(current.copy(workAllocationPercent = percent))
        }
    }

    fun updateActivityMode(mode: ActivityMode) {
        viewModelScope.launch {
            userPreferencesRepository.updateActivityMode(mode)
            reminderScheduler.scheduleNextReminder()
        }
    }

    fun updateQuickAddAmount(amountMl: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateQuickAddAmount(amountMl)
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferences.value
            userPreferencesRepository.updatePreferences(current.copy(soundEnabled = enabled))
        }
    }

    fun toggleVibrate(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferences.value
            userPreferencesRepository.updatePreferences(current.copy(vibrateEnabled = enabled))
        }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            val current = userPreferences.value
            userPreferencesRepository.updatePreferences(current.copy(darkThemeMode = mode))
        }
    }

    fun clearAllHistory(onCleared: () -> Unit) {
        viewModelScope.launch {
            waterRepository.clearAllLogs()
            onCleared()
        }
    }

    fun resetAllSettings(onReset: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.resetPreferences()
            waterRepository.clearAllLogs()
            reminderScheduler.cancelReminder()
            onReset()
        }
    }

    class Factory(
        private val prefsRepo: UserPreferencesRepository,
        private val waterRepo: WaterRepository,
        private val scheduler: ReminderScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(prefsRepo, waterRepo, scheduler) as T
        }
    }
}
