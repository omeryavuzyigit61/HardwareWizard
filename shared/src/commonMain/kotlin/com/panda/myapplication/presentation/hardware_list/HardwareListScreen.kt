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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.remember
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
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.presentation.theme.HardwareWizardTheme
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
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    // One-off effect
                }
            }
        }
    }

    HardwareListContent(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onSearchQueryChange = viewModel::onSearchQueryChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareListContent(
    uiState: HardwareListUiState,
    onEvent: (HardwareListUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onSearchQueryChange: (String) -> Unit = { onEvent(HardwareListUiEvent.OnSearchQueryChanged(it)) },
) {
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
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

                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Parça adı, marka (AMD, RTX 4070, DDR5...)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(Spacing.cardRadius),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Filter Sheet Trigger Button
                    Surface(
                        onClick = { onEvent(HardwareListUiEvent.OnToggleFilterSheet) },
                        shape = RoundedCornerShape(Spacing.small),
                        color = if (uiState.filters.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (uiState.filters.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                        modifier = Modifier.padding(end = Spacing.small),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.small, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Filtreler",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.filters.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            )
                            if (uiState.filters.isActive) {
                                Spacer(modifier = Modifier.width(Spacing.extraSmall))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.secondary),
                                )
                            }
                        }
                    }

                    // Category Chips
                    CategoryFilterChips(
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = { onEvent(HardwareListUiEvent.OnCategorySelected(it)) },
                        modifier = Modifier.weight(1f),
                    )
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
                            )
                        }
                    }
                }
            }
        }
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
fun CategoryFilterChips(
    selectedCategory: HardwareCategory?,
    onCategorySelected: (HardwareCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = listOf(
        null to "Tümü",
        HardwareCategory.CPU to "CPU",
        HardwareCategory.GPU to "GPU",
        HardwareCategory.RAM to "RAM",
        HardwareCategory.MOTHERBOARD to "Anakart",
        HardwareCategory.PSU to "PSU",
        HardwareCategory.STORAGE to "Depolama",
        HardwareCategory.COOLER to "Soğutucu",
        HardwareCategory.CASE to "Kasa",
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
    ) {
        categories.forEach { (cat, label) ->
            val isSelected = selectedCategory == cat
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat) },
                label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(Spacing.small),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                ),
            )
        }
    }
}

@Composable
fun HardwareCard(
    item: HardwareItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HardwareThumbnail(
                imageUrl = item.effectiveImageUrl,
                category = item.category,
                displayName = item.displayName,
            )
            Spacer(modifier = Modifier.width(Spacing.medium))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryBadge(category = item.category)
                        Spacer(modifier = Modifier.width(Spacing.small))
                        Text(
                            text = item.brand,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    val priceText = item.currentPriceTry.formatPriceWithCurrency("Stok / Fiyat Yok")
                    Text(
                        text = priceText,
                        style = if (item.currentPriceTry != null) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelSmall,
                        fontWeight = if (item.currentPriceTry != null) FontWeight.Black else FontWeight.Normal,
                        color = if (item.currentPriceTry != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(Spacing.extraSmall))

                Text(
                    text = formatSpecSummary(item.spec),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
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
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .size(76.dp)
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
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg.copy(alpha = 0.35f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = category.name,
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
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeBg)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = category.name,
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
    is CoolerSpec -> "${spec.type.name} • Max TDP: ${spec.maxCoolingTdp.watts}W"
    is CaseSpec -> "${spec.type} • GPU: ${spec.maxGpuLengthMm}mm • Radyatör: ${spec.maxRadiatorSizeMm}mm"
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
