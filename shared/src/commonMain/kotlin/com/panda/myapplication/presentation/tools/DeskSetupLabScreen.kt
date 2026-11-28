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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.presentation.components.FluidInteractiveSlider
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.components.HardwarePickerDialog
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.LocalAppSettings
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.formatLengthMm
import org.koin.compose.koinInject
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

private data class ResolutionSpec(
    val label: String,
    val widthPx: Int,
    val heightPx: Int,
    val gpuLoadPercent: Int,
    val isUltrawide: Boolean = false,
)

private val RESOLUTIONS = listOf(
    ResolutionSpec("1080p (FHD)", 1920, 1080, 100),
    ResolutionSpec("1440p (QHD)", 2560, 1440, 177),
    ResolutionSpec("UWQHD (3440x1440)", 3440, 1440, 238, isUltrawide = true),
    ResolutionSpec("4K (UHD)", 3840, 2160, 400),
)

private val SCREEN_SIZES_16_9 = listOf(24.0, 24.5, 27.0, 31.5, 42.0)
private val SCREEN_SIZES_21_9 = listOf(29.0, 34.0, 38.0, 45.0)

private data class AudioCodecOption(
    val nameTr: String,
    val nameEn: String,
    val maxCleanImpedanceOhm: Int,
    val vrmsRms: Double,
    val vrmsOutput: String,
) {
    fun localizedName(isTr: Boolean): String = if (isTr) nameTr else nameEn
}

private val AUDIO_CODECS = listOf(
    AudioCodecOption("Realtek ALC897 (Giriş/Orta Anakart)", "Realtek ALC897 (Mainstream Motherboard)", 60, 1.1, "1.1 Vrms"),
    AudioCodecOption("Realtek ALC1220 (Üst Segment Anakart)", "Realtek ALC1220 (Premium Motherboard)", 150, 2.0, "2.0 Vrms"),
    AudioCodecOption("ALC4080 + Savitech SV3H712 (Amiral DAC)", "ALC4080 + Savitech SV3H712 (Flagship DAC)", 280, 2.8, "2.8 Vrms"),
)

@Composable
fun DeskSetupLabScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    hardwareRepository: HardwareRepository = koinInject(),
    activeBuildRepository: ActiveBuildRepository = koinInject(),
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val lengthUnit = LocalAppSettings.current.lengthUnit
    val activeBuild by activeBuildRepository.activeBuild.collectAsState()

    var selectedResIdx by remember { mutableIntStateOf(1) } // 1440p
    var selectedScreenSizeInch by remember { mutableStateOf(27.0) }
    var selectedMonitorName by remember { mutableStateOf<String?>(null) }

    var selectedPollingHz by remember { mutableIntStateOf(4000) }
    var selectedMouseName by remember { mutableStateOf<String?>(null) }

    var rapidTriggerTravelMmTenths by remember { mutableIntStateOf(12) } // 1.2mm press depth simulated
    var selectedMinActuationMm by remember { mutableStateOf(0.1) }
    var selectedKeyboardName by remember { mutableStateOf<String?>(null) }

    var selectedImpedanceOhm by remember { mutableIntStateOf(80) }
    var selectedHeadsetSensitivityDbMw by remember { mutableIntStateOf(98) }
    var selectedHeadsetName by remember { mutableStateOf<String?>(null) }
    var selectedCodecIdx by remember { mutableIntStateOf(0) }

    var activePickerCategory by remember { mutableStateOf<HardwareCategory?>(null) }

    activePickerCategory?.let { cat ->
        HardwarePickerDialog(
            title = if (isTr) "Katalogdan Ekipman Seçin" else "Select Gear from Catalog",
            targetCategories = listOf(cat),
            hardwareRepository = hardwareRepository,
            activeBuild = activeBuild,
            onDismiss = { activePickerCategory = null },
            onSelectItem = { item ->
                when (val s = item.spec) {
                    is MonitorSpec -> {
                        selectedMonitorName = item.displayName
                        if (s.screenSizeInch > 10.0) selectedScreenSizeInch = s.screenSizeInch
                        selectedResIdx = when {
                            s.resolution.contains("3440") -> 2
                            s.resolution.contains("3840") || s.resolution.contains("4K", true) -> 3
                            s.resolution.contains("2560") || s.resolution.contains("1440") || s.resolution.contains("QHD", true) -> 1
                            else -> 0
                        }
                    }
                    is MouseSpec -> {
                        selectedMouseName = item.displayName
                        val hz = s.pollingRateHz
                        selectedPollingHz = when {
                            hz >= 8000 -> 8000
                            hz >= 4000 -> 4000
                            hz >= 2000 -> 2000
                            else -> 1000
                        }
                    }
                    is KeyboardSpec -> {
                        selectedKeyboardName = item.displayName
                        selectedMinActuationMm = s.minActuationMm.takeIf { it > 0.0 } ?: if (s.isHallEffect) 0.1 else 2.0
                    }
                    is HeadsetSpec -> {
                        selectedHeadsetName = item.displayName
                        if (s.impedanceOhm > 0) selectedImpedanceOhm = s.impedanceOhm
                    }
                    is MotherboardSpec -> {
                        val ac = s.audioCodec?.uppercase().orEmpty()
                        selectedCodecIdx = when {
                            ac.contains("4080") || ac.contains("SAVITECH") || ac.contains("ESS") -> 2
                            ac.contains("1220") || ac.contains("1200") || ac.contains("4082") -> 1
                            else -> 0
                        }
                    }
                    else -> Unit
                }
                activePickerCategory = null
            },
        )
    }

    val res = RESOLUTIONS[selectedResIdx]
    val validScreenSizes = if (res.isUltrawide) SCREEN_SIZES_21_9 else SCREEN_SIZES_16_9
    val effectiveInch = if (selectedMonitorName != null) {
        selectedScreenSizeInch
    } else if (selectedScreenSizeInch in validScreenSizes) {
        selectedScreenSizeInch
    } else {
        if (res.isUltrawide) 34.0 else 27.0
    }

    val diagonalPixels = sqrt((res.widthPx.toDouble() * res.widthPx + res.heightPx.toDouble() * res.heightPx))
    val ppi = (diagonalPixels / effectiveInch.coerceAtLeast(10.0)).roundToInt()
    val idealDistanceCm = ((3438.0 / ppi.coerceAtLeast(40)) * 2.54).roundToInt().coerceIn(45, 120)
    val idealDistanceFormatted = if (lengthUnit == LengthUnit.IMPERIAL) {
        "~${((idealDistanceCm / 2.54) * 10).roundToInt() / 10.0} in"
    } else {
        "~$idealDistanceCm cm"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = strings.drawerDeskTitle,
                subtitle = strings.drawerDeskSubtitle,
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
                        SectionHeaderWithPicker(
                            title = strings.deskMonitorTitle,
                            selectedItemName = selectedMonitorName,
                            accentColor = NeonCyan,
                            isTr = isTr,
                            onPickClick = { activePickerCategory = HardwareCategory.MONITOR },
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            RESOLUTIONS.forEachIndexed { idx, item ->
                                FilterChip(
                                    selected = idx == selectedResIdx,
                                    onClick = {
                                        selectedResIdx = idx
                                        selectedMonitorName = null
                                        val nextSizes = if (item.isUltrawide) SCREEN_SIZES_21_9 else SCREEN_SIZES_16_9
                                        if (selectedScreenSizeInch !in nextSizes) {
                                            selectedScreenSizeInch = if (item.isUltrawide) 34.0 else 27.0
                                        }
                                    },
                                    label = { Text(item.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonEmerald,
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
                            validScreenSizes.forEach { inch ->
                                FilterChip(
                                    selected = effectiveInch == inch && selectedMonitorName == null,
                                    onClick = {
                                        selectedMonitorName = null
                                        selectedScreenSizeInch = inch
                                    },
                                    label = { Text("${inch}\" " + strings.deskScreenSuffix) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonCyan,
                                    ),
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(strings.deskPixelDensity, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "$ppi PPI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        ppi >= 105 -> NeonEmerald
                                        ppi >= 88 -> NeonCyan
                                        else -> NeonAmber
                                    },
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    strings.deskViewingDistance,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = idealDistanceFormatted,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End,
                            ) {
                                Text(
                                    strings.deskGpuPixelLoad,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.End,
                                )
                                Text(
                                    text = if (isTr) "%${res.gpuLoadPercent}" else "${res.gpuLoadPercent}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (res.gpuLoadPercent >= 238) NeonAmber else NeonEmerald,
                                )
                            }
                        }

                        val formattedLoadPct = if (isTr) "%${res.gpuLoadPercent}" else "${res.gpuLoadPercent}%"
                        Text(
                            text = when {
                                ppi >= 135 -> strings.deskPpiRetina
                                    .replaceFirst("%d", ppi.toString())
                                    .replace("%%%d", formattedLoadPct)
                                    .replace("%%d", formattedLoadPct)
                                    .replaceFirst("%d%", "${res.gpuLoadPercent}%")
                                    .replaceFirst("%d", res.gpuLoadPercent.toString())
                                ppi >= 100 -> strings.deskPpiBalanced.replaceFirst("%d", ppi.toString())
                                else -> strings.deskPpiLow
                                    .replaceFirst("%d", ppi.toString())
                                    .replaceFirst("%s", "${effectiveInch}\"")
                                    .replaceFirst("%s", res.label)
                                    .replaceFirst("%d", idealDistanceCm.toString())
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
                        SectionHeaderWithPicker(
                            title = strings.deskMouseTitle,
                            selectedItemName = selectedMouseName,
                            accentColor = ElectricBlue,
                            isTr = isTr,
                            onPickClick = { activePickerCategory = HardwareCategory.MOUSE },
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(1000, 2000, 4000, 8000).forEach { hz ->
                                val selected = selectedPollingHz == hz
                                Surface(
                                    onClick = {
                                        selectedMouseName = null
                                        selectedPollingHz = hz
                                    },
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
                        val irqPerSec = selectedPollingHz
                        val minSaturationDpi = when (selectedPollingHz) {
                            8000 -> 3200
                            4000 -> 1600
                            2000 -> 1200
                            else -> 800
                        }
                        val cpuOverheadText = when (selectedPollingHz) {
                            1000 -> strings.deskMouse1k
                            2000 -> strings.deskMouse2k
                            4000 -> strings.deskMouse4k
                            else -> strings.deskMouse8k
                        }

                        Text(
                            text = "${strings.deskReportInterval.replace("%s", intervalMs.toString())} (${irqPerSec} IRQ/s • Min ${minSaturationDpi} DPI)  •  $cpuOverheadText",
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
                        SectionHeaderWithPicker(
                            title = strings.deskKeyboardTitle,
                            selectedItemName = selectedKeyboardName,
                            accentColor = NeonAmber,
                            isTr = isTr,
                            onPickClick = { activePickerCategory = HardwareCategory.KEYBOARD },
                        )

                        val pressDepthMm = rapidTriggerTravelMmTenths / 10.0
                        val dispDepth = formatLengthMm(pressDepthMm.roundToInt(), lengthUnit).let {
                            if (lengthUnit == LengthUnit.METRIC) "$pressDepthMm mm" else "${((pressDepthMm / 25.4) * 100).roundToInt() / 100.0} in"
                        }
                        Text(
                            text = strings.deskKeyDepth.replace("%s mm", dispDepth).replace("%s", pressDepthMm.toString()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        FluidInteractiveSlider(
                            value = rapidTriggerTravelMmTenths.toFloat(),
                            onValueChange = { rapidTriggerTravelMmTenths = it.roundToInt() },
                            valueRange = 1f..40f,
                            accentColor = NeonCyan,
                        )

                        val mechActuated = pressDepthMm >= 2.0
                        // Only compute reset lift distance when the mechanical switch has actually actuated (>= 2.0mm)
                        val mechResetDistMm = if (mechActuated) ((pressDepthMm - 1.8) * 10).roundToInt() / 10.0 else 0.0
                        val hallThreshold = selectedMinActuationMm.coerceIn(0.1, 1.5)
                        val hallActuated = pressDepthMm >= hallThreshold

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
                                        text = if (mechActuated) {
                                            strings.deskMechResetDelay.replace("%s", mechResetDistMm.toString())
                                        } else {
                                            if (isTr) "Tetiklenme için ${(20 - rapidTriggerTravelMmTenths) / 10.0} mm daha basılmalı"
                                            else "Press ${(20 - rapidTriggerTravelMmTenths) / 10.0} mm further to actuate (2.0mm)"
                                        },
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

            // 4. Headset Impedance, Sensitivity & Motherboard Audio Codec Matcher
            item {
                FluidOrganicCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = NeonEmerald,
                    secondaryColor = NeonCyan,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        SectionHeaderWithPicker(
                            title = strings.deskAudioTitle,
                            selectedItemName = selectedHeadsetName,
                            accentColor = NeonEmerald,
                            isTr = isTr,
                            onPickClick = { activePickerCategory = HardwareCategory.HEADSET },
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            listOf(16, 32, 80, 250, 300, 600).forEach { ohm ->
                                val selected = selectedImpedanceOhm == ohm
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedHeadsetName = null
                                        selectedImpedanceOhm = ohm
                                    },
                                    label = { Text("$ohm Ω") },
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
                            listOf(93 to (if (isTr) "93 dB/mW (Planar/Zor)" else "93 dB/mW (Planar/Low)"), 98 to "98 dB/mW (Stüdyo)", 106 to (if (isTr) "106 dB/mW (Yüksek)" else "106 dB/mW (High)")).forEach { (sens, label) ->
                                FilterChip(
                                    selected = selectedHeadsetSensitivityDbMw == sens,
                                    onClick = { selectedHeadsetSensitivityDbMw = sens },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonEmerald.copy(alpha = 0.18f),
                                        selectedLabelColor = NeonEmerald,
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
                        // Real acoustic SPL headroom calculation: P_mW = (V_rms^2 / R) * 1000; SPL_max = Sens + 10*log10(P_mW)
                        val maxPowerMw = ((activeCodec.vrmsRms * activeCodec.vrmsRms) / selectedImpedanceOhm.coerceAtLeast(8)) * 1000.0
                        val peakSplDb = (selectedHeadsetSensitivityDbMw + 10.0 * kotlin.math.log10(maxPowerMw.coerceAtLeast(0.01))).roundToInt()
                        // Audiophile clean dynamic headroom target is >= 110 dB SPL
                        val needsDac = peakSplDb < 110 || selectedImpedanceOhm > activeCodec.maxCleanImpedanceOhm
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
                                        strings.deskAudioDacNeeded
                                            .replaceFirst("%d", selectedImpedanceOhm.toString())
                                            .replaceFirst("%s", activeCodecName)
                                    } else {
                                        strings.deskAudioDacOk.replaceFirst("%d", selectedImpedanceOhm.toString())
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (needsDac) NeonRose else NeonEmerald,
                                )
                                Text(
                                    text = if (isTr) {
                                        "Maks. Güç: ${(maxPowerMw * 10).roundToInt() / 10.0} mW • Tepe Ses Basıncı: ~$peakSplDb dB SPL (Hedef ≥110 dB)"
                                    } else {
                                        "Max Power: ${(maxPowerMw * 10).roundToInt() / 10.0} mW • Peak Headroom: ~$peakSplDb dB SPL (Target ≥110 dB)"
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

@Composable
private fun SectionHeaderWithPicker(
    title: String,
    selectedItemName: String?,
    accentColor: Color,
    isTr: Boolean,
    onPickClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = accentColor,
            )
            if (!selectedItemName.isNullOrBlank()) {
                Text(
                    text = selectedItemName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
            onClick = onPickClick,
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(13.dp))
                Text(
                    text = if (isTr) "Katalog" else "Catalog",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            }
        }
    }
}
