package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.AppStrings
import com.panda.myapplication.presentation.util.LocalAppStrings

private enum class UndervoltTab(val emoji: String) {
    CPU("🔥"),
    GPU("🎮"),
    RAM("⚡"),
    CLEAR_CMOS("🚨");

    fun getTitle(strings: AppStrings): String = when (this) {
        CPU -> strings.uvTabCpu
        GPU -> strings.uvTabGpu
        RAM -> strings.uvTabRam
        CLEAR_CMOS -> strings.uvTabCmos
    }
}

@Composable
fun UndervoltGuideScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToTuningVault: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(UndervoltTab.CPU) }
    val strings = LocalAppStrings.current
    val isTurkish = strings.isTurkish

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = strings.drawerUndervoltTitle,
                subtitle = strings.drawerUndervoltSubtitle,
                icon = "⚡",
                onNavigateBack = onNavigateBack,
                onOpenDrawer = onOpenDrawer,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = Spacing.medium),
        ) {
            Spacer(modifier = Modifier.height(Spacing.small))

            // Action Banner to Community Tuning Vault
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NeonCyan.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigateToTuningVault() },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "⚡ Topluluk Silikon Kasası (OC/UV)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                            )
                            Text(
                                text = "Doğrulanmış ayarları inceleyin veya kendi sistem donanım profilinizi paylaşın.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = "Git",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Tab Selector Chips (Horizontally Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UndervoltTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = "${tab.emoji} ${tab.getTitle(strings)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (tab) {
                                UndervoltTab.CPU -> NeonAmber.copy(alpha = 0.2f)
                                UndervoltTab.GPU -> ElectricBlue.copy(alpha = 0.2f)
                                UndervoltTab.RAM -> NeonEmerald.copy(alpha = 0.2f)
                                UndervoltTab.CLEAR_CMOS -> NeonRose.copy(alpha = 0.2f)
                            },
                            selectedLabelColor = when (tab) {
                                UndervoltTab.CPU -> NeonAmber
                                UndervoltTab.GPU -> ElectricBlue
                                UndervoltTab.RAM -> NeonEmerald
                                UndervoltTab.CLEAR_CMOS -> NeonRose
                            },
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                when (selectedTab) {
                    UndervoltTab.CPU -> {
                        item(key = "cpu_banner") {
                            val warn = UndervoltGuideContent.getCpuWarning(isTurkish)
                            GuideWarningBanner(
                                title = warn.title,
                                description = warn.description,
                                accentColor = NeonAmber,
                            )
                        }
                        item(key = "cpu_intel_13_14") {
                            val data = UndervoltGuideContent.getIntelSection(isTurkish)
                            GuideSectionCard(
                                icon = "🔵",
                                data = data,
                                accentColor = NeonAmber,
                                secondaryColor = ElectricBlue,
                                titleColor = ElectricBlue,
                            )
                        }
                        item(key = "cpu_amd_ryzen") {
                            val data = UndervoltGuideContent.getAmdRyzenSection(isTurkish)
                            GuideSectionCard(
                                icon = "🔴",
                                data = data,
                                accentColor = NeonEmerald,
                                secondaryColor = NeonAmber,
                                titleColor = NeonEmerald,
                            )
                        }
                        item(key = "cpu_stress_test") {
                            val protocol = UndervoltGuideContent.getCpuStability(isTurkish)
                            GuideProtocolCard(data = protocol)
                        }
                    }
                    UndervoltTab.GPU -> {
                        item(key = "gpu_banner") {
                            val warn = UndervoltGuideContent.getGpuWarning(isTurkish)
                            GuideWarningBanner(
                                title = warn.title,
                                description = warn.description,
                                accentColor = ElectricBlue,
                            )
                        }
                        item(key = "gpu_nvidia") {
                            val data = UndervoltGuideContent.getGpuNvidiaSection(isTurkish)
                            GuideSectionCard(
                                icon = "🟢",
                                data = data,
                                accentColor = ElectricBlue,
                                secondaryColor = NeonEmerald,
                                titleColor = ElectricBlue,
                            )
                        }
                        item(key = "gpu_amd") {
                            val data = UndervoltGuideContent.getGpuAmdSection(isTurkish)
                            GuideSectionCard(
                                icon = "🔴",
                                data = data,
                                accentColor = NeonRose,
                                secondaryColor = NeonAmber,
                                titleColor = NeonRose,
                            )
                        }
                        item(key = "gpu_crash_recovery") {
                            val shortcut = UndervoltGuideContent.getGpuShortcut(isTurkish)
                            GuideShortcutCard(data = shortcut)
                        }
                    }
                    UndervoltTab.RAM -> {
                        item(key = "ram_banner") {
                            val warn = UndervoltGuideContent.getRamWarning(isTurkish)
                            GuideWarningBanner(
                                title = warn.title,
                                description = warn.description,
                                accentColor = NeonRose,
                            )
                        }
                        item(key = "ram_ddr5_vs_ddr4") {
                            val data = UndervoltGuideContent.getRamVoltagesSection(isTurkish)
                            GuideSectionCard(
                                icon = "⚡",
                                data = data,
                                accentColor = NeonEmerald,
                                secondaryColor = ElectricBlue,
                                titleColor = NeonEmerald,
                            )
                        }
                        item(key = "ram_subtimings") {
                            val data = UndervoltGuideContent.getRamSubtimingsSection(isTurkish)
                            GuideSectionCard(
                                icon = "⏱️",
                                data = data,
                                accentColor = NeonRose,
                                secondaryColor = NeonAmber,
                                titleColor = NeonRose,
                            )
                        }
                        item(key = "ram_stability_test") {
                            val protocol = UndervoltGuideContent.getRamStability(isTurkish)
                            GuideProtocolCard(data = protocol)
                        }
                    }
                    UndervoltTab.CLEAR_CMOS -> {
                        item(key = "clear_cmos_guide") {
                            val data = UndervoltGuideContent.getClearCmosSection(isTurkish)
                            GuideSectionCard(
                                icon = "🚨",
                                data = data,
                                accentColor = NeonRose,
                                secondaryColor = NeonAmber,
                                titleColor = NeonRose,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideSectionCard(
    icon: String,
    data: GuideSectionData,
    accentColor: Color,
    secondaryColor: Color,
    titleColor: Color,
) {
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        secondaryColor = secondaryColor,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
            }

            Text(
                text = data.subtitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )

            Text(
                text = data.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            data.steps.forEach { stepItem ->
                GuideStepCard(
                    step = stepItem.step,
                    title = stepItem.title,
                    description = stepItem.description,
                )
            }
        }
    }
}

@Composable
private fun GuideProtocolCard(data: GuideProtocolData) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
            )
            data.points.forEachIndexed { index, point ->
                Text(
                    text = point,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (index == data.points.lastIndex) NeonAmber else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun GuideShortcutCard(data: GuideShortcutData) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
            )
            Text(
                text = data.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            ) {
                Text(
                    text = data.keyCombo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonEmerald,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            Text(
                text = data.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun GuideStepCard(
    step: String,
    title: String,
    description: String,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NeonCyan.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = step,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GuideWarningBanner(
    title: String,
    description: String,
    accentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text("⚠️", fontSize = 20.sp, modifier = Modifier.padding(top = 2.dp, end = 8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
