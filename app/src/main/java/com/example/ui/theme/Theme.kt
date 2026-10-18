package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BattleBooyahDarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = Color.Black,
    secondary = AccentGold,
    onSecondary = Color.Black,
    tertiary = NeonPurple,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BattleBooyahDarkColorScheme,
        typography = Typography,
        content = content
    )
}
