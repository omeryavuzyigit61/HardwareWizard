package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CameraFront
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.TabletSpec
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.util.LocalAppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MobileBenchmarkCard(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish

    val phoneSpec = item.spec as? PhoneSpec
    val tabletSpec = item.spec as? TabletSpec

    val antutuScore = phoneSpec?.antutuScore ?: tabletSpec?.antutuScore
    val geekSingle = phoneSpec?.geekbenchSingle ?: tabletSpec?.geekbenchSingle
    val geekMulti = phoneSpec?.geekbenchMulti ?: tabletSpec?.geekbenchMulti
    val stabilityPct = phoneSpec?.stabilityPercent ?: tabletSpec?.stabilityPercent
    val chipset = phoneSpec?.chipset ?: tabletSpec?.chipset ?: ""
    val processNode = (phoneSpec?.processNodeNm ?: tabletSpec?.processNodeNm ?: 0).takeIf { it > 0 }
    val cleanChipset = when {
        chipset.contains("nm", ignoreCase = true) -> chipset
        processNode != null -> "$chipset (${processNode}nm)"
        else -> chipset
    }
    val pubgFps = phoneSpec?.pubgFpsSupport?.takeIf { it.isNotBlank() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(ElectricBlue.copy(alpha = 0.35f)),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Analytics,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTr) "Mobil Benchmark & Kararlılık" else "Mobile Benchmark & Stability",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (isTr) "Doğrulanmış AnTuTu v10 & Geekbench 6" else "Verified AnTuTu v10 & Geekbench 6",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.small))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NeonEmerald.copy(alpha = 0.15f))
                        .border(1.dp, NeonEmerald.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = if (isTr) "Doğrulanmış" else "Verified",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(Spacing.medium))

            if (antutuScore != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(Spacing.medium),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = "AnTuTu v10",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue,
                                    maxLines = 1,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(ElectricBlue)
                                        .size(5.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cleanChipset,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatPoints(antutuScore)} ${if (isTr) "Puan" else "pts"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Spacer(modifier = Modifier.width(Spacing.small))

                        val tierText = when {
                            antutuScore >= 2_000_000 -> if (isTr) "S++ Amiral" else "S++ Flagship"
                            antutuScore >= 1_600_000 -> if (isTr) "S+ Amiral" else "S+ Flagship"
                            antutuScore >= 1_200_000 -> if (isTr) "S Üst Seviye" else "S High-End"
                            antutuScore >= 800_000 -> if (isTr) "A Orta-Üst" else "A Upper-Mid"
                            else -> if (isTr) "B Giriş-Orta" else "B Entry-Mid"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .border(1.dp, NeonEmerald.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = tierText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonEmerald,
                                maxLines = 1,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.small))
            }

            if (geekSingle != null && geekMulti != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(NeonCyan.copy(alpha = 0.25f)),
                        ),
                    ) {
                        Column(modifier = Modifier.padding(Spacing.small)) {
                            Text(
                                text = if (isTr) "Tek Çekirdek (GB6)" else "Single-Core (GB6)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatPoints(geekSingle)} pts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                            )
                            Text(
                                text = if (isTr) "Anlık tepki hızı" else "Instant UI responsiveness",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = SolidColor(NeonPurple.copy(alpha = 0.25f)),
                        ),
                    ) {
                        Column(modifier = Modifier.padding(Spacing.small)) {
                            Text(
                                text = if (isTr) "Çoklu Çekirdek (GB6)" else "Multi-Core (GB6)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatPoints(geekMulti)} pts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonPurple,
                            )
                            Text(
                                text = if (isTr) "Render & çoklu görev" else "Export & heavy multitasking",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.small))
            }

            if (stabilityPct != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(Spacing.small),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Thermostat,
                                    contentDescription = null,
                                    tint = if (stabilityPct >= 70) NeonEmerald else NeonAmber,
                                    modifier = Modifier.size(15.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTr) "3DMark Termal Kararlılık (Wild Life Extreme)" else "3DMark Thermal Stability",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text(
                                text = if (isTr) "%$stabilityPct" else "$stabilityPct%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = if (stabilityPct >= 70) NeonEmerald else NeonAmber,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val progressAnim by animateFloatAsState(
                            targetValue = (stabilityPct / 100f).coerceIn(0f, 1f),
                            animationSpec = tween(durationMillis = 600),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressAnim)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (stabilityPct >= 70) NeonEmerald else NeonAmber),
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTr) {
                                "20 dakikalık kesintisiz ağır oyun yükünde performans koruma oranı."
                            } else {
                                "Performance retention percentage across continuous 20-min stress testing."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.small))
            }

            Text(
                text = if (isTr) "Resmi Oyun Desteği & Donanım Rozetleri" else "Official Game Capabilities & Badges",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                phoneSpec?.dxomarkCamera?.let { cam ->
                    GameCapabilityChip(
                        icon = Icons.Rounded.PhotoCamera,
                        text = if (isTr) "DXOMARK Kamera: $cam" else "DXOMARK Camera: $cam",
                        accentColor = NeonEmerald,
                    )
                }
                phoneSpec?.dxomarkSelfie?.let { sf ->
                    GameCapabilityChip(
                        icon = Icons.Rounded.CameraFront,
                        text = "DXOMARK Selfie: $sf",
                        accentColor = NeonCyan,
                    )
                }
                phoneSpec?.dxomarkDisplay?.let { dp ->
                    GameCapabilityChip(
                        icon = Icons.Rounded.Smartphone,
                        text = if (isTr) "DXOMARK Ekran: $dp" else "DXOMARK Display: $dp",
                        accentColor = ElectricBlue,
                    )
                }
                phoneSpec?.dxomarkBattery?.let { bt ->
                    GameCapabilityChip(
                        icon = Icons.Rounded.BatteryChargingFull,
                        text = if (isTr) "DXOMARK Pil: $bt" else "DXOMARK Battery: $bt",
                        accentColor = NeonAmber,
                    )
                }
                phoneSpec?.dxomarkAudio?.let { au ->
                    GameCapabilityChip(
                        icon = Icons.Rounded.VolumeUp,
                        text = if (isTr) "DXOMARK Ses: $au" else "DXOMARK Audio: $au",
                        accentColor = NeonPurple,
                    )
                }

                if (pubgFps != null) {
                    GameCapabilityChip(
                        icon = Icons.Rounded.SportsEsports,
                        text = "PUBG Mobile: $pubgFps",
                        accentColor = NeonEmerald,
                    )
                }

                val hz = (phoneSpec?.refreshRateHz ?: tabletSpec?.refreshRateHz ?: 0)
                if (hz >= 90) {
                    GameCapabilityChip(
                        icon = Icons.Rounded.Bolt,
                        text = if (isTr) "${hz}Hz Akıcı Panel" else "${hz}Hz Smooth Display",
                        accentColor = ElectricBlue,
                    )
                }

                if (hasMobileHardwareRayTracing(chipset)) {
                    GameCapabilityChip(
                        icon = Icons.Rounded.AutoAwesome,
                        text = if (isTr) "Donanımsal Ray Tracing" else "Hardware Ray Tracing",
                        accentColor = NeonPurple,
                    )
                }

                if (phoneSpec?.hasOis == true && phoneSpec.oisType.isNotBlank()) {
                    GameCapabilityChip(
                        icon = Icons.Rounded.CenterFocusStrong,
                        text = phoneSpec.oisType,
                        accentColor = NeonCyan,
                    )
                }
                if (tabletSpec != null && tabletSpec.stylusSupport.isNotBlank()) {
                    GameCapabilityChip(
                        icon = Icons.Rounded.Draw,
                        text = tabletSpec.stylusSupport,
                        accentColor = NeonAmber,
                    )
                }
            }
        }
    }
}

internal fun hasMobileHardwareRayTracing(chipset: String): Boolean {
    val c = chipset.lowercase()
    return c.contains("a17 pro") ||
        c.contains("a18") ||
        c.contains("a19") ||
        c.contains("m3") ||
        c.contains("m4") ||
        c.contains("8 gen 2") ||
        c.contains("8 gen 3") ||
        c.contains("8s gen 3") ||
        c.contains("8 elite") ||
        c.contains("dimensity 9200") ||
        c.contains("dimensity 9300") ||
        c.contains("dimensity 9400") ||
        c.contains("exynos 2200") ||
        c.contains("exynos 2400") ||
        c.contains("exynos 2500")
}

@Composable
private fun GameCapabilityChip(
    icon: ImageVector,
    text: String,
    accentColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }
    }
}

internal fun formatPoints(value: Int): String {
    val s = value.toString()
    val sb = StringBuilder()
    for (i in s.indices) {
        sb.append(s[i])
        val rem = s.length - 1 - i
        if (rem > 0 && rem % 3 == 0) {
            sb.append(".")
        }
    }
    return sb.toString()
}
