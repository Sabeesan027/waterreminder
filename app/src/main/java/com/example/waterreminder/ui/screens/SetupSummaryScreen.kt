package com.example.waterreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.domain.model.ReminderSchedule
import com.example.waterreminder.domain.model.UserPreferences
import com.example.waterreminder.ui.components.FormScreenScaffold
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.GradientButton
import com.example.waterreminder.ui.components.HeroIcon
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.OnboardingStepIndicator
import com.example.waterreminder.ui.components.SecondaryTextAction
import com.example.waterreminder.ui.components.SectionHeader
import com.example.waterreminder.ui.components.SoftDivider
import com.example.waterreminder.ui.components.StaggeredEntrance
import com.example.waterreminder.ui.components.StatusPill
import com.example.waterreminder.ui.components.formatDays
import com.example.waterreminder.ui.components.formatInterval
import com.example.waterreminder.ui.components.formatLiters
import com.example.waterreminder.ui.components.formatMl
import com.example.waterreminder.ui.theme.SuccessGreen

@Composable
fun SetupSummaryScreen(
    userPreferences: UserPreferences,
    schedule: ReminderSchedule? = null,
    onGoToDashboard: () -> Unit,
    onEditSetup: () -> Unit
) {
    FormScreenScaffold(
        bottomBar = {
            OnboardingStepIndicator(currentStep = 4)
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Go to Dashboard",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onGoToDashboard
            )
            Spacer(modifier = Modifier.height(4.dp))
            SecondaryTextAction(text = "Review & edit setup", onClick = onEditSetup)
        }
    ) {
        StaggeredEntrance(index = 0) {
            HeroIcon(icon = Icons.Default.CheckCircle, size = 112.dp, tint = SuccessGreen)
        }

        Spacer(modifier = Modifier.height(16.dp))

        StaggeredEntrance(index = 1) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                StatusPill(text = "Ready to go", color = SuccessGreen, icon = Icons.Default.CheckCircle)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "You're All Set!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Here's a quick summary of your personalized hydration plan.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionHeader(
                        title = "Your Plan",
                        actionLabel = "Edit",
                        onAction = onEditSetup
                    )

                    SummaryItem(
                        icon = Icons.Default.TrackChanges,
                        label = "Daily Goal",
                        value = "${formatMl(userPreferences.dailyGoalMl)} (${formatLiters(userPreferences.dailyGoalMl)})"
                    )

                    SoftDivider()

                    SummaryItem(
                        icon = Icons.Default.NotificationsActive,
                        label = "Reminders",
                        value = if (userPreferences.remindersEnabled) "Enabled" else "Paused",
                        valueColor = if (userPreferences.remindersEnabled) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (schedule != null) {
                        SoftDivider()
                        SummaryItem(
                            icon = Icons.Default.DateRange,
                            label = "Active Days",
                            value = formatDays(schedule.daysOfWeek)
                        )
                        SoftDivider()
                        SummaryItem(
                            icon = Icons.Default.Schedule,
                            label = "Frequency",
                            value = "Every " + formatInterval(schedule.intervalMinutes)
                        )
                    }

                    SoftDivider()

                    SummaryItem(
                        icon = Icons.Default.Work,
                        label = "Activity Mode",
                        value = userPreferences.activityMode.displayName
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, size = 36.dp, iconSize = 18.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
