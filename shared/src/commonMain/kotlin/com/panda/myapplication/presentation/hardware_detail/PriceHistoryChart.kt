package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.model.PricePoint
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.CardSurface
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.theme.SlateDarkBackground
import com.panda.myapplication.presentation.util.formatPrice
import kotlin.math.roundToInt

enum class PriceChartRange(val label: String, val maxPoints: Int) {
    ONE_MONTH("1 Ay", 2),
    SIX_MONTHS("6 Ay", 5),
    ONE_YEAR("1 Yıl", 8),
    ALL("Tümü", 100);

    fun localizedLabel(isTr: Boolean = true): String = if (isTr) label else when (this) {
        ONE_MONTH -> "1 Mo"
        SIX_MONTHS -> "6 Mo"
        ONE_YEAR -> "1 Yr"
        ALL -> "All"
    }
}

@Composable
fun PriceHistoryCard(
    priceHistory: List<PricePoint>,
    currentPriceTry: Double?,
    lowestPriceTry: Double?,
    highestPriceTry: Double?,
    modifier: Modifier = Modifier,
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    var selectedRange by remember { mutableStateOf(PriceChartRange.ALL) }

    val effectivePoints = remember(priceHistory, currentPriceTry) {
        if (priceHistory.isNotEmpty()) {
            priceHistory
        } else if (currentPriceTry != null && currentPriceTry > 0) {
            listOf(
                PricePoint(
                    date = if (isTr) "Bugün" else "Today",
                    timestamp = if (isTr) "Bugün" else "Today",
                    price = currentPriceTry,
                    priceTry = currentPriceTry,
                    storeName = if (isTr) "En Ucuz" else "Lowest",
                ),
            )
        } else {
            emptyList()
        }
    }

    val filteredPoints = remember(effectivePoints, selectedRange) {
        if (effectivePoints.isEmpty()) {
            emptyList()
        } else {
            val count = selectedRange.maxPoints.coerceAtLeast(1)
            if (effectivePoints.size <= count) effectivePoints else effectivePoints.takeLast(count)
        }
    }

    val current = currentPriceTry ?: filteredPoints.lastOrNull()?.effectivePrice ?: 0.0
    val lowest = lowestPriceTry ?: filteredPoints.minOfOrNull { it.effectivePrice } ?: current
    val highest = highestPriceTry ?: filteredPoints.maxOfOrNull { it.effectivePrice } ?: current

    // Calculate percentage change over the selected range
    val changePercent = remember(filteredPoints) {
        if (filteredPoints.size >= 2) {
            val first = filteredPoints.first().effectivePrice
            val last = filteredPoints.last().effectivePrice
            if (first > 0) ((last - first) / first) * 100.0 else 0.0
        } else 0.0
    }

    val trendColor = if (changePercent <= 0) NeonEmerald else NeonRose
    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = NeonCyan,
        secondaryColor = trendColor,
        contentPadding = Spacing.medium,
    ) {
        Column {
            // Header: Title and Market Analysis Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonCyan),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = tr(isTr, "FİYAT GEÇMİŞİ & PİYASA ANALİZİ (TL)", "PRICE HISTORY & MARKET TREND"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 1.sp,
                    )
                }

                val badgeColor = when {
                    changePercent < -0.01 -> NeonEmerald
                    changePercent > 0.01 -> NeonRose
                    else -> NeonCyan
                }
                val badgeBg = badgeColor.copy(alpha = 0.12f)
                val trendText = when {
                    changePercent < -0.01 -> tr(isTr, "Düşüşte", "Dropping")
                    changePercent > 0.01 -> tr(isTr, "Yükselişte", "Rising")
                    else -> tr(isTr, "Referans Fiyat", "Reference Price")
                }
                val sign = if (changePercent > 0.01) "+" else ""
                val badgeText = if (filteredPoints.size >= 2) {
                    "$sign${changePercent.formatOneDecimal()}% ($trendText)"
                } else {
                    "${tr(isTr, "En Düşük", "Lowest")}: ₺${current.formatPrice()}"
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeColor),
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Price Stat Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = tr(isTr, "GÜNCEL PİYASA FİYATI", "CURRENT MARKET PRICE"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (current > 0) "₺${current.formatPrice()}" else tr(isTr, "Stok / Fiyat Yok", "Out of Stock / No Price"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                    )
                }

                if (lowest > 0 || highest > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = tr(isTr, "EN DÜŞÜK", "LOWEST"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (lowest > 0) "₺${lowest.formatPrice()}" else "-",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald,
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = tr(isTr, "EN YÜKSEK", "HIGHEST"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (highest > 0) "₺${highest.formatPrice()}" else "-",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonRose,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Filter Chips (1 Ay | 6 Ay | 1 Yıl | Tümü)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                PriceChartRange.entries.forEach { range ->
                    val isSelected = range == selectedRange
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRange = range },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else CardSurfaceElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline,
                        ),
                    ) {
                        Text(
                            text = range.localizedLabel(isTr),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Neon Chart Canvas
            if (filteredPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tr(isTr, "Fiyat geçmişi verisi hazırlanıyor...", "Price history data is loading..."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                PriceHistoryChart(
                    points = filteredPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Footnote
            Text(
                text = tr(isTr, "💡 Fiyat noktalarına dokunarak geçmiş tarih ve fiyat detaylarını inceleyebilirsiniz.", "💡 Tap chart points to view historical dates and pricing records."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
fun PriceHistoryChart(
    points: List<PricePoint>,
    modifier: Modifier = Modifier,
) {
    var selectedIndex by remember(points) { mutableIntStateOf(points.lastIndex) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        )
    }

    val minPrice = remember(points) { points.minOf { it.effectivePrice } }
    val maxPrice = remember(points) { points.maxOf { it.effectivePrice } }
    val priceDelta = remember(minPrice, maxPrice) { (maxPrice - minPrice).coerceAtLeast(100.0) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        if (points.size > 1) {
                            val stepX = (size.width - 40.dp.toPx()) / (points.size - 1)
                            val touchX = (offset.x - 20.dp.toPx()).coerceIn(0f, size.width - 40.dp.toPx())
                            val index = (touchX / stepX).roundToInt().coerceIn(0, points.lastIndex)
                            selectedIndex = index
                        } else {
                            selectedIndex = 0
                        }
                    }
                }
                .pointerInput(points) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        if (points.size > 1) {
                            val stepX = (size.width - 40.dp.toPx()) / (points.size - 1)
                            val touchX = (change.position.x - 20.dp.toPx()).coerceIn(0f, size.width - 40.dp.toPx())
                            val index = (touchX / stepX).roundToInt().coerceIn(0, points.lastIndex)
                            selectedIndex = index
                        } else {
                            selectedIndex = 0
                        }
                    }
                },
        ) {
            val width = size.width
            val height = size.height
            val padX = 20.dp.toPx()
            val padTop = 30.dp.toPx()
            val padBottom = 20.dp.toPx()
            val usableHeight = height - padTop - padBottom
            val usableWidth = width - (padX * 2)

            // 1. Dashed Horizontal Grid Lines
            val gridLines = 3
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            for (i in 0..gridLines) {
                val y = padTop + (usableHeight * (i.toFloat() / gridLines))
                drawLine(
                    color = Color(0xFF1F2937),
                    start = Offset(padX, y),
                    end = Offset(width - padX, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect,
                )
            }

            // 2. Compute Point Coordinates
            val isSinglePoint = points.size == 1
            val stepX = if (isSinglePoint) 0f else usableWidth / (points.size - 1)
            val currentProgress = animProgress.value

            val coords = if (isSinglePoint) {
                val targetY = padTop + (usableHeight / 2f)
                val animatedY = height - padBottom - ((height - padBottom - targetY) * currentProgress)
                listOf(Offset(width / 2f, animatedY))
            } else {
                points.mapIndexed { index, point ->
                    val normY = if (maxPrice > minPrice) {
                        ((point.effectivePrice - minPrice) / priceDelta).toFloat()
                    } else {
                        0.5f
                    }
                    val targetY = height - padBottom - (normY * usableHeight)
                    val animatedY = height - padBottom - ((height - padBottom - targetY) * currentProgress)
                    Offset(
                        x = padX + (index * stepX),
                        y = animatedY,
                    )
                }
            }

            if (isSinglePoint) {
                val pointY = coords[0].y
                // Gradient Fill Under Reference Line
                val fillPath = Path().apply {
                    moveTo(padX, pointY)
                    lineTo(width - padX, pointY)
                    lineTo(width - padX, height - padBottom)
                    lineTo(padX, height - padBottom)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.28f),
                            NeonCyan.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                        startY = pointY,
                        endY = height - padBottom,
                    ),
                )
                // Clean Straight Neon Reference Line
                drawLine(
                    color = NeonCyan,
                    start = Offset(padX, pointY),
                    end = Offset(width - padX, pointY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            } else if (coords.size >= 2) {
                // 3. Smooth Cubic Spline Path
                val strokePath = Path()
                val fillPath = Path()

                strokePath.moveTo(coords[0].x, coords[0].y)
                fillPath.moveTo(coords[0].x, coords[0].y)

                for (i in 0 until coords.size - 1) {
                    val p0 = coords[i]
                    val p1 = coords[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y

                    strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }

                fillPath.lineTo(coords.last().x, height - padBottom)
                fillPath.lineTo(coords.first().x, height - padBottom)
                fillPath.close()

                // Gradient Area Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.32f),
                            NeonCyan.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        startY = padTop,
                        endY = height - padBottom,
                    ),
                )

                // Neon Line Stroke
                drawPath(
                    path = strokePath,
                    color = NeonCyan,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
                )
            }

            // 4. Draw Small Data Dots
            coords.forEachIndexed { index, coord ->
                val isSelected = index == selectedIndex
                if (isSelected) {
                    // Vertical guideline at selected point
                    drawLine(
                        color = NeonCyan.copy(alpha = 0.6f),
                        start = Offset(coord.x, padTop),
                        end = Offset(coord.x, height - padBottom),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = dashEffect,
                    )

                    // Outer glowing circle
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = coord,
                    )
                    // Inner ring
                    drawCircle(
                        color = NeonCyan,
                        radius = 6.dp.toPx(),
                        center = coord,
                    )
                    // Center dot
                    drawCircle(
                        color = SlateDarkBackground,
                        radius = 2.5.dp.toPx(),
                        center = coord,
                    )
                } else {
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.8f),
                        radius = 3.dp.toPx(),
                        center = coord,
                    )
                }
            }
        }

        // 5. Floating Interactive Neon Tooltip
        val activePoint = points.getOrNull(selectedIndex)
        if (activePoint != null) {
            val stepPercent = if (points.size > 1) selectedIndex.toFloat() / (points.size - 1) else 0.5f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = if (stepPercent < 0.35f) Alignment.TopStart else if (stepPercent > 0.65f) Alignment.TopEnd else Alignment.TopCenter,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SlateDarkBackground,
                    border = BorderStroke(1.dp, NeonCyan),
                    shadowElevation = 6.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = activePoint.effectiveDate.ifBlank { activePoint.date },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                        )
                        if (!activePoint.storeName.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• ${activePoint.storeName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₺${activePoint.effectivePrice.formatPrice()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                        )
                    }
                }
            }
        }
    }
}

private fun Double.formatOneDecimal(): String {
    val absVal = kotlin.math.abs(this)
    val intPart = absVal.toInt()
    val fracPart = ((absVal - intPart) * 10).toInt()
    return "$intPart.$fracPart"
}

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText
