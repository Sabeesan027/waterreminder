package com.example.waterreminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A78),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5EEFF),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = Color(0xFF3E6370),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC1E8F5),
    onSecondaryContainer = Color(0xFF001F27),
    tertiary = Color(0xFF2B6B57),
    onTertiary = Color.White,
    background = Color(0xFFF4FAFD),
    onBackground = Color(0xFF111D21),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111D21),
    surfaceVariant = Color(0xFFDCE8EC),
    onSurfaceVariant = Color(0xFF3F4D51),
    outline = Color(0xFF6F797D),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepNavyBackground,
    primaryContainer = BrightBlue,
    onPrimaryContainer = TextPrimary,
    secondary = LightCyan,
    onSecondary = DeepNavyBackground,
    secondaryContainer = NavyCardBackground,
    onSecondaryContainer = TextSecondary,
    tertiary = SuccessGreen,
    onTertiary = DeepNavyBackground,
    background = DeepNavyBackground,
    onBackground = TextPrimary,
    surface = DeepNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCardBackground,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun WaterReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
