package com.example.waterreminder.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.ui.components.DetailTopBar
import com.example.waterreminder.ui.components.FieldLabel
import com.example.waterreminder.ui.components.FormScreenScaffold
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.GradientButton
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.OnboardingStepIndicator
import com.example.waterreminder.ui.components.SelectableChip
import com.example.waterreminder.ui.components.StaggeredEntrance
import com.example.waterreminder.ui.components.WaterDropLogoIcon
import com.example.waterreminder.ui.components.formatLiters
import com.example.waterreminder.ui.components.formatMl
import kotlin.math.roundToInt

private const val MIN_GOAL_ML = 500
private const val MAX_GOAL_ML = 6000
private const val GOAL_STEP_ML = 250
private const val GLASS_SIZE_ML = 250

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalSetupScreen(
    initialGoalMl: Int = 3500,
    onGoalSet: (Int) -> Unit,
    isOnboarding: Boolean = true,
    onBack: (() -> Unit)? = null
) {
    // Keyed on the initial value so the screen picks up the stored goal once it loads.
    var goalMl by remember(initialGoalMl) {
        mutableIntStateOf(initialGoalMl.coerceIn(MIN_GOAL_ML, MAX_GOAL_ML))
    }
    val presetGoals = listOf(1500, 2000, 2500, 3000, 3500, 4000)

    FormScreenScaffold(
        topBar = if (!isOnboarding && onBack != null) {
            { DetailTopBar(title = "Daily Goal", subtitle = "Adjust your hydration target", onBack = onBack) }
        } else null,
        bottomBar = {
            if (isOnboarding) {
                OnboardingStepIndicator(currentStep = 2)
                Spacer(modifier = Modifier.height(16.dp))
            }
            GradientButton(
                text = if (isOnboarding) "Continue" else "Save Goal",
                icon = Icons.Default.Check,
                onClick = { onGoalSet(goalMl) }
            )
        }
    ) {
        StaggeredEntrance(index = 0) {
            WaterDropLogoIcon(size = 104.dp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        StaggeredEntrance(index = 1) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Set Your Daily Goal",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose how much water you want to drink each day.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepperButton(
                            icon = Icons.Default.Remove,
                            contentDescription = "Decrease goal",
                            enabled = goalMl > MIN_GOAL_ML,
                            onClick = { goalMl = (goalMl - GOAL_STEP_ML).coerceAtLeast(MIN_GOAL_ML) }
                        )

                        AnimatedContent(
                            targetState = goalMl,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInVertically { it / 2 } + fadeIn()) togetherWith
                                        (slideOutVertically { -it / 2 } + fadeOut())
                                } else {
                                    (slideInVertically { -it / 2 } + fadeIn()) togetherWith
                                        (slideOutVertically { it / 2 } + fadeOut())
                                }
                            },
                            label = "goalValue"
                        ) { value ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatMl(value),
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatLiters(value) + " per day",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        StepperButton(
                            icon = Icons.Default.Add,
                            contentDescription = "Increase goal",
                            enabled = goalMl < MAX_GOAL_ML,
                            onClick = { goalMl = (goalMl + GOAL_STEP_ML).coerceAtMost(MAX_GOAL_ML) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = goalMl.toFloat(),
                        onValueChange = {
                            goalMl = ((it / GOAL_STEP_ML).roundToInt() * GOAL_STEP_ML)
                                .coerceIn(MIN_GOAL_ML, MAX_GOAL_ML)
                        },
                        valueRange = MIN_GOAL_ML.toFloat()..MAX_GOAL_ML.toFloat(),
                        steps = (MAX_GOAL_ML - MIN_GOAL_ML) / GOAL_STEP_ML - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                            inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatLiters(MIN_GOAL_ML), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatLiters(MAX_GOAL_ML), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "About ${goalMl / GLASS_SIZE_ML} glasses of $GLASS_SIZE_ML ml",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        StaggeredEntrance(index = 3, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FieldLabel("Popular goals")
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetGoals.forEach { preset ->
                        SelectableChip(
                            label = formatLiters(preset),
                            selected = goalMl == preset,
                            onClick = { goalMl = preset }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        StaggeredEntrance(index = 4, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.TipsAndUpdates, size = 38.dp, iconSize = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Most adults do well with 2–3.5 L a day. Drink more on hot days or when you're active.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
