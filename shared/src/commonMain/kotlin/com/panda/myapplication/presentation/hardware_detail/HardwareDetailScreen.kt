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
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.components.PriceOverrideDialog
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.HardwareWizardTheme
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
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
    onNavigateToVsArena: () -> Unit = {},
    onOpenAuthSheet: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HardwareDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HardwareDetailEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is HardwareDetailEffect.ShowVsAddedToast -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "Arenaya Git",
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
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var showPriceDialog by remember { mutableStateOf(false) }
    var showCommunitySubmitSheet by remember { mutableStateOf(false) }
    var selectedDealForDetail by remember { mutableStateOf<CommunityDeal?>(null) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current
    val currency = LocalAppCurrency.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DetailTopBar(
                title = uiState.item?.brand ?: "Donanım Detayı",
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
                            text = "Teknik özellikler yükleniyor...",
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
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text("Listeye Dön", color = MaterialTheme.colorScheme.onPrimary)
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
                                    item = item,
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
                                                message = "⚠️ Topluluk fiyatı veya fırsat bildirmek için lütfen önce giriş yapın.",
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
    onAddToVsArenaClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
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
                        text = "⚖️ VS Arenaya Ekle",
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
    onPriceOverrideClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DetailBadge(
                            text = item.category.displayName,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        if (item.isCustomPrice) {
                            DetailBadge(
                                text = "🏷️ ÖZEL FİYAT",
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
                    "Model: ${item.model} • Parça No (SKU): ${item.spec.partNumber}"
                } else {
                    "Model: ${item.model}"
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
                                text = if (item.isCustomPrice) "Düzenle ✎" else "Fiyat Gir ✎",
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
                        title = "Çekirdek & İzlek Kapasitesi",
                        currentValue = "${s.coreCount} Çekirdek / ${s.threadCount} İzlek",
                        fraction = (s.coreCount / 32f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Maksimum 32 Çekirdek referansına göre",
                    )

                    val clockMhz = (s.boostClock ?: s.baseClock).mhz
                    MetricProgressBar(
                        title = "Maksimum İşlemci Frekansı",
                        currentValue = "${clockMhz / 1000f} GHz",
                        fraction = (clockMhz / 6000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "6.0 GHz tepe saat hızına oranla",
                    )

                    MetricProgressBar(
                        title = "TDP Güç Tüketimi",
                        currentValue = "${s.tdp.watts} W",
                        fraction = (s.tdp.watts / 300f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Soğutucu gereksinim düzeyi",
                    )

                    val multiCoreIndex = ((s.coreCount * 250f + clockMhz) / 14000f).coerceIn(0f, 1f)
                    MetricProgressBar(
                        title = "Sentetik Performans İndeksi",
                        currentValue = "${(multiCoreIndex * 100).toInt()}% Skoru",
                        fraction = multiCoreIndex,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Hesaplama ve render gücü tahmini",
                    )
                }

                is GpuSpec -> {
                    val vramGb = s.vram.megabytes / 1024
                    MetricProgressBar(
                        title = "Video Bellek (VRAM)",
                        currentValue = "$vramGb GB ${s.memoryType}",
                        fraction = (vramGb / 24f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "24 GB referans tepe VRAM",
                    )

                    val gpuClock = (s.boostClock ?: s.coreClock).mhz
                    MetricProgressBar(
                        title = "Grafik Çekirdek Frekansı",
                        currentValue = "$gpuClock MHz",
                        fraction = (gpuClock / 3000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "3000 MHz hedef frekansa göre",
                    )

                    MetricProgressBar(
                        title = "TDP Güç Çekimi",
                        currentValue = "${s.tdp.watts} W",
                        fraction = (s.tdp.watts / 450f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Kartın doğrudan çektiği güç",
                    )

                    MetricProgressBar(
                        title = "Önerilen Güç Kaynağı (PSU)",
                        currentValue = "${s.recommendedPsu.watts} W",
                        fraction = (s.recommendedPsu.watts / 1000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Sistem geneli tavsiye edilen besleme",
                    )
                }

                is RamSpec -> {
                    val capGb = s.capacity.megabytes / 1024
                    MetricProgressBar(
                        title = "Bellek Kapasitesi",
                        currentValue = "$capGb GB (${s.moduleCount}x Modül)",
                        fraction = (capGb / 128f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "128 GB maksimum masaüstü sınırına göre",
                    )

                    MetricProgressBar(
                        title = "Frekans Veri Hızı",
                        currentValue = "${s.speed.mhz} MT/s",
                        fraction = (s.speed.mhz / 8400f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "JEDEC & XMP/EXPO maksimum profili",
                    )

                    val latencyEfficiency = (1f - ((s.casLatency - 14).coerceIn(0, 30) / 30f)).coerceIn(0.1f, 1f)
                    MetricProgressBar(
                        title = "Gecikme Hızı (CAS Latency)",
                        currentValue = "CL${s.casLatency}",
                        fraction = latencyEfficiency,
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Daha düşük gecikme = yüksek duyarlılık",
                    )
                }

                is StorageSpec -> {
                    MetricProgressBar(
                        title = "Sıralı Okuma Hızı",
                        currentValue = "${s.sequentialReadMbps} MB/sn",
                        fraction = (s.sequentialReadMbps / 14000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "PCIe Gen 5 sınırına göre (14.000 MB/s)",
                    )

                    MetricProgressBar(
                        title = "Sıralı Yazma Hızı",
                        currentValue = "${s.sequentialWriteMbps} MB/sn",
                        fraction = (s.sequentialWriteMbps / 12000f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Dosya kaydetme ve kopyalama hızı",
                    )

                    val storageCapGb = s.capacity.megabytes / 1024
                    MetricProgressBar(
                        title = "Depolama Alanı",
                        currentValue = if (storageCapGb >= 1024) "${storageCapGb / 1024} TB" else "$storageCapGb GB",
                        fraction = (storageCapGb / 4096f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Maksimum 4 TB referans",
                    )
                }

                is CoolerSpec -> {
                    MetricProgressBar(
                        title = "Maksimum Isı Dağıtımı (TDP)",
                        currentValue = "${s.maxCoolingTdp.watts} W",
                        fraction = (s.maxCoolingTdp.watts / 400f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Masaüstü tepe soğutma katsayısı",
                    )

                    val radiatorMm = s.radiatorSizeMm ?: 120
                    MetricProgressBar(
                        title = "Radyatör / Fan Boyutu",
                        currentValue = s.radiatorSizeMm?.let { "${it} mm" } ?: "Hava Soğutma Kulesi",
                        fraction = (radiatorMm / 420f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = if (s.type == CoolerType.LIQUID) "Sıvı soğutma radyatör uzunluğu" else "Standart kule tipi montaj",
                    )
                }

                is PsuSpec -> {
                    MetricProgressBar(
                        title = "Çıkış Gücü Kapasitesi",
                        currentValue = "${s.wattage.watts} W",
                        fraction = (s.wattage.watts / 1600f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "1600 Watt tepe standardına göre",
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
                        title = "Verimlilik Derecesi",
                        currentValue = s.efficiency.displayName,
                        fraction = effFraction,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "80 PLUS enerji verimlilik sertifikası",
                    )
                }

                is MotherboardSpec -> {
                    MetricProgressBar(
                        title = "Bellek Yuva Desteği",
                        currentValue = "${s.ramSlots} RAM Slotu",
                        fraction = (s.ramSlots / 8f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Maksimum genişletilebilirlik",
                    )

                    val pcieFraction = when (s.pcieGeneration) {
                        PcieGeneration.GEN5 -> 1.0f
                        PcieGeneration.GEN4 -> 0.8f
                        PcieGeneration.GEN3 -> 0.6f
                        PcieGeneration.GEN2 -> 0.4f
                    }
                    MetricProgressBar(
                        title = "PCI Express Veri Yolu",
                        currentValue = s.pcieGeneration.displayName,
                        fraction = pcieFraction,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Ekran kartı ve NVMe bant genişliği standardı",
                    )
                }

                is CaseSpec -> {
                    s.maxGpuLengthMm?.let { gpuLen ->
                        MetricProgressBar(
                            title = "Maksimum GPU Uzunluğu",
                            currentValue = "$gpuLen mm",
                            fraction = (gpuLen / 450f).coerceIn(0f, 1f),
                            accentColor = MaterialTheme.colorScheme.primary,
                            subText = "450 mm referans kasa derinliğine göre",
                        )
                    }
                    s.maxRadiatorSizeMm?.let { radSize ->
                        MetricProgressBar(
                            title = "Maksimum Radyatör Desteği",
                            currentValue = "$radSize mm",
                            fraction = (radSize / 420f).coerceIn(0f, 1f),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            subText = "Sıvı soğutma radyatör montaj kapasitesi",
                        )
                    }
                }

                is PrebuiltSpec -> {
                    MetricProgressBar(
                        title = "Oyun Performans Skoru",
                        currentValue = "${s.gamingScore} / 100",
                        fraction = (s.gamingScore / 100f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "1080p: ${s.fps1080p} FPS • 1440p: ${s.fps1440p} FPS • 4K: ${s.fps4k} FPS",
                    )
                    val sepPrice = s.estimatedSeparatePriceTry
                    if (sepPrice != null && sepPrice > 0) {
                        val advantageFraction = (s.advantagePercent / 30.0).coerceIn(0.0, 1.0).toFloat()
                        MetricProgressBar(
                            title = "Hazır Sistem Avantaj Endeksi",
                            currentValue = "%${s.advantagePercent.toInt()} Avantajlı",
                            fraction = advantageFraction,
                            accentColor = if (s.isAdvantageous) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            subText = "Tek tek toplama (${sepPrice.toLong()} ₺) ile karşılaştırma",
                        )
                    }
                }

                is LaptopSpec -> {
                    MetricProgressBar(
                        title = "Gaming Benchmark Skoru",
                        currentValue = "${s.gamingScore} / 100",
                        fraction = (s.gamingScore / 100f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "1080p: ${s.fps1080p} FPS • 1440p: ${s.fps1440p} FPS referans oyun ortalaması",
                    )
                    MetricProgressBar(
                        title = "Grafik İşlemci Gücü (TGP)",
                        currentValue = s.gpuWattTgp,
                        fraction = 0.85f,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "${s.gpuModel} tam güç watt kapasitesi",
                    )
                    MetricProgressBar(
                        title = "Batarya & Mobilite",
                        currentValue = "${s.batteryWh} Wh • ${s.weightKg} kg",
                        fraction = (s.batteryWh / 100f).coerceIn(0f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Yüksek kapasiteli batarya hücresi",
                    )
                }

                is MouseSpec -> {
                    MetricProgressBar(
                        title = "Raporlama Hızı (Polling Rate)",
                        currentValue = "${s.pollingRateHz} Hz (${1000.0 / s.pollingRateHz} ms)",
                        fraction = (s.pollingRateHz / 8000f).coerceIn(0.12f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "8000 Hz e-spor tepe standardına göre",
                    )
                    val lightnessScore = ((120f - s.weightG) / 85f).coerceIn(0.1f, 1f)
                    MetricProgressBar(
                        title = "Ultra Hafiflik Endeksi",
                        currentValue = "${s.weightG} gram",
                        fraction = lightnessScore,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Düşük eylemsizlik ve hızlı flick kabiliyeti",
                    )
                    MetricProgressBar(
                        title = "Sensör Çözünürlüğü",
                        currentValue = "${s.maxDpi} DPI (${s.sensor})",
                        fraction = (s.maxDpi / 44000f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "Optik izleme hassasiyeti",
                    )
                }

                is KeyboardSpec -> {
                    val actuationEff = ((2.2f - s.minActuationMm.toFloat()) / 2.1f).coerceIn(0.15f, 1f)
                    MetricProgressBar(
                        title = "Tetikleme Mesafesi & Hızı",
                        currentValue = "${s.minActuationMm} mm" + if (s.rapidTriggerSupported) " (Rapid Trigger)" else "",
                        fraction = actuationEff,
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "0.1 mm Hall Effect manyetik referansına göre",
                    )
                    MetricProgressBar(
                        title = "Raporlama Hızı (Polling Rate)",
                        currentValue = "${s.pollingRateHz} Hz",
                        fraction = (s.pollingRateHz / 8000f).coerceIn(0.12f, 1f),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Tuş basım iletim gecikmesi (${1000.0 / s.pollingRateHz} ms)",
                    )
                }

                is HeadsetSpec -> {
                    MetricProgressBar(
                        title = "Sürücü Çapı & Akustik Güç",
                        currentValue = "${s.driverSizeMm} mm (${s.driverType})",
                        fraction = (s.driverSizeMm / 53f).coerceIn(0.3f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "Sahne genişliği ve bas derinliği",
                    )
                    MetricProgressBar(
                        title = "Empedans & Sürülebilirlik",
                        currentValue = "${s.impedanceOhm} Ω",
                        fraction = (s.impedanceOhm / 250f).coerceIn(0.12f, 1f),
                        accentColor = if (s.dacAmpRequired) NeonAmber else MaterialTheme.colorScheme.secondary,
                        subText = if (s.dacAmpRequired) "Harici DAC/Amfi önerilir (Stüdyo sınıfı)" else "Anakart / Dongle ile doğrudan tam güç",
                    )
                }

                is MonitorSpec -> {
                    MetricProgressBar(
                        title = "Yenileme Hızı (Akıcılık)",
                        currentValue = "${s.refreshRateHz} Hz",
                        fraction = (s.refreshRateHz / 540f).coerceIn(0.2f, 1f),
                        accentColor = MaterialTheme.colorScheme.primary,
                        subText = "540 Hz e-spor tepe referansına göre",
                    )
                    val respScore = if (s.responseSpeedGtGMs <= 0.05) 1.0f else (1f - (s.responseSpeedGtGMs.toFloat() / 5f)).coerceIn(0.2f, 0.9f)
                    MetricProgressBar(
                        title = "Griden Griye Tepki Süresi (GtG)",
                        currentValue = "${s.responseSpeedGtGMs} ms (${s.panelType})",
                        fraction = respScore,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        subText = "Ghosting ve hareket bulanıklığı direnci",
                    )
                    MetricProgressBar(
                        title = "Keskinlik & Piksel Yoğunluğu",
                        currentValue = "${s.ppi} PPI (${s.resolution})",
                        fraction = (s.ppi / 165f).coerceIn(0.3f, 1f),
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        subText = "${s.screenSizeInch}\" ekran boyutunda metin ve görüntü netliği",
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

@Composable
private fun MultiGameFpsSimulationCard(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val gameMatrix = remember(item.id) { BenchmarkEngine.resolveGameFpsForItem(item) } ?: return
    var selectedResolution by remember { mutableStateOf(GamingResolution.P1080) }
    var selectedCategoryFilter by remember { mutableStateOf<GameCategory?>(null) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎮", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = LocalAppStrings.current.detailMultiGameFps,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Rekabetçi E-Spor (5) ve Ağır AAA (7) Test Katsayıları",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

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
                    null to "Tümü (12)",
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

            visibleEntries.forEach { game ->
                val avgFpsVal = game.fps.avgFps(selectedResolution)
                val low1FpsVal = game.fps.low1PctFps(selectedResolution)
                val fpsColor = when {
                    avgFpsVal >= 120 -> NeonEmerald
                    avgFpsVal >= 60 -> NeonCyan
                    else -> NeonAmber
                }
                val barCeiling = if (game.category == GameCategory.ESPORTS) 540f else 180f
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
        }
    }
}

@Composable
private fun PolymorphicTechnicalSpecsSection(
    item: HardwareItem,
    modifier: Modifier = Modifier,
) {
    val rows = buildPolymorphicSpecRows(item)

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
                        text = LocalAppStrings.current.detailTechSpecs,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                DetailBadge(
                    text = item.category.displayName,
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
                        color = if (value == "Veri Bulunamadı" || value == "Belirtilmedi") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1.3f),
                    )
                }
            }
        }
    }
}

private const val UNSPECIFIED_TEXT = "Belirtilmedi"

private fun buildPolymorphicSpecRows(item: HardwareItem): List<Pair<String, String>> {
    val rows = mutableListOf<Pair<String, String>>()
    rows.add("Marka" to item.brand)
    rows.add("Model Kodu" to item.model)

    when (val s = item.spec) {
        is CpuSpec -> {
            rows.add("Soket" to s.socket.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Çekirdek & İzlek Dizilimi" to (s.coreTopology ?: "${s.coreCount} Çekirdek / ${s.threadCount} İzlek"))
            val boostText = s.boostClock?.let { "${it.mhz / 1000f} GHz" } ?: UNSPECIFIED_TEXT
            rows.add("Temel / Boost Frekans" to "${s.baseClock.mhz / 1000f} GHz / $boostText")
            val cacheText = listOfNotNull(s.l2CacheMb?.let { "${it} MB L2" }, s.l3CacheMb?.let { "${it} MB L3" }).joinToString(" • ")
            rows.add("L2 ve L3 Önbellek" to cacheText.ifBlank { UNSPECIFIED_TEXT })
            rows.add("TDP ve Güç Limitleri" to (s.powerLimits ?: "${s.tdp.watts} W TDP"))
            rows.add("Mimari & Üretim Nodu" to listOfNotNull(s.architecture, s.processNode).joinToString(" • ").ifBlank { UNSPECIFIED_TEXT })
            rows.add("Bellek Kontrolcü Desteği" to (s.maxMemorySpeed ?: UNSPECIFIED_TEXT))
            rows.add("PCIe Sürümü & Hat Sayısı" to (s.pcieVersion ?: UNSPECIFIED_TEXT))
            rows.add("Dahili Grafik (iGPU)" to (s.igpuModel ?: if (s.hasIntegratedGraphics) "Mevcut (Dahili Grafik)" else "Yok (Harici GPU Gerekli)"))
            rows.add("Çarpan Kilidi (Overclock)" to if (s.unlockedMultiplier == true) "Açık (Unlocked)" else if (s.unlockedMultiplier == false) "Kapalı (Locked)" else UNSPECIFIED_TEXT)
        }

        is GpuSpec -> {
            rows.add("Grafik İşlemci & Mimari" to listOfNotNull(s.chipset, s.architecture).joinToString(" • ").ifBlank { UNSPECIFIED_TEXT })
            rows.add("Üretim Teknolojisi & Transistör" to (s.processNode ?: UNSPECIFIED_TEXT))
            val coresText = listOfNotNull(s.cudaCores?.let { "$it Çekirdek" }, s.rtCores?.let { "$it RT" }, s.tensorAiTops).joinToString(" • ")
            rows.add("Gölgelendirici / RT / Tensor" to coresText.ifBlank { UNSPECIFIED_TEXT })
            val vramSpeed = s.memorySpeedGbps?.let { " • $it Gbps" } ?: ""
            rows.add("VRAM Kapasitesi & Tipi" to "${s.vram.megabytes / 1024} GB ${s.memoryType}$vramSpeed")
            val busBw = listOfNotNull(s.busWidthBits?.let { "$it-Bit" }, s.bandwidthGbs?.let { "$it GB/s" }).joinToString(" • ")
            rows.add("Bellek Veri Yolu & Bant Genişliği" to busBw.ifBlank { UNSPECIFIED_TEXT })
            val boostText = s.boostClock?.let { "${it.mhz} MHz" } ?: UNSPECIFIED_TEXT
            rows.add("Çekirdek Frekansı (Temel/Boost)" to "${s.coreClock.mhz} MHz / $boostText")
            rows.add("TGP (TDP) / Önerilen PSU" to "${s.tdp.watts} W / ${s.recommendedPsu.watts} W PSU")
            rows.add("Güç Girişleri" to (s.powerConnectors ?: UNSPECIFIED_TEXT))
            rows.add("Kart Boyutları & Slot Kalınlığı" to listOfNotNull(s.lengthMm?.let { "$it mm" }, s.slotWidth).joinToString(" • ").ifBlank { UNSPECIFIED_TEXT })
            rows.add("Görüntü Çıkışları" to (s.displayOutputs ?: UNSPECIFIED_TEXT))
            rows.add("Parça Numarası (Part Number)" to (s.partNumber ?: item.model))
        }

        is MotherboardSpec -> {
            rows.add("Soket Tipi & Yonga Seti" to "${s.socket} • ${s.chipset}")
            rows.add("VRM Güç Faz Dizilimi" to (s.vrmPhases ?: UNSPECIFIED_TEXT))
            rows.add("PCB Katmanı & Bakır Standardı" to (s.pcbLayers ?: UNSPECIFIED_TEXT))
            rows.add("Form Faktörü" to s.formFactor.displayName)
            val maxMemLabel = s.maxMemoryGb?.let { ", Maks. $it GB" } ?: ""
            rows.add("Bellek Desteği & Hız Aşırtma" to (s.memorySpeedOc ?: "${s.memoryGeneration.displayName} (${s.ramSlots} Slot$maxMemLabel)"))
            rows.add("M.2 NVMe Yuva Detayı" to (s.m2SlotsDetail ?: (s.m2Slots?.let { "${it}x M.2 NVMe" } ?: UNSPECIFIED_TEXT)))
            rows.add("PCIe x16 Gen Standardı" to s.pcieGeneration.displayName)
            val netFallback = when (s.hasWifi) {
                true -> "Wi-Fi + Ethernet"
                false -> "Ethernet (Wi-Fi Yok)"
                null -> UNSPECIFIED_TEXT
            }
            rows.add("Ağ & Kablosuz Bağlantı" to (s.network ?: netFallback))
            rows.add("Ses Yonga Kodeği" to (s.audioCodec ?: UNSPECIFIED_TEXT))
            rows.add("Arka Panel I/O Çıkışları" to (s.interfacePorts ?: UNSPECIFIED_TEXT))
            rows.add("SATA Port Sayısı" to (s.sataPorts?.let { "${it}x SATA 3.0" } ?: UNSPECIFIED_TEXT))
        }

        is RamSpec -> {
            val totalGb = s.capacity.megabytes / 1024
            val perModGb = totalGb / s.moduleCount.coerceAtLeast(1)
            rows.add("Bellek Standardı & Kapasite" to "${totalGb} GB (${s.moduleCount}x${perModGb} GB Kit) ${s.generation.displayName}")
            rows.add("IC Bellek Yongası (Die Tipi)" to (s.dieType ?: UNSPECIFIED_TEXT))
            rows.add("Frekans & CL Zamanlamaları" to "${s.speed.mhz} MHz • ${s.timings ?: "CL${s.casLatency}"}")
            val latencyNs = s.firstWordLatencyNs ?: ((2000.0 * s.casLatency / s.speed.mhz.coerceAtLeast(1)) * 10.0).toInt() / 10.0
            rows.add("İlk Tepki Süresi (True Latency)" to "$latencyNs ns")
            rows.add("Çalışma Voltajı" to (s.voltage?.let { "$it V" } ?: UNSPECIFIED_TEXT))
            rows.add("Hız Aşırtma Profili" to (s.profile ?: UNSPECIFIED_TEXT))
            val coolerRgb = listOfNotNull(
                s.heatSpreader?.let { if (it) "Alüminyum Soğutucu Blok" else "Soğutucusuz" },
                s.rgb?.let { if (it) "RGB Aydınlatma" else "RGB Yok" },
            ).joinToString(" • ")
            rows.add("Soğutucu Blok / RGB" to coolerRgb.ifBlank { UNSPECIFIED_TEXT })
        }

        is StorageSpec -> {
            val totalGb = s.capacity.megabytes / 1024
            rows.add("Depolama Kapasitesi" to (if (totalGb >= 1024) "${totalGb / 1024} TB (${totalGb} GB)" else "${totalGb} GB"))
            rows.add("Kontrolcü Modeli" to (s.controller ?: UNSPECIFIED_TEXT))
            rows.add("DRAM Önbellek Yapısı" to (s.dramCache ?: (s.cacheMb?.let { "$it MB DRAM Cache" } ?: UNSPECIFIED_TEXT)))
            rows.add("NAND Flaş Standardı" to (s.nandType ?: UNSPECIFIED_TEXT))
            rows.add("Sıralı Okuma / Yazma Hızı" to "${s.sequentialReadMbps} MB/sn Okuma • ${s.sequentialWriteMbps} MB/sn Yazma")
            rows.add("Rastgele 4K IOPS" to (s.randomIops ?: UNSPECIFIED_TEXT))
            rows.add("Arayüz & Protokol" to (s.pcieGeneration?.let { "PCIe ${it.displayName} x4 NVMe" } ?: s.storageInterface.displayName))
            rows.add("Form Faktörü" to s.formFactor.displayName)
            rows.add("Dayanıklılık Ömrü (TBW)" to (s.enduranceTbw?.let { "$it TBW" } ?: UNSPECIFIED_TEXT))
        }

        is PsuSpec -> {
            rows.add("Çıkış Gücü Kapasitesi" to "${s.wattage.watts} Watt")
            rows.add("Cultists PSU Tier List" to (s.tierRating ?: UNSPECIFIED_TEXT))
            val effText = listOfNotNull(s.efficiency.displayName, s.cybeneticsCert).joinToString(" • ")
            rows.add("Cybenetics & 80 PLUS Sertifikası" to effText)
            rows.add("Cybenetics Lambda Gürültü Derecesi" to (s.cybeneticsLambda ?: UNSPECIFIED_TEXT))
            rows.add("ATX Standardı & Geçici Yük" to (s.atxStandard ?: if (s.supportsPcie5Power) "ATX 3.0 / PCIe 5.0" else UNSPECIFIED_TEXT))
            rows.add("12V-2x6 / 12VHPWR (600W) Çıkış" to (s.pcie12Vhpwr ?: if (s.supportsPcie5Power) "Mevcut (12VHPWR / 12V-2x6)" else "Yok"))
            rows.add("İç Topoloji & Kapasitör Standardı" to (s.topology ?: s.capacitors ?: UNSPECIFIED_TEXT))
            rows.add("Güvenlik & Koruma Devreleri" to (s.protections ?: UNSPECIFIED_TEXT))
            rows.add("Modülerlik Yapısı" to (s.modularity ?: UNSPECIFIED_TEXT))
            rows.add("Soğutma Fanı & Boyut" to (s.fanAndSize ?: UNSPECIFIED_TEXT))
            val cables = listOfNotNull(s.epsConnectors, s.pcieConnectors, s.sataConnectors).joinToString(" • ")
            rows.add("Kablo Çıkışları (EPS / PCIe / SATA)" to cables.ifBlank { UNSPECIFIED_TEXT })
        }

        is CoolerSpec -> {
            rows.add("Soğutma Tipi" to s.type.displayName)
            rows.add("Maksimum Soğutma Gücü (TDP)" to "${s.maxCoolingTdp.watts} W")
            val radOrHeight = s.radiatorDimensions ?: if (s.type == CoolerType.LIQUID) {
                s.radiatorSizeMm?.let { "${it} mm Radyatör" } ?: UNSPECIFIED_TEXT
            } else {
                s.heightMm?.let { "${it} mm Kule Tipi" } ?: UNSPECIFIED_TEXT
            }
            rows.add("Radyatör / Kule Boyutları" to radOrHeight)
            rows.add("Fan Dönüş Hızı (RPM)" to (s.fanRpm ?: UNSPECIFIED_TEXT))
            rows.add("Hava Akışı & Statik Basınç" to (s.fanAirflow ?: UNSPECIFIED_TEXT))
            rows.add("Ses Seviyesi (Gürültü)" to (s.noiseDba?.let { "$it dBA" } ?: UNSPECIFIED_TEXT))
            rows.add("Desteklenen Soketler" to (if (s.supportedSockets.isNotEmpty()) s.supportedSockets.joinToString(", ") else UNSPECIFIED_TEXT))
        }

        is CaseSpec -> {
            rows.add("Kasa Tipi / Form Faktörü" to (s.type ?: UNSPECIFIED_TEXT))
            rows.add("Maksimum GPU Uzunluğu" to (s.maxGpuLengthMm?.let { "$it mm" } ?: UNSPECIFIED_TEXT))
            rows.add("Maksimum CPU Soğutucu Yüksekliği" to (s.maxCpuCoolerHeightMm?.let { "$it mm" } ?: UNSPECIFIED_TEXT))
            rows.add("Radyatör Desteği (Üst/Ön/Yan)" to (s.radiatorSupport ?: (s.maxRadiatorSizeMm?.let { "${it} mm" } ?: UNSPECIFIED_TEXT)))
            rows.add("Dahili Fan Dizilimi" to (s.includedFans ?: UNSPECIFIED_TEXT))
            rows.add("Ön Panel Bağlantıları (Tip-C)" to (s.frontPanelUsb ?: UNSPECIFIED_TEXT))
            val sideDrive = listOfNotNull(s.sidePanel, s.driveBays).joinToString(" • ")
            rows.add("Yan Panel & Disk Yuvaları" to sideDrive.ifBlank { UNSPECIFIED_TEXT })
            val mbSupport = if (s.supportedMotherboardFormFactors.isNotEmpty()) {
                s.supportedMotherboardFormFactors.joinToString(", ") { it.displayName }
            } else {
                UNSPECIFIED_TEXT
            }
            rows.add("Desteklenen Anakart Tipleri" to mbSupport)
        }

        is PrebuiltSpec -> {
            rows.add("Satıcı Mağaza" to s.storeName.ifBlank { UNSPECIFIED_TEXT })
            rows.add("İşlemci (CPU)" to s.cpuModel.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Ekran Kartı (GPU)" to s.gpuModel.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Anakart Modeli" to s.motherboardModel.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Sistem Belleği (RAM)" to s.ramSpec.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Depolama Birimi (SSD)" to s.storageSpec.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Parça Parça Toplama Fiyatı" to (s.estimatedSeparatePriceTry?.let { if (it > 0) "${it.formatPrice()} ₺" else null } ?: UNSPECIFIED_TEXT))
            rows.add("Hazır Sistem Kâr/Avantaj Tutarı" to (s.priceAdvantageTry?.let { adv ->
                val sign = if (adv > 0) "+" else ""
                "$sign${adv.formatPrice()} ₺ (%${s.advantagePercent.toInt()})"
            } ?: UNSPECIFIED_TEXT))
            rows.add("1080p / 1440p / 4K Referans FPS" to "${s.fps1080p} FPS / ${s.fps1440p} FPS / ${s.fps4k} FPS")
            rows.add("Gaming Performans Skoru" to "${s.gamingScore} / 100")
        }

        is LaptopSpec -> {
            rows.add("Cihaz Markası" to s.brand.ifBlank { item.brand })
            rows.add("Grafik Kartı & Gerçek TGP Gücü" to "${s.gpuModel} (${s.gpuWattTgp.ifBlank { UNSPECIFIED_TEXT }})")
            rows.add("Grafik Anahtarı (MUX Switch)" to s.muxSwitch.ifBlank { UNSPECIFIED_TEXT })
            rows.add("İşlemci (CPU)" to s.cpuModel.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Ekran & Panel Standardı" to s.displaySpec.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Bellek Konfigürasyonu (RAM)" to s.ramSpec.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Yüksek Hızlı Depolama (SSD)" to s.storageSpec.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Batarya Kapasitesi" to if (s.batteryWh > 0) "${s.batteryWh} Wh" else UNSPECIFIED_TEXT)
            rows.add("Gövde Ağırlığı" to if (s.weightKg > 0.0) "${s.weightKg} kg" else UNSPECIFIED_TEXT)
            rows.add("Termal Soğutma Mimarisi" to s.coolingTech.ifBlank { UNSPECIFIED_TEXT })
            rows.add("1080p / 1440p Referans FPS" to "${s.fps1080p} FPS / ${s.fps1440p} FPS")
            rows.add("Gaming Skoru" to "${s.gamingScore} / 100")
        }

        is MouseSpec -> {
            rows.add("Optik Sensör Kodu" to s.sensor.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Maksimum Çözünürlük (DPI)" to "${s.maxDpi} DPI")
            rows.add("Raporlama Hızı (Polling Rate)" to "${s.pollingRateHz} Hz (${1000.0 / s.pollingRateHz.coerceAtLeast(1)} ms)")
            rows.add("Anahtar (Switch) Tipi" to s.switchType.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Gövde Ağırlığı" to "${s.weightG} gram")
            rows.add("MCU / Kablosuz Denetleyici" to s.mcu.ifBlank { "Nordic 52840" })
            rows.add("Tıklama Gecikmesi (Click Latency)" to "${s.clickLatencyMs} ms")
            rows.add("Ergonomi / Kasa Formu" to s.shapeType.ifBlank { "Simetrik" })
            rows.add("Bağlantı Teknolojisi" to s.connectivity.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Batarya Kullanım Süresi" to (s.batteryHours?.let { "$it Saat" } ?: "Kablolu (Sınırsız)"))
        }

        is KeyboardSpec -> {
            rows.add("Anahtar (Switch) Mimarisi" to "${s.switchType}${if (s.isHallEffect) " (Manyetik Hall Effect)" else ""}")
            rows.add("Rapid Trigger Desteği" to if (s.rapidTriggerSupported) "Var (Dinamik Hızlı Tetikleme)" else "Standart Sabit Sıfırlama")
            rows.add("Tetikleme Ayar Aralığı" to "${s.minActuationMm} mm - ${s.maxActuationMm} mm")
            rows.add("Raporlama Hızı (Polling Rate)" to "${s.pollingRateHz} Hz")
            rows.add("Tuş Dizilimi (Form Faktörü)" to s.layout.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Hot-Swap (Lehimsiz Değişim)" to if (s.hotSwap) "Destekleniyor" else "Sabit")
            rows.add("Kasa & Plaka Materyali" to s.caseMaterial.ifBlank { "Alüminyum CNC / ABS" })
            rows.add("Bağlantı Tipi" to s.connectivity.ifBlank { UNSPECIFIED_TEXT })
        }

        is HeadsetSpec -> {
            rows.add("Sürücü (Driver) Mimarisi" to s.driverType.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Sürücü Çapı" to "${s.driverSizeMm} mm")
            rows.add("Empedans (Direnç)" to "${s.impedanceOhm} Ω (Ohm)")
            rows.add("Frekans Tepki Aralığı" to s.frequencyResponse.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Harici DAC / Amfi İhtiyacı" to if (s.dacAmpRequired) "Önerilir (Yüksek Empedans)" else "Gerekmez (Doğrudan Besleme)")
            rows.add("Mikrofon Tipi" to s.micType.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Ağırlık" to "${s.weightG} gram")
            rows.add("Bağlantı Seçenekleri" to s.connectivity.ifBlank { "2.4GHz / Bluetooth / 3.5mm" })
            rows.add("Akustik İzolasyon" to s.soundIsolation.ifBlank { "Pasif İzolasyon" })
        }

        is MonitorSpec -> {
            rows.add("Panel Teknolojisi" to s.panelType.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Gerçek Panel Bit Derinliği" to s.bitDepth.ifBlank { "8-bit + FRC" })
            rows.add("Ekran Boyutu" to "${s.screenSizeInch} inç")
            rows.add("Çözünürlük" to s.resolution.ifBlank { UNSPECIFIED_TEXT })
            rows.add("Yenileme Hızı (Refresh Rate)" to "${s.refreshRateHz} Hz")
            rows.add("Tepki Süresi" to "${s.responseSpeedGtGMs} ms (GtG) • ${s.responseMprtMs ?: 0.5} ms (MPRT)")
            rows.add("Parlaklık Düzeyi (SDR / HDR)" to "${s.brightnessSdrNit} Nit / ${s.brightnessHdrNit} Nit")
            rows.add("Renk Gamutu Kapsamı" to s.colorGamut.ifBlank { "%100 sRGB" })
            rows.add("Değişken Tazeleme Hızı (VRR)" to s.vrrSupport.ifBlank { "G-Sync Compatible / FreeSync Premium" })
            rows.add("Piksel Yoğunluğu (PPI)" to "${s.ppi} PPI")
            rows.add("HDR Sertifikası" to s.hdrCertification.ifBlank { UNSPECIFIED_TEXT })
        }
    }
    return rows
}

@Composable
private fun DetailBottomBar(
    isAddedToBuild: Boolean,
    priceText: String,
    onAddToBuildClick: () -> Unit,
    onAddToVsArenaClick: () -> Unit = {},
    onPriceOverrideClick: () -> Unit = {},
    isPrebuilt: Boolean = false,
    isLaptop: Boolean = false,
    isCustomPrice: Boolean = false,
    customPriceNote: String? = null,
    modifier: Modifier = Modifier,
) {
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
                                    text = if (isPrebuilt) "Sisteme Aktarıldı ✓" else "Sihirbaza Eklendi ✓",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            } else {
                                PlusIconCanvas(color = MaterialTheme.colorScheme.onPrimary)
                                Spacer(modifier = Modifier.width(Spacing.small))
                                Text(
                                    text = if (isPrebuilt) "Sihirbaza Aktar" else "Sihirbaza Ekle",
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
    item: HardwareItem,
    spec: PrebuiltSpec,
    onTransferToWizard: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = "AKILLI KÂR / ZARAR & TOPLAMA ANALİZİ",
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
                                text = "Parçaları Tek Tek Toplama Tutarı",
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
                                    text = "+${spec.priceAdvantageTry.toLong()} ₺ (%${spec.advantagePercent.toInt()} Kâr)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = badgeTextColor,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.small))

                    val statusMsg = if (spec.priceAdvantageTry != null && spec.priceAdvantageTry > 0.0) {
                        "✓ Bu hazır sistem, parçaların piyasa tekil satış fiyatına kıyasla ${spec.priceAdvantageTry.toLong()} ₺ daha uygundur."
                    } else {
                        "Bileşenlerin piyasa fiyatı hazır sistem tutarıyla dengeli durumdadır."
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
                text = "İçindeki 6 Ana Donanım Bileşeni",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Spacing.small))

            val components = listOf(
                Triple("⚡", "İşlemci (CPU)", spec.cpuModel),
                Triple("🎮", "Ekran Kartı (GPU)", spec.gpuModel),
                Triple("🔌", "Anakart", spec.motherboardModel),
                Triple("🧠", "Bellek (RAM)", spec.ramSpec),
                Triple("💾", "Depolama (SSD)", spec.storageSpec),
                Triple("🖥️", "Kasa & Güç Kaynağı", spec.psuCaseSpec),
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
                    text = "Bu Sistemi Sihirbaza Aktar & Özelleştir",
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            text = "GERÇEK GPU TGP GÜÇ DEĞERİ",
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
                            text = "Kısıtlanmamış tam güç (Full Wattage) grafik yongası • ${spec.muxSwitch}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // İçindeki 6 Ana Donanım Bileşeni (OEM Hazır Sistem Mimarisinde)
            Text(
                text = "İçindeki 6 Ana Donanım Bileşeni",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Spacing.small))

            val laptopComponents = listOf(
                LaptopComponentItem(
                    icon = "⚡",
                    title = "İşlemci (CPU)",
                    mainText = spec.cpuModel,
                    subText = "${spec.effectiveCpuCores} • ${spec.effectiveCpuClockGhz}",
                ),
                LaptopComponentItem(
                    icon = "🎮",
                    title = "Ekran Kartı (GPU & Güç)",
                    mainText = "${spec.gpuModel} (${spec.gpuWattTgp})",
                    subText = "${spec.muxSwitch} Desteği",
                ),
                LaptopComponentItem(
                    icon = "🖥️",
                    title = "Ekran (Panel)",
                    mainText = spec.effectivePanelDetails,
                    subText = "Yüksek Yenileme Hızı & Düşük Gecikmeli Espor Paneli",
                ),
                LaptopComponentItem(
                    icon = "🧠",
                    title = "Bellek (RAM)",
                    mainText = spec.ramSpec,
                    subText = spec.effectiveRamSlots,
                ),
                LaptopComponentItem(
                    icon = "💾",
                    title = "Depolama (SSD)",
                    mainText = spec.storageSpec,
                    subText = spec.effectiveStorageSlots,
                ),
                LaptopComponentItem(
                    icon = "🔋",
                    title = "Gövde, Pil & Soğutma",
                    mainText = "${spec.batteryWh} Wh Batarya • ${spec.effectiveChargerWatt} • ${spec.weightKg} kg",
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
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
        )
    }
}

@Composable
private fun BackArrowCanvas(color: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
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
private fun CheckMarkCanvas(color: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
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
private fun PlusIconCanvas(color: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
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
                        text = if (offers.isNotEmpty()) "${offers.size} Doğrulanmış" else "Canlı Arama",
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
                text = "TÜRKİYE MAĞAZALARINDA CANLI FİYAT ARA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(Spacing.extraSmall))
            Text(
                text = "Tek tıkla mağazada anlık stok ve fiyat aratın:",
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
                                        text = "Ara ↗",
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
                        contentDescription = "$displayName - Açı ${page + 1}",
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
            text = category.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = badgeText,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Görsel Doğrulanıyor / Eşleştirilmedi",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = badgeText.copy(alpha = 0.8f),
        )
    }
}



