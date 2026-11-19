package com.example.waterreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.ui.components.FormScreenScaffold
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.GradientButton
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.OnboardingStepIndicator
import com.example.waterreminder.ui.components.StaggeredEntrance
import com.example.waterreminder.ui.components.WaterDropLogoIcon

private data class WelcomeFeature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val welcomeFeatures = listOf(
    WelcomeFeature(
        Icons.Default.NotificationsActive,
        "Smart reminders",
        "Gentle nudges that fit around your day."
    ),
    WelcomeFeature(
        Icons.Default.WaterDrop,
        "One-tap logging",
        "Add your usual glass in a single tap."
    ),
    WelcomeFeature(
        Icons.Default.Insights,
        "Progress insights",
        "See daily and weekly trends at a glance."
    )
)

@Composable
fun WelcomeScreen(
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    FormScreenScaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkipClick) {
                    Text(
                        text = "Skip",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        bottomBar = {
            OnboardingStepIndicator(currentStep = 0)
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = "Get Started",
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                onClick = onNextClick
            )
        }
    ) {
        StaggeredEntrance(index = 0) {
            WaterDropLogoIcon(size = 150.dp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        StaggeredEntrance(index = 1) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Stay Hydrated Every Day",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Get timely reminders, track your intake, and build a healthy habit that sticks.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            welcomeFeatures.forEachIndexed { index, feature ->
                StaggeredEntrance(index = index + 2, modifier = Modifier.fillMaxWidth()) {
                    FeatureRow(feature)
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(feature: WelcomeFeature) {
    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = feature.icon, size = 44.dp, iconSize = 22.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = feature.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
