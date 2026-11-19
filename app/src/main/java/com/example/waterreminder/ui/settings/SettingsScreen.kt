package com.example.waterreminder.ui.settings

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import com.example.waterreminder.domain.model.ActivityMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onResetCompleted: () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

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
                ProfileSection(
                    userName = prefs.userName,
                    dailyGoalMl = prefs.dailyGoalMl,
                    workAllocationPercent = prefs.workAllocationPercent,
                    quickAddMl = prefs.quickAddMl,
                    onNameChange = { viewModel.updateName(it) },
                    onGoalChange = { viewModel.updateDailyGoal(it) },
                    onWorkAllocationChange = { viewModel.updateWorkAllocation(it) },
                    onQuickAddChange = { viewModel.updateQuickAddAmount(it) }
                )
            }

            item {
                ActivityModeSection(
                    currentMode = prefs.activityMode,
                    onSelectMode = { viewModel.updateActivityMode(it) }
                )
            }

            item {
                NotificationPrefSection(
                    soundEnabled = prefs.soundEnabled,
                    vibrateEnabled = prefs.vibrateEnabled,
                    onToggleSound = { viewModel.toggleSound(it) },
                    onToggleVibrate = { viewModel.toggleVibrate(it) }
                )
            }

            item {
                AppearanceSection(
                    currentTheme = prefs.darkThemeMode,
                    onSelectTheme = { viewModel.updateThemeMode(it) }
                )
            }

            item {
                DataManagementSection(
                    onClearHistoryClick = { showClearConfirm = true },
                    onResetClick = { showResetConfirm = true }
                )
            }

            item {
                AboutSection()
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Water History") },
            text = { Text("Are you sure you want to delete all recorded water logs? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory { showClearConfirm = false }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset All Settings") },
            text = { Text("This will reset all preferences, schedules, and water history to initial defaults. You will return to the onboarding flow.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllSettings {
                            showResetConfirm = false
                            onResetCompleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileSection(
    userName: String,
    dailyGoalMl: Int,
    workAllocationPercent: Int,
    quickAddMl: Int,
    onNameChange: (String) -> Unit,
    onGoalChange: (Int) -> Unit,
    onWorkAllocationChange: (Int) -> Unit,
    onQuickAddChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Profile & Hydration Goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = onNameChange,
                label = { Text("Your Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoalField(goalMl = dailyGoalMl, onGoalChange = onGoalChange)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Workday Allocation: $workAllocationPercent%", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row {
                listOf(50, 60, 70, 80).forEach { pct ->
                    FilterChip(
                        selected = workAllocationPercent == pct,
                        onClick = { onWorkAllocationChange(pct) },
                        label = { Text("${pct}%") }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            QuickAddField(quickAddMl = quickAddMl, onQuickAddChange = onQuickAddChange)
        }
    }
}

@Composable
private fun GoalField(goalMl: Int, onGoalChange: (Int) -> Unit) {
    OutlinedTextField(
        value = goalMl.toString(),
        onValueChange = { inputStr: String ->
            val num = inputStr.filter { it.isDigit() }.toIntOrNull()
            if (num != null) onGoalChange(num)
        },
        label = { Text("Daily Goal (ml)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun QuickAddField(quickAddMl: Int, onQuickAddChange: (Int) -> Unit) {
    OutlinedTextField(
        value = quickAddMl.toString(),
        onValueChange = { inputStr: String ->
            val num = inputStr.filter { it.isDigit() }.toIntOrNull()
            if (num != null) onQuickAddChange(num)
        },
        label = { Text("Default Quick-Add (ml)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ActivityModeSection(
    currentMode: ActivityMode,
    onSelectMode: (ActivityMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Activity Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            ActivityMode.values().forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(mode.displayName, fontWeight = FontWeight.SemiBold)
                        Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilterChip(
                        selected = currentMode == mode,
                        onClick = { onSelectMode(mode) },
                        label = { Text(if (currentMode == mode) "Active" else "Select") }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPrefSection(
    soundEnabled: Boolean,
    vibrateEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibrate: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Notification Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sound", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = soundEnabled, onCheckedChange = onToggleSound)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vibration", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = vibrateEnabled, onCheckedChange = onToggleVibrate)
            }
        }
    }
}

@Composable
private fun AppearanceSection(
    currentTheme: String,
    onSelectTheme: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row {
                listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (key, label) ->
                    FilterChip(
                        selected = currentTheme == key,
                        onClick = { onSelectTheme(key) },
                        label = { Text(label) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DataManagementSection(
    onClearHistoryClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Data Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClearHistoryClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear Hydration History")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onResetClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset All Application Settings")
            }
        }
    }
}

@Composable
private fun AboutSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Water Reminder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Version 1.0 • Built with Kotlin & Jetpack Compose", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Medical Disclaimer: The default goal of 3500 ml is an editable example. Individual hydration needs vary depending on activity, climate, and medical conditions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
