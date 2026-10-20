package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.DealStatus
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.CommunityPriceRepository
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.theme.TextSecondary
import com.panda.myapplication.presentation.util.formatPrice
import kotlin.math.roundToInt

data class SecondHandValuationItem(
    val item: HardwareItem,
    val newPriceTry: Double,
    val quickSellTry: Double,
    val marketMedianTry: Double,
    val showcaseMintTry: Double,
    val retentionPercentage: Int,
    val verifiedCommunityDealsCount: Int,
)

@Composable
fun SecondHandValuationScreen(
    activeBuildRepository: ActiveBuildRepository,
    communityPriceRepository: CommunityPriceRepository,
    onNavigateBack: () -> Unit,
    onNavigateToWizard: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val activeBuild by activeBuildRepository.activeBuild.collectAsState()
    val allDeals by communityPriceRepository.observeAllDeals().collectAsState(initial = emptyList())

    val verifiedSecondHandDeals = remember(allDeals) {
        allDeals.filter { it.status == DealStatus.APPROVED && it.dealType == CommunityDealType.SECOND_HAND }
    }

    val valuationItems: List<SecondHandValuationItem> = remember(activeBuild, verifiedSecondHandDeals) {
        activeBuild.items.map { item ->
            val dealsForItem = verifiedSecondHandDeals.filter { it.hardwareId.equals(item.id.value, ignoreCase = true) }
            val basePrice = item.currentPriceTry ?: 0.0

            // Donanım kategorisine göre 2. el değer koruma çarpanı (Depreciation curve)
            val baseRetentionFactor = when (item.category) {
                HardwareCategory.GPU -> 0.74 // GPU'lar yüksek talep görür
                HardwareCategory.CPU -> 0.77 // CPU'lar bozulma riski en düşük parçadır
                HardwareCategory.RAM -> 0.72
                HardwareCategory.MOTHERBOARD -> 0.68
                HardwareCategory.STORAGE -> 0.62 // SSD'lerde TBW ömrü faktörü
                HardwareCategory.PSU -> 0.70
                HardwareCategory.COOLER -> 0.65 // Sıvı soğutma pompa ömrü
                HardwareCategory.CASE -> 0.66
                else -> 0.70
            }

            val marketMedian = if (dealsForItem.isNotEmpty()) {
                dealsForItem.map { it.priceTry }.average()
            } else {
                basePrice * baseRetentionFactor
            }

            val quickSell = marketMedian * 0.83 // Hızlı nakit: %17 indirimli
            val showcaseMint = marketMedian * 1.13 // Vitrin / Kusursuz kutulu faturalı: %13 primli
            val retention = ((marketMedian / basePrice.coerceAtLeast(1.0)) * 100).roundToInt().coerceIn(40, 95)

            SecondHandValuationItem(
                item = item,
                newPriceTry = basePrice,
                quickSellTry = quickSell,
                marketMedianTry = marketMedian,
                showcaseMintTry = showcaseMint,
                retentionPercentage = retention,
                verifiedCommunityDealsCount = dealsForItem.size,
            )
        }
    }

    val totalNewPrice = valuationItems.sumOf { it.newPriceTry }
    val totalQuickSell = valuationItems.sumOf { it.quickSellTry }
    val totalMarketMedian = valuationItems.sumOf { it.marketMedianTry }
    val totalShowcaseMint = valuationItems.sumOf { it.showcaseMintTry }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = LocalAppStrings.current.drawerValuationTitle,
                subtitle = LocalAppStrings.current.drawerValuationSubtitle,
                icon = "🏷️",
                onNavigateBack = onNavigateBack,
                onOpenDrawer = onOpenDrawer,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Header Hero Card
            item(key = "valuation_hero") {
                ValuationHeroHeader(
                    hasItems = valuationItems.isNotEmpty(),
                    componentCount = valuationItems.size,
                )
            }

            // 2. Summary Pricing 3-Tier Card (Hızlı Nakit, Gerçekçi Pazar, Vitrin Değeri)
            if (valuationItems.isNotEmpty()) {
                item(key = "total_valuation_card") {
                    TotalValuationTiersCard(
                        totalNew = totalNewPrice,
                        quickSell = totalQuickSell,
                        marketMedian = totalMarketMedian,
                        showcaseMint = totalShowcaseMint,
                    )
                }

                // 3. Section Header: Parça Parça 2. El Dağılımı
                item(key = "components_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = LocalAppStrings.current.valuationBreakdownTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            text = strings.valComponentsCountFormat.replace("%d", valuationItems.size.toString()),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                }

                // 4. Component Valuation Cards
                items(valuationItems, key = { it.item.id.value }) { valItem ->
                    ComponentValuationCard(valItem = valItem)
                }

                // 5. Market Tips & Strategy Guide
                item(key = "selling_tips_guide") {
                    SecondHandSellingTipsCard()
                }
            } else {
                // Empty Build State
                item(key = "empty_state") {
                    EmptyBuildValuationPrompt(onNavigateToWizard = onNavigateToWizard)
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ValuationHeroHeader(
    hasItems: Boolean,
    componentCount: Int,
) {
    val strings = LocalAppStrings.current
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = NeonEmerald,
        secondaryColor = ElectricBlue,
        contentPadding = Spacing.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔄", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.valuationHeroTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonEmerald,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, NeonEmerald),
                ) {
                    Text(
                        text = if (hasItems) strings.valuationActiveParts.replace("%d", componentCount.toString()) else strings.valuationNoSystem,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = strings.valuationHeroSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TotalValuationTiersCard(
    totalNew: Double,
    quickSell: Double,
    marketMedian: Double,
    showcaseMint: Double,
) {
    val strings = LocalAppStrings.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = LocalAppStrings.current.valuationTotalMarket,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                )
                Text(
                    text = LocalAppStrings.current.valuationNewTotal.replace("%s", formatTry(totalNew)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Tier Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 1. Hızlı Nakit (Acil Satış)
                TierColumnBox(
                    title = "⚡ " + strings.valQuickCash,
                    subtitle = strings.valQuickCashDesc,
                    price = quickSell,
                    color = NeonAmber,
                    modifier = Modifier.weight(1f),
                )

                // 2. Gerçekçi Piyasa Medyanı (Önerilen)
                TierColumnBox(
                    title = "🎯 " + strings.valMarketValue,
                    subtitle = "Dengeli Piyasa",
                    price = marketMedian,
                    color = NeonEmerald,
                    isPrimary = true,
                    modifier = Modifier.weight(1.15f),
                )

                // 3. Vitrin / Kusursuz
                TierColumnBox(
                    title = "✨ Vitrin / Kusursuz",
                    subtitle = "Kutulu & Garantili",
                    price = showcaseMint,
                    color = ElectricBlue,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TierColumnBox(
    title: String,
    subtitle: String,
    price: Double,
    color: Color,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isPrimary) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(if (isPrimary) 1.5.dp else 0.5.dp, if (isPrimary) color else MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatTry(price),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = color,
                fontSize = if (isPrimary) 14.sp else 12.sp,
            )
        }
    }
}

@Composable
private fun ComponentValuationCard(valItem: SecondHandValuationItem) {
    val strings = LocalAppStrings.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = valItem.item.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${valItem.item.category.displayName} • " + strings.valNewPriceLabel.replace("%s", formatTry(valItem.newPriceTry)),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, NeonEmerald.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = "%${valItem.retentionPercentage} Koruma",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Price Values in One Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(strings.valQuickCash, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                    Text(formatTry(valItem.quickSellTry), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = NeonAmber)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings.valMarketValue, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                    Text(formatTry(valItem.marketMedianTry), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = NeonEmerald)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Kutulu Vitrin", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                    Text(formatTry(valItem.showcaseMintTry), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ElectricBlue)
                }
            }

            if (valItem.verifiedCommunityDealsCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.valVerifiedDealsFormat.replace("%d", valItem.verifiedCommunityDealsCount.toString()),
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun SecondHandSellingTipsCard() {
    val strings = LocalAppStrings.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LocalAppStrings.current.valuationTipsTitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonAmber,
                )
            }

            SellingTipRow(
                icon = "📦",
                title = "Kutu & Fatura Etkisi (+%10-15)",
                description = strings.valTip1Desc,
            )

            SellingTipRow(
                icon = "📊",
                title = strings.valTip2Title,
                description = strings.valTip2Desc,
            )

            SellingTipRow(
                icon = "✨",
                title = "Temizlik ve Termal Macun",
                description = strings.valTip3Desc,
            )
        }
    }
}

@Composable
private fun SellingTipRow(icon: String, title: String, description: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = icon, fontSize = 16.sp, modifier = Modifier.padding(top = 2.dp, end = 8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun EmptyBuildValuationPrompt(onNavigateToWizard: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "🖥️", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = LocalAppStrings.current.valuationEmptyTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = LocalAppStrings.current.valuationEmptySubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToWizard,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
            ) {
                Text(
                    text = LocalAppStrings.current.valuationGoToWizard,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
            }
        }
    }
}

private fun formatTry(amount: Double): String {
    val rounded = amount.roundToInt()
    val formatted = rounded.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$formatted ₺"
}
