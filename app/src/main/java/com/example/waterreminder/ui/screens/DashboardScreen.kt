package com.example.waterreminder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.domain.model.LogSource
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.ui.components.AdaptiveNavigationLayout
import com.example.waterreminder.ui.components.CustomAmountDialog
import com.example.waterreminder.ui.components.EditLogDialog
import com.example.waterreminder.ui.components.EmptyStateCard
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.HeaderIconButton
import com.example.waterreminder.ui.components.HydrationProgressRing
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.MetricTile
import com.example.waterreminder.ui.components.SectionHeader
import com.example.waterreminder.ui.components.SoftDivider
import com.example.waterreminder.ui.components.formatLiters
import com.example.waterreminder.ui.components.formatMl
import com.example.waterreminder.ui.components.nameInitials
import com.example.waterreminder.ui.home.HomeViewModel
import com.example.waterreminder.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCustomAmountDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<WaterLog?>(null) }

    // Listen for recently deleted logs and show an undo snackbar
    LaunchedEffect(state.recentlyDeletedLog) {
        val deleted = state.recentlyDeletedLog ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "Removed ${deleted.amountMl} ml log",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDeleteWaterLog()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AdaptiveNavigationLayout(
            currentRoute = "dashboard",
            onNavigate = onNavigate
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Dashboard Header
                item {
                    DashboardHeader(
                        name = state.userPreferences.userName,
                        greeting = state.greeting,
                        dateFormatted = state.todayDateFormatted,
                        onProfileClick = { onNavigate("profile") },
                        onNotificationClick = { onNavigate("reminders") }
                    )
                }

                // 2. Motivational Banner
                if (state.motivationalMessage.isNotBlank()) {
                    item {
                        MotivationalBanner(
                            message = state.motivationalMessage,
                            isGoalReached = state.hydrationSummary.isGoalReached
                        )
                    }
                }

                // 3. Main Progress Gauge Card
                item {
                    MainProgressRingCard(
                        consumedMl = state.hydrationSummary.consumedMl,
                        goalMl = state.hydrationSummary.goalMl,
                        progress = state.hydrationSummary.progressPercentage / 100f
                    )
                }

                // 4. Metric Tiles (Daily Goal, Remaining, Progress)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricTile(
                            icon = Icons.Default.TrackChanges,
                            label = "Daily Goal",
                            value = formatLiters(state.hydrationSummary.goalMl),
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            icon = Icons.Default.WaterDrop,
                            label = "Remaining",
                            value = formatLiters(state.hydrationSummary.remainingMl),
                            accent = if (state.hydrationSummary.remainingMl == 0) SuccessGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            label = "Progress",
                            value = "${state.hydrationSummary.progressPercentage.toInt()}%",
                            accent = if (state.hydrationSummary.isGoalReached) SuccessGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 5. Next Reminder Banner
                item {
                    NextReminderCard(
                        nextReminderTime = state.nextReminderFormatted,
                        remindersEnabled = state.userPreferences.remindersEnabled,
                        onSnoozeClick = viewModel::snoozeNextReminder,
                        onManageClick = { onNavigate("reminders") }
                    )
                }

                // 6. Quick Add Section
                item {
                    QuickAddSection(
                        quickAddAmountMl = state.userPreferences.quickAddMl,
                        onQuickAdd = { amount -> viewModel.addWater(amount, source = LogSource.QUICK_ADD) },
                        onCustomClick = { showCustomAmountDialog = true }
                    )
                }

                // 7. Today's Intake Section & History Link
                item {
                    SectionHeader(
                        title = "Today's Intake",
                        icon = Icons.Default.LocalDrink,
                        actionLabel = "View History",
                        onAction = { onNavigate("history") }
                    )
                }

                item {
                    TodayIntakeCard(
                        logs = state.hydrationSummary.logs,
                        onLogClick = { editingLog = it },
                        onQuickAddDefault = {
                            viewModel.addWater(state.userPreferences.quickAddMl, source = LogSource.QUICK_ADD)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp)
        )
    }

    if (showCustomAmountDialog) {
        CustomAmountDialog(
            initialAmountMl = state.userPreferences.quickAddMl,
            onDismissRequest = { showCustomAmountDialog = false },
            onConfirm = { amount ->
                viewModel.addWater(amount, source = LogSource.MANUAL)
                showCustomAmountDialog = false
            }
        )
    }

    editingLog?.let { log ->
        EditLogDialog(
            log = log,
            onDismissRequest = { editingLog = null },
            onSave = { amount, note ->
                viewModel.updateWaterLog(log, amount, note)
                editingLog = null
            },
            onDelete = {
                viewModel.deleteWaterLog(log)
                editingLog = null
            }
        )
    }
}

@Composable
private fun DashboardHeader(
    name: String,
    greeting: String,
    dateFormatted: String,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onProfileClick)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
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
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (dateFormatted.isNotBlank()) "$greeting • $dateFormatted" else greeting,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = name.ifBlank { "Hydration Buddy" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HeaderIconButton(
            icon = Icons.Default.Notifications,
            contentDescription = "Reminders",
            onClick = onNotificationClick
        )
    }
}

@Composable
private fun MotivationalBanner(
    message: String,
    isGoalReached: Boolean
) {
    val accent = if (isGoalReached) SuccessGreen else MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
        color = accent.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TipsAndUpdates,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun MainProgressRingCard(
    consumedMl: Int,
    goalMl: Int,
    progress: Float
) {
    val compactHeight = LocalConfiguration.current.screenHeightDp < 500

    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HydrationProgressRing(
                progress = progress,
                consumedMl = consumedMl,
                goalMl = goalMl,
                size = if (compactHeight) 180.dp else 236.dp,
                strokeWidth = if (compactHeight) 14.dp else 18.dp
            )
        }
    }
}

@Composable
private fun NextReminderCard(
    nextReminderTime: String,
    remindersEnabled: Boolean,
    onSnoozeClick: () -> Unit,
    onManageClick: () -> Unit
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
                    .clickable(onClick = onManageClick)
            ) {
                IconBadge(
                    icon = Icons.Default.Alarm,
                    size = 42.dp,
                    tint = if (remindersEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (remindersEnabled) "Next Reminder" else "Reminders Paused",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = nextReminderTime,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (remindersEnabled) {
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onSnoozeClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Snooze", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAddSection(
    quickAddAmountMl: Int,
    onQuickAdd: (Int) -> Unit,
    onCustomClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = "Quick Add",
            icon = Icons.Default.Add
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(150, 250, 500, 750, quickAddAmountMl).distinct().sorted()
            presets.forEach { amount ->
                val isDefault = amount == quickAddAmountMl
                QuickAddTile(
                    amountMl = amount,
                    isDefault = isDefault,
                    onClick = { onQuickAdd(amount) }
                )
            }
            QuickAddCustomTile(onClick = onCustomClick)
        }
    }
}

@Composable
private fun QuickAddTile(
    amountMl: Int,
    isDefault: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (isDefault) colors.primary else colors.outline.copy(alpha = 0.4f),
                RoundedCornerShape(18.dp)
            )
            .clickable(role = Role.Button, onClick = onClick),
        color = if (isDefault) colors.primary.copy(alpha = 0.18f) else colors.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+$amountMl ml",
                fontSize = 13.sp,
                fontWeight = if (isDefault) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isDefault) colors.primary else colors.onSurface
            )
        }
    }
}

@Composable
private fun QuickAddCustomTile(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, colors.primary, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick),
        color = colors.primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Custom",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimary
            )
        }
    }
}

@Composable
private fun TodayIntakeCard(
    logs: List<WaterLog>,
    onLogClick: (WaterLog) -> Unit,
    onQuickAddDefault: () -> Unit
) {
    if (logs.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Default.LocalDrink,
            title = "No water logged today",
            message = "Start your day with a fresh glass to build your streak."
        ) {
            FilledTonalButton(
                onClick = onQuickAddDefault,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log First Glass", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        GlowCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                logs.take(5).forEachIndexed { index, log ->
                    if (index > 0) SoftDivider()
                    TodayLogItemRow(log = log, onClick = { onLogClick(log) })
                }
                if (logs.size > 5) {
                    Text(
                        text = "+ ${logs.size - 5} more entries in history",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayLogItemRow(
    log: WaterLog,
    onClick: () -> Unit
) {
    val formattedTime = remember(log.timestamp) {
        Instant.ofEpochMilli(log.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = Icons.Default.WaterDrop,
                size = 34.dp,
                iconSize = 16.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "+${formatMl(log.amountMl)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (log.note.isNotBlank()) log.note else sourceLabel(log.source),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formattedTime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit log",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun sourceLabel(source: LogSource): String = when (source) {
    LogSource.MANUAL -> "Manual entry"
    LogSource.QUICK_ADD -> "Quick add"
    LogSource.NOTIFICATION -> "From reminder"
    LogSource.WIDGET -> "Widget"
}
