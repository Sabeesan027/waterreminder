package com.example.waterreminder.ui.reminders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.waterreminder.domain.model.BreakPeriod
import com.example.waterreminder.domain.model.ReminderMode
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.domain.model.ReminderTime
import com.example.waterreminder.ui.components.CustomTimePickerDialog

@Composable
fun RemindersScreen(viewModel: RemindersViewModel) {
    val state by viewModel.uiState.collectAsState()

    var showAddTimeDialog by remember { mutableStateOf(false) }
    var showAddBreakDialog by remember { mutableStateOf(false) }
    var timePickerTarget by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MasterSwitchCard(
                    enabled = state.masterRemindersEnabled,
                    onToggle = { viewModel.toggleMasterReminders(it) }
                )
            }

            if (state.schedules.isNotEmpty()) {
                item {
                    ScheduleSelectorRow(
                        schedules = state.schedules,
                        activeScheduleId = state.activeSchedule?.id,
                        onSelectSchedule = { viewModel.selectSchedule(it) }
                    )
                }
            }

            val active = state.activeSchedule
            if (active != null) {
                item {
                    DaysSelectorCard(
                        activeDays = active.daysOfWeek,
                        onToggleDay = { viewModel.toggleDay(it) }
                    )
                }

                item {
                    ScheduleWindowCard(
                        startTime = active.startTime,
                        endTime = active.endTime,
                        onPickStart = { timePickerTarget = "start" },
                        onPickEnd = { timePickerTarget = "end" }
                    )
                }

                item {
                    ReminderModeCard(
                        mode = active.mode,
                        intervalMinutes = active.intervalMinutes,
                        specificTimes = state.specificTimes,
                        onSetMode = { viewModel.setScheduleMode(it) },
                        onSetInterval = { viewModel.setIntervalMinutes(it) },
                        onOpenAddTime = { showAddTimeDialog = true },
                        onDeleteTime = { viewModel.deleteSpecificTime(it) }
                    )
                }

                item {
                    SnoozeAndBreaksCard(
                        snoozeMinutes = active.snoozeMinutes,
                        breakPeriods = state.breakPeriods,
                        onSetSnooze = { viewModel.setSnoozeMinutes(it) },
                        onOpenAddBreak = { showAddBreakDialog = true },
                        onToggleBreak = { viewModel.toggleBreakPeriod(it) },
                        onDeleteBreak = { viewModel.deleteBreakPeriod(it) }
                    )
                }

                item {
                    SuppressGoalCard(
                        suppressAfterGoalReached = active.suppressAfterGoalReached,
                        onToggle = { viewModel.updateSchedule(active.copy(suppressAfterGoalReached = it)) }
                    )
                }
            }
        }
    }

    timePickerTarget?.let { target ->
        val active = state.activeSchedule ?: return@let
        val initVal = if (target == "start") active.startTime else active.endTime
        CustomTimePickerDialog(
            initialTime = initVal,
            onDismissRequest = { timePickerTarget = null },
            onTimeSelected = { selectedTime ->
                if (target == "start") {
                    viewModel.updateSchedule(active.copy(startTime = selectedTime))
                } else {
                    viewModel.updateSchedule(active.copy(endTime = selectedTime))
                }
                timePickerTarget = null
            }
        )
    }

    if (showAddTimeDialog) {
        CustomTimePickerDialog(
            initialTime = "10:00",
            title = "Add Exact Reminder Time",
            onDismissRequest = { showAddTimeDialog = false },
            onTimeSelected = { timeStr ->
                viewModel.addSpecificTime(timeStr)
                showAddTimeDialog = false
            }
        )
    }

    if (showAddBreakDialog) {
        AddBreakDialog(
            onDismissRequest = { showAddBreakDialog = false },
            onConfirm = { name, start, end ->
                viewModel.addBreakPeriod(name, start, end)
                showAddBreakDialog = false
            }
        )
    }
}

@Composable
private fun MasterSwitchCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Reminders Master Switch",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (enabled) "Active" else "Paused",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun ScheduleSelectorRow(
    schedules: List<ReminderSchedule>,
    activeScheduleId: Long?,
    onSelectSchedule: (Long) -> Unit
) {
    Column {
        Text("Schedules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row {
            schedules.forEach { s ->
                FilterChip(
                    selected = activeScheduleId == s.id,
                    onClick = { onSelectSchedule(s.id) },
                    label = { Text(s.name) }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun DaysSelectorCard(
    activeDays: Set<Int>,
    onToggleDay: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Active Days", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dayLabels = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
                dayLabels.forEach { (valDay, label) ->
                    FilterChip(
                        selected = activeDays.contains(valDay),
                        onClick = { onToggleDay(valDay) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleWindowCard(
    startTime: String,
    endTime: String,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Schedule Window", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Start Time", style = MaterialTheme.typography.labelSmall)
                    OutlinedButton(onClick = onPickStart) {
                        Text(startTime, fontWeight = FontWeight.Bold)
                    }
                }
                Text("to", style = MaterialTheme.typography.bodyMedium)
                Column {
                    Text("End Time", style = MaterialTheme.typography.labelSmall)
                    OutlinedButton(onClick = onPickEnd) {
                        Text(endTime, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderModeCard(
    mode: ReminderMode,
    intervalMinutes: Int,
    specificTimes: List<ReminderTime>,
    onSetMode: (ReminderMode) -> Unit,
    onSetInterval: (Int) -> Unit,
    onOpenAddTime: () -> Unit,
    onDeleteTime: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Reminder Mode", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                FilterChip(
                    selected = mode == ReminderMode.INTERVAL,
                    onClick = { onSetMode(ReminderMode.INTERVAL) },
                    label = { Text("Interval") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = mode == ReminderMode.SPECIFIC_TIMES,
                    onClick = { onSetMode(ReminderMode.SPECIFIC_TIMES) },
                    label = { Text("Specific Times") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mode == ReminderMode.INTERVAL) {
                Text("Frequency Preset", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    listOf(15, 30, 45, 60, 90, 120).forEach { mins ->
                        FilterChip(
                            selected = intervalMinutes == mins,
                            onClick = { onSetInterval(mins) },
                            label = { Text("${mins}m") }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Exact Reminder Times", style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(onClick = onOpenAddTime) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Add Time")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (specificTimes.isEmpty()) {
                    Text("No specific times added yet.", style = MaterialTheme.typography.bodySmall)
                } else {
                    specificTimes.forEach { st ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(st.time, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onDeleteTime(st.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnoozeAndBreaksCard(
    snoozeMinutes: Int,
    breakPeriods: List<BreakPeriod>,
    onSetSnooze: (Int) -> Unit,
    onOpenAddBreak: () -> Unit,
    onToggleBreak: (BreakPeriod) -> Unit,
    onDeleteBreak: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Snooze Duration", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row {
                listOf(5, 10, 15, 20, 30, 60).forEach { mins ->
                    FilterChip(
                        selected = snoozeMinutes == mins,
                        onClick = { onSetSnooze(mins) },
                        label = { Text("${mins}m") }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Break Periods", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onOpenAddBreak) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Break")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (breakPeriods.isEmpty()) {
                Text("No breaks configured.", style = MaterialTheme.typography.bodySmall)
            } else {
                breakPeriods.forEach { bp ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("${bp.startTime} - ${bp.endTime}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = bp.isEnabled,
                                onCheckedChange = { onToggleBreak(bp) }
                            )
                            IconButton(onClick = { onDeleteBreak(bp.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuppressGoalCard(
    suppressAfterGoalReached: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pause Reminders After Goal Met", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("Stops reminders for today once daily goal is reached.", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = suppressAfterGoalReached,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun AddBreakDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, start: String, end: String) -> Unit
) {
    var name by remember { mutableStateOf("Tea Break") }
    var startTime by remember { mutableStateOf("15:00") }
    var endTime by remember { mutableStateOf("15:15") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Add Break Period", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Break Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Start", style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(onClick = { }) { Text(startTime) }
                    }
                    Column {
                        Text("End", style = MaterialTheme.typography.labelSmall)
                        OutlinedButton(onClick = { }) { Text(endTime) }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(name, startTime, endTime) }) { Text("Save") }
                }
            }
        }
    }
}
