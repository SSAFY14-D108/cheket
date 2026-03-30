package com.ssafy.cheket.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CheketColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnBackground,
    surfaceVariant = Muted,
    onSurfaceVariant = MutedForeground,
    outline = BorderColor,
    error = Danger,
    secondary = Secondary,
    onSecondary = White,
)

@Composable
fun CheketTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CheketColorScheme,
        typography = Typography,
        content = content,
    )
}
