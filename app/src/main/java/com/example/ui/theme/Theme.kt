package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BloxDarkColorScheme = darkColorScheme(
    primary = BloxCyan,
    onPrimary = BloxTextPrimary,
    primaryContainer = BloxCardElevated,
    onPrimaryContainer = BloxTextPrimary,
    secondary = BloxGreen,
    onSecondary = BloxTextPrimary,
    tertiary = BloxGold,
    background = BloxDarkBg,
    onBackground = BloxTextPrimary,
    surface = BloxSurface,
    onSurface = BloxTextPrimary,
    surfaceVariant = BloxCard,
    onSurfaceVariant = BloxTextSecondary,
    outline = BloxBorder,
    error = BloxRed,
    onError = BloxTextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BloxDarkColorScheme,
        typography = Typography,
        content = content
    )
}
