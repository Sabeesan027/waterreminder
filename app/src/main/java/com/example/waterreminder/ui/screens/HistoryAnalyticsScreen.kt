package com.example.waterreminder.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.ui.components.AdaptiveNavigationLayout
import com.example.waterreminder.ui.components.EditLogDialog
import com.example.waterreminder.ui.components.EmptyStateCard
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.HydrationProgressRing
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.MetricTile
import com.example.waterreminder.ui.components.ScreenHeader
import com.example.waterreminder.ui.components.SectionHeader
import com.example.waterreminder.ui.components.SegmentedSelector
import com.example.waterreminder.ui.components.SelectableChip
import com.example.waterreminder.ui.components.SoftDivider
import com.example.waterreminder.ui.components.WeeklyBarChart
import com.example.waterreminder.ui.components.formatLiters
import com.example.waterreminder.ui.components.formatMl
import com.example.waterreminder.ui.history.HistoryViewModel
import com.example.waterreminder.ui.theme.SuccessGreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryAnalyticsScreen(
    viewModel: HistoryViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val compactHeight = LocalConfiguration.current.screenHeightDp < 500
    var selectedTab by remember { mutableStateOf("daily") } // "daily", "weekly"
    var editingLog by remember { mutableStateOf<WaterLog?>(null) }

    val isSelectedDateToday = state.selectedDate == LocalDate.now()

    AdaptiveNavigationLayout(
        currentRoute = "history",
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
                    title = "History",
                    subtitle = "Track your water intake and build consistency.",
                    trailing = {
                        if (!isSelectedDateToday) {
                            SelectableChip(
                                label = "Today",
                                selected = false,
                                icon = Icons.Default.Today,
                                compact = true,
                                onClick = viewModel::selectToday
                            )
                        } else {
                            IconBadge(
                                icon = Icons.Default.DateRange,
                                size = 44.dp
                            )
                        }
                    }
                )
            }

            // 2. Segmented Selector (Daily / Weekly)
            item {
                SegmentedSelector(
                    options = listOf(
                        "daily" to "Daily View",
                        "weekly" to "Weekly Trends"
                    ),
                    selectedKey = selectedTab,
                    onSelect = { selectedTab = it }
                )
            }

            if (selectedTab == "daily") {
                // 3. Date Navigator Card
                item {
                    DateNavigatorCard(
                        dateFormatted = state.selectedDateFormatted,
                        onPrevious = viewModel::selectPreviousDay,
                        onNext = viewModel::selectNextDay
                    )
                }

                // 4. Progress Ring Card
                item {
                    DailyProgressCard(
                        consumedMl = state.dailySummary.consumedMl,
                        goalMl = state.dailySummary.goalMl,
                        progress = state.dailySummary.progressPercentage / 100f,
                        compact = compactHeight
                    )
                }

                // 5. Daily Metrics
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricTile(
                            icon = Icons.Default.LocalDrink,
                            label = "Consumed",
                            value = formatMl(state.dailySummary.consumedMl),
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            icon = Icons.Default.TrackChanges,
                            label = "Daily Goal",
                            value = formatMl(state.dailySummary.goalMl),
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            icon = Icons.Default.Percent,
                            label = "Completion",
                            value = "${state.dailySummary.progressPercentage.toInt()}%",
                            accent = if (state.dailySummary.isGoalReached) SuccessGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 6. Intake Logs Section Header
                item {
                    SectionHeader(
                        title = "Intake Logs",
                        icon = Icons.Default.WaterDrop,
                        actionLabel = "${state.dailySummary.logs.size} entries"
                    )
                }

                // 7. Intake Log List or Empty State
                if (state.dailySummary.logs.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.WaterDrop,
                            title = "No logs recorded",
                            message = if (isSelectedDateToday) {
                                "You haven't logged any water today yet."
                            } else {
                                "No hydration data logged on this day."
                            }
                        )
                    }
                } else {
                    items(state.dailySummary.logs, key = { it.id }) { log ->
                        IntakeLogCard(log = log, onEdit = { editingLog = log })
                    }
                }
            } else {
                // Weekly Trends View
                item {
                    WeeklyStatsCard(
                        totalMl = state.monthlyTotalMl,
                        averageMl = state.monthlyAverageMl,
                        completionRate = state.monthlyGoalCompletionRate
                    )
                }

                item {
                    GlowCard(modifier = Modifier.fillMaxWidth()) {
                        WeeklyBarChart(
                            items = state.weeklyBarData,
                            maxGoalMl = state.dailySummary.goalMl
                        )
                    }
                }

                item {
                    GlowCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(icon = Icons.Default.Insights, size = 42.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Weekly Insight",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.monthlyGoalCompletionRate >= 70f) {
                                        "Great job! You hit your goal on most days this week."
                                    } else {
                                        "Keep sipping regularly through the day to boost your weekly average."
                                    },
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
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
private fun DateNavigatorCard(
    dateFormatted: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Day",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateFormatted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Day",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DailyProgressCard(
    consumedMl: Int,
    goalMl: Int,
    progress: Float,
    compact: Boolean
) {
    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HydrationProgressRing(
                progress = progress,
                consumedMl = consumedMl,
                goalMl = goalMl,
                size = if (compact) 160.dp else 200.dp,
                strokeWidth = if (compact) 14.dp else 16.dp
            )
        }
    }
}

@Composable
private fun WeeklyStatsCard(
    totalMl: Int,
    averageMl: Int,
    completionRate: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile(
            icon = Icons.Default.WaterDrop,
            label = "Weekly Total",
            value = formatLiters(totalMl),
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            icon = Icons.Default.Repeat,
            label = "Daily Average",
            value = formatLiters(averageMl),
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            icon = Icons.Default.TrackChanges,
            label = "Goal Hit Rate",
            value = "${completionRate.toInt()}%",
            accent = if (completionRate >= 70f) SuccessGreen else MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IntakeLogCard(
    log: WaterLog,
    onEdit: () -> Unit
) {
    val formattedTime = remember(log.timestamp) {
        Instant.ofEpochMilli(log.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onEdit),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Default.WaterDrop,
                    size = 36.dp,
                    iconSize = 18.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "+${formatMl(log.amountMl)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (log.note.isNotBlank()) log.note else formattedTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (log.note.isNotBlank()) {
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit log",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
