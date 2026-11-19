package com.example.waterreminder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.ui.components.DAY_PRESETS
import com.example.waterreminder.ui.components.DaySelector
import com.example.waterreminder.ui.components.FieldLabel
import com.example.waterreminder.ui.components.FormScreenScaffold
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.GradientButton
import com.example.waterreminder.ui.components.HeroIcon
import com.example.waterreminder.ui.components.OnboardingStepIndicator
import com.example.waterreminder.ui.components.SelectableChip
import com.example.waterreminder.ui.components.SettingSwitchRow
import com.example.waterreminder.ui.components.SoftDivider
import com.example.waterreminder.ui.components.StaggeredEntrance
import com.example.waterreminder.ui.components.formatDays
import com.example.waterreminder.ui.components.formatInterval

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderSetupScreen(
    initialRemindersEnabled: Boolean = true,
    initialWorkingDays: Set<Int> = setOf(1, 2, 3, 4, 5),
    initialIntervalMinutes: Int = 60,
    initialSnoozeMinutes: Int = 15,
    onFinishSetup: (
        enabled: Boolean,
        days: Set<Int>,
        intervalMins: Int,
        snoozeMins: Int,
        suppressBreaks: Boolean
    ) -> Unit
) {
    var remindersEnabled by remember { mutableStateOf(initialRemindersEnabled) }
    var selectedDays by remember { mutableStateOf(initialWorkingDays) }
    var intervalMins by remember { mutableIntStateOf(initialIntervalMinutes) }
    var snoozeMins by remember { mutableIntStateOf(initialSnoozeMinutes) }
    var suppressDuringBreaks by remember { mutableStateOf(true) }

    val intervalOptions = listOf(30, 45, 60, 90, 120)
    val snoozeOptions = listOf(5, 10, 15, 20, 30)

    FormScreenScaffold(
        bottomBar = {
            OnboardingStepIndicator(currentStep = 3)
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Complete Setup",
                icon = Icons.Default.Check,
                onClick = {
                    onFinishSetup(
                        remindersEnabled,
                        selectedDays,
                        intervalMins,
                        snoozeMins,
                        suppressDuringBreaks
                    )
                }
            )
        }
    ) {
        StaggeredEntrance(index = 0) {
            HeroIcon(icon = Icons.Default.NotificationsActive, size = 108.dp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        StaggeredEntrance(index = 1) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Set Up Your Reminders",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Choose when and how often you want gentle hydration nudges.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Master toggle card
        StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                SettingSwitchRow(
                    title = "Enable reminders",
                    subtitle = if (remindersEnabled) "You'll be notified on schedule" else "Reminders are paused",
                    checked = remindersEnabled,
                    onCheckedChange = { remindersEnabled = it },
                    icon = Icons.Default.NotificationsActive
                )
            }
        }

        // Detailed settings (collapses when reminders are disabled)
        AnimatedVisibility(
            visible = remindersEnabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Reminder Days Card
                GlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FieldLabel("Reminder days")
                            Text(
                                text = formatDays(selectedDays),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Presets
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

                        // Individual days
                        DaySelector(
                            selectedDays = selectedDays,
                            onDaysChange = { selectedDays = it }
                        )
                    }
                }

                // Frequency & Snooze Card
                GlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Frequency
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FieldLabel("Reminder frequency")
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
                                FieldLabel("Snooze duration")
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

                        SoftDivider()

                        // Suppress during breaks
                        SettingSwitchRow(
                            title = "Pause during breaks",
                            subtitle = "Silence reminders during your lunch/break times",
                            checked = suppressDuringBreaks,
                            onCheckedChange = { suppressDuringBreaks = it },
                            icon = Icons.Default.WorkHistory
                        )
                    }
                }
            }
        }
    }
}
