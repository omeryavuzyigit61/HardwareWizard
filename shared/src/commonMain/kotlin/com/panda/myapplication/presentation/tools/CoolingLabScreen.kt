package com.panda.myapplication.presentation.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.engine.HardwareSearchEngine
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.presentation.components.FluidInteractiveSlider
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import kotlin.math.roundToInt

private val IceBlueTempColor = Color(0xFF00E5FF)
private val SafeGreenTempColor = Color(0xFF00E676)
private val WarningYellowTempColor = Color(0xFFFFD600)
private val DangerRedTempColor = Color(0xFFFF1744)

private data class CpuThermalPreset(
    val id: String,
    val name: String,
    val socket: String,
    val baseTdpWatts: Int,
    val peakPptMtpWatts: Int,
    val tjMaxCelsius: Int,
    val notes: String,
    val isFromCatalog: Boolean = false,
)

private data class CoolerThermalPreset(
    val id: String,
    val name: String,
    val typeLabel: String,
    val maxDissipationWatts: Int,
    val noiseDba: Double,
    val isFromCatalog: Boolean = false,
)

private val CPU_PRESETS = listOf(
    CpuThermalPreset("preset_7500f", "AMD Ryzen 5 7500F", "AM5", 65, 88, 95, "6 Çekirdek • Düşük PPT, stok veya tek kule soğutucu ile serin çalışır."),
    CpuThermalPreset("preset_7800x3d", "AMD Ryzen 7 7800X3D", "AM5", 120, 162, 89, "3D V-Cache katmanı nedeniyle ısı yoğunluğu yüksektir (TjMax 89°C)."),
    CpuThermalPreset("preset_9800x3d", "AMD Ryzen 7 9800X3D", "AM5", 120, 162, 95, "2. Nesil V-Cache altta konumlandığı için 7800X3D'den ~6°C daha serindir."),
    CpuThermalPreset("preset_9950x", "AMD Ryzen 9 9950X", "AM5", 170, 230, 95, "16 Çekirdek • Ağır render yükünde 230W PPT çeker, 360mm AIO önerilir."),
    CpuThermalPreset("preset_14600k", "Intel Core i5-14600K", "LGA1700", 125, 181, 100, "PL2 tam yükte 181W MTP çeker; çift kule hava veya 240mm sıvı soğutma şarttır."),
    CpuThermalPreset("preset_14700k", "Intel Core i7-14700K", "LGA1700", 125, 253, 100, "20 Çekirdek • PL2 253W MTP sınırında hava soğutucuları doygunluğa ulaştırır."),
    CpuThermalPreset("preset_14900k", "Intel Core i9-14900K", "LGA1700", 125, 285, 100, "Ekstrem termal yoğunluk • 360mm/420mm üst sınıf AIO olmadan Thermal Throttling kaçınılmazdır."),
    CpuThermalPreset("preset_285k", "Intel Core Ultra 9 285K", "LGA1851", 125, 250, 105, "Arrow Lake TSMC N3B • 14900K'ya kıyasla daha verimli, 360mm AIO ile tam stabil."),
)

private val COOLER_PRESETS = listOf(
    CoolerThermalPreset("preset_stock", "Stok Fan (Wraith Stealth / Laminar)", "Stok Hava", 95, 36.5),
    CoolerThermalPreset("preset_ak120", "Thermalright Assassin King 120 SE", "Tek Kule Hava (120mm)", 180, 25.6),
    CoolerThermalPreset("preset_pa120", "Thermalright Peerless Assassin 120 SE", "Çift Kule Hava (6 Isı Borusu)", 235, 26.5),
    CoolerThermalPreset("preset_nhd15", "Noctua NH-D15 G2", "Amiral Gemisi Çift Kule", 260, 24.8),
    CoolerThermalPreset("preset_lf240", "Arctic Liquid Freezer III 240", "240mm Sıvı Soğutma (AIO)", 250, 27.0),
    CoolerThermalPreset("preset_lf360", "Arctic Liquid Freezer III 360", "360mm Sıvı Soğutma (38mm Radyatör)", 310, 28.5),
    CoolerThermalPreset("preset_lf420", "Arctic Liquid Freezer III 420", "420mm Ekstrem AIO", 350, 29.0),
)

private fun mapCatalogCpuToThermalPreset(item: HardwareItem, spec: CpuSpec): CpuThermalPreset {
    val baseTdp = spec.tdp.watts.coerceAtLeast(35)
    val upperName = item.displayName.uppercase()
    val socketUpper = spec.socket.uppercase()
    val isAmd = socketUpper.contains("AM5") || socketUpper.contains("AM4") || item.brand.contains("AMD", ignoreCase = true)

    val peakPpt = when {
        upperName.contains("14900") || upperName.contains("13900") -> maxOf((baseTdp * 2.15).roundToInt(), 280)
        upperName.contains("285K") -> 250
        upperName.contains("14700") || upperName.contains("13700") -> maxOf((baseTdp * 1.95).roundToInt(), 253)
        upperName.contains("14600") || upperName.contains("13600") -> maxOf((baseTdp * 1.45).roundToInt(), 181)
        isAmd -> (baseTdp * 1.35).roundToInt()
        else -> (baseTdp * 1.48).roundToInt()
    }

    val tjMax = when {
        upperName.contains("7800X3D") || upperName.contains("5800X3D") || upperName.contains("5700X3D") -> 89
        socketUpper.contains("1851") -> 105
        isAmd -> 95
        else -> 100
    }

    val cacheNote = spec.l3CacheMb?.let { "${it}MB L3 Önbellek • " } ?: ""
    return CpuThermalPreset(
        id = item.id.value,
        name = item.displayName,
        socket = spec.socket,
        baseTdpWatts = baseTdp,
        peakPptMtpWatts = peakPpt,
        tjMaxCelsius = tjMax,
        notes = "Katalogdan Seçildi • ${spec.coreCount} Çekirdek / ${spec.threadCount} İzlek • ${cacheNote}Gerçek PPT/MTP: ${peakPpt}W",
        isFromCatalog = true,
    )
}

private fun mapCatalogCoolerToThermalPreset(item: HardwareItem, spec: CoolerSpec): CoolerThermalPreset {
    val estimatedWatts = when {
        spec.maxCoolingTdp.watts > 50 -> spec.maxCoolingTdp.watts
        spec.radiatorSizeMm != null && spec.radiatorSizeMm >= 420 -> 350
        spec.radiatorSizeMm != null && spec.radiatorSizeMm >= 360 -> 300
        spec.radiatorSizeMm != null && spec.radiatorSizeMm >= 280 -> 270
        spec.radiatorSizeMm != null && spec.radiatorSizeMm >= 240 -> 245
        spec.type == CoolerType.LIQUID -> 240
        else -> 195
    }
    val typeLabel = if (spec.type == CoolerType.LIQUID) {
        "${spec.radiatorSizeMm ?: 240}mm Sıvı Soğutma (AIO)"
    } else {
        "Hava Soğutma (${spec.heightMm ?: 155}mm)"
    }
    return CoolerThermalPreset(
        id = item.id.value,
        name = item.displayName,
        typeLabel = typeLabel,
        maxDissipationWatts = estimatedWatts,
        noiseDba = spec.noiseDba ?: 27.5,
        isFromCatalog = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoolingLabScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repository: HardwareRepository = koinInject()

    val catalogCpus by remember {
        repository.observeHardwareItemsByCategory(HardwareCategory.CPU).map { res ->
            (res as? DomainResult.Success)?.data.orEmpty()
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val catalogCoolers by remember {
        repository.observeHardwareItemsByCategory(HardwareCategory.COOLER).map { res ->
            (res as? DomainResult.Success)?.data.orEmpty()
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedCpuIndex by remember { mutableIntStateOf(1) } // 7800X3D default
    var customCatalogCpu by remember { mutableStateOf<CpuThermalPreset?>(null) }

    var selectedCoolerIndex by remember { mutableIntStateOf(2) } // Peerless Assassin default
    var customCatalogCooler by remember { mutableStateOf<CoolerThermalPreset?>(null) }

    var ambientTempCelsius by remember { mutableIntStateOf(24) }
    var isCpuPickerOpen by remember { mutableStateOf(false) }
    var isCoolerPickerOpen by remember { mutableStateOf(false) }

    val cpu = customCatalogCpu ?: CPU_PRESETS[selectedCpuIndex]
    val cooler = customCatalogCooler ?: COOLER_PRESETS[selectedCoolerIndex]

    // Thermal Physics Simulation
    val saturationRatio = cpu.peakPptMtpWatts.toFloat() / cooler.maxDissipationWatts.coerceAtLeast(1).toFloat()
    val headroomWatts = cooler.maxDissipationWatts - cpu.peakPptMtpWatts
    val x3dDensityOffset = when {
        cpu.name.contains("7800X3D", ignoreCase = true) || cpu.name.contains("5800X3D", ignoreCase = true) -> 7
        cpu.name.contains("9800X3D", ignoreCase = true) || cpu.name.contains("X3D", ignoreCase = true) -> 3
        else -> 0
    }

    val maxLimit = cpu.tjMaxCelsius.coerceAtLeast(60)
    val idleTemp = (ambientTempCelsius + 11 + (cpu.baseTdpWatts * 0.05)).roundToInt().coerceIn(20, maxLimit)
    val gamingWatts = (cpu.peakPptMtpWatts * 0.62).roundToInt()
    val gamingRatio = gamingWatts.toFloat() / cooler.maxDissipationWatts.toFloat()
    val calculatedGamingTemp = (ambientTempCelsius + 22 + (gamingRatio * 48f) + x3dDensityOffset).roundToInt()
    val minGaming = (idleTemp + 4).coerceAtMost(maxLimit)
    val gamingTemp = calculatedGamingTemp.coerceIn(minGaming, maxLimit)

    val renderTempRaw = (ambientTempCelsius + 26 + (saturationRatio * 62f) + x3dDensityOffset).roundToInt()
    val isThrottling = renderTempRaw >= maxLimit || saturationRatio > 1.02f
    val minRender = (gamingTemp + 2).coerceAtMost(maxLimit)
    val renderTemp = renderTempRaw.coerceIn(minRender, maxLimit)

    val statusColor by animateColorAsState(
        targetValue = when {
            isThrottling || renderTemp >= 90 -> DangerRedTempColor
            saturationRatio > 0.85f || renderTemp >= 80 -> WarningYellowTempColor
            else -> SafeGreenTempColor
        },
        animationSpec = tween(350),
        label = "statusColor",
    )
    val statusBadgeText = when {
        isThrottling -> LocalAppStrings.current.coolingThrottleStatus
        saturationRatio > 0.85f -> LocalAppStrings.current.coolingWarningStatus
        else -> LocalAppStrings.current.coolingSafeStatus
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = LocalAppStrings.current.drawerCoolingTitle,
                subtitle = LocalAppStrings.current.drawerCoolingSubtitle,
                icon = "🌡️",
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
            // 1. CPU Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(modifier = Modifier.padding(Spacing.medium)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "1. İŞLEMCİ (CPU) SEÇİMİ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp,
                            )
                            OutlinedButton(
                                onClick = { isCpuPickerOpen = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonCyan),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "🔍 ${LocalAppStrings.current.selectCpuCatalog} (${catalogCpus.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                )
                            }
                        }

                        if (customCatalogCpu != null) {
                            Spacer(modifier = Modifier.height(Spacing.small))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NeonEmerald.copy(alpha = 0.14f),
                                border = BorderStroke(1.5.dp, NeonEmerald),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ÖZEL SEÇİM (KATALOG): ${cpu.name}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = NeonEmerald,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "TDP: ${cpu.baseTdpWatts}W • Gerçek PPT/MTP: ${cpu.peakPptMtpWatts}W",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                    Surface(
                                        onClick = { customCatalogCpu = null },
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                    ) {
                                        Text(
                                            text = "Sıfırla",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonAmber,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = "Önerilen Referans İşlemciler:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CPU_PRESETS.forEachIndexed { index, item ->
                                val selected = customCatalogCpu == null && index == selectedCpuIndex
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        customCatalogCpu = null
                                        selectedCpuIndex = index
                                    },
                                    label = {
                                        Text(
                                            text = item.name,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonCyan,
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) NeonCyan else MaterialTheme.colorScheme.outline,
                                    ),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = "${cpu.socket} • Kağıt TDP: ${cpu.baseTdpWatts}W • Gerçek Tepe Güç (PPT/MTP): ${cpu.peakPptMtpWatts}W • TjMax: ${cpu.tjMaxCelsius}°C",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = cpu.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // 2. Cooler Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(modifier = Modifier.padding(Spacing.medium)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "2. SOĞUTUCU (COOLER) SEÇİMİ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp,
                            )
                            OutlinedButton(
                                onClick = { isCoolerPickerOpen = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonCyan),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "🔍 ${LocalAppStrings.current.selectCoolerCatalog} (${catalogCoolers.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                )
                            }
                        }

                        if (customCatalogCooler != null) {
                            Spacer(modifier = Modifier.height(Spacing.small))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NeonEmerald.copy(alpha = 0.14f),
                                border = BorderStroke(1.5.dp, NeonEmerald),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "ÖZEL SOĞUTUCU: ${cooler.name}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = NeonEmerald,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "${cooler.typeLabel} • ${cooler.maxDissipationWatts}W Kapasite",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                    Surface(
                                        onClick = { customCatalogCooler = null },
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                    ) {
                                        Text(
                                            text = "Sıfırla",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonAmber,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.small))
                        Text(
                            text = "Önerilen Referans Soğutucular:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            COOLER_PRESETS.forEachIndexed { index, item ->
                                val selected = customCatalogCooler == null && index == selectedCoolerIndex
                                Surface(
                                    onClick = {
                                        customCatalogCooler = null
                                        selectedCoolerIndex = index
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) NeonCyan.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "${item.typeLabel} • Gürültü: ${item.noiseDba} dBA",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            text = "${item.maxDissipationWatts}W Kapasite",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = NeonEmerald,
                                            softWrap = false,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Oda / Kasa İçi Ortam Sıcaklığı",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "$ambientTempCelsius °C",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                            )
                        }
                        com.panda.myapplication.presentation.components.FluidInteractiveSlider(
                            value = ambientTempCelsius.toFloat(),
                            onValueChange = { ambientTempCelsius = it.roundToInt() },
                            valueRange = 18f..36f,
                            accentColor = NeonAmber,
                        )
                    }
                }
            }

            // 3. Live Simulation Result Card
            item {
                FluidOrganicCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = statusColor,
                    secondaryColor = if (isThrottling) NeonRose else if (saturationRatio > 0.85f) NeonAmber else NeonCyan,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, statusColor),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = statusBadgeText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.medium))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text = "CPU Tepe Isı Yükü (PPT/MTP)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${cpu.peakPptMtpWatts} W",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NeonAmber,
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Soğutucu Kapasitesi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${cooler.maxDissipationWatts} W",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Termal Pay (Headroom)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (headroomWatts >= 0) "+${headroomWatts} W" else "${headroomWatts} W",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = statusColor,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.small))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(Spacing.small))

                    // Animated Temperature Bars with Heat Wave / Pulse Effect
                    ThermalTempRow(
                        label = "Masaüstü Boşta (Idle)",
                        tempCelsius = idleTemp,
                        tjMax = cpu.tjMaxCelsius,
                        powerWatts = 28,
                    )
                    ThermalTempRow(
                        label = "Oyun Yükü (Gaming ~%60 PPT)",
                        tempCelsius = gamingTemp,
                        tjMax = cpu.tjMaxCelsius,
                        powerWatts = gamingWatts,
                    )
                    ThermalTempRow(
                        label = "Tam Yük Render / Cinebench (%100 PPT)",
                        tempCelsius = renderTemp,
                        tjMax = cpu.tjMaxCelsius,
                        powerWatts = cpu.peakPptMtpWatts,
                    )

                    Spacer(modifier = Modifier.height(Spacing.small))

                    Text(
                        text = if (isThrottling) {
                            "❗ Mühendislik Uyarısı: ${cpu.name} tam yükte ${cpu.peakPptMtpWatts}W ısı üretirken ${cooler.name} maksimum ${cooler.maxDissipationWatts}W dağıtabilir. İşlemci ${cpu.tjMaxCelsius}°C sınırına çarpıp saat hızını düşürecektir (Thermal Throttling)."
                        } else {
                            "✅ Mühendislik Onayı: ${cooler.name}, ${cpu.name} işlemcisinin ${cpu.peakPptMtpWatts}W PPT/MTP yükünü ${headroomWatts}W ekstra güvenlik payı ile rahatça sönümler."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }

    if (isCpuPickerOpen) {
        CatalogCpuPickerSheet(
            cpus = catalogCpus,
            onDismiss = { isCpuPickerOpen = false },
            onSelectCpu = { item, spec ->
                customCatalogCpu = mapCatalogCpuToThermalPreset(item, spec)
                isCpuPickerOpen = false
            },
        )
    }

    if (isCoolerPickerOpen) {
        CatalogCoolerPickerSheet(
            coolers = catalogCoolers,
            onDismiss = { isCoolerPickerOpen = false },
            onSelectCooler = { item, spec ->
                customCatalogCooler = mapCatalogCoolerToThermalPreset(item, spec)
                isCoolerPickerOpen = false
            },
        )
    }
}

@Composable
private fun ThermalTempRow(
    label: String,
    tempCelsius: Int,
    tjMax: Int,
    powerWatts: Int,
) {
    // Dynamic Temperature Color Rules:
    // <65°C Ice Blue (#00E5FF), 65-79°C Safe Green (#00E676), 80-89°C Warning Yellow (#FFD600), 90°C+ Danger Red (#FF1744)
    val targetColor = when {
        tempCelsius >= 90 -> DangerRedTempColor
        tempCelsius >= 80 -> WarningYellowTempColor
        tempCelsius >= 65 -> SafeGreenTempColor
        else -> IceBlueTempColor
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 320),
        label = "barColorAnim",
    )

    val animatedTempFloat by animateFloatAsState(
        targetValue = tempCelsius.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "tempNumberAnim",
    )

    val targetFraction = (tempCelsius.toFloat() / tjMax.coerceAtLeast(1).toFloat()).coerceIn(0.08f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "barFractionAnim",
    )

    // Heat wave / pulse shimmer effect
    val infiniteTransition = rememberInfiniteTransition(label = "heatWave")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (tempCelsius >= 80) 950 else 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulsePhase",
    )

    val displayedTemp = animatedTempFloat.roundToInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$label (~${powerWatts}W)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$displayedTemp °C / $tjMax °C",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = animatedColor,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val fluidColors = remember(targetColor) {
            when {
                tempCelsius >= 90 -> listOf(Color(0xFFFB7185), NeonRose, DangerRedTempColor)
                tempCelsius >= 80 -> listOf(Color(0xFFEA580C), NeonAmber, WarningYellowTempColor)
                tempCelsius >= 65 -> listOf(NeonCyan, Color(0xFF059669), SafeGreenTempColor)
                else -> listOf(NeonPurple, ElectricBlue, IceBlueTempColor)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp)),
        ) {
            val w = size.width
            val h = size.height
            val centerY = h / 2f
            val cornerR = h / 2f

            // 1. Inactive Track (Dark frosted glass pill)
            drawRoundRect(
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerR, cornerR),
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.08f),
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = 1.dp.toPx()),
            )

            val fillWidth = (w * animatedFraction.coerceIn(0.06f, 1f))

            // 2. Glowing Liquid Aura under leading droplet head
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        fluidColors.last().copy(alpha = 0.45f * pulsePhase),
                        Color.Transparent,
                    ),
                    center = Offset(fillWidth, centerY),
                    radius = 16.dp.toPx(),
                ),
                center = Offset(fillWidth, centerY),
                radius = 16.dp.toPx(),
            )

            // 3. Active Liquid Stream (Fluid Gradient Pill)
            val liquidGradient = Brush.horizontalGradient(
                colors = fluidColors,
                startX = 0f,
                endX = fillWidth.coerceAtLeast(10f),
            )

            drawRoundRect(
                brush = liquidGradient,
                topLeft = Offset.Zero,
                size = Size(fillWidth, h),
                cornerRadius = CornerRadius(cornerR, cornerR),
            )

            // 4. Glossy Specular Highlight (Liquid water drop surface reflection)
            if (fillWidth > 14.dp.toPx()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.45f),
                    start = Offset(6.dp.toPx(), 2.5.dp.toPx()),
                    end = Offset(fillWidth - 6.dp.toPx(), 2.5.dp.toPx()),
                    strokeWidth = 1.4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                // Droplet head center glisten
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = 2.dp.toPx(),
                    center = Offset(fillWidth - 5.dp.toPx(), centerY),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogCpuPickerSheet(
    cpus: List<HardwareItem>,
    onDismiss: () -> Unit,
    onSelectCpu: (HardwareItem, CpuSpec) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(cpus, searchQuery) {
        cpus.filter { item ->
            item.spec is CpuSpec && (searchQuery.isBlank() || HardwareSearchEngine.matches(item, searchQuery))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .padding(horizontal = Spacing.medium),
        ) {
            Text(
                text = "🔍 Kataloğumdan İşlemci (CPU) Seç",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
            )
            Text(
                text = "Seçilen işlemcinin gerçek TDP ve hesaplanan PPT/MTP tepe güç değeri simülatöre aktarılır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("İşlemci ara (7500F, 7800X3D, 9950X, 14700K, AM5...)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(
                    items = filtered,
                    key = { it.id.value },
                ) { item ->
                    val spec = item.spec as? CpuSpec ?: return@items
                    val mapped = remember(item.id) { mapCatalogCpuToThermalPreset(item, spec) }
                    Surface(
                        onClick = { onSelectCpu(item, spec) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${spec.socket} • ${spec.coreCount}C/${spec.threadCount}T • TDP: ${mapped.baseTdpWatts}W • PPT: ${mapped.peakPptMtpWatts}W",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.currentPriceTry.formatPriceWithCurrency(fallback = "Seç"),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonEmerald,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogCoolerPickerSheet(
    coolers: List<HardwareItem>,
    onDismiss: () -> Unit,
    onSelectCooler: (HardwareItem, CoolerSpec) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(coolers, searchQuery) {
        coolers.filter { item ->
            item.spec is CoolerSpec && (searchQuery.isBlank() || HardwareSearchEngine.matches(item, searchQuery))
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .padding(horizontal = Spacing.medium),
        ) {
            Text(
                text = "🔍 Kataloğumdan Soğutucu (Cooler) Seç",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
            )
            Text(
                text = "Seçilen hava veya sıvı soğutucunun ısı dağıtma kapasitesi (W) simülatöre aktarılır.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Soğutucu ara (Thermalright, Arctic, 240mm, 360mm, Noctua...)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(
                    items = filtered,
                    key = { it.id.value },
                ) { item ->
                    val spec = item.spec as? CoolerSpec ?: return@items
                    val mapped = remember(item.id) { mapCatalogCoolerToThermalPreset(item, spec) }
                    Surface(
                        onClick = { onSelectCooler(item, spec) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${mapped.typeLabel} • Kapasite: ${mapped.maxDissipationWatts}W",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${mapped.maxDissipationWatts}W",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonEmerald,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ToolTopBar(
    title: String,
    subtitle: String,
    icon: String,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Spacing.medium, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    onClick = onOpenDrawer,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                ) {
                    Text(
                        text = "☰",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(text = icon, fontSize = 16.sp)
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Surface(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    text = "${LocalAppStrings.current.closeButton} ✕",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
