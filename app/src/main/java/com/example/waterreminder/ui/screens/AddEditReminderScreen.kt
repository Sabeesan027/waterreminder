package com.example.waterreminder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.ui.components.CustomTimePickerDialog
import com.example.waterreminder.ui.components.DAY_PRESETS
import com.example.waterreminder.ui.components.DaySelector
import com.example.waterreminder.ui.components.DetailTopBar
import com.example.waterreminder.ui.components.FieldLabel
import com.example.waterreminder.ui.components.FormScreenScaffold
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.GradientButton
import com.example.waterreminder.ui.components.HeroIcon
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.SelectableChip
import com.example.waterreminder.ui.components.SettingSwitchRow
import com.example.waterreminder.ui.components.SoftDivider
import com.example.waterreminder.ui.components.StaggeredEntrance
import com.example.waterreminder.ui.components.StatusPill
import com.example.waterreminder.ui.components.formatDays
import com.example.waterreminder.ui.components.formatInterval
import com.example.waterreminder.ui.components.formatTime12h

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditReminderScreen(
    schedule: ReminderSchedule? = null,
    onSave: (ReminderSchedule) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(schedule?.name ?: "Afternoon Hydration") }
    var selectedDays by remember { mutableStateOf(schedule?.daysOfWeek ?: setOf(1, 2, 3, 4, 5)) }
    var startTimeStr by remember { mutableStateOf(schedule?.startTime ?: "08:30") }
    var endTimeStr by remember { mutableStateOf(schedule?.endTime ?: "17:30") }
    var intervalMins by remember { mutableIntStateOf(schedule?.intervalMinutes ?: 60) }
    var snoozeMins by remember { mutableIntStateOf(schedule?.snoozeMinutes ?: 15) }
    var isEnabled by remember { mutableStateOf(schedule?.isEnabled ?: true) }

    var editingTimeTarget by remember { mutableStateOf<String?>(null) } // "start", "end", or null

    val intervalOptions = listOf(30, 45, 60, 90, 120)
    val snoozeOptions = listOf(5, 10, 15, 20, 30)

    val isEditing = schedule != null

    FormScreenScaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            DetailTopBar(
                title = if (isEditing) "Edit Reminder" else "New Reminder",
                subtitle = if (isEditing) "Update schedule settings" else "Configure a custom schedule",
                onBack = onBack
            )
        },
        bottomBar = {
            GradientButton(
                text = if (isEditing) "Save Changes" else "Create Reminder",
                icon = Icons.Default.Check,
                onClick = {
                    val updated = (schedule ?: ReminderSchedule()).copy(
                        name = title.ifBlank { "Water Reminder" },
                        daysOfWeek = selectedDays,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        intervalMinutes = intervalMins,
                        snoozeMinutes = snoozeMins,
                        isEnabled = isEnabled
                    )
                    onSave(updated)
                }
            )
        }
    ) {
        StaggeredEntrance(index = 0) {
            HeroIcon(icon = Icons.Default.Alarm, size = 96.dp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Schedule summary preview badge
        StaggeredEntrance(index = 1) {
            StatusPill(
                text = "${formatTime12h(startTimeStr)} – ${formatTime12h(endTimeStr)} • Every ${formatInterval(intervalMins)}",
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.Schedule
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title & Active state card
        StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FieldLabel("Schedule Name")
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("e.g. Workday Hydration") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    SoftDivider()

                    SettingSwitchRow(
                        title = "Enable schedule",
                        subtitle = if (isEnabled) "Active and notifying" else "Temporarily paused",
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        icon = Icons.Default.NotificationsActive
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Time Window Card (Start & End)
        StaggeredEntrance(index = 3, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FieldLabel("Active Hours")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimeSelectorButton(
                            label = "Start time",
                            time = startTimeStr,
                            modifier = Modifier.weight(1f),
                            onClick = { editingTimeTarget = "start" }
                        )
                        TimeSelectorButton(
                            label = "End time",
                            time = endTimeStr,
                            modifier = Modifier.weight(1f),
                            onClick = { editingTimeTarget = "end" }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Repeat Days Card
        StaggeredEntrance(index = 4, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FieldLabel("Repeat Days")
                        Text(
                            text = formatDays(selectedDays),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DAY_PRESETS.forEach { (label, preset) ->
                            SelectableChip(
                                label = label,
                                selected = selectedDays == preset,
                                onClick = { selectedDays = preset },
                                compact = true
                            )
                        }
                    }

                    DaySelector(
                        selectedDays = selectedDays,
                        onDaysChange = { selectedDays = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Frequency & Snooze Card
        StaggeredEntrance(index = 5, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Frequency
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FieldLabel("Reminder Frequency")
                            Text(
                                text = "Every " + formatInterval(intervalMins),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            intervalOptions.forEach { mins ->
                                SelectableChip(
                                    label = formatInterval(mins),
                                    selected = intervalMins == mins,
                                    icon = Icons.Default.Schedule,
                                    onClick = { intervalMins = mins }
                                )
                            }
                        }
                    }

                    SoftDivider()

                    // Snooze
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FieldLabel("Snooze Duration")
                            Text(
                                text = "$snoozeMins min",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            snoozeOptions.forEach { mins ->
                                SelectableChip(
                                    label = "${mins}m",
                                    selected = snoozeMins == mins,
                                    icon = Icons.Default.Snooze,
                                    onClick = { snoozeMins = mins },
                                    compact = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editingTimeTarget?.let { target ->
        val initial = if (target == "start") startTimeStr else endTimeStr
        CustomTimePickerDialog(
            initialTime = initial,
            title = if (target == "start") "Select Start Time" else "Select End Time",
            onDismissRequest = { editingTimeTarget = null },
            onTimeSelected = { selectedTime ->
                if (target == "start") startTimeStr = selectedTime else endTimeStr = selectedTime
                editingTimeTarget = null
            }
        )
    }
}

@Composable
private fun TimeSelectorButton(
    label: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatTime12h(time),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
