package com.example.waterreminder.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.waterreminder.ui.components.FormScreenScaffold
import com.example.waterreminder.ui.components.GlowCard
import com.example.waterreminder.ui.components.GradientButton
import com.example.waterreminder.ui.components.HeroIcon
import com.example.waterreminder.ui.components.IconBadge
import com.example.waterreminder.ui.components.OnboardingStepIndicator
import com.example.waterreminder.ui.components.SecondaryTextAction
import com.example.waterreminder.ui.components.StaggeredEntrance
import com.example.waterreminder.ui.components.StatusPill
import com.example.waterreminder.ui.theme.SuccessGreen

@Composable
fun PermissionSetupScreen(
    onContinue: () -> Unit,
    onNotNow: () -> Unit
) {
    val context = LocalContext.current
    val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    var notificationsGranted by remember {
        mutableStateOf(
            !needsRuntimePermission || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            notificationsGranted = granted
            onContinue()
        }
    )

    FormScreenScaffold(
        bottomBar = {
            OnboardingStepIndicator(currentStep = 1)
            Spacer(modifier = Modifier.height(16.dp))
            GradientButton(
                text = if (notificationsGranted) "Continue" else "Allow & Continue",
                icon = if (notificationsGranted) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                onClick = {
                    if (needsRuntimePermission && !notificationsGranted) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onContinue()
                    }
                }
            )
            if (!notificationsGranted) {
                Spacer(modifier = Modifier.height(4.dp))
                SecondaryTextAction(text = "Not now", onClick = onNotNow)
            }
        }
    ) {
        StaggeredEntrance(index = 0) {
            HeroIcon(icon = Icons.Default.NotificationsActive, size = 124.dp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        StaggeredEntrance(index = 1) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Enable Notifications",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We'll only notify you when it's time for a sip. No spam, ever.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
                PermissionRow(
                    icon = Icons.Default.Notifications,
                    title = "Reminder notifications",
                    description = "Alerts when it's time to drink water.",
                    status = {
                        if (notificationsGranted) {
                            StatusPill(text = "Allowed", color = SuccessGreen, icon = Icons.Default.CheckCircle)
                        } else {
                            StatusPill(text = "Required", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
            StaggeredEntrance(index = 3, modifier = Modifier.fillMaxWidth()) {
                PermissionRow(
                    icon = Icons.Default.Schedule,
                    title = "On-time delivery",
                    description = "Reminders keep working in the background."
                )
            }
            StaggeredEntrance(index = 4, modifier = Modifier.fillMaxWidth()) {
                PermissionRow(
                    icon = Icons.Default.Lock,
                    title = "Private by design",
                    description = "Your hydration history stays on this device."
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    status: (@Composable () -> Unit)? = null
) {
    GlowCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = icon, size = 44.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (status != null) {
                Spacer(modifier = Modifier.width(8.dp))
                status()
            }
        }
    }
}
