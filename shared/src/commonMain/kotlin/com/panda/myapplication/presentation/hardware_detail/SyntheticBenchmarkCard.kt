package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panda.myapplication.domain.model.CpuSyntheticBenchmark
import com.panda.myapplication.domain.model.GpuSyntheticBenchmark
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.HardwareWizardTheme
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose

/**
 * Modern synthetic benchmark card adhering to CPU-Monkey / 3DMark standards.
 * Supports multi-benchmark suites, single-core/multi-core CPU tabs, and GPU synthetic tests.
 * Gracefully renders null tests with muted fallback badges ("Test Verisi Bulunamadı").
 */
@Composable
fun SyntheticBenchmarkCard(
    category: HardwareCategory,
    benchmarkInfo: HardwareBenchmarkInfo?,
    modifier: Modifier = Modifier,
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    if (benchmarkInfo == null && category != HardwareCategory.CPU && category != HardwareCategory.GPU) {
        return
    }

    val isCpu = category == HardwareCategory.CPU || benchmarkInfo?.cpuSynthetic != null
    val isGpu = category == HardwareCategory.GPU || benchmarkInfo?.gpuSynthetic != null

    if (!isCpu && !isGpu) {
        return
    }

    var selectedCpuTab by remember { mutableIntStateOf(0) }

    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = if (isCpu) ElectricBlue else NeonRose,
        secondaryColor = NeonPurple,
        shape = RoundedCornerShape(Spacing.cardRadius),
        contentPadding = Spacing.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            // Header Row: Dot Indicator + Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = if (isCpu) tr(isTr, "Sentetik CPU Benchmark (CPU-Monkey)", "Synthetic CPU Benchmarks (CPU-Monkey)") else tr(isTr, "3DMark Sentetik Grafik Benchmark", "3DMark Synthetic Graphics Benchmarks"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = benchmarkInfo?.testSuite ?: tr(isTr, "Donanım Benchmark Laboratuvarı", "Hardware Benchmark Lab"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.small))

                // Verification and Tier Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val isVerified = benchmarkInfo?.isVerified == true
                    val sourceText = if (isVerified) tr(isTr, "Doğrulanmış Test", "Verified Benchmark") else tr(isTr, "Simülasyon", "Simulation")
                    val sourceContainer = if (isVerified) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                    val sourceContent = if (isVerified) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(sourceContainer)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .padding(horizontal = Spacing.small, vertical = 2.dp),
                    ) {
                        Text(
                            text = sourceText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = sourceContent,
                        )
                    }

                    val tier = benchmarkInfo?.tier ?: "A-Tier"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = Spacing.small, vertical = 2.dp),
                    ) {
                        Text(
                            text = tier,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            // Content Body: CPU Tabs or GPU Direct List
            if (isCpu) {
                val cpuData = benchmarkInfo?.cpuSynthetic

                // Secondary Tab Row for Single-Core vs Multi-Core
                SecondaryTabRow(
                    selectedTabIndex = selectedCpuTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                ) {
                    Tab(
                        selected = selectedCpuTab == 0,
                        onClick = { selectedCpuTab = 0 },
                        text = {
                            Text(
                                text = tr(isTr, "Tek Çekirdek (Single-Core)", "Single-Core"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedCpuTab == 0) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                    Tab(
                        selected = selectedCpuTab == 1,
                        onClick = { selectedCpuTab = 1 },
                        text = {
                            Text(
                                text = tr(isTr, "Çok Çekirdek (Multi-Core)", "Multi-Core"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedCpuTab == 1) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.extraSmall))

                if (selectedCpuTab == 0) {
                    // Single-Core Test Items
                    SyntheticMetricRow(
                        testName = "Cinebench 2024 (Single-Core)",
                        score = cpuData?.cinebench2024Single,
                        referenceCeiling = 180,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "180 pts referans tepe tek çekirdek performansı", "180 pts reference peak single-core performance"),
                    )
                    SyntheticMetricRow(
                        testName = "Cinebench R23 (Single-Core)",
                        score = cpuData?.cinebenchR23Single,
                        referenceCeiling = 2500,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "2,500 pts masaüstü tek çekirdek referansı", "2,500 pts desktop single-core reference"),
                    )
                    SyntheticMetricRow(
                        testName = "Geekbench 6 (Single-Core)",
                        score = cpuData?.geekbench6Single,
                        referenceCeiling = 3800,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "3,800 pts referans modern mimari skoru", "3,800 pts modern architecture reference score"),
                    )
                } else {
                    // Multi-Core Test Items
                    SyntheticMetricRow(
                        testName = "Cinebench 2024 (Multi-Core)",
                        score = cpuData?.cinebench2024Multi,
                        referenceCeiling = 3000,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "3,000 pts tepe iş istasyonu render referansı", "3,000 pts peak workstation render reference"),
                    )
                    SyntheticMetricRow(
                        testName = "Cinebench R23 (Multi-Core)",
                        score = cpuData?.cinebenchR23Multi,
                        referenceCeiling = 45000,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "45,000 pts çoklu çekirdek tepe referansı", "45,000 pts multi-core peak ceiling reference"),
                    )
                    SyntheticMetricRow(
                        testName = "Geekbench 6 (Multi-Core)",
                        score = cpuData?.geekbench6Multi,
                        referenceCeiling = 25000,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "25,000 pts genel işlem kapasitesi referansı", "25,000 pts general compute capacity reference"),
                    )
                    SyntheticMetricRow(
                        testName = "PassMark CPU Mark",
                        score = cpuData?.passmarkScore,
                        referenceCeiling = 70000,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = tr(isTr, "70,000 pts global sentetik işlemci skoru", "70,000 pts global synthetic compute ceiling"),
                    )
                }
            } else if (isGpu) {
                val gpuData = benchmarkInfo?.gpuSynthetic

                SyntheticMetricRow(
                    testName = "3DMark Time Spy Graphics",
                    score = gpuData?.timeSpyGraphics,
                    referenceCeiling = 38000,
                    accentColor = MaterialTheme.colorScheme.primary,
                    subText = tr(isTr, "DirectX 12 1440p (38,000 pts tepe grafik gücü referansı)", "DirectX 12 1440p (38,000 pts peak graphics reference)"),
                )
                SyntheticMetricRow(
                    testName = "3DMark Time Spy Extreme",
                    score = gpuData?.timeSpyExtreme,
                    referenceCeiling = 20000,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    subText = tr(isTr, "DirectX 12 4K UHD (20,000 pts tepe çözünürlük referansı)", "DirectX 12 4K UHD (20,000 pts peak 4K resolution reference)"),
                )
                SyntheticMetricRow(
                    testName = "3DMark Steel Nomad",
                    score = gpuData?.steelNomad,
                    referenceCeiling = 10000,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    subText = tr(isTr, "Modern Cross-Platform Non-Raytracing saf raster skoru", "Modern cross-platform pure raster benchmark"),
                )
            }
        }
    }
}

/**
 * Individual test metric row displaying score, animated neon progress bar,
 * or a clean muted badge if the score is null.
 */
@Composable
private fun SyntheticMetricRow(
    testName: String,
    score: Int?,
    referenceCeiling: Int,
    accentColor: Color,
    subText: String,
    modifier: Modifier = Modifier,
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val fraction = if (score != null) {
        (score.toFloat() / referenceCeiling.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 600),
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = testName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (score != null) {
                Text(
                    text = formatThousands(score) + " pts",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .padding(horizontal = Spacing.small, vertical = 2.dp),
                ) {
                    Text(
                        text = tr(isTr, "Test Verisi Bulunamadı", "No Benchmark Data Available"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.extraSmall))

        if (score != null) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = tr(isTr, "Bu benchmark için doğrulanmış ölçüm henüz katalogda bulunmuyor.", "Verified measurements for this benchmark are not yet available in catalog."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatThousands(value: Int): String {
    return value.toString().reversed().chunked(3).joinToString(",").reversed()
}

@Preview(name = "SyntheticBenchmarkCard - CPU Light")
@Composable
private fun SyntheticBenchmarkCardCpuPreview() {
    HardwareWizardTheme {
        SyntheticBenchmarkCard(
            category = HardwareCategory.CPU,
            benchmarkInfo = HardwareBenchmarkInfo(
                score = 96,
                tier = "S-Tier",
                cpuSynthetic = CpuSyntheticBenchmark(
                    cinebench2024Single = 134,
                    cinebench2024Multi = 2180,
                    cinebenchR23Single = 2240,
                    cinebenchR23Multi = 33200,
                    geekbench6Single = 3410,
                    geekbench6Multi = 20450,
                    passmarkScore = 54200,
                ),
                testSuite = "CPU-Monkey & Cinebench Lab",
            ),
        )
    }
}

@Preview(name = "SyntheticBenchmarkCard - CPU Null Values Dark")
@Composable
private fun SyntheticBenchmarkCardCpuNullPreview() {
    HardwareWizardTheme {
        SyntheticBenchmarkCard(
            category = HardwareCategory.CPU,
            benchmarkInfo = HardwareBenchmarkInfo(
                score = 80,
                tier = "B-Tier",
                cpuSynthetic = CpuSyntheticBenchmark(
                    cinebench2024Single = null,
                    cinebench2024Multi = null,
                    cinebenchR23Single = 1820,
                    cinebenchR23Multi = 18500,
                    geekbench6Single = null,
                    geekbench6Multi = null,
                    passmarkScore = null,
                ),
            ),
        )
    }
}

@Preview(name = "SyntheticBenchmarkCard - GPU Dark")
@Composable
private fun SyntheticBenchmarkCardGpuPreview() {
    HardwareWizardTheme {
        SyntheticBenchmarkCard(
            category = HardwareCategory.GPU,
            benchmarkInfo = HardwareBenchmarkInfo(
                score = 95,
                tier = "S-Tier",
                gpuSynthetic = GpuSyntheticBenchmark(
                    timeSpyGraphics = 28400,
                    timeSpyExtreme = 14100,
                    steelNomad = 6750,
                ),
                testSuite = "3DMark UL Benchmarks",
            ),
        )
    }
}

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText
