package com.example.waterreminder.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.waterreminder.ui.components.StaggeredEntrance
import com.example.waterreminder.ui.components.nameInitials
import com.example.waterreminder.ui.settings.SettingsViewModel

@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.userPreferences.collectAsState()
    var name by remember(prefs.userName) { mutableStateOf(prefs.userName) }

    val trimmed = name.trim()

    FormScreenScaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            DetailTopBar(
                title = "Your Profile",
                subtitle = "Personalize your experience",
                onBack = onBack
            )
        },
        bottomBar = {
            GradientButton(
                text = "Save Changes",
                icon = Icons.Default.Check,
                enabled = trimmed.isNotBlank(),
                onClick = {
                    viewModel.updateName(trimmed)
                    onBack()
                }
            )
        }
    ) {
        // Large Avatar with Live Initials
        StaggeredEntrance(index = 0) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .border(2.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nameInitials(if (trimmed.isNotBlank()) trimmed else prefs.userName),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 44.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        StaggeredEntrance(index = 1) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "What should we call you?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your name personalizes home greetings and reminder notifications.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name input card
        StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FieldLabel("Preferred Name")
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("e.g. Sabin") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Greeting Preview Card
        StaggeredEntrance(index = 3, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.FormatQuote, size = 38.dp, iconSize = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Greeting Preview",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "“Good morning, ${trimmed.ifBlank { "there" }}! Time for a fresh glass.”",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info card
        StaggeredEntrance(index = 4, modifier = Modifier.fillMaxWidth()) {
            GlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(icon = Icons.Default.TipsAndUpdates, size = 38.dp, iconSize = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your name is stored locally on this device and is never shared.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
