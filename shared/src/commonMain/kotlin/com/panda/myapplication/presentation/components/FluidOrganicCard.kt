package com.panda.myapplication.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GoldAccent
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose

/**
 * Organic Liquid Water-Drop Card with animated fluid gradient border and subtle inward ambient glow.
 *
 * Tailored for high-end hardware engineering dashboards:
 * - Breathing liquid gradient border that flows continuously along the card edges.
 * - Inward soft ambient glow that bleeds gently from the border into the top surface of the card.
 * - Top specular highlight sheen simulating liquid surface tension.
 */
@Composable
fun FluidOrganicCard(
    modifier: Modifier = Modifier,
    accentColor: Color = ElectricBlue,
    secondaryColor: Color? = null,
    containerColor: Color = Color.Unspecified,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.8.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val secondary = remember(accentColor, secondaryColor) {
        secondaryColor ?: when {
            accentColor == NeonRose || accentColor == Color(0xFFEF4444) -> Color(0xFFFB923C) // Warm flame
            accentColor == NeonEmerald || accentColor == Color(0xFF10B981) -> NeonCyan // Aqua emerald
            accentColor == ElectricBlue || accentColor == Color(0xFF38BDF8) -> NeonPurple // Cyber violet
            accentColor == NeonAmber || accentColor == Color(0xFFF59E0B) -> GoldAccent // Golden amber
            else -> NeonCyan
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fluidOrganicCard")

    // Continuous liquid movement phase
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "fluidBorderPhase",
    )

    // Gentle breathing pulse for the inward ambient glow
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fluidInwardPulse",
    )

    // Animated flowing gradient brush for the liquid border
    val animatedBorderBrush = Brush.linearGradient(
        colors = listOf(
            accentColor,
            secondary,
            accentColor.copy(alpha = 0.70f),
            secondary.copy(alpha = 0.95f),
            accentColor,
        ),
        start = Offset(x = 1000f * phase, y = 0f),
        end = Offset(x = 1000f * phase + 700f, y = 800f),
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = if (containerColor != Color.Unspecified) containerColor else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(borderWidth, animatedBorderBrush),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape),
        ) {
            // 1. Inward vertical ambient wash
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = pulseGlow),
                                accentColor.copy(alpha = pulseGlow * 0.25f),
                                Color.Transparent,
                            ),
                            startY = 0f,
                            endY = 240f,
                        ),
                    ),
            )

            // 2. Corner radial liquid reflection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                secondary.copy(alpha = pulseGlow * 0.6f),
                                Color.Transparent,
                            ),
                            center = Offset(x = 120f, y = 0f),
                            radius = 280f,
                        ),
                    ),
            )

            // 3. Top specular reflection line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                accentColor.copy(alpha = 0.45f + pulseGlow),
                                secondary.copy(alpha = 0.65f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            // 4. Card Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                content = content,
            )
        }
    }
}
