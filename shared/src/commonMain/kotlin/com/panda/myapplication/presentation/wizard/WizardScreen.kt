package com.panda.myapplication.presentation.wizard

import com.panda.myapplication.presentation.util.localized

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SquareFoot
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.WarningAmber
import com.panda.myapplication.presentation.components.ModernSoftIcon
import com.panda.myapplication.presentation.components.SoftCategoryIcon
import com.panda.myapplication.presentation.components.iconVector
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.panda.myapplication.presentation.util.formatPrice
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import com.panda.myapplication.domain.engine.BottleneckAnalysis
import com.panda.myapplication.domain.engine.BottleneckType
import com.panda.myapplication.domain.engine.BuildDoctorReport
import com.panda.myapplication.domain.engine.ClearanceRadarResult
import com.panda.myapplication.domain.engine.GameFpsEstimate
import com.panda.myapplication.domain.engine.GamingSimulationResult
import com.panda.myapplication.domain.engine.PlatformStatusBadge
import com.panda.myapplication.domain.engine.QualityPreset
import com.panda.myapplication.domain.engine.ResolutionOption
import com.panda.myapplication.domain.engine.UpgradeRecommendation
import com.panda.myapplication.domain.engine.UpscalingMode
import com.panda.myapplication.domain.engine.WattageCockpitInfo
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.GamingChairSpec
import com.panda.myapplication.domain.model.MicrophoneSpec
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MousepadSpec
import com.panda.myapplication.domain.model.WebcamSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.presentation.hardware_list.HardwareThumbnail
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
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
    HardwareCategory.MONITOR,
    HardwareCategory.MOUSE,
    HardwareCategory.MOUSEPAD,
    HardwareCategory.KEYBOARD,
    HardwareCategory.HEADSET,
    HardwareCategory.MICROPHONE,
    HardwareCategory.WEBCAM,
    HardwareCategory.GAMING_CHAIR,
)

@Suppress("DEPRECATION")
@Composable
fun WizardRoute(
    onNavigateToCatalogWithCategory: (HardwareCategory) -> Unit,
    onNavigateToItemDetail: (HardwareItem) -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
    viewModel: WizardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WizardEffect.ShowToast -> {
                    val msg = if (isTr) effect.message else (effect.messageEn ?: effect.message)
                    snackbarHostState.showSnackbar(msg)
                }
                is WizardEffect.ShareBuild -> {
                    val shareText = if (isTr) effect.text else (effect.textEn ?: effect.text)
                    clipboardManager.setText(AnnotatedString(shareText))
                    snackbarHostState.showSnackbar(strings.wizardSystemCardCopied)
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
        onPresetChanged = viewModel::onPresetChanged,
        onUpscalingChanged = viewModel::onUpscalingChanged,
        onOpenShareDialog = viewModel::onOpenShareDialog,
        onDismissShareDialog = viewModel::onDismissShareDialog,
        onShareIntentRequested = viewModel::onShareIntentRequested,
        onDismissPicker = viewModel::onDismissPicker,
        onPickerSearchQueryChange = viewModel::onPickerSearchQueryChange,
        onSelectComponent = viewModel::onSelectComponent,
        onItemDetailClick = onNavigateToItemDetail,
        onOpenDrawer = onOpenDrawer,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun WizardContent(
    uiState: WizardUiState,
    onSlotClicked: (HardwareCategory) -> Unit,
    onRemoveSlot: (HardwareCategory) -> Unit,
    onClearBuild: () -> Unit,
    onResolutionChanged: (ResolutionOption) -> Unit,
    onPresetChanged: (QualityPreset) -> Unit,
    onUpscalingChanged: (UpscalingMode) -> Unit,
    onOpenShareDialog: () -> Unit,
    onDismissShareDialog: () -> Unit,
    onShareIntentRequested: () -> Unit,
    onDismissPicker: () -> Unit,
    onPickerSearchQueryChange: (String) -> Unit,
    onSelectComponent: (HardwareItem) -> Unit,
    onItemDetailClick: (HardwareItem) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    onOpenDrawer = onOpenDrawer,
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

            // AI Build Doctor Diagnostic Card
            item {
                BuildDoctorSection(
                    report = uiState.buildDoctor,
                )
            }

            // Module 3: Physical Size & Case Clearance Radar
            item {
                ClearanceRadarSection(
                    radar = uiState.clearanceRadar,
                    isCaseSelected = uiState.activeBuild.case != null,
                )
            }

            // Module 4: Interactive Gaming Performance Lab & Synthetic Simulation
            item {
                GamingSimulationSection(
                    simulation = uiState.gamingSimulation,
                    selectedResolution = uiState.selectedResolution,
                    selectedPreset = uiState.selectedPreset,
                    selectedUpscaling = uiState.selectedUpscaling,
                    onResolutionChanged = onResolutionChanged,
                    onPresetChanged = onPresetChanged,
                    onUpscalingChanged = onUpscalingChanged,
                )
            }

            // Module 1: 8 Component Slots
            item {
                SlotListHeader(
                    filledCount = uiState.activeBuild.totalCount,
                    totalCount = WIZARD_SLOTS.size,
                    totalPriceTry = uiState.activeBuild.totalPriceTry,
                )
            }

            items(
                items = WIZARD_SLOTS,
                key = { it.name },
            ) { category ->
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
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
    onOpenDrawer: (() -> Unit)? = null,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            if (onOpenDrawer != null) {
                Surface(
                    onClick = onOpenDrawer,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                ) {
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Menu",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

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
                        text = tr(isTr, "DONANIM LABORATUVARI", "HARDWARE LAB"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 1.5.sp,
                    )
                }
                Text(
                    text = strings.wizardInteractiveTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            OutlinedButton(
                onClick = onClearBuild,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(tr(isTr, "Sıfırla", "Reset"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(
                onClick = onShareBuild,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = tr(isTr, "Paylaş", "Share"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    val cockpitAccent = if (platformStatus.isCompatible) NeonCyan else NeonRose
    val cockpitSecondary = if (platformStatus.isCompatible) NeonEmerald else NeonAmber

    FluidOrganicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        accentColor = cockpitAccent,
        secondaryColor = cockpitSecondary,
        contentPadding = Spacing.medium,
    ) {
        Column {
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
                            text = localizedPlatformStatus(platformStatus, isTr),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                        )
                    }
                }

                Text(
                    text = tr(isTr, "KOKPİT HUD", "COCKPIT HUD"),
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
                        text = tr(isTr, "CANLI GÜÇ TÜKETİMİ (TDP)", "LIVE POWER DRAW (TDP)"),
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
                            text = "• ${tr(isTr, "Tepe", "Peak")}: ${cockpitInfo.peakTdpWithTolerance}W (+20%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tr(isTr, "ÖNERİLEN PSU", "RECOMMENDED PSU"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = NeonRose,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (isTr) "Seçilen PSU (${cockpitInfo.currentPsuWattage}W), sistemin ${cockpitInfo.recommendedPsuWattage}W ihtiyacının altında!" else "Selected PSU (${cockpitInfo.currentPsuWattage}W) is below system recommendation (${cockpitInfo.recommendedPsuWattage}W)!",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRose,
                        fontWeight = FontWeight.Bold,
                    )
                }
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
                    text = tr(isTr, "DARBOĞAZ ANALİZİ", "BOTTLENECK ANALYSIS"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val bottleneckColor = when (bottleneck.type) {
                    BottleneckType.BALANCED -> NeonEmerald
                    BottleneckType.CPU_BOTTLENECK -> NeonRose
                    BottleneckType.GPU_BOTTLENECK -> NeonAmber
                }
                val bottleneckDisp = if (bottleneck.percentage == 0) {
                    tr(isTr, "CPU ve GPU seçildiğinde hesaplanacak", "Calculated when CPU and GPU are selected")
                } else {
                    when (bottleneck.type) {
                        BottleneckType.BALANCED -> tr(isTr, "%${bottleneck.percentage} - Mükemmel Denge", "${bottleneck.percentage}% - Balanced Match")
                        BottleneckType.CPU_BOTTLENECK -> tr(isTr, "%${bottleneck.percentage} - CPU Darboğazı Riski", "${bottleneck.percentage}% - CPU Bottleneck Risk")
                        BottleneckType.GPU_BOTTLENECK -> tr(isTr, "%${bottleneck.percentage} - GPU Limitli / Tam Yükte", "${bottleneck.percentage}% - GPU Bound / Full Load")
                    }
                }
                Text(
                    text = bottleneckDisp,
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    val radarAccent = if (radar.hasClearanceIssue) NeonRose else NeonCyan
    val radarSecondary = if (radar.hasClearanceIssue) NeonAmber else ElectricBlue

    FluidOrganicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        accentColor = radarAccent,
        secondaryColor = radarSecondary,
        contentPadding = Spacing.medium,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModernSoftIcon(
                        icon = Icons.Rounded.SquareFoot,
                        accentColor = if (radar.hasClearanceIssue) NeonRose else NeonCyan,
                        size = 32.dp,
                        iconSize = 18.dp,
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = tr(isTr, "KASA SIĞMA RADARI (CLEARANCE)", "CASE CLEARANCE RADAR"),
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
                            text = tr(isTr, "SIĞMA UYARISI!", "CLEARANCE WARNING!"),
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
                    text = tr(isTr, "Kasa seçildiğinde GPU uzunluğu, soğutucu yüksekliği ve radyatör sığma marjı burada otomatik hesaplanacak.", "When a case is selected, GPU length, cooler height, and radiator margins will be calculated automatically."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // GPU Clearance
                ClearanceMetricRow(
                    label = tr(isTr, "Ekran Kartı Uzunluğu", "GPU Length"),
                    currentValue = radar.gpuLengthMm?.let { "${it}mm" } ?: tr(isTr, "GPU Yok", "No GPU"),
                    maxValue = "${radar.maxGpuLengthMm ?: 0}mm ${tr(isTr, "Destek", "Max")}",
                    marginMm = radar.gpuClearanceMm,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Cooler Height Clearance
                ClearanceMetricRow(
                    label = tr(isTr, "Hava Soğutucu Yüksekliği", "Air Cooler Height"),
                    currentValue = radar.coolerHeightMm?.let { "~${it}mm" } ?: tr(isTr, "Hava Soğutma Yok", "No Air Cooler"),
                    maxValue = "${radar.maxCoolerHeightMm ?: 0}mm ${tr(isTr, "Destek", "Max")}",
                    marginMm = radar.coolerClearanceMm,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Radiator Clearance
                ClearanceMetricRow(
                    label = tr(isTr, "Sıvı Soğutma Radyatörü", "Liquid Cooler Radiator"),
                    currentValue = radar.coolerRadiatorMm?.let { "${it}mm" } ?: tr(isTr, "Sıvı Soğutma Yok", "No AIO Cooler"),
                    maxValue = "${radar.maxRadiatorMm ?: 0}mm ${tr(isTr, "Destek", "Max")}",
                    marginMm = radar.radiatorClearanceMm,
                )

                if (radar.ramClearanceStatus != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Straighten,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr(isTr, "RAM Boşluğu: ", "RAM Clearance: "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                            )
                            Text(
                                text = radar.ramClearanceStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                if (radar.radiatorSupportSummary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AcUnit,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr(isTr, "Radyatör Yuvası: ", "Radiator Mount: "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                            )
                            Text(
                                text = radar.radiatorSupportSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                if (radar.fanCapacitySummary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Air,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tr(isTr, "Kasa Fanı: ", "Case Fans: "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = radar.fanCapacitySummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                if (radar.hasClearanceIssue && radar.issueMessage != null) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = NeonRose,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = radar.issueMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonRose,
                        )
                    }
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

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
                    text = if (isFit) "+${marginMm}mm ${tr(isTr, "Boşluk", "Clearance")}" else "${marginMm}mm ${tr(isTr, "Taşıyor!", "Exceeds!")}",
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

// region Module: AI Build Doctor Section

@Composable
private fun BuildDoctorSection(
    report: BuildDoctorReport,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    var isExpanded by remember { mutableStateOf(true) }

    val healthScoreColor = when {
        report.healthScore >= 85 -> NeonEmerald
        report.healthScore >= 60 -> NeonAmber
        else -> NeonRose
    }

    FluidOrganicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        accentColor = healthScoreColor,
        secondaryColor = NeonCyan,
        contentPadding = Spacing.medium,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModernSoftIcon(
                        icon = Icons.Rounded.MedicalServices,
                        accentColor = healthScoreColor,
                        size = 32.dp,
                        iconSize = 18.dp,
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = tr(isTr, "AI SİSTEM DOKTORU & SAĞLIK RAPORU", "AI SYSTEM DOCTOR & HEALTH REPORT"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = healthScoreColor,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = healthScoreColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, healthScoreColor.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = "${report.healthScore} / 100",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = healthScoreColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            // Health status description & Progress bar
            LinearProgressIndicator(
                progress = { (report.healthScore / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = healthScoreColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = localizedDoctorStatus(report.statusText, isTr),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = tr(isTr, "Teşhis", "Diagnosis"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Weakest Link Indicator Pill
            report.weakestLinkComponent?.let { weakest ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ModernSoftIcon(
                            icon = Icons.Rounded.WarningAmber,
                            accentColor = NeonAmber,
                            size = 28.dp,
                            iconSize = 16.dp,
                        )
                        Column {
                            Text(
                                text = tr(isTr, "ZAYIF HALKA (EN BÜYÜK LİMİTLEYİCİ)", "WEAKEST LINK (PRIMARY BOTTLENECK)"),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                            )
                            Text(
                                text = weakest,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // Resolution Bottleneck Matrix mini row
            if (report.resolutionBottlenecks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ResolutionOption.entries.forEach { res ->
                        val b = report.resolutionBottlenecks[res]
                        val badgeColor = when (b?.type) {
                            BottleneckType.BALANCED -> NeonEmerald
                            BottleneckType.CPU_BOTTLENECK -> NeonRose
                            BottleneckType.GPU_BOTTLENECK -> NeonCyan
                            null -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = res.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                )
                                Text(
                                    text = if (b != null) {
                                        val bType = when(b.type) {
                                            BottleneckType.BALANCED -> tr(isTr, "Denge", "Balanced")
                                            BottleneckType.CPU_BOTTLENECK -> "CPU"
                                            BottleneckType.GPU_BOTTLENECK -> "GPU"
                                        }
                                        if (isTr) "%${b.percentage} $bType" else "${b.percentage}% $bType"
                                    } else "-",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            // Doctor diagnosis box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = localizedDoctorDiagnosis(report.doctorDiagnosis, isTr),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(10.dp),
                )
            }

            // Actionable Upgrade Recommendations
            if (report.upgradeRecommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${tr(isTr, "ÖNERİLEN GELİŞTİRMELER", "RECOMMENDED UPGRADES")} (${report.upgradeRecommendations.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = if (isExpanded) tr(isTr, "Gizle", "Hide") else tr(isTr, "Göster", "Show"),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                        )
                    }
                }

                if (isExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        report.upgradeRecommendations.forEach { rec ->
                            UpgradeRecommendationCard(rec = rec)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeRecommendationCard(
    rec: UpgradeRecommendation,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NeonCyan.copy(alpha = 0.2f),
                        modifier = Modifier.size(20.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${rec.priority}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = rec.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = "${tr(isTr, "F/P", "Value")}: ${rec.roiScore}/10",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rec.explanation,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NeonPurple.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = "${tr(isTr, "Beklenen Kazanç", "Expected Gain")}: ${rec.expectedGain}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonPurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            if (rec.suggestedModels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (isTr) "Tavsiye" else "Recommendation"}: ${rec.suggestedModels.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
    selectedPreset: QualityPreset,
    selectedUpscaling: UpscalingMode,
    onResolutionChanged: (ResolutionOption) -> Unit,
    onPresetChanged: (QualityPreset) -> Unit,
    onUpscalingChanged: (UpscalingMode) -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    FluidOrganicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        accentColor = NeonCyan,
        secondaryColor = NeonPurple,
        contentPadding = Spacing.medium,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ModernSoftIcon(
                        icon = Icons.Rounded.SportsEsports,
                        accentColor = NeonCyan,
                        size = 32.dp,
                        iconSize = 18.dp,
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = tr(isTr, "OYUN & SENTETİK FPS SİMÜLATÖRÜ", "GAMING & BENCHMARK FPS SIMULATOR"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                    )
                }

                Text(
                    text = tr(isTr, "CANLI TAHMİN", "LIVE PREDICTION"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // 1. Resolution Selector
            Text(
                text = tr(isTr, "ÇÖZÜNÜRLÜK", "RESOLUTION"),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
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
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline),
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp),
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

            Spacer(modifier = Modifier.height(Spacing.small))

            // 2. Graphics Preset Selector
            Text(
                text = tr(isTr, "GRAFİK KALİTESİ", "GRAPHICS QUALITY"),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                QualityPreset.entries.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPresetChanged(preset) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) NeonPurple else MaterialTheme.colorScheme.outline),
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) NeonPurple else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // 3. Upscaling & Frame Gen Selector
            Text(
                text = tr(isTr, "ÖLÇEKLENDİRME / FRAME GEN", "UPSCALING / FRAME GEN"),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                UpscalingMode.entries.forEach { mode ->
                    val isSelected = mode == selectedUpscaling
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onUpscalingChanged(mode) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) NeonEmerald else MaterialTheme.colorScheme.outline),
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = mode.label.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) NeonEmerald else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
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
                        text = tr(isTr, "Oyun simülasyonu ve FPS değerlerini hesaplamak için CPU ve GPU ekleyin.", "Add CPU and GPU to calculate gaming simulation and FPS predictions."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                // Real-time aggregate telemetry HUD banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(Spacing.small)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "ORTALAMA OYUN PERFORMANSI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${simulation.overallAvgFps}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = NeonCyan,
                                    )
                                    Text(
                                        text = " FPS",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan,
                                        modifier = Modifier.padding(bottom = 3.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(%1 Low: ${simulation.overallLow1Fps} FPS)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 3.dp),
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (simulation.vramWarning) NeonRose.copy(alpha = 0.15f) else NeonEmerald.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (simulation.vramWarning) NeonRose else NeonEmerald),
                            ) {
                                Text(
                                    text = "VRAM: ${simulation.estimatedVramGb} GB",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (simulation.vramWarning) NeonRose else NeonEmerald,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                )
                            }
                        }

                        if (simulation.vramWarning) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = NeonRose,
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = if (isTr) "Uyarı: Tahmini VRAM tüketimi (${simulation.estimatedVramGb}GB), ekran kartınızın belleğini aşıyor. Takılmalar oluşabilir." else "Warning: Estimated VRAM usage (${simulation.estimatedVramGb}GB) exceeds GPU capacity. Stuttering may occur.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = NeonRose,
                                )
                            }
                        }

                        if (simulation.smoothnessRating.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = simulation.smoothnessRating,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.gameName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (game.isEsports) (if (isTr) "E-SPOR" else "ESPORTS") else "AAA",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (game.isEsports) NeonCyan else NeonAmber,
                    )
                }
                Text(
                    text = game.settings,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
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
                Text(
                    text = "%1 Low: ${game.low1PctFps} FPS",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    totalPriceTry: Double,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${tr(isTr, "BİLEŞEN YUVALARI", "COMPONENT SLOTS")} ($filledCount / $totalCount)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )

        val wizardCurrency = LocalAppCurrency.current
        val priceText = totalPriceTry.formatPriceWithCurrency(wizardCurrency, "0")
        Text(
            text = "${strings.wizardTotalPrefix}: $priceText",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
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
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val categoryTitle = category.localized(strings)

    if (item == null) {
        // Empty Slot Card with Modern Soft Icon
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onSlotClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
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
                    SoftCategoryIcon(
                        category = category,
                        size = 44.dp,
                        iconSize = 22.dp,
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Column {
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = strings.wizardNoPartSelected,
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
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = strings.wizardAddPart,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                        )
                    }
                }
            }
        }
    } else {
        // Filled Slot Card — 3-Block Universal Responsive Card Topology
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, if (item.isCustomPrice) NeonAmber.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.4f)),
        ) {
            Column(modifier = Modifier.padding(Spacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Sol Blok (Görsel): 72x72dp
                    HardwareThumbnail(
                        imageUrl = item.imageUrl,
                        displayName = item.displayName,
                        category = category,
                    )

                    // Orta Blok (İçerik): weight(1f), minWidth = 0dp, maxLines = 2 + Ellipsis
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 0.dp)
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = categoryTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (item.isCustomPrice) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalOffer,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(11.dp),
                                    )
                                    Text(
                                        text = tr(isTr, "Özel Fiyat", "Custom Price"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { onDetailClick() },
                        )
                        val specSummary = formatSlotSpecSummary(item, isTr)
                        if (specSummary.isNotBlank()) {
                            Text(
                                text = specSummary,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        val benchPair = when {
                            item.spec is CpuSpec -> {
                                val pts = item.benchmarkInfo?.effectiveMultiCore
                                if (pts != null) (Icons.Rounded.Speed to "Cinebench: $pts pts • ${item.benchmarkTier ?: "Tier"}") else null
                            }
                            item.spec is GpuSpec -> {
                                val pts = item.benchmarkInfo?.effectiveTimeSpy
                                if (pts != null) (Icons.Rounded.SportsEsports to "3DMark Time Spy: $pts pts • ${item.benchmarkTier ?: "Tier"}") else null
                            }
                            else -> item.benchmarkScore?.let { (Icons.Rounded.Star to "${tr(isTr, "Skor", "Score")}: $it/100 • ${item.benchmarkTier ?: "Tier"}") }
                        }
                        if (benchPair != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = benchPair.first,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = benchPair.second,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    // Sağ Blok (Fiyat): minWidth = 96dp, maxLines = 1, softWrap = false
                    Column(
                        modifier = Modifier
                            .widthIn(min = 96.dp)
                            .wrapContentWidth(Alignment.End),
                        horizontalAlignment = Alignment.End,
                    ) {
                        val priceText = item.currentPriceTry.formatPriceWithCurrency(LocalAppCurrency.current, LocalAppStrings.current.noPriceFallback)
                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                item.isCustomPrice -> NeonAmber
                                item.currentPriceTry != null -> NeonEmerald
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            softWrap = false,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.End,
                        )
                    }
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
                        Text(tr(isTr, "Detay & Benchmark", "Detail & Benchmark"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.width(Spacing.small))

                    OutlinedButton(
                        onClick = onSlotClick,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(tr(isTr, "Değiştir", "Change"), style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    }

                    Spacer(modifier = Modifier.width(Spacing.small))

                    OutlinedButton(
                        onClick = onRemoveClick,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(tr(isTr, "Kaldır", "Remove"), style = MaterialTheme.typography.labelSmall, color = NeonRose)
                    }
                }
            }
        }
    }
}

private fun formatSlotSpecSummary(item: HardwareItem, isTr: Boolean = true): String = when (val s = item.spec) {
    is com.panda.myapplication.domain.model.PhoneSpec -> "${s.displaySizeInch}\" ${s.displayTech} • ${s.chipset} • ${s.storageGb}GB • ${s.mainCameraMp}MP"
    is com.panda.myapplication.domain.model.TabletSpec -> "${s.displaySizeInch}\" • ${s.chipset} • ${s.storageGb}GB • ${s.batteryMah}mAh" 
    is CpuSpec -> "${s.socket} • ${s.coreCount} ${if (isTr) "Çekirdek" else "Cores"} • ${s.tdp.watts}W"
    is MotherboardSpec -> "${s.socket} • ${s.chipset} • ${s.memoryGeneration.name} • ${s.formFactor.name}"
    is RamSpec -> "${s.generation.name} • ${s.speed.mhz}MT/s • ${s.capacity.megabytes / 1024}GB"
    is GpuSpec -> listOfNotNull(s.chipset, "${s.vram.megabytes / 1024}GB", s.lengthMm?.let { "${it}mm" }).joinToString(" • ")
    is StorageSpec -> "${s.mediaType.name} • ${s.capacity.megabytes / 1024}GB • ${s.sequentialReadMbps}MB/s"
    is CoolerSpec -> "${s.type.name} • ${s.radiatorSizeMm?.let { "${it}mm" } ?: "Hava"}"
    is CaseSpec -> listOfNotNull(
        s.type ?: "ATX Kasa",
        s.maxGpuLengthMm?.let { "GPU: max ${it}mm" },
        s.maxRadiatorSizeMm?.let { "Rad: ${it}mm" },
    ).joinToString(" • ")
    is PsuSpec -> "${s.wattage.watts}W • ${s.efficiency.name}"
    is PrebuiltSpec -> "${s.cpuModel} • ${s.gpuModel} • ${s.storeName}"
    is LaptopSpec -> "${s.gpuModel} (${s.gpuWattTgp}) • ${s.displaySpec}"
    is MouseSpec -> "${s.sensor} • ${s.pollingRateHz}Hz • ${s.weightG}g"
    is KeyboardSpec -> "${s.switchType} • ${s.minActuationMm}mm • ${s.layout}"
    is HeadsetSpec -> "${s.driverType} (${s.driverSizeMm}mm) • ${s.impedanceOhm}Ω"
    is MonitorSpec -> "${s.screenSizeInch}\" ${s.panelType} • ${s.resolution} • ${s.refreshRateHz}Hz"
    is MicrophoneSpec -> "${s.capsuleType} • ${s.polarPattern} • ${s.connectivity}"
    is MousepadSpec -> "${s.surfaceType} • ${s.speedGlideTier} • ${s.dimensionsMm}"
    is WebcamSpec -> "${s.maxResolution} @ ${s.maxFrameRateFps}FPS • ${s.sensorModel}"
    is GamingChairSpec -> "${s.chairType} • ${s.upholsteryMaterial} • Max ${s.maxWeightCapacityKg}kg"
    else -> item.model
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(540.dp)
            .navigationBarsPadding()
            .padding(horizontal = Spacing.medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "${category.localized(strings).uppercase()} ${tr(isTr, "SEÇİMİ", "SELECTION")}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${items.size} ${tr(isTr, "uyumlu seçenek mevcut", "compatible options available")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${tr(isTr, "Çift Yönlü Filtre", "Bidirectional Filter")}: $constraintDesc",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        // Search Bar with Local State for 120 FPS Typing
        var localSearchQuery by remember(category) { mutableStateOf(searchQuery) }

        OutlinedTextField(
            value = localSearchQuery,
            onValueChange = {
                localSearchQuery = it
                onSearchChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(tr(isTr, "Model veya marka ara...", "Search model or brand..."), style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                    text = tr(isTr, "Bu kriterlere veya arama sorgusuna uygun parça bulunamadı.", "No components match these criteria or search query."),
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Sol Blok (Görsel): 72x72dp
            HardwareThumbnail(
                imageUrl = item.imageUrl,
                displayName = item.displayName,
                category = item.category,
            )

            // Orta Blok (İçerik): weight(1f), minWidth = 0dp, maxLines = 2 + Ellipsis
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 0.dp)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatSlotSpecSummary(item, isTr),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Sağ Blok (Fiyat ve Aksiyon): minWidth = 96dp, maxLines = 1, softWrap = false
            Column(
                modifier = Modifier
                    .widthIn(min = 96.dp)
                    .wrapContentWidth(Alignment.End),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val priceText = item.currentPriceTry.formatPriceWithCurrency(LocalAppCurrency.current, LocalAppStrings.current.noPriceFallback)
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.End,
                )
                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = strings.wizardSelect,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        softWrap = false,
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.onPrimary,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = "HARDWARE WIZARD",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 1.5.sp,
                            )
                        }
                        Text(
                            text = tr(isTr, "SİSTEM KARTI", "SYSTEM CARD"),
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
                            text = localizedPlatformName(platformStatus.platformName, isTr),
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
                    SharePartRow(HardwareCategory.CPU.localized(strings), build.cpu?.displayName)
                    SharePartRow(HardwareCategory.MOTHERBOARD.localized(strings), build.motherboard?.displayName)
                    SharePartRow(HardwareCategory.RAM.localized(strings), build.ram?.displayName)
                    SharePartRow(HardwareCategory.GPU.localized(strings), build.gpu?.displayName)
                    SharePartRow(HardwareCategory.STORAGE.localized(strings), build.storage?.displayName)
                    SharePartRow(HardwareCategory.COOLER.localized(strings), build.cooler?.displayName)
                    SharePartRow(HardwareCategory.CASE.localized(strings), build.case?.displayName)
                    SharePartRow(HardwareCategory.PSU.localized(strings), build.psu?.displayName)
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
                        Text(tr(isTr, "GÜÇ TÜKETİMİ", "POWER DRAW"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${cockpitInfo.componentTdpSum}W TDP", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                    Column {
                        Text(tr(isTr, "DARBOĞAZ", "BOTTLENECK"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(bottleneck.message, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = NeonEmerald)
                    }
                }

                if (gamingSim.isReady && gamingSim.games.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text("${tr(isTr, "OYUN PERFORMANSI", "GAMING PERFORMANCE")} (${gamingSim.resolution.label}):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    val priceText = build.totalPriceTry.formatPriceWithCurrency(LocalAppCurrency.current, "N/A")
                    Column {
                        Text(tr(isTr, "TOPLAM BÜTÇE", "TOTAL BUDGET"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(priceText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NeonCyan)
                    }

                    Text(
                        text = "BUILT WITH HARDWARE WIZARD",
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
                        Text(tr(isTr, "Kapat", "Close"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = onCopyAndShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    ) {
                        Text(tr(isTr, "Panoya Kopyala", "Copy to Clipboard"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SharePartRow(category: String, name: String?) {
    val strings = LocalAppStrings.current

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
            text = name ?: strings.wizardNotSelected,
            style = MaterialTheme.typography.labelSmall,
            color = if (name != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = Spacing.small),
        )
    }
}

// endregion

private fun localizedPlatformStatus(status: PlatformStatusBadge, isTr: Boolean): String {
    if (isTr) return status.statusText
    return when {
        status.statusText == "CPU veya Anakart ekleyerek başlayın" -> "Start by adding a CPU or Motherboard"
        status.statusText.contains("Soket Uyuşmazlığı") -> status.statusText.replace("Soket Uyuşmazlığı", "Socket Mismatch")
        status.statusText.contains("Bellek Nesli Uyuşmazlığı") -> status.statusText.replace("Bellek Nesli Uyuşmazlığı", "Memory Generation Mismatch")
        status.statusText.contains("Platformu - %100 Uyumlu") -> status.statusText.replace("Platformu - %100 Uyumlu", "Platform - 100% Compatible")
        status.statusText.contains("Mimarisi - Soket Uyumlu") -> status.statusText.replace("Mimarisi - Soket Uyumlu", "Architecture - Socket Compatible")
        status.statusText.contains("Altyapısı Aktif") -> status.statusText.replace("Altyapısı Aktif", "Platform Active")
        else -> status.statusText
    }
}

private fun localizedPlatformName(name: String, isTr: Boolean): String {
    if (isTr) return name
    if (name == "Platform Seçilmedi") return "Platform Not Selected"
    return name
}

private fun localizedDoctorStatus(statusText: String, isTr: Boolean): String {
    if (isTr) return statusText
    return when {
        statusText == "Sistem Boş" -> "System Empty"
        statusText == "Mükemmel Denge & Yüksek Verim" -> "Excellent Balance & High Efficiency"
        statusText == "İyi Durumda - Küçük İyileştirmeler Yapılabilir" -> "Good Condition - Minor Improvements Possible"
        statusText == "Optimize Edilmeli - Belirgin Dengesizlik Var" -> "Needs Optimization - Noticeable Imbalance"
        statusText == "Kritik Sorunlar Mevcut - Revizyon Gerekli" -> "Critical Issues Present - Revision Required"
        statusText.contains("Mükemmel Denge") -> statusText.replace("Mükemmel Denge", "Excellent Balance")
        statusText.contains("Dengeli") -> statusText.replace("Dengeli", "Balanced")
        statusText.contains("Darboğaz") -> statusText.replace("Darboğaz", "Bottleneck")
        else -> statusText
    }
}

private fun localizedDoctorDiagnosis(diagnosis: String, isTr: Boolean): String {
    if (isTr) return diagnosis
    return when {
        diagnosis == "Sistem analizi için parça ekleyin." -> "Add components to analyze system health."
        diagnosis.contains("CPU ve GPU") -> "Select CPU and GPU components to begin health and bottleneck analysis."
        diagnosis.contains("Bileşenleriniz birbiriyle son derece uyumlu") -> "Your components are excellently paired. Power, cooling, and bottleneck margins are at optimal levels."
        diagnosis.contains("Sistem dengesi genel standartları karşılıyor") -> "System balance meets standard performance thresholds."
        diagnosis.contains("Sistemden tam performans almak için") -> "To maximize system performance, consider upgrading the weakest component."
        else -> diagnosis
    }
}

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText
