package com.example.waterreminder.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.ui.theme.SuccessGreen
import java.util.Locale

data class BarItemData(
    val dayLabel: String,
    val consumedMl: Int,
    val goalMl: Int,
    val isSelected: Boolean = false
)

@Composable
fun WeeklyBarChart(
    items: List<BarItemData>,
    modifier: Modifier = Modifier,
    maxGoalMl: Int = 3500,
    onBarClick: ((BarItemData) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daily Trend",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Last 7 Days",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val maxScale = (items.maxOfOrNull { maxOf(it.consumedMl, it.goalMl) } ?: maxGoalMl)
            .coerceAtLeast(1000)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEach { item ->
                BarColumn(
                    item = item,
                    maxScale = maxScale,
                    onClick = { onBarClick?.invoke(item) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun BarColumn(
    item: BarItemData,
    maxScale: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetFillRatio = (item.consumedMl.toFloat() / maxScale).coerceIn(0.06f, 1f)
    val fillRatio by animateFloatAsState(
        targetValue = targetFillRatio,
        animationSpec = tween(700),
        label = "barHeight"
    )
    val isGoalReached = item.consumedMl >= item.goalMl && item.goalMl > 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() },
        verticalArrangement = Arrangement.Bottom
    ) {
        val valueStr = if (item.consumedMl > 0) String.format(Locale.US, "%,d", item.consumedMl) else "0"
        Text(
            text = valueStr,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isGoalReached) SuccessGreen else if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(22.dp)
                .fillMaxHeight(fillRatio)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    brush = when {
                        isGoalReached -> Brush.verticalGradient(listOf(SuccessGreen, Color(0xFF0F9B6D)))
                        item.isSelected -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                        item.consumedMl > 0 -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.secondary, Color(0xFF0A4F96)))
                        else -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), Color.Transparent))
                    }
                )
                .border(
                    width = if (item.isSelected) 1.dp else 0.dp,
                    color = if (item.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.dayLabel,
            fontSize = 11.sp,
            fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
