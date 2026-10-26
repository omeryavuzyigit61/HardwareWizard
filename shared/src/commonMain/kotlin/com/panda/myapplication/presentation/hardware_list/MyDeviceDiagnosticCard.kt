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
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.panda.myapplication.domain.util.getMyDeviceDiagnostic
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.Spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyDeviceDiagnosticCard(
    modifier: Modifier = Modifier,
) {
    val diagnostic = remember { getMyDeviceDiagnostic() }
    var isExpanded by remember { mutableStateOf(false) }

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
            // Header Row (Never squishes, fully weighted)
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
                        contentDescription = "Cihazım",
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
                            text = "📱 CİHAZIM DONANIM TEŞHİSİ",
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
                                text = "CANLI SİSTEM",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${diagnostic.manufacturer} ${diagnostic.modelName}",
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
                                text = "Katalogda İlk %${100 - diagnostic.catalogPercentile} Dilimde",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Expand / Collapse Chevron indicator
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(chevronRotation),
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Quick Specs Chips Matrix
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        DiagChip(
                            icon = Icons.Rounded.Bolt,
                            label = "Yonga Seti",
                            value = diagnostic.socName,
                            color = NeonCyan,
                        )
                        DiagChip(
                            icon = Icons.Rounded.Memory,
                            label = "Bellek",
                            value = "${diagnostic.totalRamGb.toInt()} GB RAM (${diagnostic.availableRamGb} GB Boş)",
                            color = NeonPurple,
                        )
                        DiagChip(
                            icon = Icons.Rounded.Speed,
                            label = "Ekran Hızı",
                            value = "${diagnostic.refreshRateHz} Hz Akıcı Panel",
                            color = NeonEmerald,
                        )
                        DiagChip(
                            icon = if (diagnostic.isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                            label = "Pil",
                            value = "%${diagnostic.batteryPercent}",
                            color = ElectricBlue,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Performance Percentile Explanation
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
                            Text(text = "📊", fontSize = 16.sp)
                            Text(
                                text = "Cihazınız katalogdaki 500+ mobil amiral gemisi ve orta segment cihazla kıyaslandı. Çoklu çekirdek ve grafik çıktısında üstün performans sergiliyor.",
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
