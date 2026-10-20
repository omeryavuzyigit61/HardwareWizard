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
import androidx.compose.ui.unit.sp
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import kotlin.math.ceil
import kotlin.math.roundToInt

private data class BuildPowerPreset(
    val label: String,
    val pcLoadWatts: Int,
    val monitorWatts: Int,
)

private val BUILD_POWER_PRESETS = listOf(
    BuildPowerPreset("R5 7500F + RTX 4060 (310W)", 310, 35),
    BuildPowerPreset("R7 7800X3D + RTX 4070 Super (440W)", 440, 45),
    BuildPowerPreset("R7 9800X3D + RTX 4080 Super (560W)", 560, 55),
    BuildPowerPreset("R9 9950X + RTX 4090 / 5090 (820W)", 820, 65),
)

private data class UpsTierClass(
    val vaRating: Int,
    val realWattCapacity: Int,
    val batteryConfig: String,
    val totalBatteryWh: Int,
    val topology: String,
)

private val UPS_TIERS = listOf(
    UpsTierClass(850, 480, "1x 12V 9Ah", 108, "Line-Interactive (Simüle Sinüs)"),
    UpsTierClass(1000, 600, "2x 12V 7Ah", 168, "Line-Interactive"),
    UpsTierClass(1500, 900, "2x 12V 9Ah", 216, "Line-Interactive / Tam Sinüs"),
    UpsTierClass(2000, 1200, "4x 12V 9Ah", 432, "Online Tam Sinüs (Double Conversion)"),
    UpsTierClass(3000, 1800, "6x 12V 9Ah", 648, "Online Tam Sinüs (Server/Workstation)"),
)

@Composable
fun UpsCalculatorScreen(
    initialBuildWatts: Int = 440,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pcWatts by remember { mutableIntStateOf(initialBuildWatts.coerceIn(180, 1100)) }
    var monitorWatts by remember { mutableIntStateOf(45) }

    val totalLoadWatts = pcWatts + monitorWatts
    val idleTotalWatts = (pcWatts * 0.24).roundToInt() + (monitorWatts * 0.75).roundToInt()

    // Engineering Formula: Required VA = Total Watts / 0.6 (with 20% safety margin recommended)
    val rawRequiredVa = ceil(totalLoadWatts / 0.6).roundToInt()
    val safeRecommendedVa = ceil((totalLoadWatts * 1.2) / 0.6).roundToInt()

    val recommendedTier = UPS_TIERS.firstOrNull { it.realWattCapacity >= (totalLoadWatts * 1.15).roundToInt() }
        ?: UPS_TIERS.last()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = LocalAppStrings.current.drawerUpsTitle,
                subtitle = LocalAppStrings.current.drawerUpsSubtitle,
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
                            text = "1. SİSTEM & MONİTÖR GÜÇ TÜKETİMİ (WATT)",
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
                        ) {
                            Text(
                                text = "Kasa Oyun Yükü Güç Çekimi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "$pcWatts W",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                            )
                        }
                        com.panda.myapplication.presentation.components.FluidInteractiveSlider(
                            value = pcWatts.toFloat(),
                            onValueChange = { pcWatts = (it / 10f).roundToInt() * 10 },
                            valueRange = 150f..1100f,
                            accentColor = NeonCyan,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Monitör + Modem / Çevre Birimi Tüketimi",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "$monitorWatts W",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonAmber,
                            )
                        }
                        com.panda.myapplication.presentation.components.FluidInteractiveSlider(
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "ÖNERİLEN MİNİMUM UPS SINIFI",
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
                                    text = "${recommendedTier.topology} • Akü: ${recommendedTier.batteryConfig}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = "Toplam Tam Yük",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "$totalLoadWatts W",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonAmber,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Ham VA (W / 0.6)",
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
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+%20 Güvenlik Paylı",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "$safeRecommendedVa VA",
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
                            Text(
                                text = "⚡ Aktif PFC Güç Kaynağı Notu: Modern 80+ Gold / ATX 3.0 güç kaynaklarında elektrik kesildiği an röle geçişinde (4-8ms) kapanma yaşanmaması için 650W üstü sistemlerde Saf Sinüs (Pure Sine Wave) veya Online UPS tercih edilmesi önerilir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            }

            // 3. Runtime Matrix across all UPS Tiers
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
                        Text(
                            text = "⏱️ ELEKTRİK KESİNTİSİNDE TAHMİNİ AKÜ SÜRELERİ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = "İnverter verimliliği (%82) ve Peukert kurşun-asit deşarj eğrisine göre hesaplanmıştır:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        UPS_TIERS.forEach { tier ->
                            val isOverloaded = totalLoadWatts > tier.realWattCapacity
                            val usableWh = tier.totalBatteryWh * 0.78 // Inverter + Peukert derating
                            val gamingMinutes = if (isOverloaded) 0 else ((usableWh / totalLoadWatts.toDouble()) * 60.0).roundToInt().coerceAtLeast(2)
                            val idleMinutes = ((usableWh / idleTotalWatts.toDouble()) * 60.0).roundToInt().coerceAtLeast(5)
                            val isRecommended = tier.vaRating == recommendedTier.vaRating

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isOverloaded -> NeonRose.copy(alpha = 0.1f)
                                    isRecommended -> NeonEmerald.copy(alpha = 0.14f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isOverloaded -> NeonRose.copy(alpha = 0.5f)
                                        isRecommended -> NeonEmerald
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
                                        Text(
                                            text = when {
                                                isOverloaded -> "AŞIRI YÜK (YETERSİZ)"
                                                isRecommended -> "★ ÖNERİLEN"
                                                else -> "UYGUN"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                isOverloaded -> NeonRose
                                                isRecommended -> NeonEmerald
                                                else -> NeonCyan
                                            },
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isOverloaded) {
                                            "Oyun yükünde (${totalLoadWatts}W) UPS kapasitesi (${tier.realWattCapacity}W) aşılır ve aşırı yük koruması devreye girer."
                                        } else {
                                            "🎮 Oyun Yükünde (${totalLoadWatts}W): ~$gamingMinutes dk  •  🖥️ Masaüstü Boşta (${idleTotalWatts}W): ~$idleMinutes dk"
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
