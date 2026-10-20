package com.panda.myapplication.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.panda.myapplication.domain.model.ThemeMode

// 1. Obsidian Studio (Dark - Default)
val DarkColorScheme = darkColorScheme(
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

// 2. Pure Slate (Light)
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = Color(0xFFE11D48),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE4E6),
    onTertiaryContainer = Color(0xFF9F1239),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White,
)

// 3. Cyber-Wood / Warm Walnut (Ahşap & Espresso)
val WoodColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B), // Warm Amber
    onPrimary = Color(0xFF1C130D),
    primaryContainer = Color(0xFF452207),
    onPrimaryContainer = Color(0xFFFDE68A),
    secondary = Color(0xFFD97706), // Antique Brass
    onSecondary = Color(0xFF1C130D),
    secondaryContainer = Color(0xFF3B1D06),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Color(0xFF10B981), // Forest Green accent
    onTertiary = Color(0xFF1C130D),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF160F0A), // Rich Espresso
    onBackground = Color(0xFFFDF6EE),
    surface = Color(0xFF231811), // Roasted Walnut
    onSurface = Color(0xFFFDF6EE),
    surfaceVariant = Color(0xFF32231A), // Cedar Grain
    onSurfaceVariant = Color(0xFFD4BFA7),
    outline = Color(0xFF422F24),
    outlineVariant = Color(0xFF593E30),
    error = Color(0xFFEF4444),
    onError = Color(0xFF1C130D),
)

// 4. Midnight OLED (Pure Black)
val OledColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8), // Electric Cyan
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0C243B),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = Color(0xFF34D399),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF063E2D),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = Color(0xFFF43F5E),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF4C0E1E),
    onTertiaryContainer = Color(0xFFFECDD3),
    background = Color(0xFF000000), // True OLED Black
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF09090B), // Zinc 950
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF18181B), // Zinc 900
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF27272A),
    outlineVariant = Color(0xFF3F3F46),
    error = Color(0xFFEF4444),
    onError = Color.Black,
)

// 5. Cyberpunk Neon (Night Violet & Acid Yellow)
val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFFFACC15), // Acid Yellow
    onPrimary = Color(0xFF120526),
    primaryContainer = Color(0xFF422006),
    onPrimaryContainer = Color(0xFFFEF08A),
    secondary = Color(0xFFC084FC), // Neon Purple
    onSecondary = Color(0xFF120526),
    secondaryContainer = Color(0xFF3B0764),
    onSecondaryContainer = Color(0xFFF3E8FF),
    tertiary = Color(0xFFF43F5E), // Neon Pink
    onTertiary = Color(0xFF120526),
    tertiaryContainer = Color(0xFF500724),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = Color(0xFF0E041E), // Deep Violet
    onBackground = Color(0xFFFDF4FF),
    surface = Color(0xFF1A0A33),
    onSurface = Color(0xFFFDF4FF),
    surfaceVariant = Color(0xFF28124D),
    onSurfaceVariant = Color(0xFFD8B4FE),
    outline = Color(0xFF4C1D95),
    outlineVariant = Color(0xFF6D28D9),
    error = Color(0xFFF43F5E),
    onError = Color(0xFF120526),
)

@Composable
fun HardwareWizardTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        ThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.WOOD -> WoodColorScheme
        ThemeMode.OLED -> OledColorScheme
        ThemeMode.CYBERPUNK -> CyberpunkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
