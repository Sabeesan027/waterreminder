package com.example.waterreminder.ui.onboarding

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.domain.model.ActivityMode
import com.example.waterreminder.ui.components.CustomTimePickerDialog

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            ProgressHeader(currentPage = state.currentPage)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (state.currentPage) {
                    0 -> WelcomePage()
                    1 -> PreferencesPage(
                        name = state.userName,
                        unit = state.unit,
                        goalMl = state.dailyGoalMl,
                        quickAddMl = state.quickAddMl,
                        onNameChange = viewModel::updateName,
                        onUnitChange = viewModel::updateUnit,
                        onGoalChange = viewModel::updateGoal,
                        onQuickAddChange = viewModel::updateQuickAdd
                    )
                    2 -> RoutinePage(
                        wakeTime = state.wakeUpTime,
                        sleepTime = state.sleepTime,
                        workStart = state.workStartTime,
                        workEnd = state.workEndTime,
                        lunchStart = state.lunchStart,
                        lunchEnd = state.lunchEnd,
                        onWakeTimeChange = viewModel::updateWakeUpTime,
                        onSleepTimeChange = viewModel::updateSleepTime,
                        onWorkStartChange = viewModel::updateWorkStart,
                        onWorkEndChange = viewModel::updateWorkEnd,
                        onLunchStartChange = viewModel::updateLunchStart,
                        onLunchEndChange = viewModel::updateLunchEnd
                    )
                    3 -> ActivityModePage(
                        selectedMode = state.activityMode,
                        onModeSelect = viewModel::updateActivityMode
                    )
                    4 -> ReminderConfigPage(
                        remindersEnabled = state.remindersEnabled,
                        workingDays = state.workingDays,
                        intervalMinutes = state.reminderIntervalMinutes,
                        snoozeMinutes = state.snoozeMinutes,
                        onRemindersEnabledChange = viewModel::updateRemindersEnabled,
                        onToggleWorkingDay = viewModel::toggleWorkingDay,
                        onIntervalChange = viewModel::updateInterval,
                        onSnoozeChange = viewModel::updateSnooze
                    )
                }
            }

            NavigationFooter(
                currentPage = state.currentPage,
                onBack = viewModel::previousPage,
                onNext = viewModel::nextPage,
                onFinish = { viewModel.completeOnboarding(onOnboardingFinished) }
            )
        }
    }
}

@Composable
private fun ProgressHeader(currentPage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        for (i in 0..4) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(if (i == currentPage) 24.dp else 10.dp, 10.dp)
                    .clip(CircleShape)
                    .background(
                        if (i == currentPage) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

@Composable
private fun NavigationFooter(
    currentPage: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentPage > 0) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (currentPage < 4) {
            Button(onClick = onNext) {
                Text("Next")
            }
        } else {
            Button(onClick = onFinish) {
                Text("Get Started")
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "💧", fontSize = 72.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Water Reminder",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Stay hydrated, healthy, and focused throughout your workday and active routine.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Medical Disclaimer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "The default 3500 ml target is a reference example. Fluid requirements vary based on climate, physical activity, and medical status. Please follow individual medical guidance if prescribed fluid limits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun PreferencesPage(
    name: String,
    unit: String,
    goalMl: Int,
    quickAddMl: Int,
    onNameChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onGoalChange: (Int) -> Unit,
    onQuickAddChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Personal Preferences",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        NameTextField(name = name, onNameChange = onNameChange)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Measurement Unit",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            FilterChip(
                selected = unit == "ml",
                onClick = { onUnitChange("ml") },
                label = { Text("Milliliters (ml)") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = unit == "oz",
                onClick = { onUnitChange("oz") },
                label = { Text("Ounces (fl oz)") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GoalTextField(goalMl = goalMl, onGoalChange = onGoalChange)

        Spacer(modifier = Modifier.height(16.dp))

        QuickAddTextField(quickAddMl = quickAddMl, onQuickAddChange = onQuickAddChange)
    }
}

@Composable
private fun NameTextField(name: String, onNameChange: (String) -> Unit) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Your Name (Optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GoalTextField(goalMl: Int, onGoalChange: (Int) -> Unit) {
    OutlinedTextField(
        value = goalMl.toString(),
        onValueChange = { inputStr: String ->
            val num = inputStr.filter { it.isDigit() }.toIntOrNull()
            if (num != null) {
                onGoalChange(num)
            }
        },
        label = { Text("Daily Water Goal (ml)") },
        suffix = { Text("ml") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun QuickAddTextField(quickAddMl: Int, onQuickAddChange: (Int) -> Unit) {
    OutlinedTextField(
        value = quickAddMl.toString(),
        onValueChange = { inputStr: String ->
            val num = inputStr.filter { it.isDigit() }.toIntOrNull()
            if (num != null) {
                onQuickAddChange(num)
            }
        },
        label = { Text("Preferred Quick-Add Amount (ml)") },
        suffix = { Text("ml") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RoutinePage(
    wakeTime: String,
    sleepTime: String,
    workStart: String,
    workEnd: String,
    lunchStart: String,
    lunchEnd: String,
    onWakeTimeChange: (String) -> Unit,
    onSleepTimeChange: (String) -> Unit,
    onWorkStartChange: (String) -> Unit,
    onWorkEndChange: (String) -> Unit,
    onLunchStartChange: (String) -> Unit,
    onLunchEndChange: (String) -> Unit
) {
    var activeTarget by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Daily Routine",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        TimePickerRow(label = "Wake-up Time", time = wakeTime) { activeTarget = "wake" }
        TimePickerRow(label = "Work Start Time", time = workStart) { activeTarget = "workStart" }
        TimePickerRow(label = "Lunch Break Start", time = lunchStart) { activeTarget = "lunchStart" }
        TimePickerRow(label = "Lunch Break End", time = lunchEnd) { activeTarget = "lunchEnd" }
        TimePickerRow(label = "Work End Time", time = workEnd) { activeTarget = "workEnd" }
        TimePickerRow(label = "Sleep Time", time = sleepTime) { activeTarget = "sleep" }
    }

    activeTarget?.let { target ->
        val currentVal = when (target) {
            "wake" -> wakeTime
            "workStart" -> workStart
            "lunchStart" -> lunchStart
            "lunchEnd" -> lunchEnd
            "workEnd" -> workEnd
            else -> sleepTime
        }
        CustomTimePickerDialog(
            initialTime = currentVal,
            onDismissRequest = { activeTarget = null },
            onTimeSelected = { selectedTime ->
                when (target) {
                    "wake" -> onWakeTimeChange(selectedTime)
                    "workStart" -> onWorkStartChange(selectedTime)
                    "lunchStart" -> onLunchStartChange(selectedTime)
                    "lunchEnd" -> onLunchEndChange(selectedTime)
                    "workEnd" -> onWorkEndChange(selectedTime)
                    "sleep" -> onSleepTimeChange(selectedTime)
                }
                activeTarget = null
            }
        )
    }
}

@Composable
private fun TimePickerRow(
    label: String,
    time: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onClick) {
            Text(text = time, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActivityModePage(
    selectedMode: ActivityMode,
    onModeSelect: (ActivityMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Activity Mode",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActivityMode.values().forEach { mode ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMode == mode) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = { onModeSelect(mode) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mode.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { onModeSelect(mode) },
                            label = { Text(if (selectedMode == mode) "Selected" else "Select") }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mode.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ReminderConfigPage(
    remindersEnabled: Boolean,
    workingDays: Set<Int>,
    intervalMinutes: Int,
    snoozeMinutes: Int,
    onRemindersEnabledChange: (Boolean) -> Unit,
    onToggleWorkingDay: (Int) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onSnoozeChange: (Int) -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Reminder Setup",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Enable Reminders", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = remindersEnabled,
                onCheckedChange = onRemindersEnabledChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Active Days", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val daysLabels = listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S")
            daysLabels.forEach { (dayVal, label) ->
                val isSelected = workingDays.contains(dayVal)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleWorkingDay(dayVal) },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Reminder Interval", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            listOf(30, 45, 60, 90).forEach { mins ->
                FilterChip(
                    selected = intervalMinutes == mins,
                    onClick = { onIntervalChange(mins) },
                    label = { Text("${mins}m") }
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Snooze Duration", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            listOf(5, 10, 15, 20, 30).forEach { mins ->
                FilterChip(
                    selected = snoozeMinutes == mins,
                    onClick = { onSnoozeChange(mins) },
                    label = { Text("${mins}m") }
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = { permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Notification Permission")
            }
        }
    }
}
