package com.panda.myapplication.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = SlateDarkBackground,
    primaryContainer = NeonCyanContainer,
    onPrimaryContainer = OnNeonCyanContainer,
    secondary = NeonEmerald,
    onSecondary = SlateDarkBackground,
    secondaryContainer = NeonEmeraldContainer,
    onSecondaryContainer = OnNeonEmeraldContainer,
    tertiary = NeonRose,
    onTertiary = SlateDarkBackground,
    tertiaryContainer = NeonRoseContainer,
    onTertiaryContainer = OnNeonRoseContainer,
    background = SlateDarkBackground,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderStroke,
    outlineVariant = BorderStrokeFocused,
    error = NeonRose,
    onError = SlateDarkBackground,
)

@Composable
fun HardwareWizardTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
