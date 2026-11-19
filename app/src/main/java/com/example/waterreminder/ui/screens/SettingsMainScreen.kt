package com.example.waterreminder.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.ui.components.AdaptiveNavigationLayout
import com.example.waterreminder.ui.components.FieldLabel
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.ScreenHeader
import com.example.waterreminder.ui.components.SectionHeader
import com.example.waterreminder.ui.components.SelectableChip
import com.example.waterreminder.ui.components.SettingSwitchRow
import com.example.waterreminder.ui.components.SoftDivider
import com.example.waterreminder.ui.components.formatLiters
import com.example.waterreminder.ui.components.formatMl
import com.example.waterreminder.ui.components.nameInitials
import com.example.waterreminder.ui.settings.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsMainScreen(
    viewModel: SettingsViewModel,
    onNavigateToProfile: () -> Unit,
    onNavigateToGoalSetup: () -> Unit,
    onResetCompleted: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val preferences by viewModel.userPreferences.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }

    AdaptiveNavigationLayout(
        currentRoute = "settings",
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
                    title = "Settings",
                    subtitle = "Preferences, targets, and notifications.",
                    trailing = {
                        IconBadge(icon = Icons.Default.Settings, size = 44.dp)
                    }
                )
            }

            // 2. Profile Preview Card
            item {
                ProfileSummaryCard(
                    name = preferences.userName,
                    dailyGoalMl = preferences.dailyGoalMl,
                    onClick = onNavigateToProfile
                )
            }

            // 3. Targets & Quick Add Section
            item {
                SectionHeader(
                    title = "Hydration Targets",
                    icon = Icons.Default.WaterDrop
                )
            }

            item {
                GlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        SettingsNavRow(
                            icon = Icons.Default.WaterDrop,
                            title = "Daily Goal",
                            subtitle = "${formatMl(preferences.dailyGoalMl)} (${formatLiters(preferences.dailyGoalMl)}) per day",
                            onClick = onNavigateToGoalSetup
                        )

                        SoftDivider()

                        // Quick Add Amount
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FieldLabel("Default Quick Add")
                                Text(
                                    text = formatMl(preferences.quickAddMl),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(150, 200, 250, 300, 500).forEach { amount ->
                                    SelectableChip(
                                        label = "+$amount ml",
                                        selected = preferences.quickAddMl == amount,
                                        onClick = { viewModel.updateQuickAddAmount(amount) },
                                        compact = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Notifications & Sound Section
            item {
                SectionHeader(
                    title = "Alerts & Audio",
                    icon = Icons.Default.Notifications
                )
            }

            item {
                GlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingsNavRow(
                            icon = Icons.Default.Notifications,
                            title = "Manage Schedules",
                            subtitle = "Intervals, quiet hours, and reminder days",
                            onClick = { onNavigate("reminders") }
                        )

                        SoftDivider()

                        SettingSwitchRow(
                            title = "Sound Alerts",
                            subtitle = "Play audio cue when a reminder fires",
                            checked = preferences.soundEnabled,
                            onCheckedChange = viewModel::toggleSound,
                            icon = Icons.AutoMirrored.Filled.VolumeUp
                        )

                        SoftDivider()

                        SettingSwitchRow(
                            title = "Vibration",
                            subtitle = "Vibrate on reminder alert",
                            checked = preferences.vibrateEnabled,
                            onCheckedChange = viewModel::toggleVibrate,
                            icon = Icons.Default.Vibration
                        )
                    }
                }
            }

            // 5. Appearance Section
            item {
                SectionHeader(
                    title = "Appearance",
                    icon = Icons.Default.Palette
                )
            }

            item {
                ThemeSelectorCard(
                    currentTheme = preferences.darkThemeMode,
                    onThemeSelected = viewModel::updateThemeMode
                )
            }

            // 6. Data & Maintenance Section
            item {
                SectionHeader(
                    title = "Data & Maintenance",
                    icon = Icons.Default.Refresh
                )
            }

            item {
                GlowCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingsNavRow(
                            icon = Icons.Default.DeleteOutline,
                            title = "Clear Water History",
                            subtitle = "Remove all logged drinks while keeping setup",
                            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { showClearHistoryConfirm = true }
                        )

                        SoftDivider()

                        SettingsNavRow(
                            icon = Icons.Default.Delete,
                            title = "Reset All App Data",
                            subtitle = "Reset preferences, schedules, and return to onboarding",
                            iconTint = MaterialTheme.colorScheme.error,
                            onClick = { showResetConfirm = true }
                        )
                    }
                }
            }

            // 7. About & Version
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Water Reminder • Version 1.0.0",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text("Clear Water History?") },
            text = { Text("This will delete all logged intake records. Your reminder schedules and goals will stay intact.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory {
                            showClearHistoryConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Clear Logs")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset App Data?") },
            text = { Text("This will reset all preferences, schedules, and water history to defaults. You will return to the welcome screen.") },
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
private fun ProfileSummaryCard(
    name: String,
    dailyGoalMl: Int,
    onClick: () -> Unit
) {
    GlowCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nameInitials(name),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = name.ifBlank { "Hydration Buddy" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Goal: ${formatMl(dailyGoalMl)} / day",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ThemeSelectorCard(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themeOptions = listOf(
        Triple("SYSTEM", "System", Icons.Default.SettingsBrightness),
        Triple("LIGHT", "Light", Icons.Default.LightMode),
        Triple("DARK", "Dark", Icons.Default.DarkMode)
    )

    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = Icons.Default.Palette, size = 40.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Color Scheme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Choose light, dark, or follow device settings",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themeOptions.forEach { (mode, label, icon) ->
                    val selected = currentTheme == mode
                    val background by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        animationSpec = tween(220),
                        label = "themeOptionBg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                        animationSpec = tween(220),
                        label = "themeOptionContent"
                    )

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onThemeSelected(mode) },
                        color = background
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            IconBadge(icon = icon, size = 40.dp, tint = iconTint)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
