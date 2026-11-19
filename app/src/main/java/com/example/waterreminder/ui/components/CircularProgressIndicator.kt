package com.example.waterreminder.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun HydrationProgressRing(
    progress: Float,
    consumedMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    strokeWidth: Dp = 18.dp
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val outline = MaterialTheme.colorScheme.outline
    val onBackground = MaterialTheme.colorScheme.onBackground
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height

            // Outer Radial Glow Effect
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                    radius = canvasWidth * 0.55f
                )
            )

            // Background Track
            drawArc(
                color = outline.copy(alpha = 0.35f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Glowing Animated Arc
            val sweepAngle = animatedProgress.value * 270f
            if (sweepAngle > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(primary, primaryContainer, primary, primaryContainer)
                    ),
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            val percentageText = (animatedProgress.value * 100).toInt()
            Text(
                text = "$percentageText%",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            val formattedConsumed = String.format(Locale.US, "%,d", consumedMl)
            val formattedGoal = String.format(Locale.US, "%,d", goalMl)
            Text(
                text = "$formattedConsumed / $formattedGoal ml",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )

            Spacer(modifier = Modifier.height(2.dp))

            val remaining = (goalMl - consumedMl).coerceAtLeast(0)
            val formattedRemaining = String.format(Locale.US, "%,d", remaining)
            Text(
                text = if (remaining > 0) "$formattedRemaining ml left" else "Goal achieved",
                fontSize = 12.sp,
                color = onSurfaceVariant
            )
        }
    }
}
