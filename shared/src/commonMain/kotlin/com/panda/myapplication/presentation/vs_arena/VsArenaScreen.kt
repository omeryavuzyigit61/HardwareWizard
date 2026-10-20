package com.panda.myapplication.presentation.vs_arena

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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.panda.myapplication.domain.engine.ComparisonWinner
import com.panda.myapplication.domain.engine.VsChampionSummary
import com.panda.myapplication.domain.engine.VsComparisonReport
import com.panda.myapplication.domain.engine.VsComparisonRow
import com.panda.myapplication.domain.engine.VsGameComparisonRow
import com.panda.myapplication.domain.model.GameCategory
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.VsArenaMode
import com.panda.myapplication.domain.repository.VsSlot
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GoldAccent
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.util.accentColor
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import com.panda.myapplication.presentation.util.iconEmoji
import com.panda.myapplication.presentation.util.shortBadgeText
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VsArenaRoute(
    onNavigateToDetail: (HardwareId) -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    viewModel: VsArenaViewModel = koinViewModel(),
) {
    VsArenaScreen(
        onItemClick = onNavigateToDetail,
        onOpenDrawer = onOpenDrawer,
        viewModel = viewModel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VsArenaScreen(
    onItemClick: (HardwareId) -> Unit = {},
    onOpenDrawer: (() -> Unit)? = null,
    viewModel: VsArenaViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "vs_header") {
                VsArenaHeader(
                    mode = state.mode,
                    onModeSelected = viewModel::selectMode,
                    componentCategory = state.componentCategory,
                    onComponentCategorySelected = viewModel::selectComponentCategory,
                    hasAnySelection = state.leftItem != null || state.rightItem != null,
                    onClearAll = viewModel::clearAll,
                    onOpenDrawer = onOpenDrawer,
                )
            }

            item(key = "vs_slots") {
                VsFighterSlotsCard(
                    leftItem = state.leftItem,
                    rightItem = state.rightItem,
                    activeCategory = state.activeCategory,
                    onSelectLeft = { viewModel.openPicker(VsSlot.LEFT) },
                    onSelectRight = { viewModel.openPicker(VsSlot.RIGHT) },
                    onClearLeft = { viewModel.clearSlot(VsSlot.LEFT) },
                    onClearRight = { viewModel.clearSlot(VsSlot.RIGHT) },
                    onSwap = viewModel::swapSlots,
                    onItemDetail = onItemClick,
                )
            }

            val report = state.report
            if (report != null) {
                item(key = "vs_champions") {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        VsChampionBannerCard(
                            summary = report.performanceChampion,
                            accentColor = ElectricBlue,
                            icon = "🏆",
                        )
                        VsChampionBannerCard(
                            summary = report.valueChampion,
                            accentColor = GoldAccent,
                            icon = "💎",
                        )
                    }
                }

                if (report.gameRows.isNotEmpty()) {
                    item(key = "vs_game_fps_matrix") {
                        VsGameFpsMatrixCard(
                            leftName = report.leftItem.model,
                            rightName = report.rightItem.model,
                            gameRows = report.gameRows,
                            selectedResolution = state.selectedResolution,
                            onResolutionChange = viewModel::selectResolution,
                            selectedCategoryFilter = state.selectedGameCategoryFilter,
                            onCategoryFilterChange = viewModel::selectGameCategoryFilter,
                        )
                    }
                }

                item(key = "vs_specs_section_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "⚔️ KAFA KAFAYA TEKNİK & MATEMATİKSEL KIYASLAMA",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            text = "${report.rows.size} Kriter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        )
                    }
                }

                items(
                    items = report.rows,
                    key = { "vs_row_${it.label}" },
                ) { row ->
                    VsComparisonMetricRow(
                        row = row,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            } else {
                item(key = "vs_empty_prompt") {
                    VsEmptyArenaCard(
                        mode = state.mode,
                        activeCategory = state.activeCategory,
                        suggestedPair = state.suggestedPair,
                        onApplySuggested = viewModel::applySuggestedPair,
                        onPickLeft = { viewModel.openPicker(VsSlot.LEFT) },
                        onPickRight = { viewModel.openPicker(VsSlot.RIGHT) },
                    )
                }
            }
        }

        // Searchable Product Picker Bottom Sheet
        if (state.activePickerSlot != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::closePicker,
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                VsProductPickerSheetContent(
                    slot = state.activePickerSlot!!,
                    category = state.activeCategory,
                    searchQuery = state.pickerSearchQuery,
                    onSearchChange = viewModel::updatePickerSearch,
                    items = state.pickerItems,
                    selectedId = if (state.activePickerSlot == VsSlot.LEFT) state.leftItem?.id else state.rightItem?.id,
                    onSelectItem = viewModel::selectItemForActiveSlot,
                )
            }
        }
    }
}

@Composable
private fun VsArenaHeader(
    mode: VsArenaMode,
    onModeSelected: (VsArenaMode) -> Unit,
    componentCategory: HardwareCategory,
    onComponentCategorySelected: (HardwareCategory) -> Unit,
    hasAnySelection: Boolean,
    onClearAll: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background),
                )
            )
            .statusBarsPadding()
            .padding(top = 14.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
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
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    ) {
                        Text(
                            text = "☰",
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Surface(
                            color = NeonPurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
                        ) {
                            Text(
                                text = "VS ARENA",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                            )
                        }
                        Text(
                            text = "Kafa Kafaya Kıyaslama",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = mode.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (hasAnySelection) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onClearAll),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Text(
                        text = "Sıfırla",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // 3 Comparison Modes Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VsArenaMode.entries.forEach { entry ->
                val isSelected = mode == entry
                val bgColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                )
                val borderColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                )
                val shortLabel = when (entry) {
                    VsArenaMode.COMPONENT_VS_COMPONENT -> "🧩 Bileşen vs Bileşen"
                    VsArenaMode.PREBUILT_VS_PREBUILT -> "🖥️ OEM Sistem"
                    VsArenaMode.LAPTOP_VS_LAPTOP -> "💻 Gaming Laptop"
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onModeSelected(entry) },
                    color = bgColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                ) {
                    Text(
                        text = shortLabel,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Component Category Chips when in COMPONENT_VS_COMPONENT mode
        if (mode == VsArenaMode.COMPONENT_VS_COMPONENT) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = VsArenaUiState.COMPONENT_CATEGORIES,
                    key = { it.name },
                ) { cat ->
                    val selected = cat == componentCategory
                    val accent = cat.accentColor()
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onComponentCategorySelected(cat) },
                        color = if (selected) accent.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) accent else MaterialTheme.colorScheme.outlineVariant,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(text = cat.iconEmoji(), fontSize = 13.sp)
                            Text(
                                text = cat.shortBadgeText(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VsFighterSlotsCard(
    leftItem: HardwareItem?,
    rightItem: HardwareItem?,
    activeCategory: HardwareCategory,
    onSelectLeft: () -> Unit,
    onSelectRight: () -> Unit,
    onClearLeft: () -> Unit,
    onClearRight: () -> Unit,
    onSwap: () -> Unit,
    onItemDetail: (HardwareId) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VsFighterCornerCard(
            modifier = Modifier.weight(1f),
            cornerLabel = "1. KÖŞE",
            cornerColor = ElectricBlue,
            item = leftItem,
            category = activeCategory,
            onSelect = onSelectLeft,
            onClear = onClearLeft,
            onDetail = onItemDetail,
        )

        // Central VS / Swap Badge
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(enabled = leftItem != null || rightItem != null, onClick = onSwap),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, NeonPurple),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (leftItem != null && rightItem != null) "⇄" else "VS",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        VsFighterCornerCard(
            modifier = Modifier.weight(1f),
            cornerLabel = "2. KÖŞE",
            cornerColor = NeonPurple,
            item = rightItem,
            category = activeCategory,
            onSelect = onSelectRight,
            onClear = onClearRight,
            onDetail = onItemDetail,
        )
    }
}

@Composable
private fun VsFighterCornerCard(
    modifier: Modifier = Modifier,
    cornerLabel: String,
    cornerColor: Color,
    item: HardwareItem?,
    category: HardwareCategory,
    onSelect: () -> Unit,
    onClear: () -> Unit,
    onDetail: (HardwareId) -> Unit,
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (item != null) cornerColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = cornerColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = cornerLabel,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = cornerColor,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                if (item != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Detay",
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onDetail(item.id) }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "✕",
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(onClick = onClear)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (item == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(text = category.iconEmoji(), fontSize = 24.sp)
                        Text(
                            text = "+ ${category.shortBadgeText()} Seç",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Listeden eklemek için dokun",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            fontSize = 10.sp,
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!item.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.displayName,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Text(text = category.iconEmoji(), fontSize = 28.sp)
                    }
                }

                Text(
                    text = item.brand.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = cornerColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = item.model,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                    val arenaCurr = LocalAppCurrency.current
                Text(text = item.currentPriceTry.formatPriceWithCurrency(arenaCurr),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.currentPriceTry != null) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    fontWeight = FontWeight.ExtraBold,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun VsChampionBannerCard(
    summary: VsChampionSummary,
    accentColor: Color,
    icon: String,
) {
    val winnerColor = when (summary.winner) {
        ComparisonWinner.LEFT, ComparisonWinner.RIGHT -> NeonEmerald
        ComparisonWinner.TIE -> NeonCyan
        ComparisonWinner.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
    }

    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = accentColor,
        secondaryColor = winnerColor,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = icon, fontSize = 16.sp)
                    Text(
                        text = summary.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = winnerColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, winnerColor.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = summary.badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = winnerColor,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            Text(
                text = summary.winnerName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = summary.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "1. Köşe:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                    Text(
                        text = summary.leftMetricText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (summary.winner == ComparisonWinner.LEFT) NeonEmerald else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "2. Köşe:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                    Text(
                        text = summary.rightMetricText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (summary.winner == ComparisonWinner.RIGHT) NeonEmerald else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun VsGameFpsMatrixCard(
    leftName: String,
    rightName: String,
    gameRows: List<VsGameComparisonRow>,
    selectedResolution: GamingResolution,
    onResolutionChange: (GamingResolution) -> Unit,
    selectedCategoryFilter: GameCategory?,
    onCategoryFilterChange: (GameCategory?) -> Unit,
) {
    val visibleGames = if (selectedCategoryFilter == null) {
        gameRows
    } else {
        gameRows.filter { it.category == selectedCategoryFilter }
    }

    FluidOrganicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        accentColor = NeonPurple,
        secondaryColor = NeonCyan,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
        contentPadding = 16.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎮 12 OYUN FPS & %1 LOW SİMÜLASYON MATRİSİ",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = "Rekabetçi E-Spor (5) & AAA Ağır Grafik (7) gerçek katsayı kıyası",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Resolution Switcher (1080p / 1440p / 4K)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GamingResolution.entries.forEach { res ->
                    val isSelected = res == selectedResolution
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onResolutionChange(res) },
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = res.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                text = res.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                fontSize = 10.sp,
                            )
                        }
                    }
                }
            }

            // Game Category Filter Chips (All / E-Spor / AAA)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val filters: List<Pair<GameCategory?, String>> = listOf(
                    null to "Tümü (${gameRows.size})",
                    GameCategory.ESPORTS to "🎯 E-Spor (5)",
                    GameCategory.AAA to "🔥 AAA Grafik (7)",
                )
                filters.forEach { (cat, label) ->
                    val selected = selectedCategoryFilter == cat
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCategoryFilterChange(cat) },
                        color = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            visibleGames.forEach { game ->
                val lAvg = game.leftFps.avgFps(selectedResolution)
                val rAvg = game.rightFps.avgFps(selectedResolution)
                val lLow = game.leftFps.low1PctFps(selectedResolution, game.leftIsHighCache)
                val rLow = game.rightFps.low1PctFps(selectedResolution, game.rightIsHighCache)
                val winner = game.winnerFor(selectedResolution)
                val delta = game.deltaBadgeFor(selectedResolution)
                val maxFps = maxOf(lAvg, rAvg, 60).toFloat()

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Surface(
                                    color = if (game.category == GameCategory.ESPORTS) NeonCyan.copy(alpha = 0.16f) else NeonAmber.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(4.dp),
                                ) {
                                    Text(
                                        text = if (game.category == GameCategory.ESPORTS) "E-SPOR" else "AAA",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (game.category == GameCategory.ESPORTS) NeonCyan else NeonAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Text(
                                    text = game.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            if (delta != null && winner != ComparisonWinner.TIE) {
                                Surface(
                                    color = NeonEmerald.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.45f)),
                                ) {
                                    Text(
                                        text = "${if (winner == ComparisonWinner.LEFT) "1. Köşe" else "2. Köşe"} $delta",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                }
                            }
                        }

                        // Left Item FPS Bar
                        VsSingleFpsBarRow(
                            cornerTag = "1.K",
                            name = leftName,
                            avgFps = lAvg,
                            low1Fps = lLow,
                            fraction = (lAvg / maxFps).coerceIn(0.08f, 1f),
                            isWinner = winner == ComparisonWinner.LEFT,
                            barColor = if (winner == ComparisonWinner.LEFT) NeonEmerald else ElectricBlue,
                        )

                        // Right Item FPS Bar
                        VsSingleFpsBarRow(
                            cornerTag = "2.K",
                            name = rightName,
                            avgFps = rAvg,
                            low1Fps = rLow,
                            fraction = (rAvg / maxFps).coerceIn(0.08f, 1f),
                            isWinner = winner == ComparisonWinner.RIGHT,
                            barColor = if (winner == ComparisonWinner.RIGHT) NeonEmerald else NeonPurple,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VsSingleFpsBarRow(
    cornerTag: String,
    name: String,
    avgFps: Int,
    low1Fps: Int,
    fraction: Float,
    isWinner: Boolean,
    barColor: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$cornerTag • $name",
                style = MaterialTheme.typography.labelSmall,
                color = if (isWinner) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$avgFps FPS  (%1 Low: $low1Fps)",
                style = MaterialTheme.typography.labelSmall,
                color = if (isWinner) NeonEmerald else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.background),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor),
            )
        }
    }
}

@Composable
private fun VsComparisonMetricRow(
    row: VsComparisonRow,
    modifier: Modifier = Modifier,
) {
    val leftWin = row.winner == ComparisonWinner.LEFT
    val rightWin = row.winner == ComparisonWinner.RIGHT

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (row.isKeyMetric) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = row.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                if (row.deltaBadge != null && (leftWin || rightWin)) {
                    Surface(
                        color = NeonEmerald.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.45f)),
                    ) {
                        Text(
                            text = "${if (leftWin) "1. Köşe" else "2. Köşe"}: ${row.deltaBadge}",
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonEmerald,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Left Value Box
                Surface(
                    modifier = Modifier.weight(1f),
                    color = if (leftWin) NeonEmerald.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (leftWin) 1.5.dp else 1.dp,
                        color = if (leftWin) NeonEmerald else MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = row.leftValue,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (leftWin) NeonEmerald else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (leftWin) FontWeight.ExtraBold else FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        if (leftWin) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }

                // Right Value Box
                Surface(
                    modifier = Modifier.weight(1f),
                    color = if (rightWin) NeonEmerald.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (rightWin) 1.5.dp else 1.dp,
                        color = if (rightWin) NeonEmerald else MaterialTheme.colorScheme.outlineVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = row.rightValue,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (rightWin) NeonEmerald else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (rightWin) FontWeight.ExtraBold else FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        if (rightWin) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VsEmptyArenaCard(
    mode: VsArenaMode,
    activeCategory: HardwareCategory,
    suggestedPair: Pair<HardwareItem, HardwareItem>?,
    onApplySuggested: () -> Unit,
    onPickLeft: () -> Unit,
    onPickRight: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "⚔️", fontSize = 36.sp)
            Text(
                text = "${activeCategory.displayName} Karşılaştırma Arenası",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Yukarıdaki 1. Köşe ve 2. Köşe kutularına dokunarak veya Katalog ekranındaki ürün kartlarında yer alan 'VS' butonuyla iki ürünü kafa kafaya kıyaslayabilirsiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onPickLeft,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, ElectricBlue),
                ) {
                    Text("1. Köşeyi Seç", color = ElectricBlue, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onPickRight,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, NeonPurple),
                ) {
                    Text("2. Köşeyi Seç", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }

            if (suggestedPair != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "Popüler Kapışma Önerisi: ${suggestedPair.first.model} vs ${suggestedPair.second.model}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
                Button(
                    onClick = onApplySuggested,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "⚡ Örnek Kapışmayı Hemen Başlat",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun VsProductPickerSheetContent(
    slot: VsSlot,
    category: HardwareCategory,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    items: List<HardwareItem>,
    selectedId: HardwareId?,
    onSelectItem: (HardwareItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "${if (slot == VsSlot.LEFT) "1. Köşe" else "2. Köşe"} İçin ${category.displayName} Seçin (${items.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
        )

        var localSearchQuery by remember(slot, category) { mutableStateOf(searchQuery) }

        OutlinedTextField(
            value = localSearchQuery,
            onValueChange = {
                localSearchQuery = it
                onSearchChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "${category.displayName} model, marka veya özellik ara...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = items,
                key = { it.id.value },
            ) { item ->
                val isSelected = item.id == selectedId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectItem(item) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Sol Blok (Görsel): 72x72dp
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (!item.imageUrl.isNullOrBlank()) Color.White else MaterialTheme.colorScheme.surfaceVariant,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!item.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.displayName,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            } else {
                                Text(text = category.iconEmoji(), fontSize = 24.sp)
                            }
                        }

                        // Orta Blok (İçerik): weight(1f), minWidth = 0dp, maxLines = 2 + Ellipsis
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(min = 0.dp)
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = item.brand.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = item.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
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
                            Text(
                                text = item.currentPriceTry.formatPriceWithCurrency("Fiyat Yok"),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (item.currentPriceTry != null) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                fontWeight = FontWeight.ExtraBold,
                                softWrap = false,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.End,
                            )

                            Surface(
                                color = ElectricBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) NeonCyan else ElectricBlue.copy(alpha = 0.5f)),
                            ) {
                                Text(
                                    text = if (isSelected) "Seçili" else "Seç",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
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
}
