package com.example.waterreminder.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.ui.components.HydrationBackground
import com.example.waterreminder.ui.components.HydrationLoader
import com.example.waterreminder.ui.components.StaggeredEntrance
import com.example.waterreminder.ui.components.WaterDropLogoIcon
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isOnboardingCompleted: Boolean,
    onNavigateNext: (String) -> Unit
) {
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 180f))
    }

    LaunchedEffect(isOnboardingCompleted) {
        delay(1800)
        onNavigateNext(if (isOnboardingCompleted) "dashboard" else "welcome")
    }

    HydrationBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = logoAlpha.value
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                }
            ) {
                WaterDropLogoIcon(size = 148.dp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            StaggeredEntrance(index = 3) {
                Text(
                    text = "Water Reminder",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            StaggeredEntrance(index = 5) {
                Text(
                    text = "Drink water  •  Stay hydrated  •  Stay healthy",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(56.dp))

            StaggeredEntrance(index = 7) {
                HydrationLoader()
            }
        }
    }
}
