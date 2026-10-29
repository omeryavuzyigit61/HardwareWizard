package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.panda.myapplication.domain.model.CaseFanSpec
import com.panda.myapplication.domain.model.UpsSpec
import com.panda.myapplication.domain.model.GamingDeskSpec
import com.panda.myapplication.domain.model.GamingLightingSpec
import com.panda.myapplication.domain.model.SmartwatchSpec
import com.panda.myapplication.domain.model.RouterSpec
import com.panda.myapplication.domain.model.ModemSpec
import com.panda.myapplication.domain.model.AccessPointSpec
import com.panda.myapplication.domain.model.RangeExtenderSpec
import com.panda.myapplication.domain.model.CompanionSoftware
import com.panda.myapplication.domain.model.CompanionSoftwareRegistry

import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.DeviceVariant
import androidx.compose.foundation.layout.widthIn

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.panda.myapplication.presentation.util.localized
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.panda.myapplication.domain.engine.BenchmarkEngine
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GameCategory
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.GamingChairSpec
import com.panda.myapplication.domain.model.MicrophoneSpec
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MousepadSpec
import com.panda.myapplication.domain.model.WebcamSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.StoreOffer
import com.panda.myapplication.domain.model.TabletSpec
import com.panda.myapplication.domain.model.VerifiedLabBenchmarks
import com.panda.myapplication.domain.model.VerifiedLabGameTest
import com.panda.myapplication.domain.model.VerifiedLabTestEntry
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.components.PriceOverrideDialog
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GoldAccent
import com.panda.myapplication.presentation.theme.HardwareWizardTheme
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import androidx.compose.ui.text.style.TextAlign
import com.panda.myapplication.presentation.util.formatPrice
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import com.panda.myapplication.presentation.util.openSafeStoreUrl
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import com.panda.myapplication.domain.model.ContentReport
import com.panda.myapplication.domain.model.ReportTargetType
import com.panda.myapplication.domain.model.ReportReason
import com.panda.myapplication.domain.repository.CommunityReportRepository
import com.panda.myapplication.presentation.components.ReportContentDialog

@Composable
fun HardwareDetailRoute(
    itemId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToVsArena: () -> Unit = {},
    onNavigateToTuningVault: () -> Unit = {},
    onOpenAuthSheet: () -> Unit = {},
    viewModel: HardwareDetailViewModel = koinViewModel(),
    reportRepository: CommunityReportRepository = koinInject(),
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HardwareDetailEffect.ShowToast -> {
                    val msg = if (isTr) effect.message else (effect.messageEn ?: effect.message)
                    snackbarHostState.showSnackbar(msg)
                }
                is HardwareDetailEffect.ShowVsAddedToast -> {
                    val msg = if (isTr) effect.message else (effect.messageEn ?: effect.message)
                    val result = snackbarHostState.showSnackbar(
                        message = msg,
                        actionLabel = if (isTr) "Arenaya Git" else "Go to Arena",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onNavigateToVsArena()
                    }
                }
            }
        }
    }

    HardwareDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onAddToBuildClick = viewModel::onAddToBuild,
        onAddToVsArenaClick = viewModel::onAddToVsArena,
        onSaveCustomPrice = viewModel::onSaveCustomPrice,
        onClearCustomPrice = viewModel::onClearCustomPrice,
        onSubmitCommunityDeal = viewModel::onSubmitCommunityDeal,
        onApproveCommunityDeal = viewModel::onApproveCommunityDeal,
        onRejectCommunityDeal = viewModel::onRejectCommunityDeal,
        onLoginAdmin = viewModel::loginAdmin,
        onLogoutAdmin = viewModel::logoutAdmin,
        onRefreshCommunityDeals = viewModel::onRefreshCommunityDeals,
        onOpenAuthSheet = onOpenAuthSheet,
        onNavigateToTuningVault = onNavigateToTuningVault,
        onSubmitTuningProfile = viewModel::onSubmitTuningProfile,
        onSubmitProTip = viewModel::onSubmitProTip,
        onDeleteTip = viewModel::onDeleteTip,
        onUpvoteTip = viewModel::onUpvoteProTip,
        onCreatePriceDropAlert = viewModel::onCreatePriceDropAlert,
        reportRepository = reportRepository,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun HardwareDetailContent(
    uiState: HardwareDetailUiState,
    onNavigateBack: () -> Unit,
    onAddToBuildClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddToVsArenaClick: () -> Unit = {},
    onSaveCustomPrice: (Double, String?) -> Unit = { _, _ -> },
    onClearCustomPrice: () -> Unit = {},
    onSubmitCommunityDeal: (Double, CommunityDealType, String?, String?, String) -> Unit = { _, _, _, _, _ -> },
    onApproveCommunityDeal: (String) -> Unit = {},
    onRejectCommunityDeal: (String, String?) -> Unit = { _, _ -> },
    onLoginAdmin: (String) -> Boolean = { false },
    onLogoutAdmin: () -> Unit = {},
    onRefreshCommunityDeals: () -> Unit = {},
    onOpenAuthSheet: () -> Unit = {},
    onNavigateToTuningVault: () -> Unit = {},
    onSubmitTuningProfile: (com.panda.myapplication.domain.model.TuningProfile) -> Unit = {},
    onSubmitProTip: (String, Int) -> Unit = { _, _ -> },
    onDeleteTip: (String) -> Unit = {},
    onUpvoteTip: (String) -> Unit = {},
    onCreatePriceDropAlert: (Double) -> Unit = {},
    reportRepository: CommunityReportRepository = koinInject(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showPriceDropAlertDialog by remember { mutableStateOf(false) }
    var showAddProTipDialog by remember { mutableStateOf(false) }
    var showTuningShareDialog by remember { mutableStateOf(false) }
    var reportingTargetReview by remember { mutableStateOf<com.panda.myapplication.domain.model.HardwareProTip?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var showPriceDialog by remember { mutableStateOf(false) }
    var showCommunitySubmitSheet by remember { mutableStateOf(false) }
    var selectedDealForDetail by remember { mutableStateOf<CommunityDeal?>(null) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val currency = LocalAppCurrency.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    val rawItem = uiState.item
    var selectedVariantKey by remember(rawItem?.id?.value) {
        mutableStateOf(rawItem?.variants?.firstOrNull()?.variantKey)
    }
    val activeVariant = rawItem?.variants?.find { it.variantKey == selectedVariantKey } ?: rawItem?.variants?.firstOrNull()
    val activeItem = if (activeVariant != null && rawItem != null) rawItem.withVariant(activeVariant) else rawItem

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(
                title = activeItem?.displayName ?: activeItem?.brand ?: (if (isTr) "Donanım Detayı" else "Hardware Details"),
                onNavigateBack = onNavigateBack,
                onAddToVsArenaClick = if (activeItem != null) onAddToVsArenaClick else null,
            )
        },
        bottomBar = {
            if (activeItem != null) {
                DetailBottomBar(
                    isAddedToBuild = uiState.isAddedToBuild,
                    isPrebuilt = activeItem.spec is PrebuiltSpec,
                    isLaptop = activeItem.spec is LaptopSpec,
                    isMobile = activeItem.category.isMobile,
                    isCustomPrice = activeItem.isCustomPrice,
                    customPriceNote = activeItem.customPriceNote,
                    priceText = activeItem.currentPriceTry.formatPriceWithCurrency(currency, strings.noPriceFallback),
                    onAddToBuildClick = onAddToBuildClick,
                    onAddToVsArenaClick = onAddToVsArenaClick,
                    onPriceOverrideClick = { showPriceDialog = true },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Text(
                            text = strings.detailLoadingSpecs,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val errorMsg = if (isTr) {
                            uiState.errorMessage.orEmpty()
                        } else {
                            (uiState.errorMessageEn ?: uiState.errorMessage).orEmpty()
                        }
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text(strings.detailReturnToList, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                uiState.item != null -> {
                    val item = activeItem ?: uiState.item
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.medium,
                            end = Spacing.medium,
                            top = Spacing.small,
                            bottom = Spacing.large,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                    ) {
                        // Hero Header Card
                        item(key = "hero_header") {
                            HeroHeaderCard(
                                item = item,
                                onPriceOverrideClick = { showPriceDialog = true },
                            )
                        }

                        // Storage & RAM Variants Card (Smartphones & Tablets)
                        if (rawItem.variants.isNotEmpty()) {
                            item(key = "storage_ram_variants") {
                                DeviceVariantSelectorCard(
                                    variants = rawItem.variants,
                                    selectedVariantKey = activeVariant?.variantKey.orEmpty(),
                                    onVariantSelect = { selectedVariantKey = it.variantKey },
                                    currency = currency,
                                    noPriceFallback = strings.noPriceFallback,
                                    isTr = isTr,
                                )
                            }
                        }

                        // Special OEM Hazır Sistem Card & Profit Analyzer
                        if (item.spec is PrebuiltSpec) {
                            item(key = "prebuilt_details") {
                                PrebuiltSystemDetailsCard(
                                    spec = item.spec,
                                    onTransferToWizard = onAddToBuildClick,
                                )
                            }
                        }

                        // Special Gaming Laptop Hardware & TGP Highlights Card
                        if (item.spec is LaptopSpec) {
                            item(key = "laptop_details") {
                                LaptopSpecialDetailsCard(
                                    item = item,
                                    spec = item.spec,
                                )
                            }
                        }

                        // 12-Game Real Benchmark & %1 Low FPS Simulation Matrix (GPU, CPU, OEM System, Laptop)
                        if (item.category == HardwareCategory.GPU ||
                            item.category == HardwareCategory.CPU ||
                            item.category == HardwareCategory.PREBUILT_PC ||
                            item.category == HardwareCategory.LAPTOP
                        ) {
                            item(key = "multi_game_fps_matrix") {
                                MultiGameFpsSimulationCard(item = item)
                            }
                        }

                        // Price History & Market Analysis (TL) Card
                        item(key = "price_history") {
                            PriceHistoryCard(
                                priceHistory = item.priceHistory,
                                currentPriceTry = item.currentPriceTry,
                                lowestPriceTry = item.lowestPriceTry,
                                highestPriceTry = item.highestPriceTry,
                            )
                        }

                        // Store Offers Comparison (Canlı TR Piyasası) Card
                        // Temporarily hidden per user request (kaldırma sadece gizle)
                        val showStoreOffers = false
                        if (showStoreOffers) {
                            item(key = "store_offers") {
                                StoreOffersCard(
                                    offers = item.offers,
                                    currentPriceTry = item.currentPriceTry,
                                    productName = item.displayName,
                                )
                            }
                        }

                        // Binance-Style Community Price Ledger & Deal Feed ("⚡ CANLI TOPLULUK FİYAT DEFTERİ")
                        item(key = "community_deals_ledger") {
                            CommunityPriceLedgerSection(
                                deals = uiState.communityDeals,
                                isAdmin = uiState.isAdmin,
                                onDealClick = { deal -> selectedDealForDetail = deal },
                                onSubmitClick = {
                                    if (!uiState.isLoggedIn) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = strings.detailAuthRequiredPrice,
                                                duration = SnackbarDuration.Short,
                                            )
                                        }
                                        onOpenAuthSheet()
                                    } else {
                                        showCommunitySubmitSheet = true
                                    }
                                },
                                onApproveDeal = onApproveCommunityDeal,
                                onRejectDeal = { dealId -> onRejectCommunityDeal(dealId, null) },
                                onAdminLoginClick = { showAdminLoginDialog = true },
                                onAdminLogoutClick = onLogoutAdmin,
                                onRefreshClick = onRefreshCommunityDeals,
                            )
                        }

                        // 💬 Kullanıcı Yorumları & Topluluk Değerlendirmeleri
                        item(key = "community_reviews_and_comments") {
                            HardwareCommentsAndReviewsSection(
                                hardwareName = item.displayName,
                                tips = uiState.proTips,
                                currentUserId = uiState.currentUser?.id,
                                currentUsername = uiState.currentUser?.username,
                                onAddTipClick = {
                                    if (!uiState.isLoggedIn) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Yorum ve deneyim paylaşmak için oturum açmalısınız.")
                                        }
                                        onOpenAuthSheet()
                                    } else {
                                        showAddProTipDialog = true
                                    }
                                },
                                onUpvoteClick = onUpvoteTip,
                                onReportClick = { tip ->
                                    reportingTargetReview = tip
                                },
                            )
                        }

                        // Topluluk OC / UV Silikon Kasası (Yalnızca CPU & GPU)
                        if (item.category == com.panda.myapplication.domain.model.HardwareCategory.CPU || item.category == com.panda.myapplication.domain.model.HardwareCategory.GPU) {
                            item(key = "community_tuning_vault") {
                                CommunityTuningVaultSection(
                                    profiles = uiState.tuningProfiles,
                                    onShareTuningClick = {
                                        if (!uiState.isLoggedIn) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (isTr) "OC/UV ayarı paylaşmak için oturum açmalısınız." else "Please sign in to share OC/UV settings."
                                                )
                                            }
                                            onOpenAuthSheet()
                                        } else {
                                            showTuningShareDialog = true
                                        }
                                    },
                                    onOpenTuningVaultClick = onNavigateToTuningVault,
                                )
                            }
                        }

                        // Synthetic Multi-Benchmark Card - STRICTLY CpuSpec & GpuSpec
                        if ((item.category == HardwareCategory.CPU || item.category == HardwareCategory.GPU) && item.benchmarkInfo != null) {
                            item(key = "synthetic_benchmark") {
                                SyntheticBenchmarkCard(
                                    category = item.category,
                                    benchmarkInfo = item.benchmarkInfo,
                                )
                            }
                        }

                        // Mobile Benchmark & Stability Card (Smartphone & Tablet)
                        if (item.category.isMobile) {
                            item(key = "mobile_benchmark_card") {
                                MobileBenchmarkCard(item = item)
                            }
                        }

                        // Performance & Power Progress Bars (CPU, GPU, Peripherals, Networking, UPS & Mobile)
                        if (item.category == HardwareCategory.CPU ||
                            item.category == HardwareCategory.GPU ||
                            item.category.isPeripheral ||
                            item.category.isMobile ||
                            item.category.isNetworking ||
                            item.category.isPowerBackup
                        ) {
                            item(key = "perf_power_section") {
                                PerformancePowerSection(item = item)
                            }
                        }

                        // Category-specific Polymorphic Technical Specifications Section
                        item(key = "tech_specs") {
                            PolymorphicTechnicalSpecsSection(item = item)
                        }

                        // Eşlikçi Yazılım & Sürücü Bulucu (Companion Software Hub)
                        val companionSoftware = CompanionSoftwareRegistry.resolveFor(item)
                        if (companionSoftware != null) {
                            item(key = "companion_software") {
                                CompanionSoftwareCard(software = companionSoftware)
                            }
                        }

                        // Akıllı Kesintisiz Güç Kaynağı (UPS) Anlık Yük & Çalışma Süresi Simülatörü
                        if (item.spec is UpsSpec) {
                            item(key = "ups_runtime_estimator") {
                                UpsRuntimeEstimatorCard(spec = item.spec as UpsSpec)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPriceDialog && uiState.item != null) {
        val currentItem = uiState.item
        PriceOverrideDialog(
            item = currentItem,
            onDismiss = { showPriceDialog = false },
            onSaveCustomPrice = { customPrice, note ->
                onSaveCustomPrice(customPrice, note)
                showPriceDialog = false
            },
            onClearCustomPrice = {
                onClearCustomPrice()
                showPriceDialog = false
            },
        )
    }

    if (showCommunitySubmitSheet && uiState.item != null) {
        SubmitCommunityDealSheet(
            hardwareName = uiState.item.displayName,
            currentUser = uiState.currentUser,
            onDismiss = { showCommunitySubmitSheet = false },
            onSubmit = { price, type, note, proof, name ->
                onSubmitCommunityDeal(price, type, note, proof, name)
            },
        )
    }

    if (selectedDealForDetail != null) {
        val currentDeal = selectedDealForDetail!!
        CommunityDealDetailDialog(
            deal = currentDeal,
            isAdmin = uiState.isAdmin,
            onDismiss = { selectedDealForDetail = null },
            onApprove = {
                onApproveCommunityDeal(currentDeal.id)
                selectedDealForDetail = null
            },
            onReject = {
                onRejectCommunityDeal(currentDeal.id, null)
                selectedDealForDetail = null
            },
        )
    }

    if (showAdminLoginDialog) {
        AdminLoginPinDialog(
            onDismiss = { showAdminLoginDialog = false },
            onLogin = onLoginAdmin,
        )
    }

    if (showPriceDropAlertDialog) {
        PriceDropAlertDialog(
            hardwareName = activeItem?.displayName ?: "",
            currentPriceTry = activeItem?.currentPriceTry,
            onDismiss = { showPriceDropAlertDialog = false },
            onSaveAlert = { targetPrice ->
                onCreatePriceDropAlert(targetPrice)
                showPriceDropAlertDialog = false
            },
        )
    }

    if (showAddProTipDialog) {
        AddProductReviewDialog(
            hardwareName = activeItem?.displayName ?: "",
            onDismiss = { showAddProTipDialog = false },
            onSubmit = { tipText, rating ->
                onSubmitProTip(tipText, rating)
                showAddProTipDialog = false
            },
        )
    }

    if (showTuningShareDialog) {
        TuningShareDialog(
            hardwareId = activeItem?.id?.value ?: "",
            hardwareName = activeItem?.displayName ?: "",
            hardwareCategory = activeItem?.category?.name ?: "CPU",
            userId = uiState.currentUser?.id ?: "usr-guest",
            userDisplayName = uiState.currentUser?.username ?: (if (isTr) "Kullanıcı" else "Member"),
            userBadge = uiState.currentUser?.effectiveBadge ?: "Lehim Çırağı",
            onDismiss = { showTuningShareDialog = false },
            onSubmit = { profile ->
                onSubmitTuningProfile(profile)
                showTuningShareDialog = false
            },
        )
    }

    reportingTargetReview?.let { target ->
        val isSelf = uiState.currentUser?.id == target.userId || (uiState.currentUser?.username != null && target.userDisplayName == uiState.currentUser?.username)
        ReportContentDialog(
            targetType = ReportTargetType.COMMENT,
            targetTitle = "${activeItem?.displayName ?: ""}: \"${target.tipText.take(45)}\"",
            reportedUserName = target.userDisplayName,
            isSelfReport = isSelf,
            isTr = isTr,
            onDismiss = { reportingTargetReview = null },
            onSubmitReport = { reason, details ->
                coroutineScope.launch {
                    val report = ContentReport(
                        id = "rep-comment-${System.currentTimeMillis()}",
                        targetType = ReportTargetType.COMMENT,
                        targetId = target.id,
                        targetTitle = "${activeItem?.displayName ?: ""}: \"${target.tipText.take(45)}\"",
                        reportedUserId = target.userId,
                        reportedUserDisplayName = target.userDisplayName,
                        reporterUserId = uiState.currentUser?.id ?: "usr-guest",
                        reporterUserDisplayName = uiState.currentUser?.username ?: (if (isTr) "Misafir" else "Guest"),
                        isSelfReport = isSelf,
                        reason = reason,
                        details = details,
                        timestampMillis = System.currentTimeMillis(),
                    )
                    reportRepository.submitReport(report)
                    if (isSelf) {
                        onDeleteTip(target.id)
                        snackbarHostState.showSnackbar(
                            if (isTr) "Yorumunuz yayından kaldırıldı." else "Your review has been removed."
                        )
                    } else {
                        snackbarHostState.showSnackbar(
                            if (isTr) "Yorum bildirimi moderasyon ekibine iletildi. Teşekkür ederiz."
                            else "Review report submitted to moderation. Thank you."
                        )
                    }
                    reportingTargetReview = null
                }
            }
        )
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAddToVsArenaClick: (() -> Unit)? = null,
) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.small, vertical = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        BackArrowCanvas(color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.small))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (onAddToVsArenaClick != null) {
                Surface(
                    onClick = onAddToVsArenaClick,
                    shape = RoundedCornerShape(10.dp),
                    color = NeonCyan.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.65f)),
                ) {
                    Text(
                        text = "⚖️ " + strings.addToVsArena,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroHeaderCard(
    item: HardwareItem,
    modifier: Modifier = Modifier,
    onPriceOverrideClick: (() -> Unit)? = null,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = if (item.isCustomPrice) NeonAmber else ElectricBlue,
        secondaryColor = if (item.isCustomPrice) NeonRose else NeonCyan,
        shape = RoundedCornerShape(Spacing.cardRadius),
        contentPadding = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            HardwareDetailHeroImage(
                images = item.effectiveImages,
                imageUrl = item.effectiveImageUrl,
                category = item.category,
                displayName = item.displayName,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.medium),
            ) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DetailBadge(
                            text = item.category.localized(strings),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        if (item.isCustomPrice) {
                            DetailBadge(
                                text = strings.detailCustomPriceTag,
                                containerColor = NeonAmber.copy(alpha = 0.2f),
                                contentColor = NeonAmber,
                            )
                        }
                    }

                    DetailBadge(
                        text = item.brand.uppercase(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(Spacing.extraSmall))

                val modelSubtitle = if (item.spec is GpuSpec && !item.spec.partNumber.isNullOrBlank()) {
                    "${if (isTr) "Model" else "Model"}: ${item.model} • ${if (isTr) "Parça No (SKU)" else "Part No (SKU)"}: ${item.spec.partNumber}"
                } else {
                    "${if (isTr) "Model" else "Model"}: ${item.model}"
                }
                Text(
                    text = modelSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (onPriceOverrideClick != null) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Surface(
                        onClick = onPriceOverrideClick,
                        shape = RoundedCornerShape(10.dp),
                        color = if (item.isCustomPrice) NeonAmber.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (item.isCustomPrice) NeonAmber.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (item.isCustomPrice) {
                                        "🏷️ ${LocalAppStrings.current.localPriceOverride}: ${item.customPriceTry.formatPriceWithCurrency("")}"
                                    } else {
                                        "🏷️ ${LocalAppStrings.current.localPriceOverride}"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isCustomPrice) NeonAmber else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (!item.customPriceNote.isNullOrBlank()) {
                                        "Not: ${item.customPriceNote}"
                                    } else {
                                        LocalAppStrings.current.localPriceDesc
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = if (item.isCustomPrice) strings.detailEditPrice else strings.detailEnterPrice,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.isCustomPrice) NeonAmber else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformancePowerSection(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = LocalAppStrings.current.detailPerformancePower,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            when (val s = item.spec) {
                is CpuSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Çekirdek & İzlek Kapasitesi", "Core & Thread Capacity"),
                        currentValue = if (isTr) "${s.coreCount} Çekirdek / ${s.threadCount} İzlek" else "${s.coreCount} Cores / ${s.threadCount} Threads",
                        fraction = (s.coreCount / 32f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "Maksimum 32 Çekirdek referansına göre", "Relative to 32-core flagship reference"),
                    )

                    val clockMhz = (s.boostClock ?: s.baseClock).mhz
                    MetricProgressBar(
                        title = tr(isTr, "Maksimum İşlemci Frekansı", "Maximum Boost Frequency"),
                        currentValue = "${clockMhz / 1000f} GHz",
                        fraction = (clockMhz / 6000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "6.0 GHz tepe saat hızına oranla", "Relative to 6.0 GHz clock reference"),
                    )

                    MetricProgressBar(
                        title = tr(isTr, "TDP Güç Tüketimi", "TDP Power Consumption"),
                        currentValue = "${s.tdp.watts} W",
                        fraction = (s.tdp.watts / 300f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = tr(isTr, "Soğutucu gereksinim düzeyi", "Cooler requirement level"),
                    )

                    val multiCoreIndex = ((s.coreCount * 250f + clockMhz) / 14000f).coerceIn(0f, 1f)
                    MetricProgressBar(
                        title = tr(isTr, "Sentetik Performans İndeksi", "Synthetic Compute Index"),
                        currentValue = "${(multiCoreIndex * 100).toInt()}% Skoru",
                        fraction = multiCoreIndex,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "Hesaplama ve render gücü tahmini", "Estimated render and compute power"),
                    )
                }

                is GpuSpec -> {
                    val vramGb = s.vram.megabytes / 1024
                    MetricProgressBar(
                        title = "Video Memory (VRAM)",
                        currentValue = "$vramGb GB ${s.memoryType}",
                        fraction = (vramGb / 24f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "24 GB referans tepe VRAM", "Relative to 24 GB flagship reference"),
                    )

                    val gpuClock = (s.boostClock ?: s.coreClock).mhz
                    MetricProgressBar(
                        title = tr(isTr, "Grafik Çekirdek Frekansı", "GPU Core Frequency"),
                        currentValue = "$gpuClock MHz",
                        fraction = (gpuClock / 3000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "3000 MHz hedef frekansa göre", "Relative to 3000 MHz target"),
                    )

                    MetricProgressBar(
                        title = tr(isTr, "TDP Güç Çekimi", "TDP Power Draw"),
                        currentValue = "${s.tdp.watts} W",
                        fraction = (s.tdp.watts / 450f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = tr(isTr, "Kartın doğrudan çektiği güç", "Direct board power consumption"),
                    )

                    MetricProgressBar(
                        title = tr(isTr, "Önerilen Güç Kaynağı (PSU)", "Recommended Power Supply (PSU)"),
                        currentValue = "${s.recommendedPsu.watts} W",
                        fraction = (s.recommendedPsu.watts / 1000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "Sistem geneli tavsiye edilen besleme", "Recommended system power delivery"),
                    )
                }

                is RamSpec -> {
                    val capGb = s.capacity.megabytes / 1024
                    MetricProgressBar(
                        title = tr(isTr, "Bellek Kapasitesi", "Memory Capacity"),
                        currentValue = if (isTr) "$capGb GB (${s.moduleCount}x Modül)" else "$capGb GB (${s.moduleCount}x Modules)",
                        fraction = (capGb / 128f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "128 GB maksimum masaüstü sınırına göre", "Relative to 128 GB desktop standard"),
                    )

                    MetricProgressBar(
                        title = tr(isTr, "Frekans Veri Hızı", "Frequency & Transfer Rate"),
                        currentValue = "${s.speed.mhz} MT/s",
                        fraction = (s.speed.mhz / 8400f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "JEDEC & XMP/EXPO maksimum profili", "JEDEC & XMP/EXPO max profile"),
                    )

                    val latencyEfficiency = (1f - ((s.casLatency - 14).coerceIn(0, 30) / 30f)).coerceIn(0.1f, 1f)
                    MetricProgressBar(
                        title = tr(isTr, "Gecikme Hızı (CAS Latency)", "CAS Latency"),
                        currentValue = "CL${s.casLatency}",
                        fraction = latencyEfficiency,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = tr(isTr, "Daha düşük gecikme = yüksek duyarlılık", "Lower latency = higher responsiveness"),
                    )
                }

                is StorageSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Sıralı Okuma Hızı", "Sequential Read Speed"),
                        currentValue = "${s.sequentialReadMbps} MB/sn",
                        fraction = (s.sequentialReadMbps / 14000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "PCIe Gen 5 sınırına göre (14.000 MB/s)", "Relative to PCIe Gen 5 ceiling (14,000 MB/s)"),
                    )

                    MetricProgressBar(
                        title = tr(isTr, "Sıralı Yazma Hızı", "Sequential Write Speed"),
                        currentValue = "${s.sequentialWriteMbps} MB/sn",
                        fraction = (s.sequentialWriteMbps / 12000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "Dosya kaydetme ve kopyalama hızı", "File write & copy throughput"),
                    )

                    val storageCapGb = s.capacity.megabytes / 1024
                    MetricProgressBar(
                        title = tr(isTr, "Depolama Alanı", "Storage Capacity"),
                        currentValue = if (storageCapGb >= 1024) "${storageCapGb / 1024} TB" else "$storageCapGb GB",
                        fraction = (storageCapGb / 4096f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = tr(isTr, "Maksimum 4 TB referans", "Relative to 4 TB flagship ceiling"),
                    )
                }

                is CoolerSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Maksimum Isı Dağıtımı (TDP)", "Maximum Heat Dissipation (TDP)"),
                        currentValue = "${s.maxCoolingTdp.watts} W",
                        fraction = (s.maxCoolingTdp.watts / 400f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "Masaüstü tepe soğutma katsayısı", "Peak desktop thermal rating"),
                    )

                    val radiatorMm = s.radiatorSizeMm ?: 120
                    MetricProgressBar(
                        title = tr(isTr, "Radyatör / Fan Boyutu", "Radiator / Fan Dimension"),
                        currentValue = s.radiatorSizeMm?.let { "${it} mm" } ?: tr(isTr, "Hava Soğutma Kulesi", "Air Cooling Tower"),
                        fraction = (radiatorMm / 420f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = if (s.type == CoolerType.LIQUID) tr(isTr, "Sıvı soğutma radyatör uzunluğu", "Liquid cooling radiator length") else tr(isTr, "Standart kule tipi montaj", "Standard tower installation"),
                    )
                }

                is PsuSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Çıkış Gücü Kapasitesi", "Output Power Capacity"),
                        currentValue = "${s.wattage.watts} W",
                        fraction = (s.wattage.watts / 1600f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "1600 Watt tepe standardına göre", "Relative to 1600W flagship ceiling"),
                    )

                    val effFraction = when (s.efficiency) {
                        PsuEfficiencyRating.TITANIUM -> 0.96f
                        PsuEfficiencyRating.PLATINUM -> 0.92f
                        PsuEfficiencyRating.GOLD -> 0.90f
                        PsuEfficiencyRating.SILVER -> 0.85f
                        PsuEfficiencyRating.BRONZE -> 0.82f
                        PsuEfficiencyRating.STANDARD -> 0.80f
                    }
                    MetricProgressBar(
                        title = tr(isTr, "Verimlilik Derecesi", "Efficiency Rating"),
                        currentValue = s.efficiency.displayName,
                        fraction = effFraction,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "80 PLUS enerji verimlilik sertifikası", "80 PLUS energy efficiency standard"),
                    )
                }

                is MotherboardSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Bellek Yuva Desteği", "Memory Slot Support"),
                        currentValue = if (isTr) "${s.ramSlots} RAM Yuvası" else "${s.ramSlots} RAM Slots",
                        fraction = (s.ramSlots / 8f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "Maksimum genişletilebilirlik", "Maximum memory expandability"),
                    )

                    val pcieFraction = when (s.pcieGeneration) {
                        PcieGeneration.GEN5 -> 1.0f
                        PcieGeneration.GEN4 -> 0.8f
                        PcieGeneration.GEN3 -> 0.6f
                        PcieGeneration.GEN2 -> 0.4f
                    }
                    MetricProgressBar(
                        title = tr(isTr, "PCI Express Veri Yolu", "PCI Express Bus Standard"),
                        currentValue = s.pcieGeneration.displayName,
                        fraction = pcieFraction,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "Ekran kartı ve NVMe bant genişliği standardı", "GPU & NVMe bandwidth generation"),
                    )
                }

                is CaseSpec -> {
                    s.maxGpuLengthMm?.let { gpuLen ->
                        MetricProgressBar(
                            title = tr(isTr, "Maksimum GPU Uzunluğu", "Maximum GPU Clearance"),
                            currentValue = "$gpuLen mm",
                            fraction = (gpuLen / 450f).coerceIn(0f, 1f),
                            accentColor = MaterialTheme.colorScheme.primary,
                            subText = tr(isTr, "450 mm referans kasa derinliğine göre", "Relative to 450mm chassis depth"),
                        )
                    }
                    s.maxRadiatorSizeMm?.let { radSize ->
                        MetricProgressBar(
                            title = tr(isTr, "Maksimum Radyatör Desteği", "Maximum Radiator Support"),
                            currentValue = "$radSize mm",
                            fraction = (radSize / 420f).coerceIn(0f, 1f),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            subText = tr(isTr, "Sıvı soğutma radyatör montaj kapasitesi", "AIO liquid radiator mounting capacity"),
                        )
                    }
                }

                is PrebuiltSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Oyun Performans Skoru", "Gaming Performance Score"),
                        currentValue = "${s.gamingScore} / 100",
                        fraction = (s.gamingScore / 100f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "1080p: ${s.fps1080p} FPS • 1440p: ${s.fps1440p} FPS • 4K: ${s.fps4k} FPS",
                    )
                    val sepPrice = s.estimatedSeparatePriceTry
                    if (sepPrice != null && sepPrice > 0) {
                        val advantageFraction = (s.advantagePercent / 30.0).coerceIn(0.0, 1.0).toFloat()
                        MetricProgressBar(
                            title = tr(isTr, "Hazır Sistem Avantaj Endeksi", "Prebuilt Value Advantage Index"),
                            currentValue = if (isTr) "%${s.advantagePercent.toInt()} Avantajlı" else "${s.advantagePercent.toInt()}% Advantage",
                            fraction = advantageFraction,
                            accentColor = if (s.isAdvantageous) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            subText = if (isTr) "Tek tek toplama (${sepPrice.toLong()} ₺) ile karşılaştırma" else "Compared to individual parts purchase (${sepPrice.toLong()} ₺)",
                        )
                    }
                }

                is LaptopSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Gaming Benchmark Skoru", "Gaming Benchmark Score"),
                        currentValue = "${s.gamingScore} / 100",
                        fraction = (s.gamingScore / 100f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = if (isTr) "1080p: ${s.fps1080p} FPS • 1440p: ${s.fps1440p} FPS referans oyun ortalaması" else "1080p: ${s.fps1080p} FPS • 1440p: ${s.fps1440p} FPS gaming average reference",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Grafik İşlemci Gücü (TGP)", "Mobile GPU TGP"),
                        currentValue = s.gpuWattTgp,
                        fraction = 0.85f,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "${s.gpuModel} tam güç watt kapasitesi", "${s.gpuModel} full wattage capability"),
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Batarya & Mobilite", "Battery & Mobility"),
                        currentValue = "${s.batteryWh} Wh • ${s.weightKg} kg",
                        fraction = (s.batteryWh / 100f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = tr(isTr, "Yüksek kapasiteli batarya hücresi", "High capacity battery cell"),
                    )
                }

                is MouseSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Raporlama Hızı (Polling Rate)", "Polling Rate"),
                        currentValue = "${s.pollingRateHz} Hz (${1000.0 / s.pollingRateHz} ms)",
                        fraction = (s.pollingRateHz / 8000f).coerceIn(0.12f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "8000 Hz e-spor tepe standardına göre", "Relative to 8000 Hz esports ceiling"),
                    )
                    val lightnessScore = ((120f - s.weightG) / 85f).coerceIn(0.1f, 1f)
                    MetricProgressBar(
                        title = tr(isTr, "Ultra Hafiflik Endeksi", "Ultralight Ergonomics Index"),
                        currentValue = "${s.weightG} gram",
                        fraction = lightnessScore,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "Düşük eylemsizlik ve hızlı flick kabiliyeti", "Low inertia and rapid flick agility"),
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Sensör Çözünürlüğü", "Sensor Resolution"),
                        currentValue = "${s.maxDpi} DPI (${s.sensor})",
                        fraction = (s.maxDpi / 44000f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = tr(isTr, "Optik izleme hassasiyeti", "Optical tracking precision"),
                    )
                }

                is KeyboardSpec -> {
                    val actuationEff = ((2.2f - s.minActuationMm.toFloat()) / 2.1f).coerceIn(0.15f, 1f)
                    MetricProgressBar(
                        title = tr(isTr, "Tetikleme Mesafesi & Hızı", "Actuation Travel & Speed"),
                        currentValue = "${s.minActuationMm} mm" + if (s.rapidTriggerSupported) " (Rapid Trigger)" else "",
                        fraction = actuationEff,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "0.1 mm Hall Effect manyetik referansına göre", "Relative to 0.1mm magnetic rapid trigger"),
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Raporlama Hızı (Polling Rate)", "Polling Rate"),
                        currentValue = "${s.pollingRateHz} Hz",
                        fraction = (s.pollingRateHz / 8000f).coerceIn(0.12f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = if (isTr) "Tuş basım iletim gecikmesi (${1000.0 / s.pollingRateHz} ms)" else "Keystroke transmission latency (${1000.0 / s.pollingRateHz} ms)",
                    )
                }

                is HeadsetSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Sürücü Çapı & Akustik Güç", "Driver Size & Acoustic Power"),
                        currentValue = "${s.driverSizeMm} mm (${s.driverType})",
                        fraction = (s.driverSizeMm / 53f).coerceIn(0.3f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "Sahne genişliği ve bas derinliği", "Soundstage width and bass depth"),
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Empedans & Sürülebilirlik", "Impedance & Driveability"),
                        currentValue = "${s.impedanceOhm} Ω",
                        fraction = (s.impedanceOhm / 250f).coerceIn(0.12f, 1f),
                        accentColor = if (s.dacAmpRequired) NeonAmber else MaterialTheme.colorScheme.secondary,
                        subText = if (s.dacAmpRequired) tr(isTr, "Harici DAC/Amfi önerilir (Stüdyo sınıfı)", "Dedicated DAC/Amp recommended (Studio Grade)") else tr(isTr, "Anakart / Dongle ile doğrudan tam güç", "Easily driven directly by motherboard / dongle"),
                    )
                }

                is MonitorSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Yenileme Hızı (Akıcılık)", "Refresh Rate (Smoothness)"),
                        currentValue = "${s.refreshRateHz} Hz",
                        fraction = (s.refreshRateHz / 540f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "540 Hz e-spor tepe referansına göre", "Relative to 540 Hz esports reference"),
                    )
                    val respScore = if (s.responseSpeedGtGMs <= 0.05) 1.0f else (1f - (s.responseSpeedGtGMs.toFloat() / 5f)).coerceIn(0.2f, 0.9f)
                    MetricProgressBar(
                        title = tr(isTr, "Griden Griye Tepki Süresi (GtG)", "Gray-to-Gray Response (GtG)"),
                        currentValue = "${s.responseSpeedGtGMs} ms (${s.panelType})",
                        fraction = respScore,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "Ghosting ve hareket bulanıklığı direnci", "Resistance to ghosting and motion blur"),
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Keskinlik & Piksel Yoğunluğu", "Pixel Density (PPI)"),
                        currentValue = "${s.ppi} PPI (${s.resolution})",
                        fraction = (s.ppi / 165f).coerceIn(0.3f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = tr(isTr, "${s.screenSizeInch}\" ekran boyutunda metin ve görüntü netliği", "Text and image clarity on ${s.screenSizeInch}\" display"),
                    )
                }

                is MicrophoneSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Maksimum Ses Basıncı Direnci (SPL)", "Max Sound Pressure Level (SPL)"),
                        currentValue = "${s.maxSplDb} dB SPL",
                        fraction = (s.maxSplDb / 150f).coerceIn(0.4f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = tr(isTr, "Yüksek sesli vokal ve yayınlarda seste bozulma (clipping) direnci", "Resistance to clipping during loud vocals and streaming"),
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Kapsül & Oda Gürültü İzolasyonu", "Capsule & Room Noise Isolation"),
                        currentValue = "${s.capsuleType} (${s.polarPattern})",
                        fraction = if (s.capsuleType.contains("Dinamik", ignoreCase = true)) 0.94f else 0.78f,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = s.connectivity,
                    )
                }

                is MousepadSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Durdurma Gücü (Stopping Power)", "Stopping Power & Flick Control"),
                        currentValue = "${s.stoppingPowerScore}/10",
                        fraction = (s.stoppingPowerScore / 10f).coerceIn(0.3f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.surfaceType} • ${s.speedGlideTier}",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Taban Stabilitesi & Kalınlık", "Base Stability & Thickness"),
                        currentValue = "${s.thicknessMm} mm (${s.baseMaterial})",
                        fraction = (s.thicknessMm.toFloat() / 6f).coerceIn(0.4f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "${s.dimensionsMm} • ${s.humidityResistance}",
                    )
                }

                is WebcamSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Kare Hızı & Akış Akıcılığı", "Frame Rate & Stream Smoothness"),
                        currentValue = "${s.maxResolution} @ ${s.maxFrameRateFps} FPS",
                        fraction = (s.maxFrameRateFps / 60f).coerceIn(0.4f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.sensorModel} (${s.sensorSizeInch})",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Görüş Açısı (FOV) & Diyafram", "Field of View (FOV) & Aperture"),
                        currentValue = "${s.fieldOfViewDeg}° • ${s.aperture}",
                        fraction = (s.fieldOfViewDeg / 100f).coerceIn(0.5f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = s.focusType,
                    )
                }

                is GamingChairSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "İskelet & Taşıma Kapasitesi", "Frame & Max Weight Capacity"),
                        currentValue = "${s.maxWeightCapacityKg} kg (${s.gasLiftClass})",
                        fraction = (s.maxWeightCapacityKg / 160f).coerceIn(0.5f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.recommendedHeightRange} • ${s.reclineAngleDeg}° Yatış",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Ortopedik Bel & Kolçak Ergonomisi", "Lumbar & Armrest Ergonomics"),
                        currentValue = s.lumbarSupportType,
                        fraction = 0.92f,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "${s.upholsteryMaterial} • ${s.armrestType}",
                    )
                }

                is PhoneSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "AnTuTu v10 Performansı", "AnTuTu v10 Performance"),
                        currentValue = "${s.antutuScore?.let { formatPoints(it) } ?: "N/A"} Puan",
                        fraction = ((s.antutuScore ?: 1500000) / 2500000f).coerceIn(0.1f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.chipset} (${s.cpuArch})",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Ekran & Akıcılık", "Display & Smoothness"),
                        currentValue = "${s.peakBrightnessNit} Nit • ${s.refreshRateHz} Hz",
                        fraction = (s.peakBrightnessNit / 3000f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "${s.displayTech} (${s.resolution})",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Batarya & Hızlı Şarj", "Battery & Charging"),
                        currentValue = "${s.batteryMah} mAh (${s.wiredChargingWatt}W)",
                        fraction = (s.batteryMah / 6000f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = s.chargingTimeToFullMin?.let { tr(isTr, "%100 dolum süresi ~$it dk", "Full charge ~$it min") } ?: "${s.wiredChargingWatt}W Hızlı Şarj",
                    )
                }

                is TabletSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Hesaplama & Çoklu Görev Gücü", "Computing & Multitasking Power"),
                        currentValue = "${s.chipset}",
                        fraction = ((s.antutuScore ?: 1800000) / 3000000f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "AnTuTu: ${s.antutuScore?.let { formatPoints(it) } ?: "N/A"} Puan",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Görsel Üretim & Panel", "Display & Creative Workspace"),
                        currentValue = "${s.displaySizeInch}\" ${s.displayTech} (${s.refreshRateHz} Hz)",
                        fraction = (s.displaySizeInch.toFloat() / 15f).coerceIn(0.3f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "${s.stylusSupport} Desteği",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Batarya Kapasitesi", "Battery Capacity"),
                        currentValue = "${s.batteryMah} mAh",
                        fraction = (s.batteryMah / 12000f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "${s.thicknessMm} mm Gövde • ${s.weightGrams}g",
                    )
                }

                is CaseFanSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Hava Akışı Performansı (Airflow)", "Airflow Performance"),
                        currentValue = "${s.maxAirflowCfm} CFM",
                        fraction = (s.maxAirflowCfm.toFloat() / 100f).coerceIn(0.15f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.fanSizeMm} mm Fan • Maks. ${s.maxRpm} RPM",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Statik Basınç (Radyatör Direnci)", "Static Pressure"),
                        currentValue = "${s.staticPressureMmH2o} mmH2O",
                        fraction = (s.staticPressureMmH2o.toFloat() / 4.5f).coerceIn(0.15f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = tr(isTr, "Radyatör ve filtre hava itiş kabiliyeti", "Push-through capability"),
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Akustik Seviye (Sessizlik)", "Acoustic Silence Rating"),
                        currentValue = "${s.noiseLevelDba} dBA",
                        fraction = ((40f - s.noiseLevelDba.toFloat()) / 25f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "${s.bearingType} • ${if (s.pwmPins == 4) "PWM 4-Pin" else "Sabit Voltaj"}",
                    )
                }

                is UpsSpec -> {
                    val isPureSine = s.sinewaveType.contains("Tam Sinüs") || s.sinewaveType.contains("Pure Sine")
                    MetricProgressBar(
                        title = tr(isTr, "Aktif Çıkış Gücü Kapasitesi", "Power Output Capacity"),
                        currentValue = "${s.wattCapacity}W (${s.vaRating} VA)",
                        fraction = (s.wattCapacity.toFloat() / 2000f).coerceIn(0.15f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.topology} • ${s.sinewaveType}",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Yedekleme Süresi (Yarı Yük)", "Backup Runtime (Half Load)"),
                        currentValue = "~${s.runtimeMinHalfLoad} Dakika",
                        fraction = (s.runtimeMinHalfLoad.toFloat() / 30f).coerceIn(0.15f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Tam Yükte: ~${s.runtimeMinFullLoad} dk (${s.batterySpecs})",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Dalga Formu & Koruma Teknolojisi", "Waveform & Protection"),
                        currentValue = s.sinewaveType,
                        fraction = if (isPureSine) 1.0f else 0.65f,
                        accentColor = if (isPureSine) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                        subText = if (isPureSine) "Tam Sinüs (Active PFC PSU ile %100 Uyumlu)" else "Simüle Sinüs (Standart Güç)",
                    )
                }

                is GamingDeskSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Çalışma Yüzeyi Boyutu", "Desk Surface Area"),
                        currentValue = "${s.deskWidthCm} x ${s.deskDepthCm} cm",
                        fraction = ((s.deskWidthCm.toFloat() * s.deskDepthCm.toFloat()) / 16000f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.surfaceFinish} Kaplama",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Taşıma Kapasitesi & Yük", "Load Capacity"),
                        currentValue = "${s.maxLoadKg} kg Taşıma",
                        fraction = (s.maxLoadKg.toFloat() / 160f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = if (s.hasCableManagement) "Entegre Kablo Yönetimi Dahil" else "Standart Ayaklar",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Yükseklik Ayar Aralığı", "Height Adjustability"),
                        currentValue = if (s.isMotorizedHeight) "${s.minHeightCm}-${s.maxHeightCm} cm (Motorlu)" else "Sabit ${s.minHeightCm} cm",
                        fraction = if (s.isMotorizedHeight) 0.95f else 0.4f,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = if (s.integratedMousepad) "Tam Boy Deskmat Dahil" else "Masa Yüzeyi",
                    )
                }

                is GamingLightingSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Işık Akısı & Parlaklık", "Luminous Flux (Brightness)"),
                        currentValue = "${s.maxLumens} Lümen",
                        fraction = (s.maxLumens.toFloat() / 3000f).coerceIn(0.15f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.formFactor} • ${s.colorTempKelvin}",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Renk Doğruluğu (CRI / Ra)", "Color Accuracy (CRI)"),
                        currentValue = "CRI ${s.criScore} Ra",
                        fraction = (s.criScore.toFloat() / 100f).coerceIn(0.5f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = s.powerSource,
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Ekran Senkronizasyonu & Ekosistem", "Sync & Ecosystem"),
                        currentValue = if (s.hasScreenMirroring) "Reaktif Ekran Senkronu Var" else "Standart RGB",
                        fraction = if (s.hasScreenMirroring) 1.0f else 0.5f,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = s.supportedEcosystems,
                    )
                }

                is SmartwatchSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Pil Ömrü & Dayanıklılık", "Battery Life"),
                        currentValue = "${s.batteryLifeDays} Gün Normal",
                        fraction = (s.batteryLifeDays.toFloat() / 21f).coerceIn(0.15f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Zirve Parlaklık: ${s.peakBrightnessNits} Nit",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Ekran & Gövde Tasarımı", "Display & Build"),
                        currentValue = "${s.screenSizeInch}\" ${s.screenPanel}",
                        fraction = (s.screenSizeInch.toFloat() / 2.0f).coerceIn(0.3f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "${s.caseMaterial} • ${s.waterResistanceAtm} ATM Su Geçirmezlik",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Sağlık & Sensör Dizilimi", "Health & Sensors"),
                        currentValue = s.osName,
                        fraction = 0.9f,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = s.sensorsList,
                    )
                }

                is RouterSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Wi-Fi Hız Sınıfı & Bantlar", "Aggregated Wi-Fi Speed"),
                        currentValue = "${s.speedClass} (${s.wifiStandard})",
                        fraction = 0.9f,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = s.bands,
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Kablolu Ağ Port Kapasitesi", "Wired LAN/WAN Bandwidth"),
                        currentValue = s.ethernetPorts,
                        fraction = if (s.ethernetPorts.contains("10GbE")) 1.0f else if (s.ethernetPorts.contains("2.5GbE")) 0.75f else 0.5f,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = s.processorAndRam,
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Oyun QoS & Bufferbloat Koruması", "Gaming QoS & SQM"),
                        currentValue = if (s.hasGamingQos && s.hasSqmBufferbloat) "Gaming QoS + SQM Aktif" else "Standart QoS",
                        fraction = if (s.hasGamingQos && s.hasSqmBufferbloat) 1.0f else 0.6f,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = s.usbSharingPorts,
                    )
                }

                is ModemSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "VDSL / Fiber Genişbant Standardı", "Broadband Profile"),
                        currentValue = s.vdslProfile,
                        fraction = 0.85f,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = if (s.fiberWanSupport) "Gigabit Fiber WAN Desteği Mevcut" else "Standart DSL",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Kablosuz Wi-Fi & Portlar", "Wi-Fi & LAN Ports"),
                        currentValue = "${s.wifiStandard} • ${s.lanPorts}",
                        fraction = 0.8f,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "${s.chipset} • ${s.voipPorts}",
                    )
                }

                is AccessPointSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Wi-Fi Standardı & Form Faktör", "Wi-Fi Standard & Form"),
                        currentValue = "${s.wifiStandard} (${s.formFactor})",
                        fraction = 0.9f,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "${s.maxClients}+ Eşzamanlı İstemci Kapasitesi",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "PoE Besleme & Uplink Hızı", "PoE Power & Uplink"),
                        currentValue = "${s.poeSupport} • ${s.uplinkPort}",
                        fraction = 0.85f,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = s.muMimoStreams,
                    )
                }

                is RangeExtenderSpec -> {
                    MetricProgressBar(
                        title = tr(isTr, "Menzil Genişletme & Wi-Fi", "Extender Wi-Fi"),
                        currentValue = "${s.extenderType} (${s.wifiStandard})",
                        fraction = 0.85f,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "~${s.coverageAreaM2} m² Kapsama Alanı",
                    )
                    MetricProgressBar(
                        title = tr(isTr, "Mesh Omurgası & Ethernet", "Mesh Backhaul & Ports"),
                        currentValue = s.meshProtocol,
                        fraction = 0.8f,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "${s.dedicatedBackhaul} • ${s.ethernetPorts}",
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricProgressBar(
    title: String,
    currentValue: String,
    fraction: Float,
    accentColor: Color,
    subText: String,
    modifier: Modifier = Modifier,
) {
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
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = currentValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                softWrap = false,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.extraSmall))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.extraSmall))

        Text(
            text = subText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class GameBenchmarkMode {
    SIMULATED,
    VERIFIED,
}

@Composable
private fun MultiGameFpsSimulationCard(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    val gameMatrix = remember(item.id) { BenchmarkEngine.resolveGameFpsForItem(item) } ?: return

    val labTests = remember(item.id) { VerifiedLabBenchmarks.getLabTestsForItem(item) }
    val hasLabTest = remember(labTests, item.id) { VerifiedLabBenchmarks.hasVerifiedLabTest(item) }

    var selectedMode by remember(item.id) { mutableStateOf(GameBenchmarkMode.SIMULATED) }
    var selectedResolution by remember { mutableStateOf(GamingResolution.P1080) }
    var selectedCategoryFilter by remember { mutableStateOf<GameCategory?>(null) }
    var selectedLabTestIndex by remember { mutableStateOf(0) }

    val allEntries = remember(gameMatrix) {
        gameMatrix.toSimulationEntries()
    }
    val visibleEntries = remember(allEntries, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) allEntries else allEntries.filter { it.category == selectedCategoryFilter }
    }

    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = NeonCyan,
        secondaryColor = NeonPurple,
        shape = RoundedCornerShape(Spacing.cardRadius),
        contentPadding = Spacing.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            // Header Row: Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎮", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = strings.detailMultiGameFps,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (selectedMode == GameBenchmarkMode.SIMULATED) {
                                tr(isTr, "Kalibre Edilmiş IPC & 3D V-Cache Algoritmik Modeli", "Calibrated IPC & 3D V-Cache Algorithmic Model")
                            } else {
                                tr(isTr, "Bağımsız Laboratuvar Ölçümleri • Gerçek Oyun & Güç Tüketimi", "Independent Lab Benchmarks • Real Gameplay & Power Draw")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Reference Test Rig Pill (Madde 2: Sabit Referans Tezgâhı)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NeonCyan.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (item.category == HardwareCategory.CPU) strings.gameBenchReferenceRigCpu else strings.gameBenchReferenceRigGpu,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontSize = 11.sp,
                    )
                }
            }

            // Segmented 2-Tab Mode Switcher (Madde 3: Simüle vs Gerçek Test)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Tab 1: Simüle Test
                val isSimSelected = selectedMode == GameBenchmarkMode.SIMULATED
                Surface(
                    onClick = { selectedMode = GameBenchmarkMode.SIMULATED },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSimSelected) NeonCyan.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSimSelected) NeonCyan else MaterialTheme.colorScheme.outline),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = strings.gameBenchTabSimulated,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSimSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSimSelected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            maxLines = 1,
                        )
                    }
                }

                // Tab 2: Doğrulanmış Gerçek Test
                val isRealSelected = selectedMode == GameBenchmarkMode.VERIFIED
                Surface(
                    onClick = { selectedMode = GameBenchmarkMode.VERIFIED },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isRealSelected) NeonRose.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isRealSelected) NeonRose else MaterialTheme.colorScheme.outline),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = strings.gameBenchTabVerified,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isRealSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isRealSelected) NeonRose else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            maxLines = 1,
                        )
                        if (hasLabTest) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), thickness = 1.dp)

            if (selectedMode == GameBenchmarkMode.SIMULATED) {
                // -------------------------------------------------------------
                // SEKME 1: SİMÜLE EDİLMİŞ 12-OYUN LİSTESİ
                // -------------------------------------------------------------

                // Resolution Switcher (1080p / 1440p / 4K)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GamingResolution.entries.forEach { res ->
                        val isSelected = selectedResolution == res
                        Surface(
                            onClick = { selectedResolution = res },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline),
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = res.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
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

                // Category Filter Pills (Tümü / E-Spor / AAA)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val filterOptions: List<Pair<GameCategory?, String>> = listOf(
                        null to tr(isTr, "Tümü (12)", "All (12)"),
                        GameCategory.ESPORTS to "🎯 E-Spor (5)",
                        GameCategory.AAA to "🔥 AAA Grafik (7)",
                    )
                    filterOptions.forEach { (cat, label) ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            onClick = { selectedCategoryFilter = cat },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) NeonEmerald.copy(alpha = 0.16f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) NeonEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 5.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                visibleEntries.forEach { game ->
                    val avgFpsVal = game.fps.avgFps(selectedResolution)
                    val maxFpsVal = game.fps.maxFps(selectedResolution)
                    val low1FpsVal = game.fps.low1PctFps(selectedResolution)
                    val minFpsVal = game.fps.minFps(selectedResolution)
                    val fpsColor = when {
                        avgFpsVal >= 120 -> NeonEmerald
                        avgFpsVal >= 60 -> NeonCyan
                        else -> NeonAmber
                    }
                    val barCeiling = if (game.category == GameCategory.ESPORTS) 1100f else 300f
                    val progress = (avgFpsVal / barCeiling).coerceIn(0.06f, 1f)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            // Top Row: Game Title & Category Badge + Resolution Preset
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = game.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = (if (game.category == GameCategory.ESPORTS) NeonCyan else NeonAmber).copy(alpha = 0.15f),
                                    ) {
                                        Text(
                                            text = if (game.category == GameCategory.ESPORTS) "E-SPOR" else "AAA",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (game.category == GameCategory.ESPORTS) NeonCyan else NeonAmber,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        )
                                    }
                                }
                                Text(
                                    text = "${selectedResolution.label} • ${game.presetSubtitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // 4-Metric Data Strip: En Yüksek | Ortalama | %1 Low | En Düşük
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                MetricStatBox(
                                    modifier = Modifier.weight(1f),
                                    label = tr(isTr, "En Yüksek", "Max FPS"),
                                    value = "$maxFpsVal",
                                    color = NeonPurple,
                                )
                                MetricStatBox(
                                    modifier = Modifier.weight(1.05f),
                                    label = tr(isTr, "Ortalama", "Average"),
                                    value = "$avgFpsVal",
                                    color = fpsColor,
                                    isHighlighted = true,
                                )
                                MetricStatBox(
                                    modifier = Modifier.weight(1f),
                                    label = tr(isTr, "%1 Low", "1% Low"),
                                    value = "$low1FpsVal",
                                    color = NeonAmber,
                                )
                                MetricStatBox(
                                    modifier = Modifier.weight(1f),
                                    label = tr(isTr, "En Düşük", "Min FPS"),
                                    value = "$minFpsVal",
                                    color = NeonRose,
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = fpsColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                }
            } else {
                // -------------------------------------------------------------
                // SEKME 2: DOĞRULANMIŞ GERÇEK LABORATUVAR TESTLERİ (Nebi Akgün Tezgâhı)
                // -------------------------------------------------------------
                if (labTests.isNotEmpty()) {
                    // Game Selector Pills (Horizontal scrollable pills for all games)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        labTests.forEachIndexed { idx, test ->
                            val isSelected = selectedLabTestIndex == idx
                            Surface(
                                onClick = { selectedLabTestIndex = idx },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeonRose.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) NeonRose else MaterialTheme.colorScheme.outline),
                            ) {
                                Text(
                                    text = test.gameTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    val activeLabTest = labTests.getOrNull(selectedLabTestIndex) ?: labTests.first()

                    // Rig & Game Title Info Header
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${activeLabTest.gameTitle} • ${activeLabTest.cleanSettings}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                ) {
                                    val hasMax = activeLabTest.entries.any { it.maxFps != null && it.maxFps > it.avgFps }
                                    Text(
                                        text = if (hasMax) tr(isTr, "Max, Ortalama & Min FPS", "Max, Avg & Min FPS") else strings.gameBenchAvgMinLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tezgâh: ${activeLabTest.rigGpuOrCpu} • ${activeLabTest.rigRam}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.5.sp,
                                color = NeonCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Benchmark Bars: AMD (Red) vs Intel (Blue) (Videodaki yatay çift bar grafiği)
                    val maxAvgFps = activeLabTest.entries.maxOfOrNull { it.avgFps } ?: 1000.0

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        activeLabTest.entries.forEach { entry ->
                            val isCurrent = entry.matchesItem(item)
                            val isNvidia = entry.fullModelName.contains("NVIDIA", ignoreCase = true) || entry.modelName.startsWith("RTX", ignoreCase = true) || entry.modelName.startsWith("GTX", ignoreCase = true)
                            val isIntel = entry.fullModelName.contains("Intel", ignoreCase = true) || entry.modelName.startsWith("Arc", ignoreCase = true) || entry.fullModelName.contains("Core", ignoreCase = true)
                            val mainBarColor = when {
                                entry.isAmd -> Color(0xFFEF4444)
                                isIntel -> Color(0xFF2563EB)
                                isNvidia -> Color(0xFF10B981)
                                else -> Color(0xFF2563EB)
                            }
                            val minBarColor = when {
                                entry.isAmd -> Color(0xFFB91C1C)
                                isIntel -> Color(0xFF1D4ED8)
                                isNvidia -> Color(0xFF047857)
                                else -> Color(0xFF1D4ED8)
                            }

                            val avgRatio = (entry.avgFps / maxAvgFps).toFloat().coerceIn(0.12f, 1f)
                            val minRatio = (entry.minFps / maxAvgFps).toFloat().coerceIn(0.08f, 1f)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) GoldAccent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                                border = BorderStroke(
                                    width = if (isCurrent) 1.5.dp else 0.5.dp,
                                    color = if (isCurrent) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Model Name & Watt
                                    Row(
                                        modifier = Modifier.width(115.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "${entry.modelName} (${entry.wattLabel})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                            color = if (isCurrent) GoldAccent else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 10.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "⭐", fontSize = 9.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Triple or Double Horizontal Bar: Optional Top=Max, Mid=Avg, Bottom=Min
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        // Optional Max Bar if present in lab benchmark
                                        if (entry.maxFps != null && entry.maxFps > entry.avgFps) {
                                            val maxRatio = (entry.maxFps / maxAvgFps).toFloat().coerceIn(0.12f, 1f)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(maxRatio)
                                                    .height(10.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(NeonPurple.copy(alpha = 0.85f)),
                                                contentAlignment = Alignment.CenterEnd,
                                            ) {
                                                Text(
                                                    text = "Max: ${entry.maxFps.toInt()}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    modifier = Modifier.padding(end = 3.dp),
                                                )
                                            }
                                        }

                                        // Average Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(avgRatio)
                                                .height(13.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(mainBarColor),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Text(
                                                text = "${entry.avgFps.toInt()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(end = 4.dp),
                                            )
                                        }

                                        // Min / 1% Low Bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(minRatio)
                                                .height(11.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(minBarColor),
                                            contentAlignment = Alignment.CenterEnd,
                                        ) {
                                            Text(
                                                text = "${entry.minFps.toInt()}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.95f),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 8.sp,
                                                modifier = Modifier.padding(end = 3.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Model için henüz gerçek test bulunmuyorsa
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "🧪 " + strings.gameBenchNoVerifiedYet,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Button(
                                onClick = { selectedMode = GameBenchmarkMode.SIMULATED },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f), contentColor = NeonCyan),
                                border = BorderStroke(1.dp, NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text = strings.gameBenchSwitchToSim, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricStatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    isHighlighted: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = if (isHighlighted) 0.16f else 0.08f),
        border = BorderStroke(
            width = if (isHighlighted) 1.dp else 0.5.dp,
            color = color.copy(alpha = if (isHighlighted) 0.6f else 0.25f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.5.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = color,
                maxLines = 1,
                softWrap = false,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = if (isHighlighted) 12.sp else 11.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    maxLines = 1,
                    softWrap = false,
                )
                Text(
                    text = " FPS",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color.copy(alpha = 0.8f),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun PolymorphicTechnicalSpecsSection(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    val rows = buildPolymorphicSpecRows(item, isTr)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = Spacing.small),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = LocalAppStrings.current.detailTechSpecs,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                DetailBadge(
                    text = item.category.localized(LocalAppStrings.current),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent)
                        .padding(horizontal = Spacing.small, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (value == "Veri Bulunamadı" || value == "Belirtilmedi" || value == "Unspecified" || value == "Not Found") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.3f),
                    )
                }
            }
        }
    }
}

private fun getUnspecifiedText(isTr: Boolean): String = if (isTr) "Belirtilmedi" else "Unspecified"

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText

private fun buildPolymorphicSpecRows(item: HardwareItem, isTr: Boolean = true): List<Pair<String, String>> {
    val rows = mutableListOf<Pair<String, String>>()
    rows.add(tr(isTr, "Marka", "Brand") to item.brand)
    rows.add(tr(isTr, "Model Kodu", "Model SKU") to item.model)

    when (val s = item.spec) {
        is CpuSpec -> {
            rows.add(tr(isTr, "Soket", "Socket") to s.socket.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Çekirdek & İzlek Dizilimi", "Core & Thread Topology") to (s.coreTopology ?: "${s.coreCount} Çekirdek / ${s.threadCount} İzlek"))
            val boostText = s.boostClock?.let { "${it.mhz / 1000f} GHz" } ?: getUnspecifiedText(isTr)
            rows.add(tr(isTr, "Temel / Boost Frekans", "Base / Boost Clocks") to "${s.baseClock.mhz / 1000f} GHz / $boostText")
            val cacheText = listOfNotNull(s.l2CacheMb?.let { "${it} MB L2" }, s.l3CacheMb?.let { "${it} MB L3" }).joinToString(" • ")
            rows.add(tr(isTr, "L2 ve L3 Önbellek", "L2 & L3 Cache") to cacheText.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "TDP ve Güç Limitleri", "TDP & Power Limits") to (s.powerLimits ?: "${s.tdp.watts} W TDP"))
            rows.add(tr(isTr, "Mimari & Üretim Nodu", "Architecture & Node") to listOfNotNull(s.architecture, s.processNode).joinToString(" • ").ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Bellek Kontrolcü Desteği", "Memory Controller Support") to (s.maxMemorySpeed ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "PCIe Sürümü & Hat Sayısı", "PCIe Version & Lanes") to (s.pcieVersion ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Dahili Grafik (iGPU)", "Integrated GPU (iGPU)") to (s.igpuModel ?: if (s.hasIntegratedGraphics) "Mevcut (Dahili Grafik)" else "Yok (Harici GPU Gerekli)"))
            rows.add(tr(isTr, "Çarpan Kilidi (Overclock)", "Multiplier (Overclock)") to if (s.unlockedMultiplier == true) "Açık (Unlocked)" else if (s.unlockedMultiplier == false) "Kapalı (Locked)" else getUnspecifiedText(isTr))
        }

        is GpuSpec -> {
            rows.add(tr(isTr, "Grafik İşlemci & Mimari", "GPU & Architecture") to listOfNotNull(s.chipset, s.architecture).joinToString(" • ").ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Üretim Teknolojisi & Transistör", "Process Node") to (s.processNode ?: getUnspecifiedText(isTr)))
            val coresText = listOfNotNull(s.cudaCores?.let { if (isTr) "$it Çekirdek" else "$it Cores" }, s.rtCores?.let { "$it RT" }, s.tensorAiTops).joinToString(" • ")
            rows.add(tr(isTr, "Gölgelendirici / RT / Tensor", "Shaders / RT / Tensor") to coresText.ifBlank { getUnspecifiedText(isTr) })
            val vramSpeed = s.memorySpeedGbps?.let { " • $it Gbps" } ?: ""
            rows.add(tr(isTr, "VRAM Kapasitesi & Tipi", "VRAM Capacity & Type") to "${s.vram.megabytes / 1024} GB ${s.memoryType}$vramSpeed")
            val busBw = listOfNotNull(s.busWidthBits?.let { "$it-Bit" }, s.bandwidthGbs?.let { "$it GB/s" }).joinToString(" • ")
            rows.add(tr(isTr, "Bellek Veri Yolu & Bant Genişliği", "Memory Bus & Bandwidth") to busBw.ifBlank { getUnspecifiedText(isTr) })
            val boostText = s.boostClock?.let { "${it.mhz} MHz" } ?: getUnspecifiedText(isTr)
            rows.add(tr(isTr, "Çekirdek Frekansı (Temel/Boost)", "Core Clocks (Base / Boost)") to "${s.coreClock.mhz} MHz / $boostText")
            rows.add(tr(isTr, "TGP (TDP) / Önerilen PSU", "TGP (TDP) / Recommended PSU") to "${s.tdp.watts} W / ${s.recommendedPsu.watts} W PSU")
            rows.add(tr(isTr, "Güç Girişleri", "Power Connectors") to (s.powerConnectors ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Kart Boyutları & Slot Kalınlığı", "Dimensions & Slot Thickness") to listOfNotNull(s.lengthMm?.let { "$it mm" }, s.slotWidth).joinToString(" • ").ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Görüntü Çıkışları", "Display Outputs") to (s.displayOutputs ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Parça Numarası (Part Number)", "Part Number (SKU)") to (s.partNumber ?: item.model))
        }

        is MotherboardSpec -> {
            rows.add(tr(isTr, "Soket Tipi & Yonga Seti", "Socket Type & Chipset") to "${s.socket} • ${s.chipset}")
            rows.add(tr(isTr, "VRM Güç Faz Dizilimi", "VRM Power Phases") to (s.vrmPhases ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "PCB Katmanı & Bakır Standardı", "PCB Layers & Copper Standard") to (s.pcbLayers ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Form Faktörü", "Form Factor") to s.formFactor.displayName)
            val maxMemLabel = s.maxMemoryGb?.let { ", Maks. $it GB" } ?: ""
            rows.add(tr(isTr, "Bellek Desteği & Hız Aşırtma", "Memory Support & OC") to (s.memorySpeedOc ?: "${s.memoryGeneration.displayName} (${s.ramSlots} Slot$maxMemLabel)"))
            rows.add(tr(isTr, "M.2 NVMe Yuva Detayı", "M.2 NVMe Slot Layout") to (s.m2SlotsDetail ?: (s.m2Slots?.let { "${it}x M.2 NVMe" } ?: getUnspecifiedText(isTr))))
            rows.add(tr(isTr, "PCIe x16 Gen Standardı", "PCIe x16 Generation") to s.pcieGeneration.displayName)
            val netFallback = when (s.hasWifi) {
                true -> "Wi-Fi + Ethernet"
                false -> "Ethernet (Wi-Fi Yok)"
                null -> getUnspecifiedText(isTr)
            }
            rows.add(tr(isTr, "Ağ & Kablosuz Bağlantı", "Networking & Wireless") to (s.network ?: netFallback))
            rows.add(tr(isTr, "Ses Yonga Kodeği", "Audio Codec") to (s.audioCodec ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Arka Panel I/O Çıkışları", "Rear Panel I/O") to (s.interfacePorts ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "SATA Port Sayısı", "SATA Ports") to (s.sataPorts?.let { "${it}x SATA 3.0" } ?: getUnspecifiedText(isTr)))
        }

        is RamSpec -> {
            val totalGb = s.capacity.megabytes / 1024
            val perModGb = totalGb / s.moduleCount.coerceAtLeast(1)
            rows.add(tr(isTr, "Bellek Standardı & Kapasite", "Memory Standard & Capacity") to "${totalGb} GB (${s.moduleCount}x${perModGb} GB Kit) ${s.generation.displayName}")
            rows.add(tr(isTr, "IC Bellek Yongası (Die Tipi)", "IC Memory Die Type") to (s.dieType ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Frekans & CL Zamanlamaları", "Frequency & Timings") to "${s.speed.mhz} MHz • ${s.timings ?: "CL${s.casLatency}"}")
            val latencyNs = s.firstWordLatencyNs ?: ((((2000.0 * s.casLatency) / s.speed.mhz.coerceAtLeast(1)) * 10.0).toInt() / 10.0)
            rows.add(tr(isTr, "İlk Tepki Süresi (True Latency)", "First Word Latency") to "$latencyNs ns")
            rows.add(tr(isTr, "Çalışma Voltajı", "Operating Voltage") to (s.voltage?.let { "$it V" } ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Hız Aşırtma Profili", "Overclock Profile") to (s.profile ?: getUnspecifiedText(isTr)))
            val coolerRgb = listOfNotNull(
                s.heatSpreader?.let { if (it) tr(isTr, "Alüminyum Soğutucu Blok", "Aluminum Heat Spreader") else tr(isTr, "Soğutucusuz", "No Heat Spreader") },
                s.rgb?.let { if (it) tr(isTr, "RGB Aydınlatma", "RGB Lighting") else tr(isTr, "RGB Yok", "No RGB") },
            ).joinToString(" • ")
            rows.add(tr(isTr, "Soğutucu Blok / RGB", "Heat Spreader / RGB") to coolerRgb.ifBlank { getUnspecifiedText(isTr) })
        }

        is StorageSpec -> {
            val totalGb = s.capacity.megabytes / 1024
            rows.add(tr(isTr, "Depolama Kapasitesi", "Storage Capacity") to (if (totalGb >= 1024) "${totalGb / 1024} TB (${totalGb} GB)" else "${totalGb} GB"))
            rows.add(tr(isTr, "Kontrolcü Modeli", "Controller Model") to (s.controller ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "DRAM Önbellek Yapısı", "DRAM Cache Structure") to (s.dramCache ?: (s.cacheMb?.let { "$it MB DRAM Cache" } ?: getUnspecifiedText(isTr))))
            rows.add(tr(isTr, "NAND Flaş Standardı", "NAND Flash Standard") to (s.nandType ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Sıralı Okuma / Yazma Hızı", "Sequential Read / Write") to if (isTr) "${s.sequentialReadMbps} MB/sn Okuma • ${s.sequentialWriteMbps} MB/sn Yazma" else "${s.sequentialReadMbps} MB/s Read • ${s.sequentialWriteMbps} MB/s Write")
            rows.add(tr(isTr, "Rastgele 4K IOPS", "Random 4K IOPS") to (s.randomIops ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Arayüz & Protokol", "Interface & Protocol") to (s.pcieGeneration?.let { "PCIe ${it.displayName} x4 NVMe" } ?: s.storageInterface.displayName))
            rows.add(tr(isTr, "Form Faktörü", "Form Factor") to s.formFactor.displayName)
            rows.add(tr(isTr, "Dayanıklılık Ömrü (TBW)", "Endurance Rating (TBW)") to (s.enduranceTbw?.let { "$it TBW" } ?: getUnspecifiedText(isTr)))
        }

        is PsuSpec -> {
            rows.add(tr(isTr, "Çıkış Gücü Kapasitesi", "Output Power Capacity") to "${s.wattage.watts} Watt")
            rows.add(tr(isTr, "Cultists PSU Tier List", "Cultists PSU Tier Rating") to (s.tierRating ?: getUnspecifiedText(isTr)))
            val effText = listOfNotNull(s.efficiency.displayName, s.cybeneticsCert).joinToString(" • ")
            rows.add(tr(isTr, "Cybenetics & 80 PLUS Sertifikası", "Cybenetics & 80 PLUS Cert") to effText)
            rows.add(tr(isTr, "Cybenetics Lambda Gürültü Derecesi", "Cybenetics Lambda Noise Rating") to (s.cybeneticsLambda ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "ATX Standardı & Geçici Yük", "ATX Standard & Excursions") to (s.atxStandard ?: if (s.supportsPcie5Power) "ATX 3.0 / PCIe 5.0" else getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "12V-2x6 / 12VHPWR (600W) Çıkış", "12V-2x6 / 12VHPWR (600W) Output") to (s.pcie12Vhpwr ?: if (s.supportsPcie5Power) "Mevcut (12VHPWR / 12V-2x6)" else "Yok"))
            rows.add(tr(isTr, "İç Topoloji & Kapasitör Standardı", "Internal Topology & Capacitors") to (s.topology ?: s.capacitors ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Güvenlik & Koruma Devreleri", "Protections & Safety Circuits") to (s.protections ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Modülerlik Yapısı", "Modularity") to (s.modularity ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Soğutma Fanı & Boyut", "Cooling Fan & Dimensions") to (s.fanAndSize ?: getUnspecifiedText(isTr)))
            val cables = listOfNotNull(s.epsConnectors, s.pcieConnectors, s.sataConnectors).joinToString(" • ")
            rows.add(tr(isTr, "Kablo Çıkışları (EPS / PCIe / SATA)", "Cables (EPS / PCIe / SATA)") to cables.ifBlank { getUnspecifiedText(isTr) })
        }

        is CoolerSpec -> {
            rows.add(tr(isTr, "Soğutma Tipi", "Cooler Type") to s.type.displayName)
            rows.add(tr(isTr, "Maksimum Soğutma Gücü (TDP)", "Max Heat Dissipation (TDP)") to "${s.maxCoolingTdp.watts} W")
            val radOrHeight = s.radiatorDimensions ?: if (s.type == CoolerType.LIQUID) {
                s.radiatorSizeMm?.let { if (isTr) "${it} mm Radyatör" else "${it}mm Radiator" } ?: getUnspecifiedText(isTr)
            } else {
                s.heightMm?.let { if (isTr) "${it} mm Kule Tipi" else "${it}mm Tower" } ?: getUnspecifiedText(isTr)
            }
            rows.add(tr(isTr, "Radyatör / Kule Boyutları", "Radiator / Tower Dimensions") to radOrHeight)
            rows.add(tr(isTr, "Fan Dönüş Hızı (RPM)", "Fan Speed (RPM)") to (s.fanRpm ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Hava Akışı & Statik Basınç", "Airflow & Static Pressure") to (s.fanAirflow ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Ses Seviyesi (Gürültü)", "Noise Level (dBA)") to (s.noiseDba?.let { "$it dBA" } ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Desteklenen Soketler", "Socket Compatibility") to (if (s.supportedSockets.isNotEmpty()) s.supportedSockets.joinToString(", ") else getUnspecifiedText(isTr)))
        }

        is CaseSpec -> {
            rows.add(tr(isTr, "Kasa Tipi / Form Faktörü", "Chassis Type / Form Factor") to (s.type ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Maksimum GPU Uzunluğu", "Max GPU Clearance") to (s.maxGpuLengthMm?.let { "$it mm" } ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Maksimum CPU Soğutucu Yüksekliği", "Max CPU Cooler Height") to (s.maxCpuCoolerHeightMm?.let { "$it mm" } ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Radyatör Desteği (Üst/Ön/Yan)", "Radiator Support (Top/Front/Side)") to (s.radiatorSupport ?: (s.maxRadiatorSizeMm?.let { "${it} mm" } ?: getUnspecifiedText(isTr))))
            rows.add(tr(isTr, "Dahili Fan Dizilimi", "Included Fans") to (s.includedFans ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Ön Panel Bağlantıları (Tip-C)", "Front Panel Ports (Type-C)") to (s.frontPanelUsb ?: getUnspecifiedText(isTr)))
            val sideDrive = listOfNotNull(s.sidePanel, s.driveBays).joinToString(" • ")
            rows.add(tr(isTr, "Yan Panel & Disk Yuvaları", "Side Panel & Drive Bays") to sideDrive.ifBlank { getUnspecifiedText(isTr) })
            val mbSupport = if (s.supportedMotherboardFormFactors.isNotEmpty()) {
                s.supportedMotherboardFormFactors.joinToString(", ") { it.displayName }
            } else {
                getUnspecifiedText(isTr)
            }
            rows.add(tr(isTr, "Desteklenen Anakart Tipleri", "Motherboard Support") to mbSupport)
        }

        is PrebuiltSpec -> {
            rows.add(tr(isTr, "Satıcı Mağaza", "Retail Store") to s.storeName.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "İşlemci (CPU)", "Processor (CPU)") to s.cpuModel.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Ekran Kartı (GPU)", "Graphics Card (GPU)") to s.gpuModel.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Anakart Modeli", "Motherboard Model") to s.motherboardModel.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Sistem Belleği (RAM)", "System Memory (RAM)") to s.ramSpec.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Depolama Birimi (SSD)", "Storage Drive (SSD)") to s.storageSpec.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Parça Parça Toplama Fiyatı", "Individual Parts Total") to (s.estimatedSeparatePriceTry?.let { if (it > 0) "${it.formatPrice()} ₺" else null } ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "Hazır Sistem Kâr/Avantaj Tutarı", "Prebuilt Savings/Advantage") to (s.priceAdvantageTry?.let { adv ->
                val sign = if (adv > 0) "+" else ""
                "$sign${adv.formatPrice()} ₺ (%${s.advantagePercent.toInt()})"
            } ?: getUnspecifiedText(isTr)))
            rows.add(tr(isTr, "1080p / 1440p / 4K Referans FPS", "1080p / 1440p / 4K Reference FPS") to "${s.fps1080p} FPS / ${s.fps1440p} FPS / ${s.fps4k} FPS")
            rows.add(tr(isTr, "Gaming Performans Skoru", "Gaming Performance Score") to "${s.gamingScore} / 100")
        }

        is LaptopSpec -> {
            rows.add(tr(isTr, "Cihaz Markası", "Device Brand") to s.brand.ifBlank { item.brand })
            rows.add(tr(isTr, "Grafik Kartı & Gerçek TGP Gücü", "Graphics & Real TGP Power") to "${s.gpuModel} (${s.gpuWattTgp.ifBlank { getUnspecifiedText(isTr) }})")
            rows.add(tr(isTr, "Grafik Anahtarı (MUX Switch)", "MUX Switch / Optimus") to s.muxSwitch.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "İşlemci (CPU)", "Processor (CPU)") to s.cpuModel.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Ekran & Panel Standardı", "Display & Panel Standard") to s.displaySpec.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Bellek Konfigürasyonu (RAM)", "Memory Configuration (RAM)") to s.ramSpec.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Yüksek Hızlı Depolama (SSD)", "High-Speed Storage (SSD)") to s.storageSpec.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Batarya Kapasitesi", "Battery Capacity") to if (s.batteryWh > 0) "${s.batteryWh} Wh" else getUnspecifiedText(isTr))
            rows.add(tr(isTr, "Gövde Ağırlığı", "Chassis Weight") to if (s.weightKg > 0.0) "${s.weightKg} kg" else getUnspecifiedText(isTr))
            rows.add(tr(isTr, "Termal Soğutma Mimarisi", "Thermal Architecture") to s.coolingTech.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "1080p / 1440p Referans FPS", "1080p / 1440p Reference FPS") to "${s.fps1080p} FPS / ${s.fps1440p} FPS")
            rows.add(tr(isTr, "Gaming Skoru", "Gaming Score") to "${s.gamingScore} / 100")
        }

        is MouseSpec -> {
            rows.add(tr(isTr, "Optik Sensör Kodu", "Optical Sensor Model") to s.sensor.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Maksimum Çözünürlük (DPI)", "Max Resolution (DPI)") to "${s.maxDpi} DPI")
            rows.add(tr(isTr, "Raporlama Hızı (Polling Rate)", "Polling Rate") to "${s.pollingRateHz} Hz (${1000.0 / s.pollingRateHz.coerceAtLeast(1)} ms)")
            rows.add(tr(isTr, "Anahtar (Switch) Tipi", "Switch Type") to s.switchType.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Gövde Ağırlığı", "Chassis Weight") to "${s.weightG} gram")
            rows.add(tr(isTr, "MCU / Kablosuz Denetleyici", "MCU / Wireless Controller") to s.mcu.ifBlank { "Nordic 52840" })
            rows.add(tr(isTr, "Tıklama Gecikmesi (Click Latency)", "Click Latency") to "${s.clickLatencyMs} ms")
            rows.add(tr(isTr, "Ergonomi / Kasa Formu", "Ergonomics / Body Form") to s.shapeType.ifBlank { tr(isTr, "Simetrik", "Symmetrical") })
            rows.add(tr(isTr, "Bağlantı Teknolojisi", "Connectivity") to s.connectivity.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Batarya Kullanım Süresi", "Battery Life") to (s.batteryHours?.let { if (isTr) "$it Saat" else "$it Hours" } ?: tr(isTr, "Kablolu (Sınırsız)", "Wired (Unlimited)")))
        }

        is KeyboardSpec -> {
            rows.add(tr(isTr, "Anahtar (Switch) Mimarisi", "Switch Architecture") to "${s.switchType}${if (s.isHallEffect) " (Manyetik Hall Effect)" else ""}")
            rows.add(tr(isTr, "Rapid Trigger Desteği", "Rapid Trigger Support") to if (s.rapidTriggerSupported) tr(isTr, "Var (Dinamik Hızlı Tetikleme)", "Yes (Dynamic Rapid Trigger)") else tr(isTr, "Standart Sabit Sıfırlama", "Standard Fixed Reset"))
            rows.add(tr(isTr, "Tetikleme Ayar Aralığı", "Actuation Range") to "${s.minActuationMm} mm - ${s.maxActuationMm} mm")
            rows.add(tr(isTr, "Raporlama Hızı (Polling Rate)", "Polling Rate") to "${s.pollingRateHz} Hz")
            rows.add(tr(isTr, "Tuş Dizilimi (Form Faktörü)", "Layout (Form Factor)") to s.layout.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Hot-Swap (Lehimsiz Değişim)", "Hot-Swap Support") to if (s.hotSwap) tr(isTr, "Destekleniyor", "Supported") else tr(isTr, "Sabit", "Soldered"))
            rows.add(tr(isTr, "Kasa & Plaka Materyali", "Case & Plate Material") to s.caseMaterial.ifBlank { tr(isTr, "Alüminyum CNC / ABS", "Aluminum CNC / ABS") })
            rows.add(tr(isTr, "Bağlantı Tipi", "Connection Type") to s.connectivity.ifBlank { getUnspecifiedText(isTr) })
        }

        is HeadsetSpec -> {
            rows.add(tr(isTr, "Sürücü (Driver) Mimarisi", "Driver Architecture") to s.driverType.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Sürücü Çapı", "Driver Diameter") to "${s.driverSizeMm} mm")
            rows.add(tr(isTr, "Empedans (Direnç)", "Impedance") to "${s.impedanceOhm} Ω (Ohm)")
            rows.add(tr(isTr, "Frekans Tepki Aralığı", "Frequency Response") to s.frequencyResponse.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Harici DAC / Amfi İhtiyacı", "External DAC / Amp Requirement") to if (s.dacAmpRequired) "Önerilir (Yüksek Empedans)" else "Gerekmez (Doğrudan Besleme)")
            rows.add(tr(isTr, "Mikrofon Tipi", "Microphone Type") to s.micType.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Ağırlık", "Weight") to "${s.weightG} gram")
            rows.add(tr(isTr, "Bağlantı Seçenekleri", "Connection Options") to s.connectivity.ifBlank { "2.4GHz / Bluetooth / 3.5mm" })
            rows.add(tr(isTr, "Akustik İzolasyon", "Acoustic Isolation") to s.soundIsolation.ifBlank { tr(isTr, "Pasif İzolasyon", "Passive Isolation") })
        }

        is MonitorSpec -> {
            rows.add(tr(isTr, "Panel Teknolojisi", "Panel Technology") to s.panelType.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Gerçek Panel Bit Derinliği", "Panel Bit Depth") to s.bitDepth.ifBlank { "8-bit + FRC" })
            rows.add(tr(isTr, "Ekran Boyutu", "Screen Size") to if (isTr) "${s.screenSizeInch} inç" else "${s.screenSizeInch} inches")
            rows.add(tr(isTr, "Çözünürlük", "Resolution") to s.resolution.ifBlank { getUnspecifiedText(isTr) })
            rows.add(tr(isTr, "Yenileme Hızı (Refresh Rate)", "Refresh Rate") to "${s.refreshRateHz} Hz")
            rows.add(tr(isTr, "Tepki Süresi", "Response Time") to "${s.responseSpeedGtGMs} ms (GtG) • ${s.responseMprtMs ?: 0.5} ms (MPRT)")
            rows.add(tr(isTr, "Parlaklık Düzeyi (SDR / HDR)", "Brightness (SDR / HDR)") to "${s.brightnessSdrNit} Nit / ${s.brightnessHdrNit} Nit")
            rows.add(tr(isTr, "Renk Gamutu Kapsamı", "Color Gamut Coverage") to s.colorGamut.ifBlank { "%100 sRGB" })
            rows.add(tr(isTr, "Değişken Tazeleme Hızı (VRR)", "Variable Refresh Rate (VRR)") to s.vrrSupport.ifBlank { "G-Sync Compatible / FreeSync Premium" })
            rows.add(tr(isTr, "Piksel Yoğunluğu (PPI)", "Pixel Density (PPI)") to "${s.ppi} PPI")
            rows.add(tr(isTr, "HDR Sertifikası", "HDR Certification") to s.hdrCertification.ifBlank { getUnspecifiedText(isTr) })
        }

        is MicrophoneSpec -> {
            rows.add(tr(isTr, "Kapsül Mimarisi", "Capsule Type") to s.capsuleType)
            rows.add(tr(isTr, "Kutup Deseni (Polar Pattern)", "Polar Pattern") to s.polarPattern)
            rows.add(tr(isTr, "Örnekleme & Bit Derinliği", "Sample Rate & Bit Depth") to s.sampleRateAndBit)
            rows.add(tr(isTr, "Frekans Tepki Aralığı", "Frequency Response") to s.frequencyResponse)
            rows.add(tr(isTr, "Maksimum Ses Basıncı (Max SPL)", "Max SPL") to "${s.maxSplDb} dB SPL")
            rows.add(tr(isTr, "Bağlantı & Arayüz", "Connectivity") to s.connectivity)
            rows.add(tr(isTr, "Dahili DSP / Ses İşleme", "Built-in DSP") to if (s.hasBuiltInDsp) tr(isTr, "Var (Donanımsal EQ/Limiter)", "Yes (Hardware EQ/Limiter)") else tr(isTr, "Yok (Saf Sinyal)", "No (Pure Signal)"))
            rows.add(tr(isTr, "+48V Phantom Power İhtiyacı", "Requires +48V Phantom Power") to if (s.requiresPhantomPower) tr(isTr, "Gerekli (+48V)", "Required (+48V)") else tr(isTr, "Gerekmez", "Not Required"))
            rows.add(tr(isTr, "Sıfır Gecikmeli Monitörleme", "Zero-Latency Monitoring") to if (s.hasZeroLatencyMonitoring) tr(isTr, "Destekleniyor (3.5mm Jak)", "Supported (3.5mm Jack)") else tr(isTr, "Ses Kartı Üzerinden", "Via Audio Interface"))
        }

        is MousepadSpec -> {
            rows.add(tr(isTr, "Yüzey Dokusu & Materyal", "Surface Material") to s.surfaceType)
            rows.add(tr(isTr, "Kayma Karakteri (Speed/Control)", "Glide Profile") to s.speedGlideTier)
            rows.add(tr(isTr, "Durdurma Gücü (Stopping Power)", "Stopping Power") to "${s.stoppingPowerScore} / 10")
            rows.add(tr(isTr, "Alt Taban Teknolojisi", "Base Material") to s.baseMaterial)
            rows.add(tr(isTr, "Boyutlar (En x Boy)", "Dimensions") to s.dimensionsMm)
            rows.add(tr(isTr, "Kalınlık", "Thickness") to "${s.thicknessMm} mm")
            rows.add(tr(isTr, "Kenar Dikişi (Stitched Edges)", "Stitched Edges") to if (s.stitchedEdges) tr(isTr, "Alçak Profil Dikişli", "Low-Profile Stitched") else tr(isTr, "Dikişsiz / Cam Kenar", "Unstitched / CNC Edge"))
            rows.add(tr(isTr, "Nem & Ter Direnci", "Humidity Resistance") to s.humidityResistance)
        }

        is WebcamSpec -> {
            rows.add(tr(isTr, "Maksimum Video Çözünürlüğü", "Max Video Resolution") to s.maxResolution)
            rows.add(tr(isTr, "Maksimum Kare Hızı", "Max Frame Rate") to "${s.maxFrameRateFps} FPS")
            rows.add(tr(isTr, "Görüntü Sensörü", "Image Sensor") to "${s.sensorModel} (${s.sensorSizeInch})")
            rows.add(tr(isTr, "Diyafram Açıklığı (Lens)", "Lens Aperture") to s.aperture)
            rows.add(tr(isTr, "Görüş Açısı (FOV)", "Field of View (FOV)") to "${s.fieldOfViewDeg}°")
            rows.add(tr(isTr, "Odaklama Sistemi", "Focus System") to s.focusType)
            rows.add(tr(isTr, "Yapay Zeka (AI) Kadraj Takibi", "AI Auto-Framing / Tracking") to if (s.hasAiTracking) tr(isTr, "Destekleniyor", "Supported") else tr(isTr, "Standart Sabit Kadraj", "Standard Fixed Frame"))
            rows.add(tr(isTr, "Donanımsal HDR", "Hardware HDR") to if (s.hasHardwareHdr) tr(isTr, "Destekleniyor", "Supported") else tr(isTr, "Standart SDR", "Standard SDR"))
            rows.add(tr(isTr, "Bağlantı Arayüzü", "Connection Interface") to s.connectionType)
            rows.add(tr(isTr, "Fiziksel Gizlilik Kapağı", "Privacy Shutter") to if (s.privacyShutter) tr(isTr, "Var", "Included") else tr(isTr, "Yok", "None"))
        }

        is GamingChairSpec -> {
            rows.add(tr(isTr, "Ergonomi & Koltuk Sınıfı", "Chair Type") to s.chairType)
            rows.add(tr(isTr, "Döşeme Kumaşı / Materyal", "Upholstery Material") to s.upholsteryMaterial)
            rows.add(tr(isTr, "L4-L5 Bel Desteği Mimarisi", "Lumbar Support System") to s.lumbarSupportType)
            rows.add(tr(isTr, "Kolçak (Armrest) Mekanizması", "Armrest Type") to s.armrestType)
            rows.add(tr(isTr, "Amortisör (Gas Lift) Sınıfı", "Gas Lift Class") to s.gasLiftClass)
            rows.add(tr(isTr, "Maksimum Taşıma Kapasitesi", "Max Weight Capacity") to "${s.maxWeightCapacityKg} kg")
            rows.add(tr(isTr, "Önerilen Kullanıcı Boyu", "Recommended User Height") to s.recommendedHeightRange)
            rows.add(tr(isTr, "Maksimum Sırt Yatış Açısı", "Max Recline Angle") to "${s.reclineAngleDeg}°")
            rows.add(tr(isTr, "Salınım & Tilt Mekanizması", "Tilt Mechanism") to s.tiltMechanism)
        }

        is PhoneSpec -> {
            rows.add(tr(isTr, "Yonga Seti (SoC)", "Chipset (SoC)") to "${s.chipset} (${s.processNodeNm}nm)")
            if (s.cpuArch.isNotBlank()) rows.add(tr(isTr, "İşlemci Çekirdek Yapısı", "CPU Architecture") to s.cpuArch)
            if (s.gpuModel.isNotBlank()) rows.add(tr(isTr, "Grafik İşlemci (GPU)", "GPU Model") to s.gpuModel)
            rows.add(tr(isTr, "Bellek & Depolama", "RAM & Storage") to "${s.ramGb} GB ${s.ramType} • ${s.storageGb} GB ${s.storageType}")
            s.antutuScore?.let { rows.add(tr(isTr, "AnTuTu v10 Skoru", "AnTuTu v10 Score") to "${formatPoints(it)} Puan") }
            if (s.geekbenchSingle != null && s.geekbenchMulti != null) {
                rows.add(tr(isTr, "Geekbench 6 (Tek/Çoklu)", "Geekbench 6 (Single/Multi)") to "${formatPoints(s.geekbenchSingle)} / ${formatPoints(s.geekbenchMulti)}")
            }
            s.stabilityPercent?.let { rows.add(tr(isTr, "3DMark Termal Kararlılık", "3DMark Thermal Stability") to "%$it Kararlı") }
            rows.add(tr(isTr, "Ekran Boyutu & Panel", "Display & Panel") to "${s.displaySizeInch} inç ${s.displayTech}")
            rows.add(tr(isTr, "Çözünürlük & Tazeleme", "Resolution & Refresh Rate") to "${s.resolution} (${s.ppi} PPI) • ${s.refreshRateHz} Hz")
            rows.add(tr(isTr, "Tepe Parlaklık (Nit)", "Peak Brightness") to "${s.peakBrightnessNit} Nit (${s.glassProtection})")
            val oisDesc = if (s.hasOis) " (${s.oisType})" else ""
            rows.add(tr(isTr, "Ana Kamera", "Main Camera") to "${s.mainCameraMp} MP • ${s.mainSensorSize} • ${s.mainAperture}$oisDesc")
            s.opticalZoomX?.let { rows.add(tr(isTr, "Telefoto / Optik Zoom", "Telephoto / Optical Zoom") to "${s.telephotoMp ?: 0} MP • ${it}x Optik Yakınlaştırma") }
            s.ultraWideMp?.let { rows.add(tr(isTr, "Ultra Geniş Açı", "Ultra-Wide Camera") to "$it MP") }
            rows.add(tr(isTr, "Video Kayıt Çözünürlüğü", "Video Recording") to s.videoResolution)
            val wirelessText = s.wirelessChargingWatt?.let { " • ${it}W Kablosuz" } ?: ""
            rows.add(tr(isTr, "Batarya & Hızlı Şarj", "Battery & Charging") to "${s.batteryMah} mAh • ${s.wiredChargingWatt}W Kablolu$wirelessText")
            rows.add(tr(isTr, "Kasa & Suya Dayanıklılık", "Build & Ingress Protection") to "${s.frameMaterial} Çerçeve • ${s.waterResistance} (${s.weightGrams}g / ${s.thicknessMm}mm)")
            rows.add(tr(isTr, "İşletim Sistemi & Destek", "OS & Support") to "${s.osName} (${s.updateYears} Yıl Güncelleme Garantisi)")
            rows.add(tr(isTr, "Oyun Yetenek Desteği", "Official Game Support") to "${s.pubgFpsSupport} Destekli")
        }

        is TabletSpec -> {
            rows.add(tr(isTr, "Yonga Seti (SoC) & Mimari", "Chipset & Architecture") to "${s.chipset} (${s.processNodeNm}nm)")
            if (s.cpuArch.isNotBlank()) rows.add(tr(isTr, "Çekirdek Dağılımı", "CPU Configuration") to s.cpuArch)
            if (s.gpuModel.isNotBlank()) rows.add(tr(isTr, "Grafik İşlemci (GPU)", "GPU Model") to s.gpuModel)
            rows.add(tr(isTr, "Bellek & Depolama", "RAM & Storage") to "${s.ramGb} GB RAM • ${s.storageGb} GB ${s.storageType}")
            s.antutuScore?.let { rows.add(tr(isTr, "AnTuTu v10 Skoru", "AnTuTu v10 Score") to "${formatPoints(it)} Puan") }
            if (s.geekbenchSingle != null && s.geekbenchMulti != null) {
                rows.add(tr(isTr, "Geekbench 6 (Tek/Çoklu)", "Geekbench 6 (Single/Multi)") to "${formatPoints(s.geekbenchSingle)} / ${formatPoints(s.geekbenchMulti)}")
            }
            rows.add(tr(isTr, "Ekran & Panel Teknolojisi", "Display & Panel Tech") to "${s.displaySizeInch} inç ${s.displayTech} • ${s.refreshRateHz} Hz")
            rows.add(tr(isTr, "Çözünürlük & Tepe Parlaklık", "Resolution & Brightness") to "${s.resolution} (${s.ppi} PPI) • ${s.peakBrightnessNit} Nit")
            rows.add(tr(isTr, "Stylus Kalem Desteği", "Stylus Support") to s.stylusSupport)
            rows.add(tr(isTr, "Ses Sistemi & Akustik", "Audio System") to "${s.speakerCount} Hoparlör${if (s.hasDolbyAtmos) " Dolby Atmos Desteği" else ""}")
            rows.add(tr(isTr, "Batarya & Hızlı Şarj", "Battery & Charging") to "${s.batteryMah} mAh • ${s.wiredChargingWatt}W")
            rows.add(tr(isTr, "Tasarım & Boyutlar", "Dimensions & Weight") to "${s.bodyMaterial} • ${s.thicknessMm} mm • ${s.weightGrams}g")
            rows.add(tr(isTr, "İşletim Sistemi", "Operating System") to "${s.osName} (${s.updateYears} Yıl Güncelleme)")
            rows.add(tr(isTr, "Hücresel Bağlantı", "Cellular Connectivity") to s.cellularOption)
        }

        is CaseFanSpec -> {
            rows.add(tr(isTr, "Fan Boyutu", "Fan Dimensions") to "${s.fanSizeMm} mm")
            rows.add(tr(isTr, "Dönüş Hızı (RPM)", "Rotational Speed") to "${s.minRpm} - ${s.maxRpm} RPM")
            rows.add(tr(isTr, "Hava Akışı (Airflow)", "Airflow") to "${s.maxAirflowCfm} CFM")
            rows.add(tr(isTr, "Statik Basınç", "Static Pressure") to "${s.staticPressureMmH2o} mmH2O")
            rows.add(tr(isTr, "Gürültü Seviyesi", "Noise Level") to "${s.noiseLevelDba} dBA")
            rows.add(tr(isTr, "Rulman / Yatak Tipi", "Bearing Type") to s.bearingType)
            rows.add(tr(isTr, "PWM Kontrol Pini", "PWM Connector") to "${s.pwmPins}-Pin PWM")
            rows.add(tr(isTr, "Daisy-Chain Bağlantı", "Daisy-Chain") to if (s.isDaisyChain) "Manyetik / Geçmeli Kablosuz Destek" else "Standart Kablolu")
            rows.add(tr(isTr, "Aydınlatma (RGB)", "Lighting") to s.argbType)
        }

        is UpsSpec -> {
            rows.add(tr(isTr, "Güç Kapasitesi", "Power Capacity") to "${s.vaRating} VA / ${s.wattCapacity} Watt")
            rows.add(tr(isTr, "Topoloji & Tip", "Topology & Type") to s.topology)
            rows.add(tr(isTr, "Dalga Şekli (Çıkış)", "Output Waveform") to s.sinewaveType)
            rows.add(tr(isTr, "Çıkış Prizleri", "Output Outlets") to "${s.outletCount} Priz")
            rows.add(tr(isTr, "Akü Yapısı", "Battery Specs") to s.batterySpecs)
            rows.add(tr(isTr, "Tipik Yedekleme Süresi", "Typical Backup Runtime") to "%50 Yük: ~${s.runtimeMinHalfLoad} dk | %100 Yük: ~${s.runtimeMinFullLoad} dk")
            rows.add(tr(isTr, "USB Yönetim Portu", "USB Management Interface") to if (s.hasUsbInterface) "Var (Otomatik Güvenli Kapatma)" else "Yok")
            rows.add(tr(isTr, "Darbe Dayanımı (Surge)", "Surge Protection") to "${s.surgeProtectionJoules} Joule")
        }

        is GamingDeskSpec -> {
            rows.add(tr(isTr, "Masa Ölçüleri (G x D)", "Dimensions (W x D)") to "${s.deskWidthCm} x ${s.deskDepthCm} cm")
            rows.add(tr(isTr, "Yükseklik Aralığı", "Height Range") to "${s.minHeightCm} - ${s.maxHeightCm} cm")
            rows.add(tr(isTr, "Yükseklik Ayar Tipi", "Adjustment Mechanism") to if (s.isMotorizedHeight) "Elektrikli Motorlu Standing Desk" else "Sabit Yükseklik")
            rows.add(tr(isTr, "Maksimum Taşıma Kapasitesi", "Max Load Capacity") to "${s.maxLoadKg} kg")
            rows.add(tr(isTr, "Yüzey Kaplaması", "Desktop Material") to s.surfaceFinish)
            rows.add(tr(isTr, "Kablo Yönetimi", "Cable Management") to if (s.hasCableManagement) "Var (Entegre Kanal / Tepsi)" else "Yok")
            rows.add(tr(isTr, "Özel Kesim Deskmat", "Integrated Deskmat") to if (s.integratedMousepad) "Tam Boy Mousepad Dahil" else "Yok")
        }

        is GamingLightingSpec -> {
            rows.add(tr(isTr, "Aydınlatma Form Faktörü", "Form Factor") to s.formFactor)
            rows.add(tr(isTr, "Işık Akısı (Maks. Lümen)", "Max Luminous Flux") to "${s.maxLumens} Lümen")
            rows.add(tr(isTr, "Renk Sıcaklığı Aralığı", "Color Temperature") to s.colorTempKelvin)
            rows.add(tr(isTr, "Renk Doğruluk İndeksi (CRI)", "Color Rendering Index") to "Ra ${s.criScore}")
            rows.add(tr(isTr, "Ekran Senkronizasyonu", "Screen Mirroring") to if (s.hasScreenMirroring) "Destekleniyor (Reaktif Görsel Senkron)" else "Yok")
            rows.add(tr(isTr, "Desteklenen Akıllı Ekosistemler", "Smart Ecosystems") to s.supportedEcosystems)
            rows.add(tr(isTr, "Bağlantı Türü", "Connectivity") to s.connectionType)
            rows.add(tr(isTr, "Güç Beslemesi", "Power Source") to s.powerSource)
        }

        is SmartwatchSpec -> {
            rows.add(tr(isTr, "Ekran Boyutu & Paneli", "Display & Panel") to "${s.screenSizeInch}\" ${s.screenPanel}")
            rows.add(tr(isTr, "Zirve Parlaklık", "Peak Brightness") to "${s.peakBrightnessNits} Nit")
            rows.add(tr(isTr, "Tipik Pil Ömrü", "Battery Life") to "${s.batteryLifeDays} Gün")
            rows.add(tr(isTr, "Su Geçirmezlik", "Water Resistance") to "${s.waterResistanceAtm} ATM (${s.waterResistanceAtm * 10} Metre)")
            rows.add(tr(isTr, "Kasa Malzemesi", "Case Material") to s.caseMaterial)
            rows.add(tr(isTr, "Sağlık Sensörleri", "Health Sensors") to s.sensorsList)
            rows.add(tr(isTr, "İşletim Sistemi", "Operating System") to s.osName)
            rows.add(tr(isTr, "Hücresel Bağlantı (LTE)", "Cellular LTE") to if (s.hasCellularLte) "eSIM / LTE Destekli" else "Bluetooth / Wi-Fi")
        }

        is RouterSpec -> {
            rows.add(tr(isTr, "Wi-Fi Standardı", "Wi-Fi Standard") to "${s.wifiStandard} (${s.bands})")
            rows.add(tr(isTr, "Hız Sınıfı", "Speed Class") to s.speedClass)
            rows.add(tr(isTr, "Ethernet Portları", "Ethernet Ports") to s.ethernetPorts)
            rows.add(tr(isTr, "Oyun QoS & SQM Desteği", "Gaming QoS & SQM") to "${if (s.hasGamingQos) "Oyun Önceliği Var" else "Yok"} • ${if (s.hasSqmBufferbloat) "SQM Anti-Bufferbloat" else "Standart"}")
            rows.add(tr(isTr, "İşlemci & RAM", "Hardware") to s.processorAndRam)
            rows.add(tr(isTr, "USB Paylaşım Portları", "USB Ports") to s.usbSharingPorts)
        }

        is ModemSpec -> {
            rows.add(tr(isTr, "VDSL / Genişbant Profili", "VDSL Profile") to s.vdslProfile)
            rows.add(tr(isTr, "Fiber WAN Desteği", "Fiber WAN") to if (s.fiberWanSupport) "Var (FTTH Fiber Uyumlu)" else "Yok")
            rows.add(tr(isTr, "Dahili Wi-Fi Standardı", "Wi-Fi Standard") to s.wifiStandard)
            rows.add(tr(isTr, "LAN Portları", "LAN Ports") to s.lanPorts)
            rows.add(tr(isTr, "VoIP Telefon Hatları", "VoIP Ports") to s.voipPorts)
            rows.add(tr(isTr, "Yonga Seti", "Chipset") to s.chipset)
        }

        is AccessPointSpec -> {
            rows.add(tr(isTr, "Wi-Fi Standardı & Form", "Wi-Fi Standard & Form") to "${s.wifiStandard} (${s.formFactor})")
            rows.add(tr(isTr, "PoE Standardı", "PoE Standard") to s.poeSupport)
            rows.add(tr(isTr, "Eşzamanlı İstemci Sayısı", "Max Clients") to "${s.maxClients}+ Cihaz")
            rows.add(tr(isTr, "MU-MIMO Anten Akışları", "MU-MIMO Streams") to s.muMimoStreams)
            rows.add(tr(isTr, "Uplink Portu", "Uplink Port") to s.uplinkPort)
        }

        is RangeExtenderSpec -> {
            rows.add(tr(isTr, "Cihaz Tipi & Wi-Fi", "Extender Type & Wi-Fi") to "${s.extenderType} (${s.wifiStandard})")
            rows.add(tr(isTr, "Dedicated Backhaul Omurga", "Dedicated Backhaul") to s.dedicatedBackhaul)
            rows.add(tr(isTr, "Mesh Protokolü", "Mesh Protocol") to s.meshProtocol)
            rows.add(tr(isTr, "Ethernet Portları", "Ethernet Ports") to s.ethernetPorts)
            rows.add(tr(isTr, "Kapsama Alanı", "Coverage Area") to "~${s.coverageAreaM2} m²")
        }
    }
    return rows
}

@Composable
private fun DetailBottomBar(
    isAddedToBuild: Boolean,
    priceText: String,
    onAddToBuildClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddToVsArenaClick: () -> Unit = {},
    onPriceOverrideClick: () -> Unit = {},
    isPrebuilt: Boolean = false,
    isLaptop: Boolean = false,
    isMobile: Boolean = false,
    isCustomPrice: Boolean = false,
    customPriceNote: String? = null,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onPriceOverrideClick),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                        val barStrings = LocalAppStrings.current
                    Text(text = if (isCustomPrice) "🏷️ ${barStrings.localPriceOverride}" else barStrings.marketPrice,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isCustomPrice) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCustomPrice) NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "✎",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCustomPrice) NeonAmber else MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isCustomPrice) NeonAmber else MaterialTheme.colorScheme.secondary,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!customPriceNote.isNullOrBlank()) {
                    Text(
                        text = customPriceNote,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onAddToVsArenaClick,
                    shape = RoundedCornerShape(Spacing.small),
                    color = NeonCyan.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, NeonCyan),
                ) {
                    Text(
                        text = "⚖️ +VS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }

                if (!isLaptop && !isMobile) {
                    val buttonContainerColor by animateColorAsState(
                        targetValue = if (isAddedToBuild) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    )

                    Button(
                        onClick = onAddToBuildClick,
                        shape = RoundedCornerShape(Spacing.small),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonContainerColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        contentPadding = PaddingValues(horizontal = Spacing.medium, vertical = Spacing.small),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isAddedToBuild) {
                                CheckMarkCanvas(color = MaterialTheme.colorScheme.onSecondary)
                                Spacer(modifier = Modifier.width(Spacing.small))
                                Text(
                                    text = if (isPrebuilt) strings.detailImportedSystem else strings.detailAddedToWizard,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            } else {
                                PlusIconCanvas(color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(Spacing.small))
                                Text(
                                    text = if (isPrebuilt) (if (isTr) "Sihirbaza Aktar" else "Import to Build") else strings.addToWizard,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
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
private fun PrebuiltSystemDetailsCard(
    spec: PrebuiltSpec,
    onTransferToWizard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = NeonCyan,
        secondaryColor = NeonEmerald,
        shape = RoundedCornerShape(Spacing.cardRadius),
        contentPadding = Spacing.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = Spacing.small),
                ) {
                    Text(text = "🖥️", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = LocalAppStrings.current.detailPrebuiltAnalysis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                DetailBadge(
                    text = spec.storeName,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Akıllı Kâr/Zarar & Avantaj Analizörü Kutusu
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(
                    1.dp,
                    if (spec.isAdvantageous) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(Spacing.medium)) {
                    Text(
                        text = strings.detailPrebuiltBreakdown,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = strings.detailPrebuiltSeparatePrice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${spec.estimatedSeparatePriceTry?.toLong() ?: 0} ₺",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        val isAdv = spec.isAdvantageous
                        val badgeBg = if (isAdv) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        val badgeTextColor = if (isAdv) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

                        if (spec.priceAdvantageTry != null && spec.priceAdvantageTry > 0.0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    text = if (isTr) "+${spec.priceAdvantageTry.toLong()} ₺ (%${spec.advantagePercent.toInt()} Kâr)" else "+${spec.priceAdvantageTry.toLong()} ₺ (+${spec.advantagePercent.toInt()}% Value)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = badgeTextColor,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.small))

                    val statusMsg = if (spec.priceAdvantageTry != null && spec.priceAdvantageTry > 0.0) {
                        if (isTr) "✓ Bu hazır sistem, parçaların piyasa tekil satış fiyatına kıyasla ${spec.priceAdvantageTry.toLong()} ₺ daha uygundur."
                        else "✓ This prebuilt is ${spec.priceAdvantageTry.toLong()} ₺ cheaper than individual retail parts combined."
                    } else {
                        if (isTr) "Bileşenlerin piyasa fiyatı hazır sistem tutarıyla dengeli durumdadır."
                        else "Individual component pricing closely matches this prebuilt package."
                    }
                    Text(
                        text = statusMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (spec.isAdvantageous) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Text(
                text = if (isTr) "İçindeki 6 Ana Donanım Bileşeni" else "6 Core Hardware Components Inside",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Spacing.small))

            val components = listOf(
                Triple("⚡", if (isTr) "İşlemci (CPU)" else "Processor (CPU)", spec.cpuModel),
                Triple("🎮", if (isTr) "Ekran Kartı (GPU)" else "Graphics Card (GPU)", spec.gpuModel),
                Triple("🔌", if (isTr) "Anakart" else "Motherboard", spec.motherboardModel),
                Triple("🧠", if (isTr) "Bellek (RAM)" else "Memory (RAM)", spec.ramSpec),
                Triple("💾", if (isTr) "Depolama (SSD)" else "Storage (SSD)", spec.storageSpec),
                Triple("🖥️", if (isTr) "Kasa & Güç Kaynağı" else "Case & Power Supply", spec.psuCaseSpec),
            )

            components.forEach { (icon, title, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            Button(
                onClick = onTransferToWizard,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Spacing.small),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    text = strings.detailPrebuiltTransfer,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun LaptopSpecialDetailsCard(
    item: HardwareItem,
    spec: LaptopSpec,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = ElectricBlue,
        secondaryColor = NeonPurple,
        shape = RoundedCornerShape(Spacing.cardRadius),
        contentPadding = Spacing.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Header
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = Spacing.small),
                ) {
                    Text(text = "💻", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = LocalAppStrings.current.detailLaptopSpecs,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                DetailBadge(
                    text = spec.brand.ifBlank { item.brand },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Gerçek TGP Watt Değeri Banner (Çok Kritik)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("⚡", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = strings.detailGpuTgpTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp,
                        )
                        Text(
                            text = "${spec.gpuModel} • ${spec.gpuWattTgp}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (isTr) "Kısıtlanmamış tam güç (Full Wattage) grafik yongası • ${spec.muxSwitch}" else "Unrestricted Full-Wattage GPU • ${spec.muxSwitch}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // İçindeki 6 Ana Donanım Bileşeni (OEM Hazır Sistem Mimarisinde)
            Text(
                text = if (isTr) "İçindeki 6 Ana Donanım Bileşeni" else "6 Core Hardware Components Inside",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Spacing.small))

            val laptopComponents = listOf(
                LaptopComponentItem(
                    icon = "⚡",
                    title = if (isTr) "İşlemci (CPU)" else "Processor (CPU)",
                    mainText = spec.cpuModel,
                    subText = "${spec.effectiveCpuCores} • ${spec.effectiveCpuClockGhz}",
                ),
                LaptopComponentItem(
                    icon = "🎮",
                    title = if (isTr) "Ekran Kartı (GPU & Güç)" else "Graphics Card (GPU & Power)",
                    mainText = "${spec.gpuModel} (${spec.gpuWattTgp})",
                    subText = "${spec.muxSwitch} " + if (isTr) "Desteği" else "Supported",
                ),
                LaptopComponentItem(
                    icon = "🖥️",
                    title = if (isTr) "Ekran (Panel)" else "Display (Panel)",
                    mainText = spec.effectivePanelDetails,
                    subText = if (isTr) "Yüksek Yenileme Hızı & Düşük Gecikmeli Espor Paneli" else "High Refresh & Low Latency Esports Panel",
                ),
                LaptopComponentItem(
                    icon = "🧠",
                    title = if (isTr) "Bellek (RAM)" else "Memory (RAM)",
                    mainText = spec.ramSpec,
                    subText = spec.effectiveRamSlots,
                ),
                LaptopComponentItem(
                    icon = "💾",
                    title = if (isTr) "Depolama (SSD)" else "Storage (SSD)",
                    mainText = spec.storageSpec,
                    subText = spec.effectiveStorageSlots,
                ),
                LaptopComponentItem(
                    icon = "🔋",
                    title = if (isTr) "Gövde, Pil & Soğutma" else "Chassis, Battery & Cooling",
                    mainText = "${spec.batteryWh} Wh " + (if (isTr) "Batarya" else "Battery") + " • ${spec.effectiveChargerWatt} • ${spec.weightKg} kg",
                    subText = spec.coolingTech,
                ),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                laptopComponents.forEach { comp ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.medium),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(text = comp.icon, style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = comp.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = comp.mainText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = comp.subText,
                                    style = MaterialTheme.typography.bodySmall,
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

private data class LaptopComponentItem(
    val icon: String,
    val title: String,
    val mainText: String,
    val subText: String,
)

@Composable
private fun DetailBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Spacing.extraSmall),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
        )
    }
}

@Composable
private fun BackArrowCanvas(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.6f, size.height * 0.2f)
            lineTo(size.width * 0.3f, size.height * 0.5f)
            lineTo(size.width * 0.6f, size.height * 0.8f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun CheckMarkCanvas(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.5f)
            lineTo(size.width * 0.45f, size.height * 0.75f)
            lineTo(size.width * 0.85f, size.height * 0.25f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun PlusIconCanvas(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(16.dp)) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.2f),
            end = Offset(size.width * 0.5f, size.height * 0.8f),
            strokeWidth = 2.5f.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.5f),
            end = Offset(size.width * 0.8f, size.height * 0.5f),
            strokeWidth = 2.5f.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private data class StoreSearchTarget(
    val name: String,
    val searchUrlTemplate: String,
)

private val TR_STORE_SEARCH_TARGETS = listOf(
    StoreSearchTarget("Akakçe", "https://www.akakce.com/arama/?q="),
    StoreSearchTarget("İncehesap", "https://www.incehesap.com/arama/?q="),
    StoreSearchTarget("Hepsiburada", "https://www.hepsiburada.com/ara?q="),
    StoreSearchTarget("Amazon TR", "https://www.amazon.com.tr/s?k="),
    StoreSearchTarget("Sinerji", "https://www.sinerji.gen.tr/arama?q="),
    StoreSearchTarget("Tebilon", "https://www.tebilon.com/arama/?q="),
    StoreSearchTarget("N11", "https://www.n11.com/arama?q="),
    StoreSearchTarget("PttAVM", "https://www.pttavm.com/arama?q="),
)

private fun encodeSearchQuery(query: String): String {
    val allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.~"
    val sb = StringBuilder()
    for (ch in query) {
        if (ch in allowed) {
            sb.append(ch)
        } else if (ch == ' ') {
            sb.append('+')
        } else {
            val bytes = ch.toString().encodeToByteArray()
            for (b in bytes) {
                sb.append('%')
                val hex = (b.toInt() and 0xFF).toString(16).uppercase()
                if (hex.length == 1) sb.append('0')
                sb.append(hex)
            }
        }
    }
    return sb.toString()
}

@Composable
private fun StoreOffersCard(
    offers: List<StoreOffer>,
    currentPriceTry: Double?,
    productName: String,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    val uriHandler = LocalUriHandler.current

    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = NeonCyan,
        secondaryColor = NeonEmerald,
        shape = RoundedCornerShape(Spacing.cardRadius),
        contentPadding = Spacing.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = LocalAppStrings.current.detailStoreOffers,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = if (offers.isNotEmpty()) strings.detailVerifiedStores.replace("%d", offers.size.toString()) else strings.detailLiveSearch,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            // Verified Scraped Offers List (if present)
            if (offers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.medium))

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    offers.forEachIndexed { index, offer ->
                        val isCheapest = index == 0
                        Surface(
                            onClick = {
                                uriHandler.openSafeStoreUrl(offer.url)
                            },
                            shape = RoundedCornerShape(Spacing.small),
                            color = if (isCheapest) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                1.dp,
                                if (isCheapest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = offer.storeName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (isCheapest) {
                                        Spacer(modifier = Modifier.width(Spacing.small))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                                        ) {
                                            Text(
                                                text = "EN UCUZ",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(Spacing.small))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₺${offer.priceTry.formatPrice()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCheapest) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                        softWrap = false,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.small))
                                    Text(
                                        text = "↗",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            )
            Spacer(modifier = Modifier.height(Spacing.medium))

            // 1-Tap Live Multi-Store Search Comparison
            Text(
                text = strings.detailLiveStoreSearch,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = strings.detailLiveStoreSearchSub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(Spacing.small))

            val cleanQuery = encodeSearchQuery(productName.ifBlank { "hardware" })
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                TR_STORE_SEARCH_TARGETS.chunked(2).forEach { rowTargets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        rowTargets.forEach { target ->
                            Surface(
                                onClick = {
                                    val fullUrl = "${target.searchUrlTemplate}$cleanQuery"
                                    uriHandler.openSafeStoreUrl(fullUrl)
                                },
                                shape = RoundedCornerShape(Spacing.small),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                                modifier = Modifier.weight(1f),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = target.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = tr(isTr, "Ara ↗", "Search ↗"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
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

@Preview
@Composable
private fun HardwareDetailContentPreview() {
    HardwareWizardTheme {
        HardwareDetailContent(
            uiState = HardwareDetailUiState(
                isLoading = false,
                item = HardwareItem(
                    id = HardwareId("cpu-1"),
                    brand = "AMD",
                    model = "Ryzen 7 7800X3D",
                    price = Money(amountMinor = 44900, currencyCode = "USD"),
                    spec = CpuSpec(
                        socket = "AM5",
                        coreCount = 8,
                        threadCount = 16,
                        baseClock = Frequency(4200),
                        boostClock = Frequency(5000),
                        tdp = Wattage(120),
                        hasIntegratedGraphics = true,
                    ),
                ),
                isAddedToBuild = false,
            ),
            onNavigateBack = {},
            onAddToBuildClick = {},
        )
    }
}

@Composable
fun HardwareDetailHeroImage(
    images: List<String> = emptyList(),
    imageUrl: String? = null,
    category: HardwareCategory,
    displayName: String,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val effectiveList = remember(images, imageUrl) {
        val list = images.filter { it.isNotBlank() }
        if (list.isNotEmpty()) list else listOfNotNull(imageUrl?.takeIf { it.isNotBlank() })
    }

    val containerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val shape = RoundedCornerShape(topStart = Spacing.cardRadius, topEnd = Spacing.cardRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(shape)
            .background(containerBg)
            .border(1.dp, borderColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (effectiveList.isEmpty()) {
            DetailPlaceholderHero(category = category)
        } else if (effectiveList.size == 1) {
            SubcomposeAsyncImage(
                model = effectiveList.first(),
                contentDescription = displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        )
                    }
                },
                error = {
                    DetailPlaceholderHero(category = category)
                },
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { effectiveList.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SubcomposeAsyncImage(
                        model = effectiveList[page],
                        contentDescription = if (isTr) "$displayName - Açı ${page + 1}" else "$displayName - View ${page + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                )
                            }
                        },
                        error = {
                            if (page > 0 && effectiveList.isNotEmpty()) {
                                SubcomposeAsyncImage(
                                    model = effectiveList[0],
                                    contentDescription = displayName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize(),
                                    error = { DetailPlaceholderHero(category = category) },
                                )
                            } else {
                                DetailPlaceholderHero(category = category)
                            }
                        },
                    )
                }
            }

            // Top-right angle index badge (e.g. 1 / 4)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "📷 ${pagerState.currentPage + 1} / ${effectiveList.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Bottom-center modern animated dot/pill indicator
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                effectiveList.indices.forEach { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateFloatAsState(
                        targetValue = if (isSelected) 22f else 6f,
                        animationSpec = tween(durationMillis = 300),
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun DetailPlaceholderHero(
    category: HardwareCategory,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val (badgeBg, badgeText) = when (category) {
        HardwareCategory.CPU -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.GPU -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HardwareCategory.RAM -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        HardwareCategory.MOTHERBOARD -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.PSU -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HardwareCategory.STORAGE -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        HardwareCategory.COOLER -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.CASE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HardwareCategory.PREBUILT_PC -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.LAPTOP -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        HardwareCategory.MOUSE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HardwareCategory.KEYBOARD -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.HEADSET -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        HardwareCategory.MONITOR -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.MICROPHONE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HardwareCategory.MOUSEPAD -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        HardwareCategory.WEBCAM -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.GAMING_CHAIR -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        HardwareCategory.SMARTPHONE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        HardwareCategory.TABLET -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(badgeBg.copy(alpha = 0.35f))
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = category.localized(strings),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = badgeText,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = strings.detailImageVerifying,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = badgeText.copy(alpha = 0.8f),
        )
    }
}





@Composable
private fun DeviceVariantSelectorCard(
    variants: List<DeviceVariant>,
    selectedVariantKey: String,
    onVariantSelect: (DeviceVariant) -> Unit,
    currency: com.panda.myapplication.domain.model.AppCurrency,
    noPriceFallback: String,
    isTr: Boolean,
    modifier: Modifier = Modifier,
) {
    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = ElectricBlue,
        secondaryColor = NeonCyan,
        shape = RoundedCornerShape(Spacing.cardRadius),
        contentPadding = Spacing.medium,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = if (isTr) "Dahili Hafıza & RAM Seçenekleri" else "Storage & RAM Options",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (isTr) "Donanım konfigürasyonunu seçin" else "Select a hardware configuration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DetailBadge(
                    text = if (isTr) "${variants.size} Seçenek" else "${variants.size} Variants",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                variants.forEach { variant ->
                    val isSelected = variant.variantKey == selectedVariantKey
                    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    val containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }

                    Surface(
                        onClick = { onVariantSelect(variant) },
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderColor,
                        ),
                        modifier = Modifier.widthIn(min = 135.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = variant.displayStorage,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                    )
                                }
                            }

                            Text(
                                text = variant.displayRam,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = variant.priceTry.formatPriceWithCurrency(currency, noPriceFallback),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun CompanionSoftwareCard(
    software: CompanionSoftware,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "⚡",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Column {
                        Text(
                            text = "Eşlikçi Yazılım & Sürücü Bulucu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = software.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                ) {
                    Text(
                        text = software.latestVersion,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = software.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                software.capabilities.forEach { feature ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    ) {
                        Text(
                            text = "• $feature",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Button(
                    onClick = {
                        try {
                            uriHandler.openUri(software.officialDownloadUrl)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp),
                ) {
                    Text(
                        text = "Resmi Sürücüyü İndir",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (software.webConfiguratorUrl != null) {
                    OutlinedButton(
                        onClick = {
                            try {
                                uriHandler.openUri(software.webConfiguratorUrl!!)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp),
                    ) {
                        Text(
                            text = "Tarayıcıdan Yönet (WebHID)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpsRuntimeEstimatorCard(
    spec: UpsSpec,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    var setupLoadWatt by remember { mutableStateOf(350f) }
    val isPureSine = spec.sinewaveType.contains("Tam Sinüs") || spec.sinewaveType.contains("Pure Sine")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                )
                Text(
                    text = tr(isTr, "⚡ Akıllı Kesinti Yedekleme Simülatörü", "⚡ Smart Outage Runtime Simulator"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = tr(
                    isTr,
                    "Sisteminizin çektiği gücü seçerek bu güç kaynağının elektrik kesintisinde kasanızı ne kadar süre açık tutabileceğini anlık hesaplayın.",
                    "Estimate how many minutes this UPS can keep your battlestation running during a power outage based on active load."
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tr(isTr, "Simüle Edilen Sistem Yükü:", "Simulated Battlestation Load:"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${setupLoadWatt.toInt()} Watt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Slider(
                value = setupLoadWatt,
                onValueChange = { setupLoadWatt = it },
                valueRange = 100f..spec.wattCapacity.toFloat().coerceAtLeast(150f),
                steps = 15,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )

            val loadRatio = (setupLoadWatt / spec.wattCapacity.toFloat().coerceAtLeast(1f)).coerceIn(0.1f, 1.0f)
            val estimatedMinutes = if (loadRatio <= 0.5f) {
                val factor = (0.5f / loadRatio).coerceIn(1.0f, 3.5f)
                (spec.runtimeMinHalfLoad * factor).toInt()
            } else {
                val progress = (loadRatio - 0.5f) / 0.5f
                (spec.runtimeMinHalfLoad - progress * (spec.runtimeMinHalfLoad - spec.runtimeMinFullLoad)).toInt().coerceAtLeast(spec.runtimeMinFullLoad)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = tr(isTr, "Tahmini Çalışma Süresi", "Estimated Runtime"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "~$estimatedMinutes Dakika",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isPureSine) "Tam Sinüs (PFC Uyumlu)" else "Simüle Sinüs",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPureSine) Color(0xFF10B981) else Color(0xFFF59E0B),
                        )
                        Text(
                            text = "${spec.wattCapacity}W / ${spec.vaRating}VA Sınır",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
