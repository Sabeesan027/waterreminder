package com.example.waterreminder.ui.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.ui.components.EditLogDialog
import com.example.waterreminder.ui.components.WeeklyBarChart
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state by viewModel.uiState.collectAsState()
    var editingLog by remember { mutableStateOf<WaterLog?>(null) }

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
                DateSelectorCard(
                    dateFormatted = state.selectedDateFormatted,
                    onPrevDate = viewModel::selectPreviousDay,
                    onNextDate = viewModel::selectNextDay,
                    isToday = state.selectedDate == LocalDate.now()
                )
            }

            item {
                DailySummaryCard(
                    consumedMl = state.dailySummary.consumedMl,
                    goalMl = state.dailySummary.goalMl,
                    workConsumedMl = state.dailySummary.workConsumedMl,
                    outsideWorkConsumedMl = state.dailySummary.outsideWorkConsumedMl
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    WeeklyBarChart(items = state.weeklyBarData)
                }
            }

            item {
                AnalyticsGrid(
                    weeklyTotalMl = state.monthlyTotalMl,
                    weeklyAvgMl = state.monthlyAverageMl,
                    completionRate = state.monthlyGoalCompletionRate,
                    logsCount = state.totalLogsCount
                )
            }

            item {
                Text(
                    text = "Logs for ${state.selectedDateFormatted}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (state.dailySummary.logs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "No water logs recorded for this date.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                state.dailySummary.logs.forEach { log ->
                    item(key = log.id) {
                        HistoryLogItemCard(
                            log = log,
                            onEdit = { editingLog = log },
                            onDelete = { viewModel.deleteWaterLog(log) }
                        )
                    }
                }
            }
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
private fun DateSelectorCard(
    dateFormatted: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    isToday: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevDate) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Day",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (!isToday) {
                    Text(
                        text = "Viewing Past/Future Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(onClick = onNextDate) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Day",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DailySummaryCard(
    consumedMl: Int,
    goalMl: Int,
    workConsumedMl: Int,
    outsideWorkConsumedMl: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Day Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(title = "Consumed", value = "$consumedMl ml", isHighlight = true)
                MetricColumn(title = "Daily Goal", value = "$goalMl ml")
                MetricColumn(title = "Remaining", value = "${(goalMl - consumedMl).coerceAtLeast(0)} ml")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(title = "Work Hours", value = "$workConsumedMl ml")
                MetricColumn(title = "Outside Work", value = "$outsideWorkConsumedMl ml")
            }
        }
    }
}

@Composable
private fun MetricColumn(title: String, value: String, isHighlight: Boolean = false) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AnalyticsGrid(
    weeklyTotalMl: Int,
    weeklyAvgMl: Int,
    completionRate: Float,
    logsCount: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Weekly Analytics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalyticsCard(
                title = "Total Intake",
                value = "$weeklyTotalMl ml",
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                title = "Daily Avg",
                value = "$weeklyAvgMl ml",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnalyticsCard(
                title = "Goal Rate",
                value = "${completionRate.toInt()}%",
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                title = "Logs Recorded",
                value = "$logsCount",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnalyticsCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun HistoryLogItemCard(
    log: WaterLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedTime = remember(log.timestamp) {
        Instant.ofEpochMilli(log.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💧", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "+${log.amountMl} ml",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$formattedTime • ${log.source.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit log",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete log",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
