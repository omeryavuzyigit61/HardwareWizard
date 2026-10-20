package com.panda.myapplication.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.SlateDarkBackground
import kotlin.math.abs
import kotlin.math.sqrt

data class FluidBottomNavTabItem(
    val route: String,
    val title: String,
    val iconType: FluidTabIconType,
)

enum class FluidTabIconType {
    CATALOG,
    WIZARD,
    VS_ARENA,
}

/**
 * High-performance, 120 FPS Elastic Water-Drop Bottom Navigation Bar.
 *
 * Designed with pure organic fluid physics (NO rectangular boxes or hard pills):
 * 1. Top Edge Sliding Droplet Bead: Glides continuously along the top border line with elastic stretch physics.
 * 2. Seamless Ambient Radial Aura: An organic water-glow wash behind the active tab (no sharp corners or box boundaries).
 * 3. Elevated Floating Icon: Bouncy spring elevation and gentle scale.
 * 4. Micro-Droplet Dot: Softly illuminates under the active tab label.
 * 5. GPU-Accelerated: Rendered via Canvas to eliminate layout recomposition jank.
 */
@Composable
fun FluidBottomBar(
    tabs: List<FluidBottomNavTabItem>,
    currentRoute: String?,
    vsFilledCount: Int,
    onTabSelected: (FluidBottomNavTabItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val selectedIndex = remember(currentRoute, tabs) {
        val found = tabs.indexOfFirst { currentRoute?.startsWith(it.route) == true }
        if (found >= 0) found else 0
    }

    // Smooth spring animation for the sliding droplet center
    val animatedTabProgress by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "fluid_tab_progress",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val totalHeightPx = constraints.maxHeight.toFloat()
            val tabCount = tabs.size.coerceAtLeast(1)
            val tabWidthPx = totalWidthPx / tabCount

            // Dynamic elastic physics: stretch along travel vector
            val travelDistance = abs(selectedIndex.toFloat() - animatedTabProgress)
            val stretchX = (1.0f + travelDistance * 0.58f).coerceIn(1.0f, 1.65f)
            val stretchY = (1.0f / sqrt(stretchX)).coerceIn(0.75f, 1.0f)

            // Current droplet center in pixels
            val currentCenterXPx = (animatedTabProgress + 0.5f) * tabWidthPx

            // -------------------------------------------------------------
            // 1. GPU CANVAS LAYER: Top Liquid Line, Sliding Bead & Ambient Aura
            // -------------------------------------------------------------
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Top subtle ambient track line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            themeSecondary.copy(alpha = 0.20f),
                            themePrimary.copy(alpha = 0.35f),
                            themeSecondary.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                    ),
                    start = Offset(0f, 1f),
                    end = Offset(w, 1f),
                    strokeWidth = 1.5.dp.toPx(),
                )

                // Seamless soft ambient water-glow aura behind active tab (NO RECTANGLE!)
                val auraRadius = (tabWidthPx * 0.44f).coerceAtLeast(40f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            themePrimary.copy(alpha = 0.22f),
                            themeSecondary.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
                        center = Offset(currentCenterXPx, h * 0.44f),
                        radius = auraRadius,
                    ),
                    center = Offset(currentCenterXPx, h * 0.44f),
                    radius = auraRadius,
                )

                // Top Edge Elastic Water-Droplet Bead
                val baseBeadWidth = 32.dp.toPx()
                val baseBeadHeight = 4.dp.toPx()
                val activeBeadWidth = baseBeadWidth * stretchX
                val activeBeadHeight = baseBeadHeight * stretchY

                // Glowing bead shadow
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            themeSecondary.copy(alpha = 0.45f),
                            themePrimary.copy(alpha = 0.85f),
                            themeSecondary.copy(alpha = 0.45f),
                        ),
                    ),
                    topLeft = Offset(currentCenterXPx - activeBeadWidth / 2f, 0f),
                    size = Size(activeBeadWidth, activeBeadHeight),
                    cornerRadius = CornerRadius(activeBeadHeight / 2f, activeBeadHeight / 2f),
                )

                // Specular core light inside the droplet bead
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.75f),
                    topLeft = Offset(currentCenterXPx - (activeBeadWidth * 0.45f) / 2f, 0.5.dp.toPx()),
                    size = Size(activeBeadWidth * 0.45f, 1.5.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()),
                )

                // Subtle glowing micro-droplet indicator beneath active tab label
                val dotRadius = 2.2.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            themePrimary,
                            themePrimary.copy(alpha = 0.35f),
                            Color.Transparent,
                        ),
                        center = Offset(currentCenterXPx, h - 5.dp.toPx()),
                        radius = dotRadius * 2.5f,
                    ),
                    center = Offset(currentCenterXPx, h - 5.dp.toPx()),
                    radius = dotRadius,
                )
            }

            // -------------------------------------------------------------
            // 2. FOREGROUND TAB ITEMS (Icons & Labels)
            // -------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex

                    // Animated elevation and scale for active tab
                    val tabScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        label = "tab_scale_$index",
                    )
                    val iconOffsetY by animateFloatAsState(
                        targetValue = if (isSelected) -2.5f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                        label = "tab_offset_y_$index",
                    )

                    val activeColor = themePrimary
                    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(tab) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = tabScale
                                    scaleY = tabScale
                                    translationY = iconOffsetY.dp.toPx()
                                }
                                .padding(top = 1.dp),
                        ) {
                            Box(
                                modifier = Modifier.size(28.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val currentIconColor = if (isSelected) activeColor else inactiveColor
                                when (tab.iconType) {
                                    FluidTabIconType.CATALOG -> FluidCatalogIcon(
                                        isSelected = isSelected,
                                        color = currentIconColor,
                                    )
                                    FluidTabIconType.WIZARD -> FluidWizardIcon(
                                        isSelected = isSelected,
                                        color = currentIconColor,
                                    )
                                    FluidTabIconType.VS_ARENA -> {
                                        Box(contentAlignment = Alignment.TopEnd) {
                                            FluidVsArenaIcon(
                                                isSelected = isSelected,
                                                color = currentIconColor,
                                            )
                                            if (vsFilledCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = 7.dp, y = (-4).dp)
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(NeonEmerald),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = vsFilledCount.toString(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = SlateDarkBackground,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (isSelected) activeColor else inactiveColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FluidCatalogIcon(
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.32f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(strokeWidth),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.15f, h * 0.55f),
            size = Size(w * 0.7f, h * 0.32f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(strokeWidth),
        )
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = Offset(w * 0.32f, h * 0.31f),
        )
        drawLine(
            color = color,
            start = Offset(w * 0.3f, h * 0.71f),
            end = Offset(w * 0.7f, h * 0.71f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun FluidWizardIcon(
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.28f, h * 0.28f),
            size = Size(w * 0.44f, h * 0.44f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(strokeWidth),
        )
        if (isSelected) {
            drawCircle(
                color = color,
                radius = 2.5.dp.toPx(),
                center = Offset(w * 0.5f, h * 0.5f),
            )
        }
        drawLine(color = color, start = Offset(w * 0.38f, h * 0.14f), end = Offset(w * 0.38f, h * 0.28f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.62f, h * 0.14f), end = Offset(w * 0.62f, h * 0.28f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.38f, h * 0.72f), end = Offset(w * 0.38f, h * 0.86f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.62f, h * 0.72f), end = Offset(w * 0.62f, h * 0.86f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.14f, h * 0.38f), end = Offset(w * 0.28f, h * 0.38f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.14f, h * 0.62f), end = Offset(w * 0.28f, h * 0.62f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.72f, h * 0.38f), end = Offset(w * 0.86f, h * 0.38f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(w * 0.72f, h * 0.62f), end = Offset(w * 0.86f, h * 0.62f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun FluidVsArenaIcon(
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = if (isSelected) 2.2.dp.toPx() else 1.8.dp.toPx()

        // Left hardware card
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.10f, h * 0.20f),
            size = Size(w * 0.33f, h * 0.60f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(strokeWidth),
        )
        // Right hardware card
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.57f, h * 0.20f),
            size = Size(w * 0.33f, h * 0.60f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(strokeWidth),
        )
        // Center lightning bolt ("VS" clash)
        val bolt = Path().apply {
            moveTo(w * 0.55f, h * 0.14f)
            lineTo(w * 0.43f, h * 0.50f)
            lineTo(w * 0.56f, h * 0.50f)
            lineTo(w * 0.45f, h * 0.86f)
        }
        drawPath(
            path = bolt,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}
