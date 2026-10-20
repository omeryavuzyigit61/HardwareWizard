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

// 6. VALORANT / Radiant Protocol (Tactical Slate & Crimson)
val ValorantColorScheme = darkColorScheme(
    primary = Color(0xFFFF4655), // Radiant Red
    onPrimary = Color(0xFF0F1923),
    primaryContainer = Color(0xFF3E1117),
    onPrimaryContainer = Color(0xFFFFD1D6),
    secondary = Color(0xFFECE8E1), // Titanium Off-White
    onSecondary = Color(0xFF0F1923),
    secondaryContainer = Color(0xFF203244),
    onSecondaryContainer = Color(0xFFECE8E1),
    tertiary = Color(0xFF00E5FF), // Spike Teal
    onTertiary = Color(0xFF0F1923),
    tertiaryContainer = Color(0xFF063B42),
    onTertiaryContainer = Color(0xFFBAE6FD),
    background = Color(0xFF0F1923), // Tactical Dark Slate
    onBackground = Color(0xFFECE8E1),
    surface = Color(0xFF172431), // Agent Terminal
    onSurface = Color(0xFFECE8E1),
    surfaceVariant = Color(0xFF203244),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF33465C),
    outlineVariant = Color(0xFF4B6482),
    error = Color(0xFFFF4655),
    onError = Color(0xFF0F1923),
)

// 7. COUNTER-STRIKE 2 (CS2) / Tactical Hazard (Hazard Gold & Carbon)
val Cs2ColorScheme = darkColorScheme(
    primary = Color(0xFFDE9B35), // Hazard Gold
    onPrimary = Color(0xFF12151A),
    primaryContainer = Color(0xFF3D270A),
    onPrimaryContainer = Color(0xFFFDE68A),
    secondary = Color(0xFF38BDF8), // CT Spec-Ops Blue
    onSecondary = Color(0xFF12151A),
    secondaryContainer = Color(0xFF0C2A44),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = Color(0xFF10B981), // Defusal Green
    onTertiary = Color(0xFF12151A),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF12151A), // Tactical Carbon
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1B2028), // Arsenal Vault
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF262E3A),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF374151),
    outlineVariant = Color(0xFF4B5563),
    error = Color(0xFFEF4444),
    onError = Color(0xFF12151A),
)

// 8. GTA VI / Vice Neon Sunset (Miami Sunset Pink & Ocean Teal)
val GtaViceColorScheme = darkColorScheme(
    primary = Color(0xFFF43F5E), // Miami Sunset Pink
    onPrimary = Color(0xFF140926),
    primaryContainer = Color(0xFF4C0E1E),
    onPrimaryContainer = Color(0xFFFECDD3),
    secondary = Color(0xFF06B6D4), // Vice Turquoise
    onSecondary = Color(0xFF140926),
    secondaryContainer = Color(0xFF083344),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = Color(0xFFFACC15), // Vice Neon Gold
    onTertiary = Color(0xFF140926),
    tertiaryContainer = Color(0xFF422006),
    onTertiaryContainer = Color(0xFFFEF08A),
    background = Color(0xFF140926), // Vice City Twilight
    onBackground = Color(0xFFFDF4FF),
    surface = Color(0xFF20103D), // Neon Palm Surface
    onSurface = Color(0xFFFDF4FF),
    surfaceVariant = Color(0xFF2E1854),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF6B21A8),
    outlineVariant = Color(0xFF9333EA),
    error = Color(0xFFF43F5E),
    onError = Color(0xFF140926),
)

// 9. THE WITCHER / White Wolf (Kaer Morhen Granite & Igni Flame)
val WitcherColorScheme = darkColorScheme(
    primary = Color(0xFFDC2626), // Wolf Medallion Crimson
    onPrimary = Color(0xFF121214),
    primaryContainer = Color(0xFF450A0A),
    onPrimaryContainer = Color(0xFFFECACA),
    secondary = Color(0xFFD97706), // Igni Flame Amber
    onSecondary = Color(0xFF121214),
    secondaryContainer = Color(0xFF3B1D06),
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = Color(0xFF9CA3AF), // Silver Sword Steel
    onTertiary = Color(0xFF121214),
    tertiaryContainer = Color(0xFF1F2937),
    onTertiaryContainer = Color(0xFFF3F4F6),
    background = Color(0xFF121214), // Kaer Morhen Granite
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF1C1C22), // Fortress Stone
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF2B2B36),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF3F3F46),
    outlineVariant = Color(0xFF52525B),
    error = Color(0xFFEF4444),
    onError = Color(0xFF121214),
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
        ThemeMode.VALORANT -> ValorantColorScheme
        ThemeMode.CS2 -> Cs2ColorScheme
        ThemeMode.GTA_VICE -> GtaViceColorScheme
        ThemeMode.WITCHER -> WitcherColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
