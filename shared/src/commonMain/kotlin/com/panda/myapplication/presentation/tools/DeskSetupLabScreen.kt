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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import kotlin.math.roundToInt
import kotlin.math.sqrt

private data class ResolutionSpec(
    val label: String,
    val widthPx: Int,
    val heightPx: Int,
    val gpuLoadPercent: Int,
)

private val RESOLUTIONS = listOf(
    ResolutionSpec("1080p (FHD)", 1920, 1080, 100),
    ResolutionSpec("1440p (QHD)", 2560, 1440, 177),
    ResolutionSpec("UWQHD (3440x1440)", 3440, 1440, 238),
    ResolutionSpec("4K (UHD)", 3840, 2160, 400),
)

private val SCREEN_SIZES_INCH = listOf(24.0, 24.5, 27.0, 31.5, 34.0)

private data class AudioCodecOption(
    val nameTr: String,
    val nameEn: String,
    val maxCleanImpedanceOhm: Int,
    val vrmsOutput: String,
) {
    fun localizedName(isTr: Boolean): String = if (isTr) nameTr else nameEn
}

private val AUDIO_CODECS = listOf(
    AudioCodecOption("Realtek ALC897 (Giriş/Orta Anakart)", "Realtek ALC897 (Mainstream Motherboard)", 60, "1.1 Vrms"),
    AudioCodecOption("Realtek ALC1220 (Üst Segment Anakart)", "Realtek ALC1220 (Premium Motherboard)", 120, "2.0 Vrms"),
    AudioCodecOption("ALC4080 + Savitech SV3H712 (Amiral Gemisi)", "ALC4080 + Savitech SV3H712 (Flagship DAC)", 250, "2.8 Vrms"),
)

@Composable
fun DeskSetupLabScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    var selectedScreenSizeIdx by remember { mutableIntStateOf(2) } // 27"
    var selectedResIdx by remember { mutableIntStateOf(1) } // 1440p
    var selectedPollingHz by remember { mutableIntStateOf(4000) }
    var rapidTriggerTravelMmTenths by remember { mutableIntStateOf(12) } // 1.2mm press depth simulated
    var selectedImpedanceOhm by remember { mutableIntStateOf(80) }
    var selectedCodecIdx by remember { mutableIntStateOf(0) }

    val screenInch = SCREEN_SIZES_INCH[selectedScreenSizeIdx]
    val res = RESOLUTIONS[selectedResIdx]
    val diagonalPixels = sqrt((res.widthPx * res.widthPx + res.heightPx * res.heightPx).toDouble())
    val ppi = (diagonalPixels / screenInch).roundToInt()
    val idealDistanceCm = ((3438.0 / ppi) * 2.54).roundToInt().coerceIn(45, 110)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = LocalAppStrings.current.drawerDeskTitle,
                subtitle = LocalAppStrings.current.drawerDeskSubtitle,
                icon = "🖱️",
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
            // 1. Monitor PPI & GPU Load Multiplier
            item {
                FluidOrganicCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = NeonCyan,
                    secondaryColor = NeonEmerald,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        Text(
                            text = strings.deskMonitorTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SCREEN_SIZES_INCH.forEachIndexed { idx, inch ->
                                FilterChip(
                                    selected = idx == selectedScreenSizeIdx,
                                    onClick = { selectedScreenSizeIdx = idx },
                                    label = { Text("${inch}\" " + strings.deskScreenSuffix) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonCyan,
                                    ),
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            RESOLUTIONS.forEachIndexed { idx, item ->
                                FilterChip(
                                    selected = idx == selectedResIdx,
                                    onClick = { selectedResIdx = idx },
                                    label = { Text(item.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonEmerald,
                                    ),
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(strings.deskPixelDensity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$ppi PPI",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        ppi >= 105 -> NeonEmerald
                                        ppi >= 88 -> NeonCyan
                                        else -> NeonAmber
                                    },
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.deskViewingDistance, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "~$idealDistanceCm cm",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(strings.deskGpuPixelLoad, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "%${res.gpuLoadPercent}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (res.gpuLoadPercent >= 238) NeonAmber else NeonEmerald,
                                )
                            }
                        }

                        Text(
                            text = when {
                                ppi >= 135 -> strings.deskPpiRetina.replace("%d", ppi.toString()).replace("%%d", res.gpuLoadPercent.toString())
                                ppi >= 100 -> strings.deskPpiBalanced.replace("%d", ppi.toString())
                                else -> strings.deskPpiLow.replace("%d", ppi.toString()).replace("%s", "${screenInch}\"").replace("%s", res.label).replace("%d", idealDistanceCm.toString())
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // 2. Mouse Polling Rate Analyzer
            item {
                FluidOrganicCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = ElectricBlue,
                    secondaryColor = NeonPurple,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        Text(
                            text = strings.deskMouseTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = ElectricBlue,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(1000, 2000, 4000, 8000).forEach { hz ->
                                val selected = selectedPollingHz == hz
                                Surface(
                                    onClick = { selectedPollingHz = hz },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, if (selected) NeonCyan else MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = "${hz}Hz",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = if (selected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "${1000.0 / hz} ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }

                        val intervalMs = 1000.0 / selectedPollingHz
                        val cpuOverheadText = when (selectedPollingHz) {
                            1000 -> strings.deskMouse1k
                            2000 -> strings.deskMouse2k
                            4000 -> strings.deskMouse4k
                            else -> strings.deskMouse8k
                        }

                        Text(
                            text = "${strings.deskReportInterval.replace("%s", intervalMs.toString())}  •  $cpuOverheadText",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedPollingHz >= 4000) NeonAmber else NeonEmerald,
                        )
                    }
                }
            }

            // 3. Keyboard Rapid Trigger Simulator
            item {
                FluidOrganicCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = NeonAmber,
                    secondaryColor = NeonRose,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        Text(
                            text = strings.deskKeyboardTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonAmber,
                        )

                        val pressDepthMm = rapidTriggerTravelMmTenths / 10.0
                        Text(
                            text = strings.deskKeyDepth.replace("%s", pressDepthMm.toString()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        com.panda.myapplication.presentation.components.FluidInteractiveSlider(
                            value = rapidTriggerTravelMmTenths.toFloat(),
                            onValueChange = { rapidTriggerTravelMmTenths = it.roundToInt() },
                            valueRange = 1f..40f,
                            accentColor = NeonCyan,
                        )

                        val mechActuated = pressDepthMm >= 2.0
                        val mechResetDistMm = if (pressDepthMm > 1.8) ((pressDepthMm - 1.8) * 10).roundToInt() / 10.0 else 0.0
                        val hallActuated = pressDepthMm >= 0.2

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (mechActuated) NeonAmber else MaterialTheme.colorScheme.outline),
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(strings.deskMechSwitch, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NeonAmber)
                                    Text(
                                        text = if (mechActuated) strings.deskMechActuated else strings.deskMechNotActuated,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (mechActuated) NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = strings.deskMechResetDelay.replace("%s", mechResetDistMm.toString()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (hallActuated) NeonEmerald else MaterialTheme.colorScheme.outline),
                                modifier = Modifier.weight(1f),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(strings.deskHallSwitch, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NeonEmerald)
                                    Text(
                                        text = if (hallActuated) strings.deskHallActuated else strings.deskHallReady,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = NeonEmerald,
                                    )
                                    Text(
                                        text = strings.deskHallResetSpeed,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Headset Impedance & Motherboard Audio Codec Matcher
            item {
                FluidOrganicCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = NeonEmerald,
                    secondaryColor = NeonCyan,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        Text(
                            text = strings.deskAudioTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonEmerald,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            listOf(16, 32, 80, 250).forEach { ohm ->
                                val selected = selectedImpedanceOhm == ohm
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedImpedanceOhm = ohm },
                                    label = { Text("$ohm Ω") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonCyan,
                                    ),
                                )
                            }
                        }

                        AUDIO_CODECS.forEachIndexed { idx, codec ->
                            val selected = idx == selectedCodecIdx
                            Surface(
                                onClick = { selectedCodecIdx = idx },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) NeonCyan.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (selected) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = codec.localizedName(isTr),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = codec.vrmsOutput,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonEmerald,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }

                        val activeCodec = AUDIO_CODECS[selectedCodecIdx]
                        val needsDac = selectedImpedanceOhm > activeCodec.maxCleanImpedanceOhm
                        val activeCodecName = activeCodec.localizedName(isTr)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (needsDac) NeonRose.copy(alpha = 0.14f) else NeonEmerald.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, if (needsDac) NeonRose else NeonEmerald),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (needsDac) {
                                        strings.deskAudioDacNeeded.replace("%d", selectedImpedanceOhm.toString()).replace("%s", activeCodecName)
                                    } else {
                                        strings.deskAudioDacOk.replace("%d", selectedImpedanceOhm.toString())
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (needsDac) NeonRose else NeonEmerald,
                                )
                                Text(
                                    text = if (needsDac) {
                                        strings.deskAudioDacNeededDesc.replace("%s", activeCodecName).replace("%s", activeCodec.vrmsOutput).replace("%d", selectedImpedanceOhm.toString())
                                    } else {
                                        strings.deskAudioDacOkDesc.replace("%s", activeCodecName).replace("%s", activeCodec.vrmsOutput).replace("%d", selectedImpedanceOhm.toString())
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
