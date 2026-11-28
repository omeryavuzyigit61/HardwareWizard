package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.engine.MemoryChannelMode
import com.panda.myapplication.domain.engine.RamLatencyEngine
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.presentation.components.FluidInteractiveSlider
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.components.HardwarePickerDialog
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.util.LocalAppStrings
import org.koin.compose.koinInject
import kotlin.math.roundToInt

private data class RamPresetConfig(
    val labelTr: String,
    val labelEn: String,
    val gen: DdrGeneration,
    val freqMts: Int,
    val casLatency: Int,
) {
    fun localized(isTr: Boolean): String = if (isTr) labelTr else labelEn
}

private val RAM_PRESETS = listOf(
    RamPresetConfig("DDR5 6000 CL30 (AMD Tatlı Nokta)", "DDR5 6000 CL30 (AMD Sweet-Spot)", DdrGeneration.DDR5, 6000, 30),
    RamPresetConfig("DDR5 6000 CL28 (Düşük Gecikme)", "DDR5 6000 CL28 (Tight Timings)", DdrGeneration.DDR5, 6000, 28),
    RamPresetConfig("DDR5 6400 CL32", "DDR5 6400 CL32", DdrGeneration.DDR5, 6400, 32),
    RamPresetConfig("DDR5 7200 CL34 (Intel XMP)", "DDR5 7200 CL34 (Intel XMP)", DdrGeneration.DDR5, 7200, 34),
    RamPresetConfig("DDR5 8000 CL38 (CUDIMM/OC)", "DDR5 8000 CL38 (CUDIMM/OC)", DdrGeneration.DDR5, 8000, 38),
    RamPresetConfig("DDR4 3600 CL16 (AM4 Gear 1)", "DDR4 3600 CL16 (AM4 Gear 1)", DdrGeneration.DDR4, 3600, 16),
    RamPresetConfig("DDR4 3200 CL16", "DDR4 3200 CL16", DdrGeneration.DDR4, 3200, 16),
)

@Composable
fun RamLatencyLabScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    hardwareRepository: HardwareRepository = koinInject(),
    activeBuildRepository: ActiveBuildRepository = koinInject(),
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val activeBuild by activeBuildRepository.activeBuild.collectAsState()
    val wizardRamSpec = activeBuild.ram?.spec as? RamSpec

    var genA by remember { mutableStateOf(DdrGeneration.DDR5) }
    var freqA by remember { mutableIntStateOf(6000) }
    var clA by remember { mutableIntStateOf(30) }
    var chA by remember { mutableStateOf(MemoryChannelMode.DUAL) }
    var catalogNameA by remember { mutableStateOf<String?>(null) }

    var genB by remember { mutableStateOf(DdrGeneration.DDR5) }
    var freqB by remember { mutableIntStateOf(6400) }
    var clB by remember { mutableIntStateOf(32) }
    var chB by remember { mutableStateOf(MemoryChannelMode.DUAL) }
    var catalogNameB by remember { mutableStateOf<String?>(null) }

    var pickingForKitA by remember { mutableStateOf<Boolean?>(null) }

    pickingForKitA?.let { isKitA ->
        HardwarePickerDialog(
            title = if (isTr) "Katalogdan RAM Kiti Seçin" else "Select RAM Kit from Catalog",
            targetCategories = listOf(HardwareCategory.RAM),
            hardwareRepository = hardwareRepository,
            activeBuild = activeBuild,
            onDismiss = { pickingForKitA = null },
            onSelectItem = { item ->
                val rs = item.spec as? RamSpec
                if (rs != null) {
                    val ch = when {
                        rs.moduleCount <= 1 -> MemoryChannelMode.SINGLE
                        rs.moduleCount >= 4 -> MemoryChannelMode.QUAD
                        else -> MemoryChannelMode.DUAL
                    }
                    val (safeF, safeCl) = RamLatencyEngine.coerceSpec(rs.generation, rs.speed.mhz, rs.casLatency)
                    if (isKitA) {
                        genA = rs.generation
                        freqA = safeF
                        clA = safeCl
                        chA = ch
                        catalogNameA = item.displayName
                    } else {
                        genB = rs.generation
                        freqB = safeF
                        clB = safeCl
                        chB = ch
                        catalogNameB = item.displayName
                    }
                }
                pickingForKitA = null
            },
        )
    }

    val analysisA = remember(genA, freqA, clA, chA) {
        RamLatencyEngine.analyzeKit(genA, freqA, clA, chA)
    }
    val analysisB = remember(genB, freqB, clB, chB) {
        RamLatencyEngine.analyzeKit(genB, freqB, clB, chB)
    }
    val verdict = remember(analysisA, analysisB) {
        RamLatencyEngine.compare(analysisA, analysisB)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = strings.drawerRamTitle,
                subtitle = strings.drawerRamSubtitle,
                icon = "MEMORY",
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
            if (wizardRamSpec != null && activeBuild.ram != null) {
                item {
                    Surface(
                        onClick = {
                            val ch = if (wizardRamSpec.moduleCount <= 1) MemoryChannelMode.SINGLE else MemoryChannelMode.DUAL
                            val (safeF, safeCl) = RamLatencyEngine.coerceSpec(wizardRamSpec.generation, wizardRamSpec.speed.mhz, wizardRamSpec.casLatency)
                            genA = wizardRamSpec.generation
                            freqA = safeF
                            clA = safeCl
                            chA = ch
                            catalogNameA = activeBuild.ram?.displayName
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = NeonCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTr) "Sihirbazdaki Belleği Kit A'ya Yükle" else "Load Wizard RAM into Kit A",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                )
                                Text(
                                    text = activeBuild.ram?.displayName.orEmpty(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            // Kit A Card
            item {
                RamKitConfiguratorCard(
                    title = strings.ramKitA,
                    selectedCatalogName = catalogNameA,
                    accentColor = NeonCyan,
                    generation = genA,
                    freqMts = analysisA.freqMts,
                    casLatency = analysisA.casLatency,
                    channelMode = chA,
                    trueLatencyNs = analysisA.trueLatencyNs,
                    bandwidthGbs = analysisA.peakBandwidthGbs,
                    isTr = isTr,
                    onPickFromCatalog = { pickingForKitA = true },
                    onGenChange = { newGen ->
                        val (f, c) = if (newGen == DdrGeneration.DDR4) 3600 to 16 else 6000 to 30
                        genA = newGen
                        freqA = f
                        clA = c
                        catalogNameA = null
                    },
                    onFreqChange = {
                        freqA = it
                        catalogNameA = null
                    },
                    onClChange = {
                        clA = it
                        catalogNameA = null
                    },
                    onChannelChange = { chA = it },
                )
            }

            // Kit B Card
            item {
                RamKitConfiguratorCard(
                    title = strings.ramKitB,
                    selectedCatalogName = catalogNameB,
                    accentColor = NeonEmerald,
                    generation = genB,
                    freqMts = analysisB.freqMts,
                    casLatency = analysisB.casLatency,
                    channelMode = chB,
                    trueLatencyNs = analysisB.trueLatencyNs,
                    bandwidthGbs = analysisB.peakBandwidthGbs,
                    isTr = isTr,
                    onPickFromCatalog = { pickingForKitA = false },
                    onGenChange = { newGen ->
                        val (f, c) = if (newGen == DdrGeneration.DDR4) 3200 to 16 else 6400 to 32
                        genB = newGen
                        freqB = f
                        clB = c
                        catalogNameB = null
                    },
                    onFreqChange = {
                        freqB = it
                        catalogNameB = null
                    },
                    onClChange = {
                        clB = it
                        catalogNameB = null
                    },
                    onChannelChange = { chB = it },
                )
            }

            // Head-to-Head Verdict & Dynamic Platform Warnings
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Rounded.Balance, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = strings.ramGuideTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp,
                            )
                        }

                        val trueLatencyA = analysisA.trueLatencyNs
                        val trueLatencyB = analysisB.trueLatencyNs
                        val bandwidthA = analysisA.peakBandwidthGbs
                        val bandwidthB = analysisB.peakBandwidthGbs

                        val latencyWinner = when {
                            trueLatencyA < trueLatencyB -> strings.ramLatencyWinnerA
                                .replaceFirst("%s", trueLatencyA.toString())
                                .replaceFirst("%s", (((trueLatencyB - trueLatencyA) * 100).roundToInt() / 100.0).toString())
                            trueLatencyB < trueLatencyA -> strings.ramLatencyWinnerB
                                .replaceFirst("%s", trueLatencyB.toString())
                                .replaceFirst("%s", (((trueLatencyA - trueLatencyB) * 100).roundToInt() / 100.0).toString())
                            else -> strings.ramLatencyEqual.replaceFirst("%s", trueLatencyA.toString())
                        }

                        val bwWinner = when {
                            bandwidthA > bandwidthB -> strings.ramBwWinnerA
                                .replaceFirst("%s", bandwidthA.toString())
                                .replaceFirst("%s", (((bandwidthA - bandwidthB) * 10).roundToInt() / 10.0).toString())
                            bandwidthB > bandwidthA -> strings.ramBwWinnerB
                                .replaceFirst("%s", bandwidthB.toString())
                                .replaceFirst("%s", (((bandwidthB - bandwidthA) * 10).roundToInt() / 10.0).toString())
                            else -> strings.ramBwEqual.replaceFirst("%s", bandwidthA.toString())
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

                        // Genuine Dynamic Platform Advice (DDR4 vs AM5 1:1 vs 1:2 Trap vs CUDIMM vs Single-Channel)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonAmber.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Rounded.Psychology, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(17.dp))
                                    Text(
                                        text = if (isTr) verdict.platformTitleTr else verdict.platformTitleEn,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = NeonAmber,
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isTr) verdict.platformAdviceTr else verdict.platformAdviceEn,
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Rounded.Functions, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(17.dp))
                                    Text(
                                        text = strings.ramFormulaTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan,
                                    )
                                }
                                Text(
                                    text = if (isTr) {
                                        "Gerçek İlk Kelime Gecikmesi (ns) = (CL × 2000) / Frekans (MT/s)\nTepe Bant Genişliği (GB/s) = (Frekans × 8B × Kanal Sayısı) / 1000"
                                    } else {
                                        "True First-Word Latency (ns) = (CL × 2000) / Frequency (MT/s)\nPeak Bandwidth (GB/s) = (Frequency × 8B × Channels) / 1000"
                                    },
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
    selectedCatalogName: String?,
    accentColor: Color,
    generation: DdrGeneration,
    freqMts: Int,
    casLatency: Int,
    channelMode: MemoryChannelMode,
    trueLatencyNs: Double,
    bandwidthGbs: Double,
    isTr: Boolean,
    onPickFromCatalog: () -> Unit,
    onGenChange: (DdrGeneration) -> Unit,
    onFreqChange: (Int) -> Unit,
    onClChange: (Int) -> Unit,
    onChannelChange: (MemoryChannelMode) -> Unit,
) {
    val strings = LocalAppStrings.current
    val fRange = RamLatencyEngine.freqRangeFor(generation)
    val cRange = RamLatencyEngine.clRangeFor(generation)

    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        containerColor = MaterialTheme.colorScheme.surface,
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
                    letterSpacing = 1.sp,
                )
                if (!selectedCatalogName.isNullOrBlank()) {
                    Text(
                        text = selectedCatalogName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Surface(
                onClick = onPickFromCatalog,
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                    Text(
                        text = if (isTr) "Katalogdan Seç" else "Pick Kit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        // Generation + Channel Mode Switchers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(DdrGeneration.DDR5, DdrGeneration.DDR4).forEach { gen ->
                FilterChip(
                    selected = generation == gen,
                    onClick = { onGenChange(gen) },
                    label = { Text(gen.name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.22f),
                        selectedLabelColor = accentColor,
                    ),
                )
            }
            MemoryChannelMode.entries.forEach { mode ->
                FilterChip(
                    selected = channelMode == mode,
                    onClick = { onChannelChange(mode) },
                    label = { Text(mode.localized(isTr), style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor.copy(alpha = 0.16f),
                        selectedLabelColor = accentColor,
                    ),
                )
            }
        }

        // Quick Presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            RAM_PRESETS.forEach { preset ->
                val isSelected = generation == preset.gen && freqMts == preset.freqMts && casLatency == preset.casLatency
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onGenChange(preset.gen)
                        onFreqChange(preset.freqMts)
                        onClChange(preset.casLatency)
                    },
                    label = { Text(preset.localized(isTr), style = MaterialTheme.typography.labelSmall) },
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
                text = "${generation.name} • ${channelMode.localized(isTr)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }
        FluidInteractiveSlider(
            value = freqMts.toFloat(),
            onValueChange = { raw ->
                val step = ((raw / 200f).roundToInt() * 200).coerceIn(fRange.first, fRange.last)
                onFreqChange(step)
            },
            valueRange = fRange.first.toFloat()..fRange.last.toFloat(),
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
            Text(
                text = "CL ${cRange.first}–${cRange.last}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FluidInteractiveSlider(
            value = casLatency.toFloat(),
            onValueChange = { onClChange(it.roundToInt().coerceIn(cRange.first, cRange.last)) },
            valueRange = cRange.first.toFloat()..cRange.last.toFloat(),
            accentColor = accentColor,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(Spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            Spacer(modifier = Modifier.width(Spacing.small))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
            ) {
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
