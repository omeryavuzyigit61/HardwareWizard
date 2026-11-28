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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.engine.BuildAnalysisEngine
import com.panda.myapplication.domain.engine.UpsSizingEngine
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.UpsSpec
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.presentation.components.FluidInteractiveSlider
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.LocalAppStrings
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import kotlin.math.ceil
import kotlin.math.roundToInt

private data class BuildPowerPreset(
    val label: String,
    val pcLoadWatts: Int,
    val monitorWatts: Int,
)

private val BUILD_POWER_PRESETS = listOf(
    BuildPowerPreset("R5 7500F + RTX 4060 (280W)", 280, 35),
    BuildPowerPreset("R7 7800X3D + RTX 4070 Super (390W)", 390, 45),
    BuildPowerPreset("R7 9800X3D + RTX 5080 (540W)", 540, 55),
    BuildPowerPreset("R9 9950X + RTX 5090 (860W)", 860, 65),
)

@Composable
fun UpsCalculatorScreen(
    initialBuildWatts: Int = 440,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    hardwareRepository: HardwareRepository = koinInject(),
    activeBuildRepository: ActiveBuildRepository = koinInject(),
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val activeBuild by activeBuildRepository.activeBuild.collectAsState()
    val wizardWatts = remember(activeBuild) {
        BuildAnalysisEngine.calculateWattageCockpit(activeBuild).componentTdpSum.takeIf { it > 0 }
    }

    val catalogUpsItems by remember {
        hardwareRepository.observeHardwareItemsByCategory(HardwareCategory.UPS).map { res ->
            (res as? DomainResult.Success)?.data.orEmpty()
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var pcWatts by remember(initialBuildWatts) {
        mutableIntStateOf((wizardWatts ?: initialBuildWatts).coerceIn(150, 1200))
    }
    var monitorWatts by remember { mutableIntStateOf(45) }

    val report = remember(pcWatts, monitorWatts, catalogUpsItems) {
        UpsSizingEngine.calculate(
            pcDcWatts = pcWatts,
            monitorWatts = monitorWatts,
            catalogUpsItems = catalogUpsItems,
        )
    }
    val recommendedTier = report.recommendedTier
    val rawRequiredVa = ceil(report.acWallLoadWatts / 0.60).roundToInt()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = strings.drawerUpsTitle,
                subtitle = strings.drawerUpsSubtitle,
                icon = "🔋",
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
            if (wizardWatts != null && wizardWatts != pcWatts) {
                item {
                    Surface(
                        onClick = { pcWatts = wizardWatts.coerceIn(150, 1200) },
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
                            Text(
                                text = if (isTr) "Sihirbazdaki Güç Yükünü Yükle (${wizardWatts}W DC)" else "Load Wizard System Power (${wizardWatts}W DC)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                            )
                        }
                    }
                }
            }

            // 1. Power Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(modifier = Modifier.padding(Spacing.medium)) {
                        Text(
                            text = strings.upsSectionWatts,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.sp,
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            BUILD_POWER_PRESETS.forEach { preset ->
                                val selected = pcWatts == preset.pcLoadWatts
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        pcWatts = preset.pcLoadWatts
                                        monitorWatts = preset.monitorWatts
                                    },
                                    label = { Text(preset.label, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonCyan,
                                    ),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.small))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = strings.upsPcWatts,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text(
                                text = "$pcWatts W DC",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                softWrap = false,
                            )
                        }
                        FluidInteractiveSlider(
                            value = pcWatts.toFloat(),
                            onValueChange = { pcWatts = (it / 10f).roundToInt() * 10 },
                            valueRange = 150f..1200f,
                            accentColor = NeonCyan,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = strings.upsMonitorWatts,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text(
                                text = "$monitorWatts W",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonAmber,
                                softWrap = false,
                            )
                        }
                        FluidInteractiveSlider(
                            value = monitorWatts.toFloat(),
                            onValueChange = { monitorWatts = (it / 5f).roundToInt() * 5 },
                            valueRange = 20f..160f,
                            accentColor = NeonAmber,
                        )
                    }
                }
            }

            // 2. Recommended UPS Class Card
            item {
                FluidOrganicCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = NeonEmerald,
                    secondaryColor = NeonCyan,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                    ) {
                        Column {
                            Text(
                                text = strings.upsRecommendedTier,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonEmerald,
                                letterSpacing = 1.sp,
                            )
                            Text(
                                text = "${recommendedTier.vaRating} VA / ${recommendedTier.realWattCapacity} W",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "${recommendedTier.localizedTopology(isTr)} • ${if (isTr) "Akü" else "Battery"}: ${recommendedTier.batteryConfig}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTr) "Priz Yükü (%90 PSU)" else "AC Wall Load (90% PSU)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "${report.acWallLoadWatts} W",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonAmber,
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = strings.upsRawVa,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "$rawRequiredVa VA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End,
                            ) {
                                Text(
                                    text = strings.upsSafetyMargin,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "${report.safeRecommendedVa} VA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonEmerald,
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonAmber.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (isTr) {
                                        "Aktif PFC Güç Kaynağı Notu: Modern 80+ Gold / ATX 3.1 güç kaynaklarında prizden çekilen gerçek AC güç (%90 verimle ${report.acWallLoadWatts}W) esas alınır. Röle geçişinde (4-8ms) kapanma olmaması için Tam Sinüs (Pure Sine Wave) veya Online UPS önerilir."
                                    } else {
                                        "Active PFC PSU Note: Real AC wall draw (${report.acWallLoadWatts}W at 90% PSU efficiency) is used. Pure Sine Wave or Online UPS is recommended to prevent reset during 4-8ms relay transfer."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            // 3. Matching Real Catalog UPS Models (from 340 catalog items)
            if (report.matchingCatalogUps.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Spacing.cardRadius),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.55f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                                Text(
                                    text = if (isTr) "KATALOGDAN UYUMLU GERÇEK UPS MODELLERİ (${catalogUpsItems.size} Model Arasından)" else "COMPATIBLE REAL CATALOG UPS MODELS (From ${catalogUpsItems.size} Models)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeonEmerald,
                                )
                            }
                            report.matchingCatalogUps.forEach { upsItem ->
                                val us = upsItem.spec as? UpsSpec
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = upsItem.displayName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (us != null) {
                                                Text(
                                                    text = "${us.topology} • ${us.sinewaveType}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (us != null) {
                                            Text(
                                                text = "${us.vaRating}VA / ${us.wattCapacity}W",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = NeonCyan,
                                                softWrap = false,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Runtime Matrix across all UPS Tiers (Real Peukert Curve)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Spacing.cardRadius),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = strings.upsRuntimeSection,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.sp,
                            )
                        }
                        Text(
                            text = if (isTr) {
                                "İnverter verimliliği (%85), PSU priz kaybı (%90) ve Peukert kurşun-asit üstel deşarj yasasına (k=1.18) göre hesaplanmıştır:"
                            } else {
                                "Calculated with 85% inverter efficiency, 90% PSU wall draw, and non-linear Peukert VRLA discharge law (k=1.18):"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        report.tierRows.forEach { row ->
                            val tier = row.tier
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    row.isOverloaded -> NeonRose.copy(alpha = 0.1f)
                                    row.isRecommended -> NeonEmerald.copy(alpha = 0.14f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        row.isOverloaded -> NeonRose.copy(alpha = 0.5f)
                                        row.isRecommended -> NeonEmerald
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    },
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "${tier.vaRating} VA (${tier.realWattCapacity}W) • ${tier.batteryConfig}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        ) {
                                            if (row.isRecommended) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(13.dp))
                                            }
                                            Text(
                                                text = when {
                                                    row.isOverloaded -> if (isTr) "AŞIRI YÜK" else "OVERLOAD"
                                                    row.isRecommended -> if (isTr) "ÖNERİLEN" else "RECOMMENDED"
                                                    else -> if (isTr) "UYGUN" else "COMPATIBLE"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = when {
                                                    row.isOverloaded -> NeonRose
                                                    row.isRecommended -> NeonEmerald
                                                    else -> NeonCyan
                                                },
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (row.isOverloaded) {
                                            if (isTr) "Oyun priz yükünde (${report.acWallLoadWatts}W) UPS kapasitesi (${tier.realWattCapacity}W) aşılır ve aşırı yük koruması devreye girer."
                                            else "At gaming AC load (${report.acWallLoadWatts}W), UPS capacity (${tier.realWattCapacity}W) is exceeded and overload protection trips."
                                        } else {
                                            if (isTr) "Oyun Yükünde (${report.acWallLoadWatts}W AC): ~${row.gamingMinutes} dk  •  Masaüstü Boşta (${report.idleAcWatts}W AC): ~${row.idleMinutes} dk"
                                            else "Gaming Load (${report.acWallLoadWatts}W AC): ~${row.gamingMinutes} min  •  Desktop Idle (${report.idleAcWatts}W AC): ~${row.idleMinutes} min"
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
}
