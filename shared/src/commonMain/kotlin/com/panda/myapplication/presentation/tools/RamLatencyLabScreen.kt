package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import kotlin.math.roundToInt

private data class RamPresetConfig(
    val label: String,
    val freqMts: Int,
    val casLatency: Int,
)

private val RAM_PRESETS = listOf(
    RamPresetConfig("DDR5 6000 CL30 (AMD Sweet-Spot)", 6000, 30),
    RamPresetConfig("DDR5 6000 CL36", 6000, 36),
    RamPresetConfig("DDR5 6400 CL32", 6400, 32),
    RamPresetConfig("DDR5 7200 CL34 (Intel XMP)", 7200, 34),
    RamPresetConfig("DDR5 8000 CL38 (CUDIMM/OC)", 8000, 38),
    RamPresetConfig("DDR4 3600 CL16", 3600, 16),
    RamPresetConfig("DDR4 3200 CL16", 3200, 16),
)

@Composable
fun RamLatencyLabScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    var freqA by remember { mutableIntStateOf(6000) }
    var clA by remember { mutableIntStateOf(30) }

    var freqB by remember { mutableIntStateOf(6400) }
    var clB by remember { mutableIntStateOf(32) }

    val trueLatencyA = ((clA * 2000.0) / freqA.coerceAtLeast(1) * 100.0).roundToInt() / 100.0
    val bandwidthA = ((freqA * 16.0) / 1000.0 * 10.0).roundToInt() / 10.0

    val trueLatencyB = ((clB * 2000.0) / freqB.coerceAtLeast(1) * 100.0).roundToInt() / 100.0
    val bandwidthB = ((freqB * 16.0) / 1000.0 * 10.0).roundToInt() / 10.0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = LocalAppStrings.current.drawerRamTitle,
                subtitle = LocalAppStrings.current.drawerRamSubtitle,
                icon = "⚡",
                onNavigateBack = onNavigateBack,
                onOpenDrawer = onOpenDrawer,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            // Kit A Card
            item {
                RamKitConfiguratorCard(
                    title = strings.ramKitA,
                    accentColor = NeonCyan,
                    freqMts = freqA,
                    casLatency = clA,
                    trueLatencyNs = trueLatencyA,
                    bandwidthGbs = bandwidthA,
                    onFreqChange = { freqA = it },
                    onClChange = { clA = it },
                )
            }

            // Kit B Card
            item {
                RamKitConfiguratorCard(
                    title = strings.ramKitB,
                    accentColor = NeonEmerald,
                    freqMts = freqB,
                    casLatency = clB,
                    trueLatencyNs = trueLatencyB,
                    bandwidthGbs = bandwidthB,
                    onFreqChange = { freqB = it },
                    onClChange = { clB = it },
                )
            }

            // Head-to-Head Verdict & Platform Warnings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.7f)),
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        Text(
                            text = "⚖️ " + strings.ramGuideTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.sp,
                        )

                        val latencyWinner = when {
                            trueLatencyA < trueLatencyB -> strings.ramLatencyWinnerA.replace("%s", trueLatencyA.toString()).replace("%s", (((trueLatencyB - trueLatencyA) * 100).roundToInt() / 100.0).toString())
                            trueLatencyB < trueLatencyA -> strings.ramLatencyWinnerB.replace("%s", trueLatencyB.toString()).replace("%s", (((trueLatencyA - trueLatencyB) * 100).roundToInt() / 100.0).toString())
                            else -> strings.ramLatencyEqual.replace("%s", trueLatencyA.toString())
                        }

                        val bwWinner = when {
                            bandwidthA > bandwidthB -> strings.ramBwWinnerA.replace("%s", bandwidthA.toString()).replace("%s", (((bandwidthA - bandwidthB) * 10).roundToInt() / 10.0).toString())
                            bandwidthB > bandwidthA -> strings.ramBwWinnerB.replace("%s", bandwidthB.toString()).replace("%s", (((bandwidthB - bandwidthA) * 10).roundToInt() / 10.0).toString())
                            else -> strings.ramBwEqual.replace("%s", bandwidthA.toString())
                        }

                        Text(
                            text = "• ${strings.ramLatencyWinner} $latencyWinner",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "• ${strings.ramBwWinner} $bwWinner",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        // Dynamic Platform Advice
                        val hasAbove6000 = freqA > 6000 || freqB > 6000
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonAmber.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🧠 " + strings.ramZenRuleTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = NeonAmber,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.ramZenRuleDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonCyan.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📘 " + strings.ramFormulaTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                )
                                Text(
                                    text = "True Latency (ns) = (CL × 2000) / Frequency (MT/s)\nDual-Channel Bandwidth (GB/s) = (Frequency × 16) / 1000",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RamKitConfiguratorCard(
    title: String,
    accentColor: androidx.compose.ui.graphics.Color,
    freqMts: Int,
    casLatency: Int,
    trueLatencyNs: Double,
    bandwidthGbs: Double,
    onFreqChange: (Int) -> Unit,
    onClChange: (Int) -> Unit,
) {
    val strings = LocalAppStrings.current
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = accentColor,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(Spacing.small))

            // Quick Presets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                RAM_PRESETS.forEach { preset ->
                    val isSelected = freqMts == preset.freqMts && casLatency == preset.casLatency
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onFreqChange(preset.freqMts)
                            onClChange(preset.casLatency)
                        },
                        label = { Text(preset.label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = strings.ramFreqLabel.replace("%d", freqMts.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (freqMts >= 4800) "DDR5" else "DDR4",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
            com.panda.myapplication.presentation.components.FluidInteractiveSlider(
                value = freqMts.toFloat(),
                onValueChange = { onFreqChange((it / 200f).roundToInt() * 200) },
                valueRange = 2600f..8800f,
                accentColor = accentColor,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = strings.ramCasLabel.replace("%d", casLatency.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            com.panda.myapplication.presentation.components.FluidInteractiveSlider(
                value = casLatency.toFloat(),
                onValueChange = { onClChange(it.roundToInt()) },
                valueRange = 14f..46f,
                accentColor = accentColor,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(Spacing.small))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = strings.ramTrueLatency,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$trueLatencyNs ns",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (trueLatencyNs <= 10.0) NeonEmerald else accentColor,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = strings.ramBandwidth,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$bandwidthGbs GB/s",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                    )
                }
            }
        }
    }
