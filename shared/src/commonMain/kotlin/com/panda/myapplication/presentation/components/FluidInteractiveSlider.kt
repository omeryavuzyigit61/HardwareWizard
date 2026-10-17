package com.panda.myapplication.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GoldAccent
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import kotlin.math.sqrt

/**
 * Liquid Water Drop / Metaball Elastic Slider.
 * Inspired by fluid droplet UI:
 * - Single continuous organic stream with bulbous water-drop anchors at both ends.
 * - Stretches and elongates dynamically when dragged forward ("esneme hareketi").
 * - Volume-preserving spring physics (stretches along X, contracts along Y, wobbles on release).
 * - Smooth modern gradient tailored to the app palette (NeonPurple -> ElectricBlue -> NeonCyan).
 */
@Composable
fun FluidInteractiveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    accentColor: Color = NeonCyan,
    secondaryColor: Color? = null,
    inactiveTrackColor: Color? = null,
    enabled: Boolean = true,
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragVelocityX by remember { mutableFloatStateOf(0f) }
    var lastDragX by remember { mutableFloatStateOf(0f) }

    // Dynamic stretch calculation: pulling forward elongates the droplet head
    val targetStretch = when {
        isDragging -> (1.30f + (dragVelocityX / 55f).coerceIn(-0.15f, 0.35f))
        else -> 1.0f
    }

    val stretchX by animateFloatAsState(
        targetValue = targetStretch,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "water_drop_stretch_x",
    )

    // Volume conservation: droplet narrows vertically when elongated horizontally
    val stretchY = (1.0f / sqrt(stretchX.coerceAtLeast(0.5f))).coerceIn(0.78f, 1.05f)

    // Ambient glow halo alpha when interacting
    val glowAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.50f else 0.15f,
        animationSpec = tween(durationMillis = 200),
        label = "droplet_glow_alpha",
    )

    // Dynamic gradient tailored to the app theme and accentColor
    val fluidGradientColors = remember(accentColor) {
        when (accentColor) {
            NeonAmber -> listOf(
                NeonRose,
                Color(0xFFFB923C), // Warm coral
                NeonAmber,
                GoldAccent,
            )
            NeonEmerald -> listOf(
                NeonCyan,
                Color(0xFF059669), // Deep emerald
                NeonEmerald,
                Color(0xFF34D399), // Mint glow
            )
            else -> listOf(
                Color(0xFF8B5CF6), // Soft vibrant violet
                NeonPurple,
                ElectricBlue,
                NeonCyan,
            )
        }
    }

    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
    val normalized = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = 4.dp)
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = { offset ->
                        isDragging = true
                        lastDragX = offset.x
                        dragVelocityX = 0f
                        val bulbRadius = 14.dp.toPx()
                        val trackStart = bulbRadius + 2.dp.toPx()
                        val trackEnd = size.width - (bulbRadius + 2.dp.toPx())
                        val fraction = ((offset.x - trackStart) / (trackEnd - trackStart).coerceAtLeast(1f)).coerceIn(0f, 1f)
                        val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                        tryAwaitRelease()
                        isDragging = false
                        dragVelocityX = 0f
                    },
                )
            }
            .pointerInput(enabled, valueRange) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        lastDragX = offset.x
                        dragVelocityX = 0f
                    },
                    onDragEnd = {
                        isDragging = false
                        dragVelocityX = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        dragVelocityX = 0f
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val delta = change.position.x - lastDragX
                        lastDragX = change.position.x
                        dragVelocityX = (dragVelocityX * 0.65f + delta * 0.35f).coerceIn(-50f, 50f)

                        val bulbRadius = 14.dp.toPx()
                        val trackStart = bulbRadius + 2.dp.toPx()
                        val trackEnd = size.width - (bulbRadius + 2.dp.toPx())
                        val fraction = ((change.position.x - trackStart) / (trackEnd - trackStart).coerceAtLeast(1f)).coerceIn(0f, 1f)
                        val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                        onValueChange(newValue)
                    },
                )
            },
    ) {
        val defaultInactiveBg = Color(0xFF1E293B).copy(alpha = 0.55f)
        val finalInactiveColor = inactiveTrackColor ?: defaultInactiveBg

        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            val bulbRadius = 13.5.dp.toPx()
            val trackHeight = 8.5.dp.toPx()
            val trackHalf = trackHeight / 2f

            val leftX = bulbRadius + 3.dp.toPx()
            val rightX = width - (bulbRadius + 3.dp.toPx())
            val availableWidth = (rightX - leftX).coerceAtLeast(1f)
            val currentX = (leftX + availableWidth * normalized).coerceIn(leftX, rightX)

            // -------------------------------------------------------------
            // 1. INACTIVE BACKGROUND TRACK (Frosted glass capsule with bulbs)
            // -------------------------------------------------------------
            val bgPath = Path().apply {
                // Left bulb
                moveTo(leftX - bulbRadius, centerY)
                cubicTo(leftX - bulbRadius, centerY - bulbRadius, leftX, centerY - bulbRadius, leftX + bulbRadius * 0.6f, centerY - bulbRadius * 0.8f)
                // Bridge to track top
                cubicTo(leftX + bulbRadius * 1.1f, centerY - bulbRadius * 0.6f, leftX + bulbRadius * 1.3f, centerY - trackHalf, leftX + bulbRadius * 1.6f, centerY - trackHalf)
                // Top of track
                lineTo(rightX - bulbRadius * 1.6f, centerY - trackHalf)
                // Bridge to right bulb
                cubicTo(rightX - bulbRadius * 1.3f, centerY - trackHalf, rightX - bulbRadius * 1.1f, centerY - bulbRadius * 0.6f, rightX - bulbRadius * 0.6f, centerY - bulbRadius * 0.8f)
                cubicTo(rightX, centerY - bulbRadius, rightX + bulbRadius, centerY - bulbRadius, rightX + bulbRadius, centerY)
                cubicTo(rightX + bulbRadius, centerY + bulbRadius, rightX, centerY + bulbRadius, rightX - bulbRadius * 0.6f, centerY + bulbRadius * 0.8f)
                // Bridge to track bottom
                cubicTo(rightX - bulbRadius * 1.1f, centerY + bulbRadius * 0.6f, rightX - bulbRadius * 1.3f, centerY + trackHalf, rightX - bulbRadius * 1.6f, centerY + trackHalf)
                // Bottom of track
                lineTo(leftX + bulbRadius * 1.6f, centerY + trackHalf)
                // Bridge to left bulb bottom
                cubicTo(leftX + bulbRadius * 1.3f, centerY + trackHalf, leftX + bulbRadius * 1.1f, centerY + bulbRadius * 0.6f, leftX + bulbRadius * 0.6f, centerY + bulbRadius * 0.8f)
                cubicTo(leftX, centerY + bulbRadius, leftX - bulbRadius, centerY + bulbRadius, leftX - bulbRadius, centerY)
                close()
            }

            drawPath(path = bgPath, color = finalInactiveColor)
            drawPath(
                path = bgPath,
                color = Color.White.copy(alpha = 0.08f),
                style = Stroke(width = 1.dp.toPx()),
            )

            // Subtle Left & Right Directional Arrows (like reference photo)
            val arrowSize = 4.5.dp.toPx()
            // Right Arrow '->' in inactive bulb
            drawLine(
                color = Color.White.copy(alpha = 0.28f),
                start = Offset(rightX - arrowSize, centerY),
                end = Offset(rightX + arrowSize, centerY),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.28f),
                start = Offset(rightX + arrowSize * 0.35f, centerY - arrowSize * 0.75f),
                end = Offset(rightX + arrowSize, centerY),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.28f),
                start = Offset(rightX + arrowSize * 0.35f, centerY + arrowSize * 0.75f),
                end = Offset(rightX + arrowSize, centerY),
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
            )

            // -------------------------------------------------------------
            // 2. ACTIVE LIQUID WATER DROP STREAM (Single unified morphing shape)
            // -------------------------------------------------------------
            val thumbRadius = bulbRadius * 0.96f
            val sX = stretchX
            val sY = stretchY

            // Glow Aura behind the liquid
            if (currentX > leftX) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            fluidGradientColors.last().copy(alpha = glowAlpha * 0.6f),
                            Color.Transparent,
                        ),
                        center = Offset(currentX, centerY),
                        radius = 28.dp.toPx() * sX,
                    ),
                    center = Offset(currentX, centerY),
                    radius = 28.dp.toPx() * sX,
                )
            }

            val liquidPath = Path().apply {
                // Left bulb start
                moveTo(leftX - bulbRadius, centerY)
                // Left bulb top arc
                cubicTo(
                    leftX - bulbRadius, centerY - bulbRadius * 0.55f,
                    leftX - bulbRadius * 0.55f, centerY - bulbRadius,
                    leftX, centerY - bulbRadius,
                )
                cubicTo(
                    leftX + bulbRadius * 0.3f, centerY - bulbRadius,
                    leftX + bulbRadius * 0.55f, centerY - bulbRadius * 0.9f,
                    leftX + bulbRadius * 0.75f, centerY - bulbRadius * 0.75f,
                )

                if (currentX > leftX + bulbRadius * 1.6f) {
                    // Smooth waist bridge down to track
                    cubicTo(
                        leftX + bulbRadius * 1.15f, centerY - bulbRadius * 0.55f,
                        leftX + bulbRadius * 1.35f, centerY - trackHalf,
                        leftX + bulbRadius * 1.7f, centerY - trackHalf,
                    )
                    // Along track top
                    val bridgeStartBeforeThumb = (currentX - thumbRadius * 1.3f * sX).coerceAtLeast(leftX + bulbRadius * 1.7f)
                    lineTo(bridgeStartBeforeThumb, centerY - trackHalf)

                    // Smooth waist bridge up to stretching thumb droplet head
                    cubicTo(
                        currentX - thumbRadius * 0.95f * sX, centerY - trackHalf,
                        currentX - thumbRadius * 0.6f * sX, centerY - (thumbRadius * sY) * 0.85f,
                        currentX, centerY - (thumbRadius * sY),
                    )
                } else {
                    // When thumb is close to base: direct fluid bridge
                    cubicTo(
                        leftX + bulbRadius, centerY - bulbRadius * 0.7f,
                        currentX - thumbRadius * 0.5f, centerY - (thumbRadius * sY),
                        currentX, centerY - (thumbRadius * sY),
                    )
                }

                // Front of the droplet (Water drop head stretching forward)
                val frontTipX = currentX + (thumbRadius * sX) * 1.08f
                cubicTo(
                    currentX + (thumbRadius * sX) * 0.75f, centerY - (thumbRadius * sY),
                    frontTipX, centerY - (thumbRadius * sY) * 0.45f,
                    frontTipX, centerY,
                )
                cubicTo(
                    frontTipX, centerY + (thumbRadius * sY) * 0.45f,
                    currentX + (thumbRadius * sX) * 0.75f, centerY + (thumbRadius * sY),
                    currentX, centerY + (thumbRadius * sY),
                )

                // Returning along the bottom
                if (currentX > leftX + bulbRadius * 1.6f) {
                    val bridgeStartBeforeThumb = (currentX - thumbRadius * 1.3f * sX).coerceAtLeast(leftX + bulbRadius * 1.7f)
                    // Smooth waist bridge down from thumb to track bottom
                    cubicTo(
                        currentX - thumbRadius * 0.6f * sX, centerY + (thumbRadius * sY) * 0.85f,
                        currentX - thumbRadius * 0.95f * sX, centerY + trackHalf,
                        bridgeStartBeforeThumb, centerY + trackHalf,
                    )
                    // Along track bottom
                    lineTo(leftX + bulbRadius * 1.7f, centerY + trackHalf)

                    // Smooth waist bridge back to left bulb bottom
                    cubicTo(
                        leftX + bulbRadius * 1.35f, centerY + trackHalf,
                        leftX + bulbRadius * 1.15f, centerY + bulbRadius * 0.55f,
                        leftX + bulbRadius * 0.75f, centerY + bulbRadius * 0.75f,
                    )
                } else {
                    // When thumb is close to base: direct fluid bridge
                    cubicTo(
                        currentX - thumbRadius * 0.5f, centerY + (thumbRadius * sY),
                        leftX + bulbRadius, centerY + bulbRadius * 0.7f,
                        leftX + bulbRadius * 0.75f, centerY + bulbRadius * 0.75f,
                    )
                }

                // Left bulb bottom arc
                cubicTo(
                    leftX + bulbRadius * 0.55f, centerY + bulbRadius * 0.9f,
                    leftX + bulbRadius * 0.3f, centerY + bulbRadius,
                    leftX, centerY + bulbRadius,
                )
                cubicTo(
                    leftX - bulbRadius * 0.55f, centerY + bulbRadius,
                    leftX - bulbRadius, centerY + bulbRadius * 0.55f,
                    leftX - bulbRadius, centerY,
                )
                close()
            }

            // Fill with smooth cyberpunk / ambient gradient
            val liquidGradient = Brush.horizontalGradient(
                colors = fluidGradientColors,
                startX = leftX - bulbRadius,
                endX = (currentX + thumbRadius * sX).coerceAtLeast(leftX + bulbRadius),
            )
            drawPath(path = liquidPath, brush = liquidGradient)

            // Glossy Specular Highlight (Water drop refraction along upper contour)
            val highlightPath = Path().apply {
                moveTo(leftX - bulbRadius * 0.6f, centerY - bulbRadius * 0.65f)
                cubicTo(
                    leftX - bulbRadius * 0.3f, centerY - bulbRadius * 0.85f,
                    leftX + bulbRadius * 0.3f, centerY - bulbRadius * 0.85f,
                    leftX + bulbRadius * 0.7f, centerY - bulbRadius * 0.65f,
                )
                if (currentX > leftX + bulbRadius * 2f) {
                    lineTo(currentX - thumbRadius * sX * 0.8f, centerY - trackHalf + 1.2.dp.toPx())
                    cubicTo(
                        currentX - thumbRadius * sX * 0.3f, centerY - (thumbRadius * sY) * 0.7f,
                        currentX + thumbRadius * sX * 0.3f, centerY - (thumbRadius * sY) * 0.7f,
                        currentX + thumbRadius * sX * 0.7f, centerY - (thumbRadius * sY) * 0.3f,
                    )
                }
            }
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.40f),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round),
            )

            // Left Arrow '<-' in left bulb (always crisp and visible like reference)
            drawLine(
                color = Color.White.copy(alpha = 0.95f),
                start = Offset(leftX - arrowSize, centerY),
                end = Offset(leftX + arrowSize, centerY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.95f),
                start = Offset(leftX - arrowSize * 0.35f, centerY - arrowSize * 0.75f),
                end = Offset(leftX - arrowSize, centerY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White.copy(alpha = 0.95f),
                start = Offset(leftX - arrowSize * 0.35f, centerY + arrowSize * 0.75f),
                end = Offset(leftX - arrowSize, centerY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round,
            )

            // Droplet Core Eye / Center Glisten on thumb head
            if (currentX > leftX + bulbRadius) {
                drawCircle(
                    color = Color.White.copy(alpha = if (isDragging) 0.85f else 0.50f),
                    radius = (3.2.dp.toPx() * sY),
                    center = Offset(currentX, centerY),
                )
            }
        }
    }
}
