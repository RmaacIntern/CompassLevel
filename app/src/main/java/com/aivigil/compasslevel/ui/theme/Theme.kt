package com.aivigil.compasslevel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    secondary = TextSecondary,
    tertiary = WarningAmber,
    background = Background,
    surface = SurfaceMid,
    onPrimary = Background,
    onSecondary = TextPrimary,
    onTertiary = Background,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun CompassLevelTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
