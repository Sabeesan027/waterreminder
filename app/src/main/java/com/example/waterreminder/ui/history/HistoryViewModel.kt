package com.example.waterreminder.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.preferences.UserPreferencesRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.domain.model.HydrationSummary
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.ui.components.BarItemData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HistoryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateFormatted: String = "",
    val dailySummary: HydrationSummary = HydrationSummary(
        date = LocalDate.now().toString(),
        consumedMl = 0,
        goalMl = 3500,
        workConsumedMl = 0,
        workGoalMl = 2450,
        outsideWorkConsumedMl = 0,
        outsideWorkGoalMl = 1050
    ),
    val weeklyBarData: List<BarItemData> = emptyList(),
    val monthlyTotalMl: Int = 0,
    val monthlyAverageMl: Int = 0,
    val monthlyGoalCompletionRate: Float = 0f,
    val totalLogsCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val waterRepository: WaterRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val uiState: StateFlow<HistoryUiState> = combine(
        _selectedDate,
        userPreferencesRepository.userPreferencesFlow
    ) { date, prefs ->
        Pair(date, prefs)
    }.flatMapLatest { (date, prefs) ->
        val dateStr = date.toString()

        val dailySummaryFlow = waterRepository.getHydrationSummary(
            date = dateStr,
            defaultGoalMl = prefs.dailyGoalMl,
            workAllocationPercent = prefs.workAllocationPercent,
            workStartTime = prefs.workStartTime,
            workEndTime = prefs.workEndTime
        )

        // Calculate Monday of selected date's week
        val monday = date.with(DayOfWeek.MONDAY)
        val sunday = date.with(DayOfWeek.SUNDAY)

        val weeklyFlow = waterRepository.getLogsForRange(monday.toString(), sunday.toString())

        combine(dailySummaryFlow, weeklyFlow) { summary, weekLogs ->
            val barItems = mutableListOf<BarItemData>()
            for (i in 0..6) {
                val d = monday.plusDays(i.toLong())
                val dayLogs = weekLogs.filter {
                    val logDate = java.time.Instant.ofEpochMilli(it.timestamp)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    logDate == d
                }
                val consumed = dayLogs.sumOf { it.amountMl }
                barItems.add(
                    BarItemData(
                        dayLabel = d.dayOfWeek.name.take(3),
                        consumedMl = consumed,
                        goalMl = prefs.dailyGoalMl,
                        isSelected = d == date
                    )
                )
            }

            val formattedDate = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
            val isToday = date == LocalDate.now()

            HistoryUiState(
                selectedDate = date,
                selectedDateFormatted = if (isToday) "Today, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}" else formattedDate,
                dailySummary = summary,
                weeklyBarData = barItems,
                monthlyTotalMl = barItems.sumOf { it.consumedMl },
                monthlyAverageMl = (barItems.map { it.consumedMl }.average()).toInt(),
                monthlyGoalCompletionRate = if (barItems.isNotEmpty()) (barItems.count { it.consumedMl >= it.goalMl }.toFloat() / barItems.size * 100f) else 0f,
                totalLogsCount = summary.logs.size
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun selectPreviousDay() {
        _selectedDate.update { it.minusDays(1) }
    }

    fun selectNextDay() {
        _selectedDate.update { it.plusDays(1) }
    }

    fun selectToday() {
        _selectedDate.update { LocalDate.now() }
    }

    fun deleteWaterLog(log: WaterLog) {
        viewModelScope.launch {
            waterRepository.deleteWaterLog(log, _selectedDate.value.toString())
        }
    }

    fun updateWaterLog(log: WaterLog, amountMl: Int, note: String) {
        viewModelScope.launch {
            val updated = log.copy(amountMl = amountMl, note = note)
            waterRepository.updateWaterLog(updated, _selectedDate.value.toString())
        }
    }

    class Factory(
        private val waterRepo: WaterRepository,
        private val prefsRepo: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(waterRepo, prefsRepo) as T
        }
    }
}
