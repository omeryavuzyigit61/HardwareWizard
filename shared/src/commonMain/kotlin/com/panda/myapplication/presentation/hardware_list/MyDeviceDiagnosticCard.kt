package com.panda.myapplication.presentation.hardware_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.util.getMyDeviceDiagnostic
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.Spacing
import com.panda.myapplication.presentation.util.LocalAppStrings
import kotlinx.coroutines.flow.firstOrNull
import org.koin.mp.KoinPlatformTools
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyDeviceDiagnosticCard(
    modifier: Modifier = Modifier,
) {
    val isTr = LocalAppStrings.current.isTurkish
    var diagnostic by remember { mutableStateOf(getMyDeviceDiagnostic()) }
    var isExpanded by remember { mutableStateOf(false) }
    var realCatalogPercentile by remember { mutableStateOf<Int?>(null) }

    val hardwareRepository = remember {
        runCatching { KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<HardwareRepository>() }.getOrNull()
    }

    LaunchedEffect(diagnostic.modelName, diagnostic.totalRamGb, diagnostic.refreshRateHz, hardwareRepository) {
        val repo = hardwareRepository ?: return@LaunchedEffect
        val phonesRes = repo.observeHardwareItemsByCategory(HardwareCategory.SMARTPHONE).firstOrNull()
        val phones = (phonesRes as? DomainResult.Success)?.data.orEmpty()
        if (phones.size >= 10) {
            val deviceScore = estimateDeviceDiagnosticTierScore(
                socName = diagnostic.socName,
                ramGb = diagnostic.totalRamGb,
                refreshHz = diagnostic.refreshRateHz,
            )
            val catalogScores = phones.mapNotNull { item ->
                val spec = item.spec as? PhoneSpec ?: return@mapNotNull null
                estimateCatalogMobileScore(spec)
            }.sorted()
            if (catalogScores.isNotEmpty()) {
                val beatenCount = catalogScores.count { it <= deviceScore }
                realCatalogPercentile = ((beatenCount.toDouble() / catalogScores.size.toDouble()) * 100.0)
                    .roundToInt()
                    .coerceIn(5, 99)
            }
        }
    }

    val effectivePercentile = realCatalogPercentile ?: diagnostic.catalogPercentile
    val topSlicePercent = (100 - effectivePercentile).coerceIn(1, 95)

    val fullModelTitle = remember(diagnostic.manufacturer, diagnostic.modelName) {
        if (diagnostic.modelName.startsWith(diagnostic.manufacturer, ignoreCase = true)) {
            diagnostic.modelName
        } else {
            "${diagnostic.manufacturer} ${diagnostic.modelName}".trim()
        }
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 270f else 90f,
        label = "chevronRotation",
    )

    FluidOrganicCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.small),
        accentColor = NeonCyan,
        secondaryColor = NeonPurple,
        contentPadding = Spacing.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonCyan.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.25f))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PhoneAndroid,
                        contentDescription = if (isTr) "Cihazım" else "My Device",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = if (isTr) "CİHAZIM DONANIM TEŞHİSİ" else "MY DEVICE DIAGNOSTIC",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 0.6.sp,
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonEmerald.copy(alpha = 0.18f),
                        ) {
                            Text(
                                text = if (isTr) "CANLI SİSTEM" else "LIVE SYSTEM",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = fullModelTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonAmber.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, NeonAmber.copy(alpha = 0.4f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(11.dp),
                            )
                            Text(
                                text = if (isTr) {
                                    "Katalogda İlk %$topSlicePercent Dilimde"
                                } else {
                                    "Top $topSlicePercent% in Catalog"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { diagnostic = getMyDeviceDiagnostic() },
                    modifier = Modifier.size(30.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = if (isTr) "Yenile" else "Refresh",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = if (isExpanded) {
                            if (isTr) "Daralt" else "Collapse"
                        } else {
                            if (isTr) "Genişlet" else "Expand"
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(chevronRotation),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    val ramText = if (diagnostic.isAvailableRamMeasured) {
                        if (isTr) {
                            "${diagnostic.totalRamGb} GB RAM (${diagnostic.availableRamGb} GB Boş)"
                        } else {
                            "${diagnostic.totalRamGb} GB RAM (${diagnostic.availableRamGb} GB Free)"
                        }
                    } else {
                        "${diagnostic.totalRamGb} GB RAM"
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        DiagChip(
                            icon = Icons.Rounded.Bolt,
                            label = if (isTr) "Yonga Seti" else "Chipset",
                            value = diagnostic.socName,
                            color = NeonCyan,
                        )
                        DiagChip(
                            icon = Icons.Rounded.Memory,
                            label = if (isTr) "Bellek" else "Memory",
                            value = ramText,
                            color = NeonPurple,
                        )
                        DiagChip(
                            icon = Icons.Rounded.Speed,
                            label = if (isTr) "Ekran Hızı" else "Display",
                            value = "${diagnostic.refreshRateHz} Hz",
                            color = NeonEmerald,
                        )
                        DiagChip(
                            icon = if (diagnostic.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                            label = if (isTr) "Pil" else "Battery",
                            value = if (isTr) "%${diagnostic.batteryPercent}" else "${diagnostic.batteryPercent}%",
                            color = ElectricBlue,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Analytics,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = if (isTr) {
                                    "Cihazınızın canlı sistem telemetrisi okundu ve katalogdaki akıllı telefonlarla kıyaslandı."
                                } else {
                                    "Live system telemetry read from this device and ranked against smartphones in the catalog."
                                },
                                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun estimateDeviceDiagnosticTierScore(
    socName: String,
    ramGb: Double,
    refreshHz: Int,
): Double {
    val lower = socName.lowercase()
    val socPoints = when {
        lower.contains("8 elite") || lower.contains("a18 pro") || lower.contains("dimensity 9400") || lower.contains("m-series") -> 96.0
        lower.contains("8 gen 3") || lower.contains("a17 pro") || lower.contains("a18") || lower.contains("dimensity 9300") || lower.contains("exynos 2500") -> 90.0
        lower.contains("exynos 2400") && !lower.contains("2400e") -> 86.0
        lower.contains("8 gen 2") || lower.contains("2400e") || lower.contains("dimensity 9200") || lower.contains("a16") || lower.contains("pro bionic") -> 82.0
        lower.contains("8+ gen 1") || lower.contains("8 gen 1") || lower.contains("8s gen 3") || lower.contains("7+ gen 3") || lower.contains("tensor g4") || lower.contains("a15") -> 75.0
        lower.contains("tensor g3") || lower.contains("dimensity 8300") || lower.contains("dimensity 8200") || lower.contains("7 gen 3") -> 68.0
        lower.contains("bionic") || lower.contains("snapdragon 7") || lower.contains("dimensity 7") || lower.contains("exynos 1480") || lower.contains("exynos 1380") -> 60.0
        else -> 50.0
    }
    val ramPoints = (ramGb.coerceIn(3.0, 16.0) / 16.0) * 20.0
    val displayPoints = if (refreshHz >= 120) 10.0 else if (refreshHz >= 90) 6.0 else 3.0
    return socPoints + ramPoints + displayPoints
}

private fun estimateCatalogMobileScore(spec: PhoneSpec): Double {
    val antutu = spec.antutuScore?.toDouble()
    val socPoints = if (antutu != null && antutu > 100_000) {
        ((antutu / 2_800_000.0) * 96.0).coerceIn(30.0, 98.0)
    } else {
        estimateDeviceDiagnosticTierScore(spec.chipset, spec.ramGb.toDouble(), spec.refreshRateHz) - 20.0
    }
    val ramPoints = (spec.ramGb.coerceIn(3, 16).toDouble() / 16.0) * 20.0
    val displayPoints = if (spec.refreshRateHz >= 120) 10.0 else if (spec.refreshRateHz >= 90) 6.0 else 3.0
    return socPoints + ramPoints + displayPoints
}

@Composable
private fun DiagChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}
