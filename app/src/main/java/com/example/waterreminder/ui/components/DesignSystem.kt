package com.example.waterreminder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.R
import com.example.waterreminder.ui.theme.BrightBlue
import com.example.waterreminder.ui.theme.CardGradientEnd
import com.example.waterreminder.ui.theme.CardGradientStart
import com.example.waterreminder.ui.theme.DeepNavyBackground
import com.example.waterreminder.ui.theme.DeepNavySurface
import com.example.waterreminder.ui.theme.NavyCardBackground
import com.example.waterreminder.ui.theme.NavyCardBorder
import com.example.waterreminder.ui.theme.NeonCyan
import com.example.waterreminder.ui.theme.TextPrimary
import com.example.waterreminder.ui.theme.TextSecondary
import com.example.waterreminder.ui.theme.WaterGradientEnd
import com.example.waterreminder.ui.theme.WaterGradientStart
import kotlin.math.sin

@Composable
fun HydrationBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val targetBackground = MaterialTheme.colorScheme.background
    val targetSurface = MaterialTheme.colorScheme.surface
    val targetPrimary = MaterialTheme.colorScheme.primary
    val animatedBackground by animateColorAsState(targetBackground, tween(450), label = "background")
    val animatedSurface by animateColorAsState(targetSurface, tween(450), label = "surface")
    val animatedPrimary by animateColorAsState(targetPrimary, tween(450), label = "primary")

    val infiniteTransition = rememberInfiniteTransition(label = "bg_wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedBackground,
                        animatedSurface,
                        animatedBackground
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-left cyan radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(animatedPrimary.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(width * 0.15f, height * 0.2f),
                    radius = width * 0.65f
                )
            )

            // Bottom-right blue radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(animatedPrimary.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(width * 0.85f, height * 0.75f),
                    radius = width * 0.75f
                )
            )

            // Subtle animated water wave paths in upper background
            val path = Path()
            val waveHeight = 25f
            val baseLine = height * 0.12f
            path.moveTo(0f, baseLine)
            var x = 0f
            while (x <= width) {
                val y = baseLine + sin(x / 120f + waveOffset) * waveHeight
                path.lineTo(x, y)
                x += 10f
            }
            path.lineTo(width, 0f)
            path.lineTo(0f, 0f)
            path.close()

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(animatedPrimary.copy(alpha = 0.05f), Color.Transparent)
                )
            )
        }
        content()
    }
}

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    cornerRadius: Dp = 18.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val resolvedBorder = borderColor ?: MaterialTheme.colorScheme.outline
    val resolvedBackground = backgroundColor ?: MaterialTheme.colorScheme.surface
    val resolvedPrimary = MaterialTheme.colorScheme.primary
    val animatedBackground by animateColorAsState(resolvedBackground, tween(400), label = "cardBackground")
    val animatedBorder by animateColorAsState(resolvedBorder, tween(400), label = "cardBorder")

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Surface(
        modifier = modifier
            .then(clickableModifier)
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedBorder.copy(alpha = 0.8f),
                        resolvedPrimary.copy(alpha = 0.3f),
                        animatedBorder.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = animatedBackground,
        tonalElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
                            animatedBackground.copy(alpha = 0.72f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun HydrationLoader(
    modifier: Modifier = Modifier,
    message: String = "Preparing your hydration plan"
) {
    val transition = rememberInfiniteTransition(label = "hydrationLoader")
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loaderOrbit"
    )
    val reverseOrbit by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1750, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loaderReverseOrbit"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(760, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loaderPulse"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val track = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(94.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val outerStroke = 5.dp.toPx()
                val innerStroke = 3.dp.toPx()
                drawCircle(
                    color = track.copy(alpha = 0.35f),
                    style = Stroke(width = outerStroke)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(primary, secondary, primary)),
                    startAngle = orbit,
                    sweepAngle = 108f,
                    useCenter = false,
                    style = Stroke(width = outerStroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = secondary.copy(alpha = 0.75f),
                    startAngle = reverseOrbit,
                    sweepAngle = 62f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.13f, size.height * 0.13f),
                    size = Size(size.width * 0.74f, size.height * 0.74f),
                    style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                primary.copy(alpha = 0.22f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val dotScale by transition.animateFloat(
                    initialValue = 0.45f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 520,
                            delayMillis = index * 130,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "loaderDot$index"
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer {
                            scaleX = dotScale
                            scaleY = dotScale
                            alpha = 0.35f + dotScale * 0.65f
                        }
                        .clip(CircleShape)
                        .background(primary)
                )
            }
        }
    }
}

@Composable
fun HydrationReminderBot(
    visible: Boolean,
    quickAddAmountMl: Int,
    onDrink: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "reminderBot")
    val bob by transition.animateFloat(
        initialValue = -7f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "botBob"
    )
    val tilt by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "botTilt"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "botBreathe"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "botGlow"
    )
    val glowScale by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "botGlowScale"
    )
    val sparklePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "botSparkles"
    )
    val botAccent = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)) +
                scaleIn(
                    animationSpec = spring(dampingRatio = 0.52f, stiffness = 280f),
                    initialScale = 0.68f
                ) +
                slideInVertically(
                    animationSpec = spring(dampingRatio = 0.68f, stiffness = 320f)
                ) { it / 2 },
            exit = fadeOut(tween(180)) +
                scaleOut(tween(220), targetScale = 0.9f) +
                slideOutVertically(tween(240)) { it / 4 }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp,
                shadowElevation = 14.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(126.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val glowRadius = size.minDimension * 0.48f * glowScale
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            botAccent.copy(alpha = glowAlpha),
                                            botAccent.copy(alpha = glowAlpha * 0.25f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = glowRadius
                                    ),
                                    radius = glowRadius,
                                    center = center
                                )

                                repeat(3) { index ->
                                    val phase = sparklePhase + index * 2.1f
                                    val orbit = size.minDimension * (0.37f + index * 0.035f)
                                    val particleAlpha = (0.3f + 0.35f * (sin(phase) + 1f))
                                        .coerceIn(0.25f, 0.95f)
                                    drawCircle(
                                        color = botAccent.copy(alpha = particleAlpha),
                                        radius = (2.1f + index * 0.45f).dp.toPx(),
                                        center = Offset(
                                            x = center.x + sin(phase) * orbit,
                                            y = center.y + sin(phase * 1.35f + index) * orbit * 0.52f
                                        )
                                    )
                                }
                            }

                            Image(
                                painter = painterResource(R.drawable.hydration_bot_3d),
                                contentDescription = "Friendly animated 3D hydration reminder bot",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(112.dp)
                                    .graphicsLayer {
                                        translationY = bob
                                        rotationZ = tilt
                                        rotationY = tilt * 1.8f
                                        scaleX = breathe
                                        scaleY = breathe
                                    }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hydration check",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "A quick water break will help you stay focused and refreshed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Later")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onDrink) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text("Drink +${quickAddAmountMl} ml")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
        label = "gradientButtonScale"
    )
    val indication = LocalIndication.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
            .clip(RoundedCornerShape(27.dp))
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(colors = listOf(WaterGradientStart, WaterGradientEnd))
                } else {
                    Brush.horizontalGradient(colors = listOf(Color.Gray.copy(alpha = 0.4f), Color.DarkGray.copy(alpha = 0.4f)))
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = indication,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DeepNavyBackground,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavyBackground
            )
        }
    }
}

@Composable
fun WaterDropLogoIcon(
    size: Dp = 130.dp
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // Outer Radial Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.4f), BrightBlue.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(w / 2f, h / 2f),
                    radius = w * 0.65f
                )
            )

            // Outer Glowing Arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(NeonCyan, BrightBlue, Color(0xFF72F5FF), NeonCyan)
                ),
                startAngle = 40f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 4.5f.dp.toPx(), cap = StrokeCap.Round)
            )

            // Water Droplet Path
            val dropPath = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                cubicTo(w * 0.82f, h * 0.44f, w * 0.88f, h * 0.66f, w * 0.76f, h * 0.82f)
                cubicTo(w * 0.65f, h * 0.96f, w * 0.35f, h * 0.96f, w * 0.24f, h * 0.82f)
                cubicTo(w * 0.12f, h * 0.66f, w * 0.18f, h * 0.44f, w * 0.5f, h * 0.12f)
                close()
            }

            drawPath(
                path = dropPath,
                brush = Brush.verticalGradient(
                    colors = listOf(NeonCyan, BrightBlue, DeepNavySurface)
                )
            )

            // Bottle Outline inside Drop
            val bottlePath = Path().apply {
                moveTo(w * 0.43f, h * 0.35f)
                lineTo(w * 0.57f, h * 0.35f)
                lineTo(w * 0.57f, h * 0.4f)
                lineTo(w * 0.64f, h * 0.45f)
                lineTo(w * 0.64f, h * 0.74f)
                cubicTo(w * 0.64f, h * 0.77f, w * 0.6f, h * 0.79f, w * 0.5f, h * 0.79f)
                cubicTo(w * 0.4f, h * 0.79f, w * 0.36f, h * 0.77f, w * 0.36f, h * 0.74f)
                lineTo(w * 0.36f, h * 0.45f)
                lineTo(w * 0.43f, h * 0.4f)
                close()
            }

            drawPath(
                path = bottlePath,
                color = Color.White.copy(alpha = 0.9f),
                style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round)
            )

            // Bottle Cap
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.41f, h * 0.31f),
                size = Size(w * 0.18f, h * 0.045f),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            // Alarm Clock Icon inside lower right of drop
            val clockCenterX = w * 0.62f
            val clockCenterY = h * 0.64f
            val clockRadius = w * 0.12f

            drawCircle(
                color = Color.White,
                center = Offset(clockCenterX, clockCenterY),
                radius = clockRadius,
                style = Stroke(width = 2.dp.toPx())
            )

            // Clock Hands (Pointing at 10:10)
            drawLine(
                color = Color.White,
                start = Offset(clockCenterX, clockCenterY),
                end = Offset(clockCenterX - clockRadius * 0.4f, clockCenterY - clockRadius * 0.4f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(clockCenterX, clockCenterY),
                end = Offset(clockCenterX + clockRadius * 0.4f, clockCenterY - clockRadius * 0.5f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun AdaptiveNavigationLayout(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val useNavigationRail = configuration.screenWidthDp >= 600
    val isShortLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    HydrationBackground(modifier = Modifier.safeDrawingPadding()) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail) {
                HydrationNavigationRail(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                var contentVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { contentVisible = true }

                AnimatedVisibility(
                    visible = contentVisible,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 960.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    enter = fadeIn(tween(350)) +
                        slideInVertically(tween(420, easing = FastOutSlowInEasing)) { it / 12 }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), content = content)
                }

                if (!useNavigationRail) {
                    HydrationBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = onNavigate,
                        compact = isShortLandscape
                    )
                }
            }
        }
    }
}

@Composable
private fun HydrationNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val destinations = listOf(
        Triple("dashboard", "Home", Icons.Default.Home),
        Triple("history", "History", Icons.Default.DateRange),
        Triple("reminders", "Reminders", Icons.Default.Notifications),
        Triple("settings", "Settings", Icons.Default.Settings)
    )

    Surface(
        modifier = Modifier.width(96.dp).fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            destinations.forEach { (route, title, icon) ->
                val selected = currentRoute == route || (route == "dashboard" && currentRoute == "home")
                val itemScale by animateFloatAsState(if (selected) 1.06f else 1f, label = "railScale")
                val itemColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
                    label = "railColor"
                )
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .graphicsLayer { scaleX = itemScale; scaleY = itemScale }
                        .background(itemColor)
                        .clickable { onNavigate(route) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HydrationBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    compact: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 58.dp else 72.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.outline
                    )
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                title = "Home",
                icon = Icons.Default.Home,
                isSelected = currentRoute == "dashboard" || currentRoute == "home",
                onClick = { onNavigate("dashboard") }
            )
            BottomNavItem(
                title = "History",
                icon = Icons.Default.DateRange,
                isSelected = currentRoute == "history",
                onClick = { onNavigate("history") }
            )
            BottomNavItem(
                title = "Reminders",
                icon = Icons.Default.Notifications,
                isSelected = currentRoute == "reminders",
                onClick = { onNavigate("reminders") }
            )
            BottomNavItem(
                title = "Settings",
                icon = Icons.Default.Settings,
                isSelected = currentRoute == "settings",
                onClick = { onNavigate("settings") }
            )
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val itemScale by animateFloatAsState(if (isSelected) 1.06f else 1f, label = "bottomNavScale")
    val selectedBackground by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
        label = "bottomNavBackground"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .graphicsLayer { scaleX = itemScale; scaleY = itemScale }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(selectedBackground)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
