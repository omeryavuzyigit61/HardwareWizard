package com.panda.myapplication.presentation.hardware_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.CircularProgressIndicator
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HardwareSpec
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.Wattage
import androidx.compose.animation.core.tween
import androidx.compose.material3.IconButton
import com.panda.myapplication.presentation.components.PriceOverrideDialog
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.HardwareWizardTheme
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.theme.TextMuted
import com.panda.myapplication.presentation.theme.TextPrimary
import com.panda.myapplication.presentation.theme.TextSecondary
import org.koin.compose.viewmodel.koinViewModel

object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val cardRadius = 16.dp
}

@Composable
fun HardwareListRoute(
    modifier: Modifier = Modifier,
    initialCategory: HardwareCategory? = null,
    viewModel: HardwareListViewModel = koinViewModel(),
    onItemClick: ((HardwareItem) -> Unit)? = null,
    onNavigateToVsArena: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            viewModel.onEvent(HardwareListUiEvent.OnCategorySelected(initialCategory))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HardwareListEffect.NavigateToDetail -> {
                    onItemClick?.invoke(effect.item)
                }
                is HardwareListEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(message = effect.message)
                }
                is HardwareListEffect.ShowVsAddedToast -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = if (onNavigateToVsArena != null) "Arenaya Git" else null,
                        duration = androidx.compose.material3.SnackbarDuration.Short,
                    )
                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        onNavigateToVsArena?.invoke()
                    }
                }
            }
        }
    }

    HardwareListContent(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onOpenDrawer = onOpenDrawer,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareListContent(
    uiState: HardwareListUiState,
    onEvent: (HardwareListUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onSearchQueryChange: (String) -> Unit = { onEvent(HardwareListUiEvent.OnSearchQueryChanged(it)) },
    onOpenDrawer: (() -> Unit)? = null,
    snackbarHostState: androidx.compose.material3.SnackbarHostState = remember { androidx.compose.material3.SnackbarHostState() },
) {
    val listState = rememberLazyListState()
    var priceDialogItem by remember { mutableStateOf<HardwareItem?>(null) }
    val strings = LocalAppStrings.current
    val currency = LocalAppCurrency.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            androidx.compose.material3.SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (onOpenDrawer != null) {
                            Surface(
                                onClick = onOpenDrawer,
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                            ) {
                                Text(
                                    text = "☰",
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "HARDWARE WIZARD",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            val subtitleText = when {
                                uiState.isLoading && uiState.items.isEmpty() -> "Donanım kataloğu hazırlanıyor..."
                                uiState.totalCount > 0 && uiState.items.size < uiState.totalCount -> "${uiState.items.size} / ${uiState.totalCount} donanım parçası listeleniyor"
                                uiState.totalCount > 0 -> "${uiState.totalCount} donanım parçası listeleniyor"
                                uiState.errorMessage != null -> "Katalog yüklenemedi"
                                else -> "Donanım parçası bulunamadı"
                            }
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                var localSearchQuery by remember { mutableStateOf(uiState.searchQuery) }
                LaunchedEffect(uiState.searchQuery) {
                    if (uiState.searchQuery.isEmpty() && localSearchQuery.isNotEmpty()) {
                        localSearchQuery = ""
                    }
                }

                OutlinedTextField(
                    value = localSearchQuery,
                    onValueChange = {
                        localSearchQuery = it
                        onSearchQueryChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = strings.searchPlaceholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingIcon = {
                        Text(text = "🔍", fontSize = 15.sp)
                    },
                    trailingIcon = {
                        if (localSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    localSearchQuery = ""
                                    onSearchQueryChange("")
                                },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Text(
                                    text = "✕",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                // Fluid Organic Segmented Tab: [ 🧩 Bireysel | 🖱️ Çevre | 🖥️ Hazır Sistem | 💻 Laptop ]
                val currentMode = uiState.segmentMode
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.small),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val tabs = listOf(
                            CatalogSegmentMode.INDIVIDUAL to ("🧩" to strings.segmentIndividual),
                            CatalogSegmentMode.PERIPHERALS to ("🖱️" to strings.segmentPeripherals),
                            CatalogSegmentMode.PREBUILT to ("🖥️" to strings.segmentPrebuilt),
                            CatalogSegmentMode.LAPTOP to ("💻" to strings.segmentLaptop),
                        )
                        tabs.forEach { (mode, pair) ->
                            val (emoji, title) = pair
                            val isSelected = currentMode == mode
                            val animatedBg by animateColorAsState(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent,
                                animationSpec = tween(durationMillis = 200),
                            )
                            val animatedBorderColor by animateColorAsState(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else Color.Transparent,
                                animationSpec = tween(durationMillis = 200),
                            )

                            Surface(
                                onClick = {
                                    onEvent(HardwareListUiEvent.OnSegmentModeSelected(mode))
                                },
                                shape = RoundedCornerShape(9.dp),
                                color = animatedBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) animatedBorderColor else Color.Transparent,
                                ),
                                modifier = Modifier.weight(1f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(text = emoji, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Filter Sheet Trigger Button
                    val isFilterActive = uiState.filters.isActive
                    Surface(
                        onClick = { onEvent(HardwareListUiEvent.OnToggleFilterSheet) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isFilterActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            if (isFilterActive) 1.2.dp else 1.dp,
                            if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier.padding(end = Spacing.small),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Text(text = "🎛️", fontSize = 12.sp)
                            Text(
                                text = strings.filterButton,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                            if (isFilterActive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NeonEmerald),
                                )
                            }
                        }
                    }

                    // Category Chips (in Bireysel or Çevre Birimi mode) or Brand/Store Chips (in OEM / Laptop mode)
                    when (currentMode) {
                        CatalogSegmentMode.INDIVIDUAL -> CategoryFilterChips(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { onEvent(HardwareListUiEvent.OnCategorySelected(it)) },
                            modifier = Modifier.weight(1f),
                        )
                        CatalogSegmentMode.PERIPHERALS -> PeripheralCategoryChips(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { onEvent(HardwareListUiEvent.OnCategorySelected(it)) },
                            modifier = Modifier.weight(1f),
                        )
                        CatalogSegmentMode.PREBUILT, CatalogSegmentMode.LAPTOP -> BrandFilterChips(
                            availableBrands = uiState.availableBrands,
                            selectedBrands = uiState.filters.selectedBrands,
                            onBrandToggled = { brand ->
                                val updated = if (uiState.filters.selectedBrands.contains(brand)) {
                                    uiState.filters.selectedBrands - brand
                                } else {
                                    uiState.filters.selectedBrands + brand
                                }
                                onEvent(HardwareListUiEvent.OnFiltersChanged(uiState.filters.copy(selectedBrands = updated)))
                            },
                            onClearBrands = {
                                onEvent(HardwareListUiEvent.OnFiltersChanged(uiState.filters.copy(selectedBrands = emptySet())))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Spacing.large)
                            .fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(Spacing.cardRadius),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.large),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(44.dp),
                            )
                            Spacer(modifier = Modifier.height(Spacing.large))
                            Text(
                                text = "HARDWARE WIZARD",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp,
                            )
                            Spacer(modifier = Modifier.height(Spacing.small))
                            Text(
                                text = "Donanım kataloğu ve canlı TR mağaza verileri hazırlanıyor...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(Spacing.medium))
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                }
                uiState.errorMessage != null && uiState.items.isEmpty() -> {
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
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Button(
                            onClick = { onEvent(HardwareListUiEvent.OnRefresh) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        ) {
                            Text("Tekrar Dene", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
                uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Kriterlere uygun donanım bulunamadı.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (uiState.filters.isActive) {
                            Spacer(modifier = Modifier.height(Spacing.small))
                            TextButton(onClick = { onEvent(HardwareListUiEvent.OnResetFilters) }) {
                                Text("Filtreleri Temizle", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.medium,
                            end = Spacing.medium,
                            bottom = Spacing.large,
                            top = Spacing.small,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        items(
                            items = uiState.items,
                            key = { it.id.value },
                        ) { item ->
                            HardwareCard(
                                item = item,
                                onClick = { onEvent(HardwareListUiEvent.OnHardwareItemClicked(item)) },
                                onAddToVsClick = { onEvent(HardwareListUiEvent.OnAddToVsArena(item)) },
                                onPriceOverrideClick = { priceDialogItem = item },
                            )
                        }
                    }
                }
            }
        }
    }

    priceDialogItem?.let { dialogItem ->
        PriceOverrideDialog(
            item = dialogItem,
            onDismiss = { priceDialogItem = null },
            onSaveCustomPrice = { customPrice, note ->
                onEvent(HardwareListUiEvent.OnSaveCustomPrice(dialogItem.id, customPrice, note))
                priceDialogItem = null
            },
            onClearCustomPrice = {
                onEvent(HardwareListUiEvent.OnClearCustomPrice(dialogItem.id))
                priceDialogItem = null
            },
        )
    }

    if (uiState.isFilterSheetVisible) {
        HardwareFilterBottomSheet(
            filters = uiState.filters,
            selectedCategory = uiState.selectedCategory,
            availableBrands = uiState.availableBrands,
            onDismiss = { onEvent(HardwareListUiEvent.OnToggleFilterSheet) },
            onFiltersChanged = { onEvent(HardwareListUiEvent.OnFiltersChanged(it)) },
            onResetFilters = { onEvent(HardwareListUiEvent.OnResetFilters) },
        )
    }
}

@Composable
fun FluidFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    accentColor: Color = NeonCyan,
) {
    val animatedBg by animateColorAsState(
        if (selected) accentColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 200),
    )
    val animatedBorderColor by animateColorAsState(
        if (selected) accentColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 200),
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = animatedBg,
        border = BorderStroke(
            if (selected) 1.2.dp else 1.dp,
            animatedBorderColor,
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (icon != null) {
                Text(text = icon, fontSize = 12.sp)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
fun CategoryFilterChips(
    selectedCategory: HardwareCategory?,
    onCategorySelected: (HardwareCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf(
        null to ("✨" to "Tümü"),
        HardwareCategory.CPU to ("⚡" to "CPU"),
        HardwareCategory.GPU to ("🎮" to "GPU"),
        HardwareCategory.RAM to ("🧠" to "RAM"),
        HardwareCategory.MOTHERBOARD to ("🖲️" to "Anakart"),
        HardwareCategory.PSU to ("🔌" to "PSU"),
        HardwareCategory.STORAGE to ("💾" to "Depolama"),
        HardwareCategory.COOLER to ("❄️" to "Soğutucu"),
        HardwareCategory.CASE to ("📦" to "Kasa"),
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        categories.forEach { (cat, pair) ->
            val (icon, label) = pair
            val isSelected = selectedCategory == cat
            FluidFilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat) },
                label = label,
                icon = icon,
                accentColor = NeonCyan,
            )
        }
    }
}

@Composable
fun PeripheralCategoryChips(
    selectedCategory: HardwareCategory?,
    onCategorySelected: (HardwareCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories: List<Pair<HardwareCategory?, Pair<String, String>>> = listOf(
        null to ("✨" to "Tümü"),
        HardwareCategory.MONITOR to ("🖥️" to "Monitör"),
        HardwareCategory.MOUSE to ("🖱️" to "Mouse"),
        HardwareCategory.KEYBOARD to ("⌨️" to "Klavye"),
        HardwareCategory.HEADSET to ("🎧" to "Kulaklık"),
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        categories.forEach { (cat, pair) ->
            val (icon, label) = pair
            val isSelected = selectedCategory == cat
            FluidFilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat) },
                label = label,
                icon = icon,
                accentColor = NeonCyan,
            )
        }
    }
}

@Composable
fun BrandFilterChips(
    availableBrands: List<String>,
    selectedBrands: Set<String>,
    onBrandToggled: (String) -> Unit,
    onClearBrands: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        val isAllSelected = selectedBrands.isEmpty()
        FluidFilterChip(
            selected = isAllSelected,
            onClick = onClearBrands,
            label = "Tümü",
            icon = "✨",
            accentColor = NeonCyan,
        )

        availableBrands.forEach { brand ->
            val isSelected = selectedBrands.contains(brand)
            FluidFilterChip(
                selected = isSelected,
                onClick = { onBrandToggled(brand) },
                label = brand,
                accentColor = NeonCyan,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HardwareCard(
    item: HardwareItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddToVsClick: (() -> Unit)? = null,
    onPriceOverrideClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(Spacing.cardRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (item.isCustomPrice) NeonAmber.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 1. SOL BLOK (Görsel - 72x72dp Sabit)
            HardwareThumbnail(
                imageUrl = item.effectiveImageUrl,
                category = item.category,
                displayName = item.displayName,
            )

            // 2. ORTA BLOK (Esnek Başlık, Rozetler & Alt Detay - weight(1f), minWidth: 0dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 0.dp)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CategoryBadge(category = item.category)
                    if (item.isCustomPrice) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = "Özel Fiyat",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                    Text(
                        text = item.brand,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }

                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                when (val spec = item.spec) {
                    is PrebuiltSpec -> {
                        if ((spec.priceAdvantageTry != null && spec.priceAdvantageTry > 0.0) ||
                            (spec.estimatedSeparatePriceTry != null && spec.estimatedSeparatePriceTry > 0.0)
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (spec.priceAdvantageTry != null && spec.priceAdvantageTry > 0.0) {
                                    val isAdv = spec.isAdvantageous
                                    val pillBg = if (isAdv) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                    val pillText = if (isAdv) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(pillBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = "+${spec.priceAdvantageTry.toLong()} ₺ Kâr",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = pillText,
                                            softWrap = false,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                if (spec.estimatedSeparatePriceTry != null && spec.estimatedSeparatePriceTry > 0.0) {
                                    Text(
                                        text = "Ayrı: ${spec.estimatedSeparatePriceTry.toLong()} ₺",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        softWrap = false,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${spec.cpuModel} • ${spec.gpuModel} • ${spec.ramSpec}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    is LaptopSpec -> {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (spec.gpuWattTgp.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = spec.gpuWattTgp,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            if (spec.muxSwitch.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = spec.muxSwitch,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${spec.cpuModel} • ${spec.displaySpec}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    else -> {
                        Text(
                            text = formatSpecSummary(item.spec),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // 3. SAĞ BLOK (Fiyat ve Aksiyon - minWidth: 96dp, wrapContentWidth(Alignment.End))
            Column(
                modifier = Modifier
                    .widthIn(min = 96.dp)
                    .wrapContentWidth(Alignment.End),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val cardCurrency = LocalAppCurrency.current
                val cardStrings = LocalAppStrings.current
                if (item.currentPriceTry != null) {
                    val priceText = item.currentPriceTry.formatPriceWithCurrency(cardCurrency, cardStrings.noPriceFallback)
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = if (item.isCustomPrice) NeonAmber else MaterialTheme.colorScheme.primary,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.End,
                    )
                } else if (onPriceOverrideClick != null) {
                    Surface(
                        onClick = onPriceOverrideClick,
                        shape = RoundedCornerShape(6.dp),
                        color = NeonAmber.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.65f)),
                    ) {
                        Text(
                            text = "🏷️ ${cardStrings.enterPrice}",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                } else {
                    Text(
                        text = cardStrings.noPriceFallback,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        textAlign = TextAlign.End,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onPriceOverrideClick != null && item.currentPriceTry != null) {
                        Surface(
                            onClick = onPriceOverrideClick,
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.isCustomPrice) NeonAmber.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            border = BorderStroke(
                                1.dp,
                                if (item.isCustomPrice) NeonAmber.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant,
                            ),
                        ) {
                            Text(
                                text = "✎",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isCustomPrice) NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }

                    if (onAddToVsClick != null) {
                        Surface(
                            onClick = onAddToVsClick,
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)),
                        ) {
                            Text(
                                text = "+ VS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HardwareThumbnail(
    imageUrl: String?,
    category: HardwareCategory,
    displayName: String,
    modifier: Modifier = Modifier,
) {
    val containerBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(shape)
            .background(containerBg)
            .border(1.dp, borderColor, shape)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        )
                    }
                },
                error = {
                    HardwarePlaceholderBadge(category = category)
                },
            )
        } else {
            HardwarePlaceholderBadge(category = category)
        }
    }
}

@Composable
fun HardwarePlaceholderBadge(
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg.copy(alpha = 0.35f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = when (category) {
                HardwareCategory.PREBUILT_PC -> "OEM PC"
                HardwareCategory.LAPTOP -> "LAPTOP"
                HardwareCategory.MOUSE -> "MOUSE"
                HardwareCategory.KEYBOARD -> "KLAVYE"
                HardwareCategory.HEADSET -> "KULAKLIK"
                HardwareCategory.MONITOR -> "MONİTÖR"
                else -> category.name
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = badgeText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun CategoryBadge(category: HardwareCategory) {
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

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeBg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = when (category) {
                HardwareCategory.PREBUILT_PC -> "OEM Sistem"
                HardwareCategory.LAPTOP -> "Laptop"
                HardwareCategory.MOUSE -> "Mouse"
                HardwareCategory.KEYBOARD -> "Klavye"
                HardwareCategory.HEADSET -> "Kulaklık"
                HardwareCategory.MONITOR -> "Monitör"
                else -> category.name
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = badgeText,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareFilterBottomSheet(
    filters: HardwareFilterOptions,
    selectedCategory: HardwareCategory?,
    availableBrands: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onFiltersChanged: (HardwareFilterOptions) -> Unit,
    onResetFilters: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Detaylı Filtreler",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                TextButton(onClick = onResetFilters) {
                    Text("Sıfırla", color = MaterialTheme.colorScheme.tertiary)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // 1. Marka Filtresi (Tüm Kategoriler İçin Çoklu Seçim)
            if (availableBrands.isNotEmpty()) {
                Text(
                    text = "MARKA",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    availableBrands.forEach { brand ->
                        val isSelected = filters.selectedBrands.contains(brand)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val next = if (isSelected) {
                                    filters.selectedBrands - brand
                                } else {
                                    filters.selectedBrands + brand
                                }
                                onFiltersChanged(filters.copy(selectedBrands = next))
                            },
                            label = { Text(brand) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            // 2. Kategoriye Özel Dinamik Sıralama
            Text(
                text = "SIRALAMA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                HardwareSortOption.optionsForCategory(selectedCategory).forEach { sort ->
                    val isSelected = filters.sortOption == sort
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFiltersChanged(filters.copy(sortOption = sort)) },
                        label = { Text(sort.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(Spacing.medium))

            // CPU Filters
            if (selectedCategory == null || selectedCategory == HardwareCategory.CPU) {
                Text(
                    text = "CPU: SOKET",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", "AM5" to "AM5", "AM4" to "AM4", "LGA1700" to "LGA1700", "LGA1851" to "LGA1851").forEach { (sock, label) ->
                        val isSelected = filters.cpuSocket == sock
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(cpuSocket = sock)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "CPU: MİNİMUM ÇEKİRDEK",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", 6 to "6+ Çekirdek", 8 to "8+ Çekirdek", 12 to "12+ Çekirdek", 16 to "16+ Çekirdek").forEach { (cores, label) ->
                        val isSelected = filters.minCoreCount == cores
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(minCoreCount = cores)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "CPU: MAKSİMUM TDP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", 65 to "≤ 65W", 105 to "≤ 105W", 125 to "≤ 125W", 170 to "≤ 170W", 250 to "≤ 250W").forEach { (tdp, label) ->
                        val isSelected = filters.maxTdpWatts == tdp
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(maxTdpWatts = tdp)) },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            // GPU Filters
            if (selectedCategory == null || selectedCategory == HardwareCategory.GPU) {
                Text(
                    text = "GPU: MİNİMUM VRAM",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", 8 to "8 GB+", 12 to "12 GB+", 16 to "16 GB+", 24 to "24 GB+").forEach { (vram, label) ->
                        val isSelected = filters.minVramGb == vram
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(minVramGb = vram)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "GPU: MARKA / ÜRETİCİ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", "NVIDIA" to "NVIDIA GeForce", "AMD" to "AMD Radeon", "Intel" to "Intel Arc").forEach { (brand, label) ->
                        val isSelected = filters.gpuBrand == brand
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(gpuBrand = brand)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "GPU: MAKSİMUM KART UZUNLUĞU",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", 240 to "≤ 240mm (ITX)", 285 to "≤ 285mm", 320 to "≤ 320mm", 360 to "≤ 360mm").forEach { (len, label) ->
                        val isSelected = filters.maxGpuLengthMm == len
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(maxGpuLengthMm = len)) },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            // RAM Filters
            if (selectedCategory == null || selectedCategory == HardwareCategory.RAM) {
                Text(
                    text = "RAM: BELLEK TİPİ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", DdrGeneration.DDR5 to "DDR5", DdrGeneration.DDR4 to "DDR4").forEach { (gen, label) ->
                        val isSelected = filters.ramGeneration == gen
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(ramGeneration = gen)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "RAM: MİNİMUM FREKANS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", 3200 to "3200 MT/s+", 3600 to "3600 MT/s+", 5600 to "5600 MT/s+", 6000 to "6000 MT/s+", 6400 to "6400 MT/s+").forEach { (spd, label) ->
                        val isSelected = filters.minRamSpeedMts == spd
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(minRamSpeedMts = spd)) },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            // Storage Filters
            if (selectedCategory == null || selectedCategory == HardwareCategory.STORAGE) {
                Text(
                    text = "DEPOLAMA: ARAYÜZ & FORM",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", StorageInterface.NVME to "M.2 NVMe", StorageInterface.SATA to "2.5\" SATA").forEach { (iface, label) ->
                        val isSelected = filters.storageInterface == iface
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(storageInterface = iface)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "DEPOLAMA: PCIE NESLİ (NVMe)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", PcieGeneration.GEN3 to "PCIe 3.0", PcieGeneration.GEN4 to "PCIe 4.0", PcieGeneration.GEN5 to "PCIe 5.0").forEach { (pgen, label) ->
                        val isSelected = filters.storagePcieGen == pgen
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(storagePcieGen = pgen)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "DEPOLAMA: MİNİMUM KAPASİTE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", 500 to "500 GB+", 1000 to "1 TB+", 2000 to "2 TB+", 4000 to "4 TB+").forEach { (cap, label) ->
                        val isSelected = filters.minStorageCapacityGb == cap
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(minStorageCapacityGb = cap)) },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            // Motherboard Filters
            if (selectedCategory == null || selectedCategory == HardwareCategory.MOTHERBOARD) {
                Text(
                    text = "ANAKART: SOKET",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", "AM5" to "AM5", "AM4" to "AM4", "LGA1700" to "LGA1700", "LGA1851" to "LGA1851").forEach { (sock, label) ->
                        val isSelected = filters.motherboardSocket == sock
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(motherboardSocket = sock)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "ANAKART: FORM FAKTÖRÜ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(
                        null to "Tümü",
                        MotherboardFormFactor.ATX to "ATX",
                        MotherboardFormFactor.MICRO_ATX to "Micro-ATX",
                        MotherboardFormFactor.MINI_ITX to "Mini-ITX",
                        MotherboardFormFactor.E_ATX to "E-ATX",
                    ).forEach { (ff, label) ->
                        val isSelected = filters.motherboardFormFactor == ff
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(motherboardFormFactor = ff)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "ANAKART: MİNİMUM M.2 YUVASI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", 1 to "1+ Yuva", 2 to "2+ Yuva", 3 to "3+ Yuva", 4 to "4+ Yuva").forEach { (m2, label) ->
                        val isSelected = filters.minM2Slots == m2
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(minM2Slots = m2)) },
                            label = { Text(label) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            // PSU Filters
            if (selectedCategory == null || selectedCategory == HardwareCategory.PSU) {
                Text(
                    text = "PSU: MİNİMUM GÜÇ KAPASİTESİ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(null to "Tümü", 550 to "550W+", 650 to "650W+", 750 to "750W+", 850 to "850W+", 1000 to "1000W+").forEach { (watt, label) ->
                        val isSelected = filters.minPsuWattage == watt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(minPsuWattage = watt)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "PSU: VERİMLİLİK SERTİFİKASI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    listOf(
                        null to "Tümü",
                        PsuEfficiencyRating.BRONZE to "80+ Bronze",
                        PsuEfficiencyRating.GOLD to "80+ Gold",
                        PsuEfficiencyRating.PLATINUM to "80+ Platinum",
                        PsuEfficiencyRating.TITANIUM to "80+ Titanium",
                    ).forEach { (eff, label) ->
                        val isSelected = filters.psuEfficiency == eff
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFiltersChanged(filters.copy(psuEfficiency = eff)) },
                            label = { Text(label) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = "PSU: MODERN STANDARTLAR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                ) {
                    FilterChip(
                        selected = filters.requireAtx3,
                        onClick = { onFiltersChanged(filters.copy(requireAtx3 = !filters.requireAtx3)) },
                        label = { Text("ATX 3.0 Standardı") },
                    )
                    FilterChip(
                        selected = filters.requirePcie5,
                        onClick = { onFiltersChanged(filters.copy(requirePcie5 = !filters.requirePcie5)) },
                        label = { Text("PCIe 5.0 (12VHPWR)") },
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(Spacing.small),
            ) {
                Text("Uygula & Kapat", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(Spacing.large))
        }
    }
}

private fun formatSpecSummary(spec: HardwareSpec): String = when (spec) {
    is CpuSpec -> "${spec.socket} • ${spec.coreCount}C/${spec.threadCount}T • ${spec.baseClock.mhz}MHz • ${spec.tdp.watts}W TDP"
    is GpuSpec -> {
        val skuSuffix = if (!spec.partNumber.isNullOrBlank()) " • SKU: ${spec.partNumber}" else ""
        "${spec.vram.megabytes / 1024}GB ${spec.memoryType} • ${spec.tdp.watts}W TDP$skuSuffix"
    }
    is RamSpec -> "${spec.generation} • ${spec.speed.mhz}MT/s • CL${spec.casLatency} • ${spec.capacity.megabytes / 1024}GB (${spec.moduleCount} modül)"
    is MotherboardSpec -> "${spec.socket} • ${spec.chipset} • ${spec.formFactor.name} • ${spec.memoryGeneration}"
    is PsuSpec -> "${spec.wattage.watts}W • ${spec.efficiency.name} • ${if (spec.isAtx3Compliant) "ATX 3.0" else "ATX"}"
    is StorageSpec -> "${spec.mediaType.name} • ${spec.capacity.megabytes / 1024}GB • ${spec.sequentialReadMbps}MB/s Okuma"
    is CoolerSpec -> "${spec.type.displayName} • Max TDP: ${spec.maxCoolingTdp.watts}W"
    is CaseSpec -> listOfNotNull(
        spec.type ?: "Mid Tower",
        spec.maxGpuLengthMm?.let { "GPU: ${it}mm" },
        spec.maxRadiatorSizeMm?.let { "Radyatör: ${it}mm" },
        spec.sidePanel,
    ).joinToString(" • ")
    is PrebuiltSpec -> "${spec.cpuModel} • ${spec.gpuModel} • ${spec.ramSpec} • ${spec.storageSpec}"
    is LaptopSpec -> "${spec.gpuModel} (${spec.gpuWattTgp}) • ${spec.muxSwitch} • ${spec.displaySpec}"
    is MouseSpec -> "${spec.sensor} • ${spec.pollingRateHz}Hz • ${spec.weightG}g • ${spec.switchType}"
    is KeyboardSpec -> "${spec.switchType} • ${if (spec.rapidTriggerSupported) "Rapid Trigger (${spec.minActuationMm}mm)" else "${spec.minActuationMm}mm"} • ${spec.layout}"
    is HeadsetSpec -> "${spec.driverType} (${spec.driverSizeMm}mm) • ${spec.impedanceOhm}Ω • ${spec.weightG}g"
    is MonitorSpec -> "${spec.screenSizeInch}\" ${spec.panelType} • ${spec.resolution} • ${spec.refreshRateHz}Hz (${spec.responseSpeedGtGMs}ms)"
}

// region Previews

private val mockHardwareList = listOf(
    HardwareItem(
        id = HardwareId("cpu_1"),
        brand = "AMD",
        model = "Ryzen 7 7800X3D",
        price = Money(34005L, "USD"),
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
    HardwareItem(
        id = HardwareId("gpu_1"),
        brand = "MSI",
        model = "GeForce RTX 4070 SUPER Ventus",
        price = Money(59999L, "USD"),
        spec = GpuSpec(
            chipset = "RTX 4070 SUPER",
            vram = MemorySize(12288),
            memoryType = "GDDR6X",
            coreClock = Frequency(1980),
            boostClock = Frequency(2505),
            tdp = Wattage(200),
            recommendedPsu = Wattage(650),
        ),
    ),
)

@Preview
@Composable
fun HardwareListContentDarkPreview() {
    HardwareWizardTheme {
        HardwareListContent(
            uiState = HardwareListUiState(
                isLoading = false,
                items = mockHardwareList,
                totalCount = 2,
                selectedCategory = null,
                searchQuery = "",
            ),
            onEvent = {},
        )
    }
}

// endregion
