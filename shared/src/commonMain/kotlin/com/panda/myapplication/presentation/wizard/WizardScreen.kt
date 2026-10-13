package com.panda.myapplication.presentation.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panda.myapplication.domain.engine.BottleneckAnalysis
import com.panda.myapplication.domain.engine.BottleneckType
import com.panda.myapplication.domain.engine.ClearanceRadarResult
import com.panda.myapplication.domain.engine.GameFpsEstimate
import com.panda.myapplication.domain.engine.GamingSimulationResult
import com.panda.myapplication.domain.engine.PlatformStatusBadge
import com.panda.myapplication.domain.engine.ResolutionOption
import com.panda.myapplication.domain.engine.WattageCockpitInfo
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.CardSurface
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.theme.SlateDarkBackground
import org.koin.compose.viewmodel.koinViewModel

private val WIZARD_SLOTS = listOf(
    HardwareCategory.CPU,
    HardwareCategory.MOTHERBOARD,
    HardwareCategory.RAM,
    HardwareCategory.GPU,
    HardwareCategory.STORAGE,
    HardwareCategory.COOLER,
    HardwareCategory.CASE,
    HardwareCategory.PSU,
)

@Composable
fun WizardRoute(
    onNavigateToCatalogWithCategory: (HardwareCategory) -> Unit,
    onNavigateToItemDetail: (HardwareItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WizardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WizardEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is WizardEffect.ShareBuild -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                    snackbarHostState.showSnackbar("⚡ Sistem Kartı panoya kopyalandı! Paylaşmaya hazır.")
                }
            }
        }
    }

    WizardContent(
        uiState = uiState,
        onSlotClicked = viewModel::onSlotClicked,
        onRemoveSlot = viewModel::onRemoveSlot,
        onClearBuild = viewModel::onClearBuild,
        onResolutionChanged = viewModel::onResolutionChanged,
        onOpenShareDialog = viewModel::onOpenShareDialog,
        onDismissShareDialog = viewModel::onDismissShareDialog,
        onShareIntentRequested = viewModel::onShareIntentRequested,
        onDismissPicker = viewModel::onDismissPicker,
        onPickerSearchQueryChange = viewModel::onPickerSearchQueryChange,
        onSelectComponent = viewModel::onSelectComponent,
        onItemDetailClick = onNavigateToItemDetail,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardContent(
    uiState: WizardUiState,
    onSlotClicked: (HardwareCategory) -> Unit,
    onRemoveSlot: (HardwareCategory) -> Unit,
    onClearBuild: () -> Unit,
    onResolutionChanged: (ResolutionOption) -> Unit,
    onOpenShareDialog: () -> Unit,
    onDismissShareDialog: () -> Unit,
    onShareIntentRequested: () -> Unit,
    onDismissPicker: () -> Unit,
    onPickerSearchQueryChange: (String) -> Unit,
    onSelectComponent: (HardwareItem) -> Unit,
    onItemDetailClick: (HardwareItem) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SlateDarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            // Header Bar
            item {
                WizardHeader(
                    onClearBuild = onClearBuild,
                    onShareBuild = onOpenShareDialog,
                )
            }

            // Module 2: Live Animated Cockpit HUD Header
            item {
                LiveCockpitSection(
                    cockpitInfo = uiState.cockpitInfo,
                    platformStatus = uiState.platformStatus,
                    bottleneck = uiState.bottleneckAnalysis,
                )
            }

            // Module 3: Physical Size & Case Clearance Radar
            item {
                ClearanceRadarSection(
                    radar = uiState.clearanceRadar,
                    isCaseSelected = uiState.activeBuild.case != null,
                )
            }

            // Module 4: Simulated Gaming & Synthetic FPS Panel
            item {
                GamingSimulationSection(
                    simulation = uiState.gamingSimulation,
                    selectedResolution = uiState.selectedResolution,
                    onResolutionChanged = onResolutionChanged,
                )
            }

            // Module 1: 8 Component Slots
            item {
                SlotListHeader(
                    filledCount = uiState.activeBuild.totalCount,
                    totalCount = WIZARD_SLOTS.size,
                    totalPriceMinor = uiState.activeBuild.totalPriceMinor,
                )
            }

            items(WIZARD_SLOTS) { category ->
                val currentItem = uiState.activeBuild.getSlot(category)
                ComponentSlotCard(
                    category = category,
                    item = currentItem,
                    onSlotClick = { onSlotClicked(category) },
                    onRemoveClick = { onRemoveSlot(category) },
                    onDetailClick = { if (currentItem != null) onItemDetailClick(currentItem) },
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                )
            }
        }
    }

    // Module 1: In-place ModalBottomSheet Slot Picker
    if (uiState.isPickerOpen && uiState.pickerCategory != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissPicker,
            sheetState = sheetState,
            containerColor = CardSurface,
            scrimColor = Color.Black.copy(alpha = 0.65f),
        ) {
            SlotPickerBottomSheetContent(
                category = uiState.pickerCategory,
                searchQuery = uiState.pickerSearchQuery,
                constraintDesc = uiState.pickerConstraintDescription,
                items = uiState.pickerItems,
                isLoading = uiState.isPickerLoading,
                onSearchChange = onPickerSearchQueryChange,
                onSelectItem = onSelectComponent,
                onDismiss = onDismissPicker,
            )
        }
    }

    // Module 5: Cyberpunk Dark-Neon System Card Share Dialog
    if (uiState.isShareDialogOpen) {
        SystemCardShareDialog(
            build = uiState.activeBuild,
            platformStatus = uiState.platformStatus,
            cockpitInfo = uiState.cockpitInfo,
            bottleneck = uiState.bottleneckAnalysis,
            gamingSim = uiState.gamingSimulation,
            onDismiss = onDismissShareDialog,
            onCopyAndShare = onShareIntentRequested,
        )
    }
}

// region Top Header

@Composable
private fun WizardHeader(
    onClearBuild: () -> Unit,
    onShareBuild: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NeonCyan),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "DONANIM LABORATUVARI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 1.5.sp,
                )
            }
            Text(
                text = "İnteraktif PC Toplama",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            OutlinedButton(
                onClick = onClearBuild,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("Sıfırla", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(
                onClick = onShareBuild,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "⚡ Paylaş",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateDarkBackground,
                )
            }
        }
    }
}

// endregion

// region Module 2: Live Animated Cockpit Section

@Composable
private fun LiveCockpitSection(
    cockpitInfo: WattageCockpitInfo,
    platformStatus: PlatformStatusBadge,
    bottleneck: BottleneckAnalysis,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            // Platform Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val badgeBorder = if (platformStatus.isCompatible) NeonEmerald else NeonRose
                val badgeBg = if (platformStatus.isCompatible) NeonEmerald.copy(alpha = 0.12f) else NeonRose.copy(alpha = 0.12f)
                val badgeTextColor = if (platformStatus.isCompatible) NeonEmerald else NeonRose

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeBorder),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(badgeTextColor),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = platformStatus.statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                        )
                    }
                }

                Text(
                    text = "KOKPİT HUD",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Wattage Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "CANLI GÜÇ TÜKETİMİ (TDP)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${cockpitInfo.componentTdpSum}W",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Tepe: ${cockpitInfo.peakTdpWithTolerance}W (+%20)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ÖNERİLEN PSU",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (cockpitInfo.recommendedPsuWattage > 0) "${cockpitInfo.recommendedPsuWattage}W" else "--",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (cockpitInfo.isSufficient) NeonEmerald else NeonRose,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Wattage Load Bar
            val maxWattReference = maxOf(cockpitInfo.recommendedPsuWattage, 1000).toFloat()
            val loadFraction = (cockpitInfo.componentTdpSum / maxWattReference).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { loadFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (cockpitInfo.isSufficient) NeonCyan else NeonRose,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            if (!cockpitInfo.isSufficient && cockpitInfo.currentPsuWattage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ Seçilen PSU (${cockpitInfo.currentPsuWattage}W), sistemin ${cockpitInfo.recommendedPsuWattage}W ihtiyacının altında!",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonRose,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(modifier = Modifier.height(Spacing.medium))

            // Bottleneck Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "DARBOĞAZ ANALİZİ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val bottleneckColor = when (bottleneck.type) {
                    BottleneckType.BALANCED -> NeonEmerald
                    BottleneckType.CPU_BOTTLENECK -> NeonRose
                    BottleneckType.GPU_BOTTLENECK -> NeonAmber
                }
                Text(
                    text = bottleneck.message,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = bottleneckColor,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val bottleneckProgress = (bottleneck.percentage / 45f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { bottleneckProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp)),
                color = when (bottleneck.type) {
                    BottleneckType.BALANCED -> NeonEmerald
                    BottleneckType.CPU_BOTTLENECK -> NeonRose
                    BottleneckType.GPU_BOTTLENECK -> NeonAmber
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

// endregion

// region Module 3: Physical Size & Case Clearance Radar

@Composable
private fun ClearanceRadarSection(
    radar: ClearanceRadarResult,
    isCaseSelected: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(
            1.dp,
            if (radar.hasClearanceIssue) NeonRose else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📐",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "KASA SIĞMA RADARI (CLEARANCE)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (radar.hasClearanceIssue) NeonRose else NeonCyan,
                    )
                }

                if (radar.hasClearanceIssue) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonRose.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeonRose),
                    ) {
                        Text(
                            text = "SIĞMA UYARISI!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonRose,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            if (!isCaseSelected) {
                Text(
                    text = "Kasa seçildiğinde GPU uzunluğu, soğutucu yüksekliği ve radyatör sığma marjı burada otomatik hesaplanacak.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // GPU Clearance
                ClearanceMetricRow(
                    label = "Ekran Kartı Uzunluğu",
                    currentValue = radar.gpuLengthMm?.let { "${it}mm" } ?: "GPU Yok",
                    maxValue = "${radar.maxGpuLengthMm ?: 0}mm Destek",
                    marginMm = radar.gpuClearanceMm,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Cooler Height Clearance
                ClearanceMetricRow(
                    label = "Hava Soğutucu Yüksekliği",
                    currentValue = radar.coolerHeightMm?.let { "~${it}mm" } ?: "Hava Soğutma Yok",
                    maxValue = "${radar.maxCoolerHeightMm ?: 0}mm Destek",
                    marginMm = radar.coolerClearanceMm,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Radiator Clearance
                ClearanceMetricRow(
                    label = "Sıvı Soğutma Radyatörü",
                    currentValue = radar.coolerRadiatorMm?.let { "${it}mm" } ?: "Sıvı Soğutma Yok",
                    maxValue = "${radar.maxRadiatorMm ?: 0}mm Destek",
                    marginMm = radar.radiatorClearanceMm,
                )

                if (radar.hasClearanceIssue && radar.issueMessage != null) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "❌ ${radar.issueMessage}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonRose,
                    )
                }
            }
        }
    }
}

@Composable
private fun ClearanceMetricRow(
    label: String,
    currentValue: String,
    maxValue: String,
    marginMm: Int?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "$currentValue • $maxValue",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (marginMm != null) {
            val isFit = marginMm >= 0
            val badgeColor = if (isFit) NeonEmerald else NeonRose
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, badgeColor),
            ) {
                Text(
                    text = if (isFit) "+${marginMm}mm Boşluk" else "${marginMm}mm Taşıyor!",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

// endregion

// region Module 4: Simulated Gaming & Synthetic FPS Panel

@Composable
private fun GamingSimulationSection(
    simulation: GamingSimulationResult,
    selectedResolution: ResolutionOption,
    onResolutionChanged: (ResolutionOption) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎮", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "OYUN & SENTETİK FPS SİMÜLATÖRÜ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                    )
                }

                Text(
                    text = "CANLI TAHMİN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Resolution Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                ResolutionOption.entries.forEach { res ->
                    val isSelected = res == selectedResolution
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onResolutionChanged(res) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) NeonCyan.copy(alpha = 0.15f) else CardSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline),
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = res.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = res.subLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            if (!simulation.isReady) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Oyun simülasyonu ve FPS değerlerini hesaplamak için CPU ve GPU ekleyin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                // Game FPS Cards
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    simulation.games.forEach { game ->
                        GameFpsCard(game = game)
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(Spacing.medium))

                // Synthetic Benchmarks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    simulation.synthetics.forEach { synth ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Column(modifier = Modifier.padding(Spacing.small)) {
                                Text(
                                    text = synth.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = synth.score.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                )
                                Text(
                                    text = synth.unit,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
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

@Composable
private fun GameFpsCard(game: GameFpsEstimate) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = game.gameName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = game.settings,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "~${game.fps}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (game.fps >= 60) NeonEmerald else NeonAmber,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "FPS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
    }
}

// endregion

// region Module 1: Component Slot Cards & List

@Composable
private fun SlotListHeader(
    filledCount: Int,
    totalCount: Int,
    totalPriceMinor: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "BİLEŞEN YUVALARI ($filledCount / $totalCount)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )

        val priceText = if (totalPriceMinor > 0) "$${totalPriceMinor / 100.0}" else "$0.00"
        Text(
            text = "Toplam: $priceText",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
        )
    }
}

@Composable
private fun ComponentSlotCard(
    category: HardwareCategory,
    item: HardwareItem?,
    onSlotClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryIcon = when (category) {
        HardwareCategory.CPU -> "⚡"
        HardwareCategory.MOTHERBOARD -> "🔌"
        HardwareCategory.RAM -> "🧠"
        HardwareCategory.GPU -> "🎮"
        HardwareCategory.STORAGE -> "💾"
        HardwareCategory.COOLER -> "❄️"
        HardwareCategory.CASE -> "🖥️"
        HardwareCategory.PSU -> "🔋"
    }

    val categoryTitle = when (category) {
        HardwareCategory.CPU -> "İşlemci (CPU)"
        HardwareCategory.MOTHERBOARD -> "Anakart (Motherboard)"
        HardwareCategory.RAM -> "Bellek (RAM)"
        HardwareCategory.GPU -> "Ekran Kartı (GPU)"
        HardwareCategory.STORAGE -> "Depolama (SSD/HDD)"
        HardwareCategory.COOLER -> "Soğutucu (Cooler)"
        HardwareCategory.CASE -> "Kasa (Chassis)"
        HardwareCategory.PSU -> "Güç Kaynağı (PSU)"
    }

    if (item == null) {
        // Empty Slot Card
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onSlotClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = categoryIcon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Column {
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Parça seçilmedi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                ) {
                    Text(
                        text = "+ Parça Ekle",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    } else {
        // Filled Slot Card
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
        ) {
            Column(modifier = Modifier.padding(Spacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(text = categoryIcon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(Spacing.medium))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = categoryTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = item.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable { onDetailClick() },
                            )
                            val specSummary = formatSlotSpecSummary(item)
                            if (specSummary.isNotBlank()) {
                                Text(
                                    text = specSummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            val benchSummary = when {
                                item.spec is CpuSpec -> {
                                    val pts = item.benchmarkInfo?.effectiveMultiCore
                                    if (pts != null) "⚡ Cinebench: $pts pts • ${item.benchmarkTier ?: "Tier"}" else null
                                }
                                item.spec is GpuSpec -> {
                                    val pts = item.benchmarkInfo?.effectiveTimeSpy
                                    if (pts != null) "🎮 3DMark Time Spy: $pts pts • ${item.benchmarkTier ?: "Tier"}" else null
                                }
                                else -> item.benchmarkScore?.let { "★ Skor: $it/100 • ${item.benchmarkTier ?: "Tier"}" }
                            }
                            if (benchSummary != null) {
                                Text(
                                    text = benchSummary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    val priceText = item.price?.let { "$${it.amountMinor / 100.0}" } ?: "N/A"
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = Spacing.small),
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                // Actions: Detail & Change & Remove
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDetailClick,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("Detay & Benchmark", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.width(Spacing.small))

                    OutlinedButton(
                        onClick = onSlotClick,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("Değiştir", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    }

                    Spacer(modifier = Modifier.width(Spacing.small))

                    OutlinedButton(
                        onClick = onRemoveClick,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("Kaldır", style = MaterialTheme.typography.labelSmall, color = NeonRose)
                    }
                }
            }
        }
    }
}

private fun formatSlotSpecSummary(item: HardwareItem): String = when (val s = item.spec) {
    is CpuSpec -> "${s.socket} • ${s.coreCount} Çekirdek • ${s.tdp.watts}W"
    is MotherboardSpec -> "${s.socket} • ${s.chipset} • ${s.memoryGeneration.name} • ${s.formFactor.name}"
    is RamSpec -> "${s.generation.name} • ${s.speed.mhz}MT/s • ${s.capacity.megabytes / 1024}GB"
    is GpuSpec -> "${s.chipset} • ${s.vram.megabytes / 1024}GB • ${s.lengthMm}mm"
    is StorageSpec -> "${s.mediaType.name} • ${s.capacity.megabytes / 1024}GB • ${s.sequentialReadMbps}MB/s"
    is CoolerSpec -> "${s.type.name} • ${s.radiatorSizeMm?.let { "${it}mm" } ?: "Hava"}"
    is CaseSpec -> "${s.type} • GPU: max ${s.maxGpuLengthMm}mm • Rad: ${s.maxRadiatorSizeMm}mm"
    is PsuSpec -> "${s.wattage.watts}W • ${s.efficiency.name}"
}

// endregion

// region Module 1: In-place ModalBottomSheet Slot Picker

@Composable
private fun SlotPickerBottomSheetContent(
    category: HardwareCategory,
    searchQuery: String,
    constraintDesc: String?,
    items: List<HardwareItem>,
    isLoading: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectItem: (HardwareItem) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(540.dp)
            .padding(horizontal = Spacing.medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "${category.name} SEÇİMİ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${items.size} uyumlu seçenek mevcut",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onDismiss) {
                Text("✕", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Bidirectional Constraint Warning/Lock Badge
        if (constraintDesc != null) {
            Spacer(modifier = Modifier.height(Spacing.small))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = NeonCyan.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔒", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "Çift Yönlü Filtre: $constraintDesc",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Model veya marka ara...", style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardSurfaceElevated,
                unfocusedContainerColor = CardSurfaceElevated,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Bu kriterlere veya arama sorgusuna uygun parça bulunamadı.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
                contentPadding = PaddingValues(bottom = Spacing.medium),
            ) {
                items(items, key = { it.id.value }) { item ->
                    PickerItemCard(
                        item = item,
                        onSelect = { onSelectItem(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerItemCard(
    item: HardwareItem,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatSlotSpecSummary(item),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(Spacing.small))

            Column(horizontalAlignment = Alignment.End) {
                val priceText = item.price?.let { "$${it.amountMinor / 100.0}" } ?: "N/A"
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                )
                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "Seç",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateDarkBackground,
                    )
                }
            }
        }
    }
}

// endregion

// region Module 5: Cyberpunk Dark-Neon "Sistem Kartı" Share Dialog

@Composable
private fun SystemCardShareDialog(
    build: ActiveBuild,
    platformStatus: PlatformStatusBadge,
    cockpitInfo: WattageCockpitInfo,
    bottleneck: BottleneckAnalysis,
    gamingSim: GamingSimulationResult,
    onDismiss: () -> Unit,
    onCopyAndShare: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = SlateDarkBackground,
            border = BorderStroke(2.dp, NeonCyan),
        ) {
            Column(modifier = Modifier.padding(Spacing.large)) {
                // Cyberpunk Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "⚡ HARDWARE WIZARD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 1.5.sp,
                        )
                        Text(
                            text = "SİSTEM KARTI",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonEmerald.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeonEmerald),
                    ) {
                        Text(
                            text = platformStatus.platformName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))
                HorizontalDivider(color = NeonCyan.copy(alpha = 0.4f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(Spacing.medium))

                // Parts Summary List
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SharePartRow("CPU", build.cpu?.displayName)
                    SharePartRow("Anakart", build.motherboard?.displayName)
                    SharePartRow("RAM", build.ram?.displayName)
                    SharePartRow("GPU", build.gpu?.displayName)
                    SharePartRow("SSD", build.storage?.displayName)
                    SharePartRow("Soğutucu", build.cooler?.displayName)
                    SharePartRow("Kasa", build.case?.displayName)
                    SharePartRow("PSU", build.psu?.displayName)
                }

                Spacer(modifier = Modifier.height(Spacing.medium))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                Spacer(modifier = Modifier.height(Spacing.medium))

                // Cockpit Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("GÜÇ TÜKETİMİ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${cockpitInfo.componentTdpSum}W TDP", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                    Column {
                        Text("DARBOĞAZ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(bottleneck.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeonEmerald)
                    }
                }

                if (gamingSim.isReady && gamingSim.games.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text("OYUN PERFORMANSI (${gamingSim.resolution.label}):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = gamingSim.games.joinToString(" • ") { "${it.gameName.take(10)}: ~${it.fps} FPS" },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Total Price & Built With Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val priceText = if (build.totalPriceMinor > 0) "$${build.totalPriceMinor / 100.0}" else "N/A"
                    Column {
                        Text("TOPLAM BÜTÇE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(priceText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NeonCyan)
                    }

                    Text(
                        text = "BUILT WITH HARDWARE WIZARD 🛠️",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    ) {
                        Text("Kapat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = onCopyAndShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    ) {
                        Text("Panoya Kopyala", fontWeight = FontWeight.Bold, color = SlateDarkBackground)
                    }
                }
            }
        }
    }
}

@Composable
private fun SharePartRow(category: String, name: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$category:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = name ?: "Seçilmedi",
            style = MaterialTheme.typography.labelSmall,
            color = if (name != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = Spacing.small),
        )
    }
}

// endregion
