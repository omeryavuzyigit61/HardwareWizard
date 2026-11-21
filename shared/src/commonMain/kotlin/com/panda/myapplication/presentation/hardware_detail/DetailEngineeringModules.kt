package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.panda.myapplication.domain.model.AccessPointSpec
import com.panda.myapplication.domain.model.CaseFanSpec
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GamingChairSpec
import com.panda.myapplication.domain.model.GamingDeskSpec
import com.panda.myapplication.domain.model.GamingLightingSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MicrophoneSpec
import com.panda.myapplication.domain.model.ModemSpec
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.MousepadSpec
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.RangeExtenderSpec
import com.panda.myapplication.domain.model.RouterSpec
import com.panda.myapplication.domain.model.SmartwatchSpec
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.TabletSpec
import com.panda.myapplication.domain.model.UpsSpec
import com.panda.myapplication.domain.model.WebcamSpec
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.domain.model.AppSettings
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.presentation.util.AppStrings
import com.panda.myapplication.presentation.util.LocalAppSettings
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.formatLengthMm
import com.panda.myapplication.presentation.util.formatTemperature

private data class HeroKpiPill(val label: String, val value: String, val accent: Color)

private data class SpecAccordionGroup(
    val title: String,
    val badge: String,
    val accentColor: Color,
    val rows: List<Pair<String, String>>,
    val initiallyExpanded: Boolean = true,
)

/**
 * Full-screen pinch-to-zoom and multi-angle pager dialog for product inspection.
 */
@Composable
fun DetailImageLightboxDialog(
    images: List<String>,
    initialPage: Int,
    displayName: String,
    onDismiss: () -> Unit,
) {
    if (images.isEmpty()) return
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, images.lastIndex),
        pageCount = { images.size },
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.96f))
                .statusBarsPadding(),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                var scale by remember(page) { mutableFloatStateOf(1f) }
                var offset by remember(page) { mutableStateOf(Offset.Zero) }
                val transformState = rememberTransformableState { _, zoomChange, panChange, _ ->
                    val nextScale = (scale * zoomChange).coerceIn(1f, 3.5f)
                    scale = nextScale
                    offset = if (nextScale > 1.05f) offset + panChange else Offset.Zero
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(page) {
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = if (scale > 1.5f) 1f else 2.2f
                                    offset = Offset.Zero
                                },
                            )
                        }
                        .transformable(state = transformState),
                    contentAlignment = Alignment.Center,
                ) {
                    SubcomposeAsyncImage(
                        model = images[page],
                        contentDescription = displayName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y,
                            ),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = NeonCyan)
                            }
                        },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                ) {
                    Text(
                        text = if (isTr) {
                            "${pagerState.currentPage + 1} / ${images.size} • Çift tıkla / yakınlaştır"
                        } else {
                            "${pagerState.currentPage + 1} / ${images.size} • Pinch or double-tap to zoom"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f)),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * 4-pill key spec bar right under the Hero header title, tailored per category.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroSpecKpiStrip(
    item: HardwareItem,
    isTr: Boolean,
    modifier: Modifier = Modifier,
) {
    val lengthUnit = LocalAppSettings.current.lengthUnit
    val pills = remember(item, isTr, lengthUnit) { buildHeroKpiPills(item, isTr, lengthUnit) }
    if (pills.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        pills.forEach { pill ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = pill.accent.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, pill.accent.copy(alpha = 0.35f)),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = pill.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Text(
                        text = pill.value,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = pill.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Displays verified source attribution chips and last-verified timestamp.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SourceProvenanceStrip(
    item: HardwareItem,
    isTr: Boolean,
    modifier: Modifier = Modifier,
) {
    val sources = item.sourceProvenance.filter { it.isNotBlank() }
    val verifiedAt = item.specVerifiedAt?.takeIf { it.isNotBlank() }
    if (sources.isEmpty() && verifiedAt == null) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = if (isTr) {
                    "Doğrulanmış Veri Kaynakları${verifiedAt?.let { " • $it" } ?: ""}"
                } else {
                    "Verified Data Sources${verifiedAt?.let { " • $it" } ?: ""}"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = NeonEmerald,
            )
        }
        if (sources.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                sources.forEach { src ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonEmerald.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.35f)),
                    ) {
                        Text(
                            text = "✓ $src",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Replaces the single flat spec table with structured, collapsible engineering modules.
 */
@Composable
fun GroupedTechnicalSpecsCard(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val appSettings = LocalAppSettings.current
    val isTr = strings.isTurkish
    val groups = remember(item, isTr, appSettings) { buildGroupedSpecSections(item, strings, isTr, appSettings) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        groups.forEach { group ->
            var expanded by remember(item.id.value, group.title) { mutableStateOf(group.initiallyExpanded) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Spacing.cardRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, group.accentColor.copy(alpha = 0.35f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(horizontal = Spacing.medium, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(group.accentColor),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = group.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = group.accentColor.copy(alpha = 0.14f),
                            ) {
                                Text(
                                    text = "${group.rows.size} ${group.badge}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = group.accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = Spacing.medium, end = Spacing.medium, bottom = Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                                thickness = 1.dp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            group.rows.forEachIndexed { idx, (label, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (idx % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
                                            else Color.Transparent
                                        )
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1.25f),
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

private fun cleanSpecRows(raw: List<Pair<String, String>>): List<Pair<String, String>> = raw.filter { (_, v) ->
    val t = v.trim()
    t.isNotEmpty() &&
        t != "Belirtilmedi" &&
        t != "Unspecified" &&
        t != "Veri Bulunamadı" &&
        t != "Not Found" &&
        !t.startsWith("0 ") &&
        !t.startsWith("0.0 ") &&
        !t.startsWith("0°") &&
        !t.startsWith("0x") &&
        !t.startsWith("Ra 0") &&
        !t.startsWith("0+") &&
        !t.startsWith("~0 ") &&
        !t.startsWith("0 - 0") &&
        t != "0" &&
        t != "()" &&
        !t.startsWith(" ()")
}

private fun buildHeroKpiPills(
    item: HardwareItem,
    isTr: Boolean,
    lengthUnit: LengthUnit = LengthUnit.METRIC,
): List<HeroKpiPill> {
    val raw = when (val s = item.spec) {
        is CpuSpec -> listOfNotNull(
            HeroKpiPill(if (isTr) "Soket" else "Socket", s.socket, ElectricBlue),
            HeroKpiPill(if (isTr) "Çekirdek" else "Cores", "${s.coreCount}C / ${s.threadCount}T", NeonCyan),
            s.l3CacheMb?.let { HeroKpiPill("L3 Cache", "$it MB", NeonPurple) },
            HeroKpiPill("TDP", "${s.tdp.watts}W", NeonAmber),
        )
        is GpuSpec -> listOfNotNull(
            HeroKpiPill("VRAM", "${s.vram.megabytes / 1024} GB ${s.memoryType}", ElectricBlue),
            s.busWidthBits?.let { HeroKpiPill(if (isTr) "Veri Yolu" else "Bus", "$it-bit", NeonCyan) },
            s.fp32Tflops?.let { HeroKpiPill("FP32", "$it TFLOPS", NeonPurple) }
                ?: (s.boostClock ?: s.coreClock).let { HeroKpiPill("Clock", "${it.mhz} MHz", NeonPurple) },
            HeroKpiPill("TDP", "${s.tdp.watts}W", NeonAmber),
        )
        is MotherboardSpec -> listOfNotNull(
            HeroKpiPill(if (isTr) "Soket" else "Socket", s.socket, ElectricBlue),
            HeroKpiPill("Chipset", s.chipset, NeonCyan),
            HeroKpiPill("RAM", "${s.memoryGeneration.displayName} (${s.ramSlots}x)", NeonPurple),
            s.m2Slots?.let { HeroKpiPill("M.2", "${it}x NVMe", NeonEmerald) },
        )
        is RamSpec -> listOfNotNull(
            HeroKpiPill(if (isTr) "Tip" else "Type", s.generation.displayName, ElectricBlue),
            HeroKpiPill(if (isTr) "Kapasite" else "Capacity", "${s.capacity.megabytes / 1024} GB", NeonCyan),
            HeroKpiPill(if (isTr) "Hız" else "Speed", "${s.speed.mhz} MT/s", NeonPurple),
            HeroKpiPill("CAS", "CL${s.casLatency}", NeonAmber),
        )
        is StorageSpec -> listOfNotNull(
            HeroKpiPill(if (isTr) "Kapasite" else "Cap", "${s.capacity.megabytes / 1024} GB", ElectricBlue),
            HeroKpiPill(if (isTr) "Okuma" else "Read", "${s.sequentialReadMbps} MB/s", NeonCyan),
            s.controller?.let { HeroKpiPill(if (isTr) "Kontrolcü" else "Ctrl", it, NeonPurple) },
            s.nandType?.let { HeroKpiPill("NAND", it, NeonAmber) },
        )
        is PsuSpec -> listOfNotNull(
            HeroKpiPill(if (isTr) "Güç" else "Power", "${s.wattage.watts}W", ElectricBlue),
            HeroKpiPill("80+", s.efficiency.displayName, NeonCyan),
            s.tierRating?.let { HeroKpiPill("Tier", it, NeonEmerald) },
            HeroKpiPill("ATX", s.atxStandard ?: if (s.isAtx3Compliant) "ATX 3.x" else "ATX", NeonAmber),
        )
        is CoolerSpec -> listOfNotNull(
            HeroKpiPill(if (isTr) "Tip" else "Type", s.type.displayName, ElectricBlue),
            HeroKpiPill("Max TDP", "${s.maxCoolingTdp.watts}W", NeonCyan),
            s.radiatorSizeMm?.let { HeroKpiPill("Rad", formatLengthMm(it, lengthUnit), NeonPurple) }
                ?: s.heightMm?.let { HeroKpiPill(if (isTr) "Yükseklik" else "Height", formatLengthMm(it, lengthUnit), NeonPurple) },
            s.noiseDba?.let { HeroKpiPill(if (isTr) "Ses" else "Noise", "$it dBA", NeonAmber) },
        )
        is CaseSpec -> listOfNotNull(
            s.type?.let { HeroKpiPill("Form", it, ElectricBlue) },
            s.maxGpuLengthMm?.let { HeroKpiPill("Max GPU", formatLengthMm(it, lengthUnit), NeonCyan) },
            s.maxCpuCoolerHeightMm?.let { HeroKpiPill("Max Cooler", formatLengthMm(it, lengthUnit), NeonPurple) },
            s.maxRadiatorSizeMm?.let { HeroKpiPill("Max Rad", formatLengthMm(it, lengthUnit), NeonAmber) },
        )
        is PhoneSpec -> listOfNotNull(
            HeroKpiPill("SoC", s.chipset, ElectricBlue),
            if (s.displaySizeInch > 0 && s.refreshRateHz > 0) HeroKpiPill("Ekran", "${s.displaySizeInch}\" ${s.refreshRateHz}Hz", NeonCyan) else null,
            s.dxomarkCamera?.let { HeroKpiPill("DXOMARK", "$it pts", NeonEmerald) }
                ?: if (s.mainCameraMp > 0) HeroKpiPill("Kamera", "${s.mainCameraMp} MP", NeonPurple) else null,
            if (s.batteryMah > 0) HeroKpiPill("Pil", "${s.batteryMah} mAh", NeonAmber) else null,
        )
        is TabletSpec -> listOfNotNull(
            HeroKpiPill("SoC", s.chipset, ElectricBlue),
            if (s.displaySizeInch > 0) HeroKpiPill("Ekran", "${s.displaySizeInch}\" ${s.refreshRateHz.takeIf { it > 0 }?.let { "${it}Hz" } ?: ""}".trim(), NeonCyan) else null,
            if (s.ramGb > 0) HeroKpiPill("Bellek", "${s.ramGb}GB / ${s.storageGb}GB", NeonPurple) else null,
            if (s.batteryMah > 0) HeroKpiPill("Pil", "${s.batteryMah} mAh", NeonAmber) else null,
        )
        is LaptopSpec -> listOfNotNull(
            if (s.cpuModel.isNotBlank()) HeroKpiPill("CPU", s.cpuModel, ElectricBlue) else null,
            if (s.gpuModel.isNotBlank()) HeroKpiPill("GPU", s.gpuModel, NeonCyan) else null,
            if (s.gpuWattTgp.isNotBlank()) HeroKpiPill("TGP", s.gpuWattTgp, NeonPurple) else null,
            if (s.batteryWh > 0) HeroKpiPill("Pil", "${s.batteryWh} Wh", NeonAmber) else null,
        )
        is MonitorSpec -> listOfNotNull(
            if (s.screenSizeInch > 0) HeroKpiPill(if (isTr) "Boyut" else "Size", "${s.screenSizeInch}\"", ElectricBlue) else null,
            if (s.resolution.isNotBlank()) HeroKpiPill(if (isTr) "Çözünürlük" else "Res", s.resolution, NeonCyan) else null,
            if (s.refreshRateHz > 0) HeroKpiPill("Hz", "${s.refreshRateHz} Hz", NeonPurple) else null,
            if (s.panelType.isNotBlank()) HeroKpiPill("Panel", s.panelType, NeonEmerald) else null,
        )
        is MouseSpec -> listOfNotNull(
            if (s.sensor.isNotBlank()) HeroKpiPill(if (isTr) "Sensör" else "Sensor", s.sensor, ElectricBlue) else null,
            if (s.weightG > 0) HeroKpiPill(if (isTr) "Ağırlık" else "Weight", "${s.weightG}g", NeonCyan) else null,
            if (s.pollingRateHz > 0) HeroKpiPill("Polling", "${s.pollingRateHz}Hz", NeonPurple) else null,
            if (s.maxDpi > 0) HeroKpiPill("Max DPI", "${s.maxDpi}", NeonAmber) else null,
        )
        is KeyboardSpec -> listOfNotNull(
            if (s.layout.isNotBlank()) HeroKpiPill("Layout", s.layout, ElectricBlue) else null,
            if (s.switchType.isNotBlank()) HeroKpiPill("Switch", s.switchType, NeonCyan) else null,
            if (s.pollingRateHz > 0) HeroKpiPill("Polling", "${s.pollingRateHz}Hz", NeonPurple) else null,
            if (s.isHallEffect) HeroKpiPill("Manyetik", "Hall Effect", NeonEmerald) else null,
        )
        is HeadsetSpec -> listOfNotNull(
            if (s.driverType.isNotBlank()) HeroKpiPill("Form", s.driverType, ElectricBlue) else null,
            if (s.driverSizeMm > 0) HeroKpiPill("Driver", "${s.driverSizeMm}mm", NeonCyan) else null,
            if (s.impedanceOhm > 0) HeroKpiPill("Ohm", "${s.impedanceOhm} Ω", NeonPurple) else null,
            if (s.weightG > 0) HeroKpiPill(if (isTr) "Ağırlık" else "Weight", "${s.weightG}g", NeonAmber) else null,
        )
        is MicrophoneSpec -> listOfNotNull(
            if (s.capsuleType.isNotBlank()) HeroKpiPill(if (isTr) "Kapsül" else "Capsule", s.capsuleType, ElectricBlue) else null,
            if (s.polarPattern.isNotBlank()) HeroKpiPill("Polar", s.polarPattern, NeonCyan) else null,
            if (s.connectivity.isNotBlank()) HeroKpiPill(if (isTr) "Bağlantı" else "I/O", s.connectivity, NeonPurple) else null,
            if (s.maxSplDb > 0) HeroKpiPill("Max SPL", "${s.maxSplDb} dB", NeonAmber) else null,
        )
        is MousepadSpec -> listOfNotNull(
            if (s.surfaceType.isNotBlank()) HeroKpiPill(if (isTr) "Yüzey" else "Surface", s.surfaceType, ElectricBlue) else null,
            if (s.speedGlideTier.isNotBlank()) HeroKpiPill(if (isTr) "Hız" else "Glide", s.speedGlideTier, NeonCyan) else null,
            if (s.dimensionsMm.isNotBlank()) HeroKpiPill(if (isTr) "Ölçü" else "Size", s.dimensionsMm, NeonPurple) else null,
            if (s.thicknessMm > 0) HeroKpiPill(if (isTr) "Kalınlık" else "Thick", "${s.thicknessMm}mm", NeonAmber) else null,
        )
        is WebcamSpec -> listOfNotNull(
            if (s.maxResolution.isNotBlank()) HeroKpiPill("Video", s.maxResolution, ElectricBlue) else null,
            if (s.maxFrameRateFps > 0) HeroKpiPill("FPS", "${s.maxFrameRateFps} FPS", NeonCyan) else null,
            if (s.fieldOfViewDeg > 0) HeroKpiPill("FOV", "${s.fieldOfViewDeg}°", NeonPurple) else null,
            if (s.connectionType.isNotBlank()) HeroKpiPill("Port", s.connectionType, NeonAmber) else null,
        )
        is GamingChairSpec -> listOfNotNull(
            if (s.chairType.isNotBlank()) HeroKpiPill(if (isTr) "Tip" else "Type", s.chairType, ElectricBlue) else null,
            if (s.upholsteryMaterial.isNotBlank()) HeroKpiPill(if (isTr) "Kaplama" else "Material", s.upholsteryMaterial, NeonCyan) else null,
            if (s.maxWeightCapacityKg > 0) HeroKpiPill("Max", "${s.maxWeightCapacityKg} kg", NeonAmber) else null,
        )
        is CaseFanSpec -> listOfNotNull(
            if (s.fanSizeMm > 0) HeroKpiPill(if (isTr) "Boyut" else "Size", "${s.fanSizeMm} mm", ElectricBlue) else null,
            if (s.maxRpm > 0) HeroKpiPill("Max RPM", "${s.maxRpm}", NeonCyan) else null,
            if (s.maxAirflowCfm > 0) HeroKpiPill("Airflow", "${s.maxAirflowCfm} CFM", NeonPurple) else null,
            if (s.noiseLevelDba > 0) HeroKpiPill("Ses", "${s.noiseLevelDba} dBA", NeonAmber) else null,
        )
        is UpsSpec -> listOfNotNull(
            if (s.vaRating > 0) HeroKpiPill("VA", "${s.vaRating} VA", ElectricBlue) else null,
            if (s.wattCapacity > 0) HeroKpiPill("Watt", "${s.wattCapacity}W", NeonCyan) else null,
            if (s.sinewaveType.isNotBlank()) HeroKpiPill(if (isTr) "Dalga" else "Wave", s.sinewaveType, NeonEmerald) else null,
        )
        is GamingDeskSpec -> listOfNotNull(
            if (s.deskWidthCm > 0) HeroKpiPill(if (isTr) "Ölçü" else "Size", "${s.deskWidthCm}x${s.deskDepthCm}cm", ElectricBlue) else null,
            if (s.maxLoadKg > 0) HeroKpiPill(if (isTr) "Kapasite" else "Load", "${s.maxLoadKg} kg", NeonCyan) else null,
            HeroKpiPill("Motor", if (s.isMotorizedHeight) "Motorlu" else "Sabit", NeonAmber),
        )
        is GamingLightingSpec -> listOfNotNull(
            if (s.formFactor.isNotBlank()) HeroKpiPill("Form", s.formFactor, ElectricBlue) else null,
            if (s.maxLumens > 0) HeroKpiPill("Lümen", "${s.maxLumens} lm", NeonCyan) else null,
            if (s.criScore > 0) HeroKpiPill("CRI", "Ra ${s.criScore}", NeonEmerald) else null,
        )
        is SmartwatchSpec -> listOfNotNull(
            if (s.screenSizeInch > 0) HeroKpiPill("Ekran", "${s.screenSizeInch}\"", ElectricBlue) else null,
            if (s.batteryLifeDays > 0) HeroKpiPill("Pil", "${s.batteryLifeDays} Gün", NeonCyan) else null,
            if (s.waterResistanceAtm > 0) HeroKpiPill("Su", "${s.waterResistanceAtm} ATM", NeonPurple) else null,
        )
        is RouterSpec -> listOfNotNull(
            if (s.wifiStandard.isNotBlank()) HeroKpiPill("Wi-Fi", s.wifiStandard, ElectricBlue) else null,
            if (s.speedClass.isNotBlank()) HeroKpiPill("Hız", s.speedClass, NeonCyan) else null,
            if (s.ethernetPorts.isNotBlank()) HeroKpiPill("Portlar", s.ethernetPorts, NeonPurple) else null,
        )
        is ModemSpec -> listOfNotNull(
            if (s.vdslProfile.isNotBlank()) HeroKpiPill("Hat", s.vdslProfile, ElectricBlue) else null,
            if (s.wifiStandard.isNotBlank()) HeroKpiPill("Wi-Fi", s.wifiStandard, NeonCyan) else null,
            if (s.chipset.isNotBlank()) HeroKpiPill("SoC", s.chipset, NeonPurple) else null,
        )
        is AccessPointSpec -> listOfNotNull(
            if (s.wifiStandard.isNotBlank()) HeroKpiPill("Wi-Fi", s.wifiStandard, ElectricBlue) else null,
            if (s.poeSupport.isNotBlank()) HeroKpiPill("PoE", s.poeSupport, NeonCyan) else null,
            if (s.maxClients > 0) HeroKpiPill("İstemci", "${s.maxClients}+", NeonAmber) else null,
        )
        is RangeExtenderSpec -> listOfNotNull(
            if (s.wifiStandard.isNotBlank()) HeroKpiPill("Wi-Fi", s.wifiStandard, ElectricBlue) else null,
            if (s.meshProtocol.isNotBlank()) HeroKpiPill("Mesh", s.meshProtocol, NeonCyan) else null,
            if (s.coverageAreaM2 > 0) HeroKpiPill("Alan", "${s.coverageAreaM2} m²", NeonEmerald) else null,
        )
    }
    return raw.filter { it.value.isNotBlank() }.take(4)
}

private fun buildGroupedSpecSections(
    item: HardwareItem,
    strings: AppStrings,
    isTr: Boolean,
    appSettings: AppSettings = AppSettings(),
): List<SpecAccordionGroup> {
    val badgeLabel = if (isTr) "özellik" else "specs"
    val isEnthusiast = appSettings.enthusiastMode
    val tempUnit = appSettings.temperatureUnit
    val lengthUnit = appSettings.lengthUnit
    val extraMutable = item.extraSpecs.toMutableMap()
    fun popExtra(vararg keys: String): List<Pair<String, String>> = keys.mapNotNull { k ->
        extraMutable.remove(k)?.takeIf { it.isNotBlank() }?.let { k to it }
    }

    val groups = mutableListOf<SpecAccordionGroup>()

    when (val s = item.spec) {
        is CpuSpec -> {
            val archRows = cleanSpecRows(
                listOfNotNull(
                    strings.specBrand to item.brand,
                    strings.specModelSku to item.model,
                    strings.specSocket to s.socket,
                    strings.specCoreTopology to (s.coreTopology ?: "${s.coreCount} ${strings.specCoresLabel} / ${s.threadCount} ${strings.specThreadsLabel}"),
                    strings.specArchNode to listOfNotNull(s.architecture, s.processNode).joinToString(" • "),
                    if (isEnthusiast) s.dieSizeMm2?.let { (if (isTr) "Silikon Zar Alanı (Die)" else "Die Size") to "$it mm²" } else null,
                    if (isEnthusiast) s.transistorsBillion?.let { (if (isTr) "Transistör Sayısı" else "Transistors") to "$it Milyar" } else null,
                    if (isEnthusiast) s.l1Cache?.let { "L1 Önbellek" to it } else null,
                    strings.specL2L3Cache to listOfNotNull(s.l2CacheMb?.let { "$it MB L2" }, s.l3CacheMb?.let { "$it MB L3" }).joinToString(" • "),
                    s.releaseDate?.let { (if (isTr) "Çıkış Tarihi" else "Release Date") to it },
                ) + if (isEnthusiast) popExtra("Silikon Kod Adı (Codename)", "Üretim Teknolojisi (Litografi)", "Çıkış Tarihi (Release)") else emptyList()
            )
            if (archRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "⚡ Silikon, Mimari & Önbellek" else "⚡ Silicon & Cache", badgeLabel, ElectricBlue, archRows, true)

            val clockPowerRows = cleanSpecRows(
                listOfNotNull(
                    strings.specBaseBoostClocks to listOfNotNull(
                        s.baseClock.mhz.takeIf { it > 0 }?.let { "${it / 1000f} GHz" },
                        s.boostClock?.mhz?.takeIf { it > 0 }?.let { "${it / 1000f} GHz" },
                    ).joinToString(" / "),
                    strings.specTdpPowerLimits to (s.powerLimits ?: "${s.tdp.watts} W TDP"),
                    s.tjMaxCelsius?.let { (if (isTr) "Maks. Çalışma Sıcaklığı (TjMax)" else "TjMax Temp") to formatTemperature(it, tempUnit) },
                    if (isEnthusiast) s.instructionSets?.let { (if (isTr) "Komut Setleri (ISA)" else "Instruction Sets") to it } else null,
                    s.npuTops?.let { "AI NPU Performansı" to it },
                    strings.specMultiplier to when (s.unlockedMultiplier) {
                        true -> strings.specUnlocked
                        false -> strings.specLocked
                        null -> ""
                    },
                )
            )
            if (clockPowerRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "🔥 Frekans, Komut Seti & Termal" else "🔥 Clocks, ISA & Thermal", badgeLabel, NeonAmber, clockPowerRows, true)

            val ioRows = cleanSpecRows(
                listOfNotNull(
                    strings.specMemController to (s.maxMemorySpeed ?: ""),
                    s.memoryChannels?.let { (if (isTr) "Bellek Kanal Sayısı" else "Memory Channels") to "$it Kanal" },
                    if (isEnthusiast) s.eccSupport?.let { "ECC Bellek Desteği" to if (it) "Var" else "Yok" } else null,
                    strings.specPcieVersion to (s.pcieLanesDetail ?: s.pcieVersion.orEmpty()),
                    strings.specIgpu to (s.igpuModel ?: if (s.hasIntegratedGraphics) strings.specIgpuIncluded else strings.specIgpuRequired),
                )
            )
            if (ioRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "🔌 Bellek Kontrolcü & PCIe G/Ç" else "🔌 Memory & PCIe I/O", badgeLabel, NeonCyan, ioRows, true)
        }

        is GpuSpec -> {
            val archRows = cleanSpecRows(
                listOfNotNull(
                    strings.specBrand to item.brand,
                    strings.specGpuArch to listOfNotNull(s.chipset, s.architecture, if (isEnthusiast) s.gpuDie else null).joinToString(" • "),
                    strings.specProcessNode to (s.processNode ?: ""),
                    strings.specShadersRtTensor to listOfNotNull(
                        s.cudaCores?.let { "$it Shader/CUDA" },
                        if (isEnthusiast) s.tmus?.let { "$it TMU" } else null,
                        if (isEnthusiast) s.rops?.let { "$it ROP" } else null,
                        s.rtCores?.let { "$it RT" },
                        s.tensorAiTops,
                    ).joinToString(" • "),
                    if (isEnthusiast) s.fp32Tflops?.let { "FP32 Saf Hesaplama Gücü" to "$it TFLOPS" } else null,
                    s.upscalingTech?.let { "Upscaling & Frame Gen" to it },
                    s.mediaEngines?.let { "Medya / Video Kodlayıcı" to it },
                    s.releaseDate?.let { (if (isTr) "Çıkış Tarihi" else "Release Date") to it },
                ) + if (isEnthusiast) popExtra("GPU Silikon Çekirdeği (Die)", "Shaders / TMUs / ROPs", "FP32 Tek Duyarlıklı Güç", "Çıkış Tarihi (Release)") else emptyList()
            )
            if (archRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "⚡ GPU Mimarisi & Matematik Birimleri" else "⚡ GPU Architecture", badgeLabel, ElectricBlue, archRows, true)

            val memRows = cleanSpecRows(
                listOfNotNull(
                    strings.specVramCapacityType to listOfNotNull(
                        (s.vram.megabytes / 1024).takeIf { it > 0 }?.let { "$it GB" },
                        s.memoryType.ifBlank { null },
                        s.memorySpeedGbps?.let { "$it Gbps" },
                    ).joinToString(" • "),
                    strings.specMemoryBusBandwidth to listOfNotNull(
                        s.busWidthBits?.let { "$it-Bit" },
                        s.bandwidthGbs?.let { "$it GB/s" },
                    ).joinToString(" • "),
                    if (isEnthusiast) s.l2CacheMb?.let { "GPU L2 Önbellek" to "$it MB" } else null,
                )
            )
            if (memRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "🧠 Bellek (VRAM) & Veri Yolu" else "🧠 Memory Subsystem", badgeLabel, NeonPurple, memRows, true)

            val physRows = cleanSpecRows(
                listOfNotNull(
                    (if (isTr) "Çekirdek Frekansı (Base / Boost)" else "Core Clocks") to listOfNotNull(
                        s.coreClock.mhz.takeIf { it > 0 }?.let { "$it MHz" },
                        s.boostClock?.mhz?.takeIf { it > 0 }?.let { "$it MHz" },
                    ).joinToString(" / "),
                    s.pcieInterface?.let { "PCIe Veri Yolu" to it },
                    (if (isTr) "TGP / Önerilen PSU" else "TGP / PSU") to "${s.tdp.watts}W / ${s.recommendedPsu.watts}W PSU",
                    (if (isTr) "Güç Girişleri" else "Power Connectors") to (s.powerConnectors ?: ""),
                    (if (isTr) "Boyut & Kalınlık" else "Dimensions") to listOfNotNull(s.lengthMm?.let { formatLengthMm(it, lengthUnit) }, s.slotWidth).joinToString(" • "),
                    (if (isTr) "Görüntü Çıkışları" else "Display Outputs") to (s.displayOutputs ?: ""),
                    (if (isTr) "Parça No (SKU)" else "Part Number") to (s.partNumber ?: item.model),
                ) + if (isEnthusiast) popExtra("PCIe Veri Yolu Arayüzü") else emptyList()
            )
            if (physRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "🔌 Frekans, Güç & Fiziksel Ölçüler" else "🔌 Clocks, Power & Board", badgeLabel, NeonCyan, physRows, true)
        }

        is PhoneSpec -> {
            val perfRows = cleanSpecRows(
                listOfNotNull(
                    (if (isTr) "Yonga Seti (SoC)" else "Chipset") to if (s.processNodeNm > 0) "${s.chipset} (${s.processNodeNm}nm)" else s.chipset,
                    (if (isTr) "CPU Çekirdek Yapısı" else "CPU Architecture") to s.cpuArch,
                    (if (isTr) "GPU Birimi" else "GPU") to s.gpuModel,
                    (if (isTr) "Bellek & Depolama" else "RAM & Storage") to listOfNotNull(
                        s.ramGb.takeIf { it > 0 }?.let { "${it}GB ${s.ramType}".trim() },
                        s.storageGb.takeIf { it > 0 }?.let { "${it}GB ${s.storageType}".trim() },
                    ).joinToString(" • "),
                    s.dxomarkCamera?.let { "DXOMARK Arka Kamera" to "$it Puan" },
                    s.dxomarkSelfie?.let { "DXOMARK Selfie" to "$it Puan" },
                    s.dxomarkDisplay?.let { "DXOMARK Ekran" to "$it Puan" },
                    s.dxomarkBattery?.let { "DXOMARK Batarya" to "$it Puan" },
                    s.dxomarkAudio?.let { "DXOMARK Ses" to "$it Puan" },
                ) + popExtra("DXOMARK Arka Kamera Puanı", "DXOMARK Selfie (Ön) Kamera Puanı", "DXOMARK Ekran Kalite Puanı", "DXOMARK Batarya Performans Puanı", "DXOMARK Ses / Hoparlör Puanı")
            )
            if (perfRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "🚀 Yonga, Bellek & DXOMARK" else "🚀 SoC, Memory & DXOMARK", badgeLabel, ElectricBlue, perfRows, true)

            val dispCamRows = cleanSpecRows(
                listOfNotNull(
                    (if (isTr) "Ekran Paneli" else "Display") to listOfNotNull(s.displaySizeInch.takeIf { it > 0 }?.let { "$it\"" }, s.displayTech.ifBlank { null }, s.refreshRateHz.takeIf { it > 0 }?.let { "$it Hz" }).joinToString(" "),
                    (if (isTr) "Çözünürlük & Yoğunluk" else "Resolution") to listOfNotNull(s.resolution.ifBlank { null }, s.ppi.takeIf { it > 0 }?.let { "$it PPI" }).joinToString(" • "),
                    (if (isTr) "Parlaklık & Cam Koruma" else "Brightness & Glass") to listOfNotNull(s.peakBrightnessNit.takeIf { it > 0 }?.let { "$it Nit" }, s.glassProtection.ifBlank { null }).joinToString(" • "),
                    s.pwmDimmingHz?.let { "PWM Göz Koruma Frekansı" to "${it}Hz" },
                    (if (isTr) "Ana Kamera" else "Main Camera") to listOfNotNull(s.mainCameraMp.takeIf { it > 0 }?.let { "$it MP" }, s.mainSensorSize.ifBlank { null }, s.mainAperture.ifBlank { null }, if (s.hasOis) s.oisType.ifBlank { "OIS" } else null).joinToString(" • "),
                    s.opticalZoomX?.let { "Telefoto Zoom" to "${s.telephotoMp ?: 0} MP • ${it}x Optik" },
                    s.ultraWideMp?.let { "Ultra Geniş" to "$it MP" },
                    (if (isTr) "Video Kayıt" else "Video") to s.videoResolution,
                )
            )
            if (dispCamRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "📸 Ekran & Kamera Mühendisliği" else "📸 Display & Camera", badgeLabel, NeonPurple, dispCamRows, true)

            val connBattRows = cleanSpecRows(
                listOfNotNull(
                    (if (isTr) "Batarya & Şarj" else "Battery") to listOfNotNull(
                        s.batteryMah.takeIf { it > 0 }?.let { "$it mAh" },
                        s.wiredChargingWatt.takeIf { it > 0 }?.let { "${it}W Kablolu" },
                        s.wirelessChargingWatt?.let { "${it}W Kablosuz" },
                    ).joinToString(" • "),
                    s.usbSpeed?.let { "USB Port Hızı" to it },
                    s.wifiBluetooth?.let { "Wi-Fi & Bluetooth" to it },
                    (if (isTr) "Kasa & Dayanıklılık" else "Build") to listOfNotNull(s.frameMaterial.ifBlank { null }, s.waterResistance.ifBlank { null }, s.weightGrams.takeIf { it > 0 }?.let { "${it}g" }).joinToString(" • "),
                    (if (isTr) "İşletim Sistemi" else "OS") to if (s.updateYears > 0) "${s.osName} (${s.updateYears} Yıl Güncelleme)" else s.osName,
                )
            )
            if (connBattRows.isNotEmpty()) groups += SpecAccordionGroup(if (isTr) "🔋 Batarya, Kasa & Bağlantı" else "🔋 Battery & Connectivity", badgeLabel, NeonCyan, connBattRows, true)
        }

        else -> {
            // Structured core technical specs from buildPolymorphicSpecRows (cleanly filtered)
            val coreRows = buildPolymorphicSpecRows(item, strings)
            if (coreRows.isNotEmpty()) {
                groups += SpecAccordionGroup(
                    title = if (isTr) "⚙️ Çekirdek Teknik Özellikler" else "⚙️ Core Technical Specs",
                    badge = badgeLabel,
                    accentColor = ElectricBlue,
                    rows = coreRows,
                    initiallyExpanded = true,
                )
            }
        }
    }

    // Add benchmark rows cleanly inside CPU/GPU if available
    item.benchmarkInfo?.let { bench ->
        val benchRows = mutableListOf<Pair<String, String>>()
        bench.cpuSynthetic?.let { c ->
            if (c.geekbench6Single != null && c.geekbench6Multi != null) {
                benchRows += "Geekbench 6 (Single / Multi)" to "${formatPoints(c.geekbench6Single)} / ${formatPoints(c.geekbench6Multi)} pts"
            }
            if (c.cinebenchR23Single != null && c.cinebenchR23Multi != null) {
                benchRows += "Cinebench R23 (Single / Multi)" to "${formatPoints(c.cinebenchR23Single)} / ${formatPoints(c.cinebenchR23Multi)} pts"
            }
            if (c.cinebench2024Single != null && c.cinebench2024Multi != null) {
                benchRows += "Cinebench 2024 (Single / Multi)" to "${formatPoints(c.cinebench2024Single)} / ${formatPoints(c.cinebench2024Multi)} pts"
            }
            c.passmarkScore?.let { benchRows += "PassMark CPU Mark" to "${formatPoints(it)} pts" }
            c.passmarkSingle?.let { benchRows += "PassMark Single Thread" to "${formatPoints(it)} pts" }
            c.blenderScore?.let { benchRows += "Blender Benchmark" to "${formatPoints(it)} samples/m" }
            c.sevenZipMips?.let { benchRows += "7-Zip Sıkıştırma" to "${formatPoints(it)} MIPS" }
        }
        bench.gpuSynthetic?.let { g ->
            g.timeSpyGraphics?.let { benchRows += "3DMark Time Spy Graphics" to "${formatPoints(it)} pts" }
            g.timeSpyExtreme?.let { benchRows += "3DMark Time Spy Extreme" to "${formatPoints(it)} pts" }
            g.steelNomad?.let { benchRows += "3DMark Steel Nomad" to "${formatPoints(it)} pts" }
            g.portRoyal?.let { benchRows += "3DMark Port Royal (RT)" to "${formatPoints(it)} pts" }
            g.speedWay?.let { benchRows += "3DMark Speed Way (DX12U)" to "${formatPoints(it)} pts" }
            g.passmarkG3d?.let { benchRows += "PassMark G3D Mark" to "${formatPoints(it)} pts" }
        }
        if (benchRows.isNotEmpty() && (item.spec is CpuSpec || item.spec is GpuSpec)) {
            groups += SpecAccordionGroup(
                title = if (isTr) "📊 Doğrulanmış Sentetik Benchmarklar" else "📊 Verified Synthetic Benchmarks",
                badge = if (isTr) "skor" else "scores",
                accentColor = NeonPurple,
                rows = benchRows,
                initiallyExpanded = true,
            )
        }
    }

    // Remaining multi-source extra specs (only expanded/shown in Enthusiast / Geek Mode)
    val remainingExtras = extraMutable.entries
        .filter { (k, v) -> k.isNotBlank() && v.isNotBlank() }
        .map { it.key to it.value }
    if (isEnthusiast && remainingExtras.isNotEmpty()) {
        groups += SpecAccordionGroup(
            title = if (isTr) "🔬 Ek Mühendislik & Kaynak Verileri" else "🔬 Extended Engineering Telemetry",
            badge = badgeLabel,
            accentColor = NeonEmerald,
            rows = remainingExtras,
            initiallyExpanded = true,
        )
    }

    return groups
}
