package com.panda.myapplication.presentation.tools

import androidx.compose.animation.animateColorAsState
import com.panda.myapplication.domain.config.FeatureFlags
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.panda.myapplication.presentation.util.LocalAppStrings
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
import com.panda.myapplication.presentation.util.LocalAppSettings
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
import com.panda.myapplication.presentation.util.formatTemperature
import com.panda.myapplication.domain.engine.CoolerThermalProfile
import com.panda.myapplication.domain.engine.CpuThermalProfile
import com.panda.myapplication.domain.engine.ThermalSimulationEngine
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.presentation.util.formatTemperatureCompact
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import kotlin.math.roundToInt

private val IceBlueTempColor = Color(0xFF00E5FF)
private val SafeGreenTempColor = Color(0xFF00E676)
private val WarningYellowTempColor = Color(0xFFFFD600)
private val DangerRedTempColor = Color(0xFFFF1744)

private val CPU_PRESETS: List<CpuThermalProfile> = ThermalSimulationEngine.CPU_PRESETS
private val COOLER_PRESETS: List<CoolerThermalProfile> = ThermalSimulationEngine.COOLER_PRESETS

private fun mapCatalogCpuToThermalPreset(item: HardwareItem, spec: CpuSpec): CpuThermalProfile =
    ThermalSimulationEngine.fromCatalogCpu(item, spec)

private fun mapCatalogCoolerToThermalPreset(item: HardwareItem, spec: CoolerSpec): CoolerThermalProfile =
    ThermalSimulationEngine.fromCatalogCooler(item, spec)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoolingLabScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val repository: HardwareRepository = koinInject()
    val activeBuildRepository: ActiveBuildRepository = koinInject()
    val activeBuild by activeBuildRepository.activeBuild.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f

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
    var customCatalogCpu by remember { mutableStateOf<CpuThermalProfile?>(null) }

    var selectedCoolerIndex by remember { mutableIntStateOf(2) } // Peerless Assassin default
    var customCatalogCooler by remember { mutableStateOf<CoolerThermalProfile?>(null) }

    var ambientTempCelsius by remember { mutableIntStateOf(24) }
    var isCpuPickerOpen by remember { mutableStateOf(false) }
    var isCoolerPickerOpen by remember { mutableStateOf(false) }

    val cpu = customCatalogCpu ?: CPU_PRESETS[selectedCpuIndex]
    val cooler = customCatalogCooler ?: COOLER_PRESETS[selectedCoolerIndex]

    // First-Principles Thermal Resistance Simulation (ThermalSimulationEngine)
    val sim = remember(cpu, cooler, ambientTempCelsius) {
        ThermalSimulationEngine.simulate(cpu, cooler, ambientTempCelsius)
    }
    val saturationRatio = sim.saturationRatio
    val headroomWatts = sim.headroomWatts
    val idleTemp = sim.idleTempCelsius
    val gamingWatts = sim.gamingWatts
    val gamingTemp = sim.gamingTempCelsius
    val renderTemp = sim.renderTempCelsius
    val isThrottling = sim.isThrottling

    val safeColorAdaptive = if (isLightTheme) Color(0xFF059669) else SafeGreenTempColor
    val warnColorAdaptive = if (isLightTheme) Color(0xFFD97706) else WarningYellowTempColor
    val dangerColorAdaptive = if (isLightTheme) Color(0xFFDC2626) else DangerRedTempColor

    val statusColor by animateColorAsState(
        targetValue = when {
            isThrottling || !sim.isSocketCompatible -> dangerColorAdaptive
            saturationRatio > 0.85f || renderTemp >= cpu.tjMaxCelsius - 6 -> warnColorAdaptive
            else -> safeColorAdaptive
        },
        animationSpec = tween(350),
        label = "statusColor",
    )
    val statusBadgeText = when {
        !sim.isSocketCompatible -> if (isTr) "SOKET MONTAJ UYUMSUZLUĞU (${cpu.socket})" else "SOCKET MOUNT MISMATCH (${cpu.socket})"
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
            // Optional Load from ActiveBuild Banner
            if (activeBuild.cpu != null || activeBuild.cooler != null) {
                item {
                    Surface(
                        onClick = {
                            activeBuild.cpu?.let { item ->
                                (item.spec as? CpuSpec)?.let { customCatalogCpu = mapCatalogCpuToThermalPreset(item, it) }
                            }
                            activeBuild.cooler?.let { item ->
                                (item.spec as? CoolerSpec)?.let { customCatalogCooler = mapCatalogCoolerToThermalPreset(item, it) }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = NeonCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTr) "Sihirbazdaki CPU & Soğutucuyu Yükle" else "Load Wizard CPU & Cooler",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                )
                                Text(
                                    text = listOfNotNull(activeBuild.cpu?.displayName, activeBuild.cooler?.displayName).joinToString(" • "),
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
                                text = strings.coolingSection1,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(Spacing.small))
                            OutlinedButton(
                                onClick = { isCpuPickerOpen = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonCyan),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "🔍 ${LocalAppStrings.current.selectCpuCatalog} (${catalogCpus.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    maxLines = 1,
                                    softWrap = false,
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
                                            text = strings.coolingSelectedCpu.replace("%s", cpu.name),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = NeonEmerald,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = strings.coolingCpuSpecsFormat
                                                .replaceFirst("%dW", "${cpu.baseTdpWatts}W")
                                                .replaceFirst("%dW", "${cpu.peakPptMtpWatts}W"),
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
                                            text = strings.coolingReset,
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
                            text = strings.coolingRecommendedCpu,
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
                        val tempUnit = LocalAppSettings.current.temperatureUnit
                        val tjMaxFormatted = formatTemperatureCompact(cpu.tjMaxCelsius, tempUnit)
                        Text(
                            text = strings.coolingRatedTdpTemplate
                                .replaceFirst("%s", cpu.socket)
                                .replaceFirst("%d", cpu.baseTdpWatts.toString())
                                .replaceFirst("%d", cpu.peakPptMtpWatts.toString())
                                .replace("%d°C", tjMaxFormatted)
                                .replace("%d °C", tjMaxFormatted)
                                .replaceFirst("%d", cpu.tjMaxCelsius.toString()),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = cpu.localizedNotes(isTr),
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
                                text = strings.coolingCoolerArch,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(Spacing.small))
                            OutlinedButton(
                                onClick = { isCoolerPickerOpen = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonCyan),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "🔍 ${LocalAppStrings.current.selectCoolerCatalog} (${catalogCoolers.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    maxLines = 1,
                                    softWrap = false,
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
                                            text = strings.coolingSelectedCooler.replace("%s", cooler.name),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = NeonEmerald,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "${cooler.localizedType(isTr)} • ${cooler.maxDissipationWatts}W ${strings.coolingCapacitySuffix} • ${cooler.ratedNoiseDba} dBA",
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
                                            text = strings.coolingReset,
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
                            text = strings.coolingRecommendedCooler,
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
                                                text = "${item.localizedType(isTr)} • ${strings.coolingNoiseLabel}: ${item.ratedNoiseDba} dBA",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            text = "${item.maxDissipationWatts}W ${strings.coolingCapacitySuffix}",
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
                                text = strings.coolingRoomTemp,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val roomTempUnit = LocalAppSettings.current.temperatureUnit
                            Text(
                                text = formatTemperature(ambientTempCelsius, roomTempUnit),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                            )
                        }
                        FluidInteractiveSlider(
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.coolingCpuTdp,
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
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = strings.coolingCoolerCapacity,
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
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(
                                text = strings.coolingHeadroomLabel,
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
                        label = strings.coolingIdleLoad,
                        tempCelsius = idleTemp,
                        tjMax = cpu.tjMaxCelsius,
                        powerWatts = sim.idleWatts,
                    )
                    ThermalTempRow(
                        label = strings.coolingGamingLoad,
                        tempCelsius = gamingTemp,
                        tjMax = cpu.tjMaxCelsius,
                        powerWatts = gamingWatts,
                    )
                    ThermalTempRow(
                        label = strings.coolingStressLoad,
                        tempCelsius = renderTemp,
                        tjMax = cpu.tjMaxCelsius,
                        powerWatts = cpu.peakPptMtpWatts,
                    )

                    Spacer(modifier = Modifier.height(Spacing.small))

                    // Acoustic & Thermal Impedance Telemetry Row
                    Text(
                        text = if (isTr) {
                            "Isıl Direnç: ${sim.coolerThermalResistanceCw} °C/W • Tahmini Akustik: Oyunda ~${sim.estimatedGamingNoiseDba} dBA / Tam Yükte ~${sim.estimatedStressNoiseDba} dBA"
                        } else {
                            "Thermal Impedance: ${sim.coolerThermalResistanceCw} °C/W • Est. Acoustics: Gaming ~${sim.estimatedGamingNoiseDba} dBA / Stress ~${sim.estimatedStressNoiseDba} dBA"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val warnTempUnit = LocalAppSettings.current.temperatureUnit
                    Text(
                        text = if (isThrottling) {
                            strings.coolingWarningDesc
                                .replaceFirst("%s", cpu.name)
                                .replaceFirst("%dW", "${cpu.peakPptMtpWatts}W")
                                .replaceFirst("%s", cooler.name)
                                .replaceFirst("%dW", "${cooler.maxDissipationWatts}W")
                                .replace("%d°C", formatTemperatureCompact(cpu.tjMaxCelsius, warnTempUnit))
                        } else {
                            strings.coolingSafeDesc
                                .replaceFirst("%s", cooler.name)
                                .replaceFirst("%s", cpu.name)
                                .replaceFirst("%dW", "${cpu.peakPptMtpWatts}W")
                                .replaceFirst("%dW", "${headroomWatts}W")
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
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val targetColor = when {
        tempCelsius >= 90 -> if (isLight) Color(0xFFDC2626) else DangerRedTempColor
        tempCelsius >= 80 -> if (isLight) Color(0xFFD97706) else WarningYellowTempColor
        tempCelsius >= 65 -> if (isLight) Color(0xFF059669) else SafeGreenTempColor
        else -> if (isLight) Color(0xFF0284C7) else IceBlueTempColor
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
    val tempUnit = LocalAppSettings.current.temperatureUnit

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
                text = "${formatTemperature(displayedTemp, tempUnit)} / ${formatTemperature(tjMax, tempUnit)}",
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

        val trackBgColor = MaterialTheme.colorScheme.surfaceVariant
        val trackOutlineColor = MaterialTheme.colorScheme.outlineVariant

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

            // 1. Inactive Track (Theme-adaptive pill)
            drawRoundRect(
                color = trackBgColor,
                size = Size(w, h),
                cornerRadius = CornerRadius(cornerR, cornerR),
            )
            drawRoundRect(
                color = trackOutlineColor.copy(alpha = 0.55f),
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
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
                text = "🔍 " + strings.selectCpuCatalog,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
            )
            Text(
                text = strings.coolingSelectedCpuDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.coolingCpuSearchPlaceholder) },
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
                                text = if (FeatureFlags.IS_PRICING_ENABLED) {
                                    item.currentPriceTry.formatPriceWithCurrency(fallback = if (isTr) "Seç" else "Select")
                                } else {
                                    if (isTr) "Seç" else "Select"
                                },
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
    val strings = LocalAppStrings.current
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
                text = "🔍 " + strings.selectCoolerCatalog,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
            )
            Text(
                text = strings.coolingSelectedCoolerDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.coolingCoolerSearchPlaceholder) },
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
                                    text = "${mapped.localizedType(strings.isTurkish)} • ${strings.coolingCapacitySuffix}: ${mapped.maxDissipationWatts}W",
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
    val headerIconVector = when {
        icon.contains("❄") || icon.contains("🌡") -> Icons.Rounded.AcUnit
        icon.contains("🔋") -> Icons.Rounded.BatteryChargingFull
        icon.contains("🎯") || icon.contains("🖱") -> Icons.Rounded.SportsEsports
        icon.contains("📚") -> Icons.AutoMirrored.Rounded.MenuBook
        icon.contains("🔄") || icon.contains("🏷") -> Icons.Rounded.Autorenew
        icon == "MEMORY" -> Icons.Rounded.Bolt
        icon.contains("⚡") -> Icons.Rounded.Tune
        else -> Icons.Rounded.Tune
    }

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
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = headerIconVector,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(17.dp),
                        )
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
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = LocalAppStrings.current.closeButton,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
    }
}
