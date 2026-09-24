package com.aivigil.compasslevel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    background = PureBlack,
    surface = DarkSurface,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary
)

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFF8FAF9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF0F4F2),
    onSurfaceVariant = Color(0xFF4B5563)
)

@Composable
fun CompassLevelTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme,
        content = content
    )
}
