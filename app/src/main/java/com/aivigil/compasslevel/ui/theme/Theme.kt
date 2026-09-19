package com.aivigil.compasslevel.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = PureBlack,
    surface = DarkSurface,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun CompassLevelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
