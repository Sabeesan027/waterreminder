package com.example.waterreminder.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.ui.components.AdaptiveNavigationLayout
import com.example.waterreminder.ui.components.EmptyStateCard
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.GradientButton
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.ScreenHeader
import com.example.waterreminder.ui.components.SectionHeader
import com.example.waterreminder.ui.components.SegmentedSelector
import com.example.waterreminder.ui.components.SettingSwitchRow
import com.example.waterreminder.ui.components.StatusPill
import com.example.waterreminder.ui.components.formatDays
import com.example.waterreminder.ui.components.formatInterval
import com.example.waterreminder.ui.components.formatTime12h
import com.example.waterreminder.ui.reminders.RemindersViewModel

private data class ScheduleTemplate(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val startTime: String,
    val endTime: String,
    val intervalMins: Int,
    val days: Set<Int>
)

private val TEMPLATES = listOf(
    ScheduleTemplate(
        name = "Workday Focus",
        description = "Every 60m • 8:30 AM to 5:30 PM",
        icon = Icons.Default.Work,
        startTime = "08:30",
        endTime = "17:30",
        intervalMins = 60,
        days = setOf(1, 2, 3, 4, 5)
    ),
    ScheduleTemplate(
        name = "Active Lifestyle",
        description = "Every 45m • 7:00 AM to 8:00 PM",
        icon = Icons.AutoMirrored.Filled.DirectionsRun,
        startTime = "07:00",
        endTime = "20:00",
        intervalMins = 45,
        days = (1..7).toSet()
    ),
    ScheduleTemplate(
        name = "Daily Essentials",
        description = "Every 90m • 9:00 AM to 7:00 PM",
        icon = Icons.Default.WbSunny,
        startTime = "09:00",
        endTime = "19:00",
        intervalMins = 90,
        days = (1..7).toSet()
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RemindersManagementScreen(
    viewModel: RemindersViewModel,
    onAddReminder: () -> Unit,
    onEditReminder: (ReminderSchedule) -> Unit,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("active") }
    var scheduleToDelete by remember { mutableStateOf<ReminderSchedule?>(null) }

    val activeCount = state.schedules.count { it.isEnabled }
    val pausedCount = state.schedules.size - activeCount
    val visibleSchedules = when (selectedFilter) {
        "active" -> state.schedules.filter { it.isEnabled }
        "paused" -> state.schedules.filterNot { it.isEnabled }
        else -> state.schedules
    }

    AdaptiveNavigationLayout(
        currentRoute = "reminders",
        onNavigate = onNavigate
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header
            item {
                ScreenHeader(
                    title = "Reminders",
                    subtitle = "Manage your water reminder schedules.",
                    trailing = {
                        IconBadge(icon = Icons.Default.Notifications, size = 44.dp)
                    }
                )
            }

            // 2. Master Switch Card
            item {
                GlowCard(modifier = Modifier.fillMaxWidth()) {
                    SettingSwitchRow(
                        title = "Master Reminders",
                        subtitle = if (state.masterRemindersEnabled) "Reminders are actively firing" else "All reminders are paused",
                        checked = state.masterRemindersEnabled,
                        onCheckedChange = viewModel::toggleMasterReminders,
                        icon = if (state.masterRemindersEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff
                    )
                }
            }

            // 3. Segmented Filter
            item {
                SegmentedSelector(
                    options = listOf(
                        "active" to "Active ($activeCount)",
                        "paused" to "Paused ($pausedCount)",
                        "all" to "All (${state.schedules.size})"
                    ),
                    selectedKey = selectedFilter,
                    onSelect = { selectedFilter = it }
                )
            }

            // 4. Schedules List or Empty State
            if (visibleSchedules.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Alarm,
                        title = when (selectedFilter) {
                            "active" -> "No active reminders"
                            "paused" -> "No paused reminders"
                            else -> "No reminder schedules yet"
                        },
                        message = "Create a schedule to receive timely notifications."
                    ) {
                        FilledTonalButton(
                            onClick = onAddReminder,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Reminder", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(visibleSchedules, key = { it.id }) { schedule ->
                    ReminderCardItem(
                        schedule = schedule,
                        onToggle = { viewModel.updateSchedule(schedule.copy(isEnabled = it)) },
                        onEdit = { onEditReminder(schedule) },
                        onDelete = { scheduleToDelete = schedule }
                    )
                }
            }

            // 5. Add New Schedule Action Button
            item {
                GradientButton(
                    text = "Add New Reminder",
                    icon = Icons.Default.Add,
                    onClick = onAddReminder
                )
            }

            // 6. Quick Templates Section
            item {
                SectionHeader(
                    title = "Quick Templates",
                    icon = Icons.Default.Alarm
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TEMPLATES.forEach { template ->
                        TemplateRow(
                            template = template,
                            onClick = {
                                viewModel.updateSchedule(
                                    ReminderSchedule(
                                        name = template.name,
                                        isEnabled = true,
                                        startTime = template.startTime,
                                        endTime = template.endTime,
                                        intervalMinutes = template.intervalMins,
                                        daysOfWeek = template.days
                                    )
                                )
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    scheduleToDelete?.let { schedule ->
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Delete Reminder?") },
            text = { Text("Are you sure you want to remove \"${schedule.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSchedule(schedule)
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReminderCardItem(
    schedule: ReminderSchedule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                IconBadge(
                    icon = Icons.Default.Alarm,
                    size = 42.dp,
                    tint = if (schedule.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatTime12h(schedule.startTime)} – ${formatTime12h(schedule.endTime)} • Every ${formatInterval(schedule.intervalMinutes)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatDays(schedule.daysOfWeek),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateRow(
    template: ScheduleTemplate,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = template.icon, size = 38.dp, iconSize = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = template.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = template.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            StatusPill(
                text = "Add",
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
