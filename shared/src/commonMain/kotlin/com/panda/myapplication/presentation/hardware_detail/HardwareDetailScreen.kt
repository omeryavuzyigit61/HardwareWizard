package com.panda.myapplication.presentation.hardware_detail

import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.CommunityDealType
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.StoreOffer
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

@Composable
fun HardwareDetailRoute(
    itemId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToVsArena: () -> Unit = {},
    onOpenAuthSheet: () -> Unit = {},
    viewModel: HardwareDetailViewModel = koinViewModel(),
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val coroutineScope = rememberCoroutineScope()
    var showPriceDialog by remember { mutableStateOf(false) }
    var showCommunitySubmitSheet by remember { mutableStateOf(false) }
    var selectedDealForDetail by remember { mutableStateOf<CommunityDeal?>(null) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val currency = LocalAppCurrency.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(
                title = uiState.item?.brand ?: (if (isTr) "Donanım Detayı" else "Hardware Details"),
                onNavigateBack = onNavigateBack,
                onAddToVsArenaClick = if (uiState.item != null) onAddToVsArenaClick else null,
            )
        },
        bottomBar = {
            if (uiState.item != null) {
                DetailBottomBar(
                    isAddedToBuild = uiState.isAddedToBuild,
                    isPrebuilt = uiState.item.spec is PrebuiltSpec,
                    isLaptop = uiState.item.spec is LaptopSpec,
                    isCustomPrice = uiState.item.isCustomPrice,
                    customPriceNote = uiState.item.customPriceNote,
                    priceText = uiState.item.currentPriceTry.formatPriceWithCurrency(currency, strings.noPriceFallback),
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
                    val item = uiState.item
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
                        item(key = "store_offers") {
                            StoreOffersCard(
                                offers = item.offers,
                                currentPriceTry = item.currentPriceTry,
                                productName = item.displayName,
                            )
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

                        // Synthetic Multi-Benchmark Card - STRICTLY CpuSpec & GpuSpec
                        if ((item.category == HardwareCategory.CPU || item.category == HardwareCategory.GPU) && item.benchmarkInfo != null) {
                            item(key = "synthetic_benchmark") {
                                SyntheticBenchmarkCard(
                                    category = item.category,
                                    benchmarkInfo = item.benchmarkInfo,
                                )
                            }
                        }

                        // Performance & Power Progress Bars (CPU, GPU & Peripherals)
                        if (item.category == HardwareCategory.CPU ||
                            item.category == HardwareCategory.GPU ||
                            item.category.isPeripheral
                        ) {
                            item(key = "perf_power_section") {
                                PerformancePowerSection(item = item)
                            }
                        }

                        // Category-specific Polymorphic Technical Specifications Section
                        item(key = "tech_specs") {
                            PolymorphicTechnicalSpecsSection(item = item)
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
            )
            Text(
                text = currentValue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor,
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
                    val low1FpsVal = game.fps.low1PctFps(selectedResolution)
                    val fpsColor = when {
                        avgFpsVal >= 120 -> NeonEmerald
                        avgFpsVal >= 60 -> NeonCyan
                        else -> NeonAmber
                    }
                    val barCeiling = if (game.category == GameCategory.ESPORTS) 950f else 260f
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
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = game.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (game.category == GameCategory.ESPORTS) "E-SPOR" else "AAA",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (game.category == GameCategory.ESPORTS) NeonCyan else NeonAmber,
                                        )
                                    }
                                    Text(
                                        text = "${selectedResolution.label} • ${game.presetSubtitle}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$avgFpsVal FPS",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = fpsColor,
                                    )
                                    Text(
                                        text = "%1 Low: $low1FpsVal FPS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
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
                    // Game Selector Pills (Counter-Strike 2, Cyberpunk 2077 vb.)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        labTests.forEachIndexed { idx, test ->
                            val isSelected = selectedLabTestIndex == idx
                            Surface(
                                onClick = { selectedLabTestIndex = idx },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeonRose.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) NeonRose else MaterialTheme.colorScheme.outline),
                            ) {
                                Text(
                                    text = test.gameTitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonRose else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = TextAlign.Center,
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
                                    text = "${activeLabTest.gameTitle} (${activeLabTest.resolutionSettings})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = strings.gameBenchAvgMinLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tezgâh: ${activeLabTest.rigGpuOrCpu} • ${activeLabTest.rigRam}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.5.sp,
                                color = NeonCyan,
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
                            val mainBarColor = if (entry.isAmd) Color(0xFFEF4444) else Color(0xFF2563EB)
                            val minBarColor = if (entry.isAmd) Color(0xFFB91C1C) else Color(0xFF1D4ED8)

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

                                    // Double Horizontal Bar: Top=Avg, Bottom=Min
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
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

                if (!isLaptop) {
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



