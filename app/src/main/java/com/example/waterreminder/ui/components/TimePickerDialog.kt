package com.example.waterreminder.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CustomTimePickerDialog(
    initialTime: String,
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val parsedTime = try {
        LocalTime.parse(initialTime, DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        LocalTime.of(8, 30)
    }

    var hour by remember { mutableIntStateOf(parsedTime.hour) }
    var minute by remember { mutableIntStateOf(parsedTime.minute) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                val formattedTime = String.format(Locale.US, "%02d:%02d", hour, minute)
                Text(
                    text = formattedTime,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(20.dp))

                HourAdjuster(
                    hour = hour,
                    onHourChange = { hour = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                MinuteAdjuster(
                    minute = minute,
                    onMinuteChange = { minute = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    GradientButton(
                        text = "OK",
                        onClick = {
                            val timeStr = String.format(Locale.US, "%02d:%02d", hour, minute)
                            onTimeSelected(timeStr)
                        },
                        modifier = Modifier.width(100.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HourAdjuster(
    hour: Int,
    onHourChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Hour (0-23)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onHourChange((hour - 1 + 24) % 24) }) { Text("-", color = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = String.format(Locale.US, "%02d", hour),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(onClick = { onHourChange((hour + 1) % 24) }) { Text("+", color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
private fun MinuteAdjuster(
    minute: Int,
    onMinuteChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Minute (0-59)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { onMinuteChange((minute - 5 + 60) % 60) }) { Text("-5", color = MaterialTheme.colorScheme.primary) }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = String.format(Locale.US, "%02d", minute),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(onClick = { onMinuteChange((minute + 5) % 60) }) { Text("+5", color = MaterialTheme.colorScheme.primary) }
        }
    }
}
