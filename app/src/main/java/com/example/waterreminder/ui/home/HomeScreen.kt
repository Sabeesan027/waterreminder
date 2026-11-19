package com.example.waterreminder.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.domain.model.WaterLog
import com.example.waterreminder.ui.components.CustomAmountDialog
import com.example.waterreminder.ui.components.EditLogDialog
import com.example.waterreminder.ui.components.HydrationProgressRing
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCustomAmountDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<WaterLog?>(null) }

    HomeLayout(
        state = state,
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        scope = scope,
        onShowCustomDialog = { showCustomAmountDialog = true },
        onEditLog = { editingLog = it }
    )

    if (showCustomAmountDialog) {
        CustomAmountDialog(
            initialAmountMl = state.userPreferences.quickAddMl,
            onDismissRequest = { showCustomAmountDialog = false },
            onConfirm = { amount ->
                viewModel.addWater(amount)
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
private fun HomeLayout(
    state: HomeUiState,
    viewModel: HomeViewModel,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    onShowCustomDialog: () -> Unit,
    onEditLog: (WaterLog) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                    HeaderSection(
                        greeting = state.greeting,
                        date = state.todayDateFormatted,
                        activityModeName = state.userPreferences.activityMode.displayName
                    )
                }

                item {
                    ProgressRingContainer(
                        progress = state.hydrationSummary.progressPercentage / 100f,
                        consumedMl = state.hydrationSummary.consumedMl,
                        goalMl = state.hydrationSummary.goalMl
                    )
                }

                item {
                    WorkProgressCard(
                        workConsumedMl = state.hydrationSummary.workConsumedMl,
                        workGoalMl = state.hydrationSummary.workGoalMl,
                        workProgressRatio = state.hydrationSummary.workProgressPercentage / 100f
                    )
                }

                item {
                    MotivationalCard(message = state.motivationalMessage)
                }

                item {
                    QuickAddSection(
                        quickAddAmountMl = state.userPreferences.quickAddMl,
                        onQuickAdd = { amount -> viewModel.addWater(amount) },
                        onOpenCustomDialog = onShowCustomDialog
                    )
                }

                item {
                    Text(
                        text = "Today's Log History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (state.hydrationSummary.logs.isEmpty()) {
                    item {
                        EmptyHistoryCard()
                    }
                } else {
                    items(
                        items = state.hydrationSummary.logs,
                        key = { log -> log.id }
                    ) { log ->
                        WaterLogItemCard(
                            log = log,
                            onEdit = { onEditLog(log) },
                            onDelete = {
                                viewModel.deleteWaterLog(log)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Deleted ${log.amountMl}ml log",
                                        actionLabel = "Undo"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDeleteWaterLog()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun HeaderSection(
    greeting: String,
    date: String,
    activityModeName: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            AssistChip(
                onClick = { },
                label = { Text(activityModeName, fontSize = 11.sp) }
            )
        }
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProgressRingContainer(
    progress: Float,
    consumedMl: Int,
    goalMl: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HydrationProgressRing(
            progress = progress,
            consumedMl = consumedMl,
            goalMl = goalMl
        )
    }
}

@Composable
private fun WorkProgressCard(
    workConsumedMl: Int,
    workGoalMl: Int,
    workProgressRatio: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Work-Hours Hydration Goal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$workConsumedMl / $workGoalMl ml",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { workProgressRatio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun MotivationalCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAddSection(
    quickAddAmountMl: Int,
    onQuickAdd: (Int) -> Unit,
    onOpenCustomDialog: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Add Water",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val presets = listOf(100, 150, 200, quickAddAmountMl, 500).distinct()
            presets.forEach { amount ->
                FilledTonalButton(onClick = { onQuickAdd(amount) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("${amount}ml")
                }
            }
            OutlinedButton(onClick = onOpenCustomDialog) {
                Text("Custom")
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "💧", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No water recorded yet today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap a quick add button above to log your first drink!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WaterLogItemCard(
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    if (log.note.isNotBlank()) {
                        Text(
                            text = log.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
