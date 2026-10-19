package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.panda.myapplication.presentation.theme.CardSurface
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GlassBorder
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SecondHandValuationTopBar(
                onNavigateBack = onNavigateBack,
                onOpenDrawer = onOpenDrawer,
            )
        },
        modifier = modifier.statusBarsPadding().navigationBarsPadding(),
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
                            text = "PARÇA BAZLI 2. EL DEĞER DAĞILIMI",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            text = "${valuationItems.size} Bileşen",
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
private fun SecondHandValuationTopBar(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onOpenDrawer,
                    shape = RoundedCornerShape(10.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, GlassBorder),
                ) {
                    Text(
                        text = "☰",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(10.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, GlassBorder),
                ) {
                    Text(
                        text = "← Geri",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Text(
                text = "2. El Sistem Değerleme",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Box(modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun ValuationHeroHeader(
    hasItems: Boolean,
    componentCount: Int,
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔄", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SİSTEMİM 2. ELDE NE KADAR EDER?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonEmerald,
                        letterSpacing = 0.8.sp,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, NeonEmerald),
                ) {
                    Text(
                        text = if (hasItems) "$componentCount Parça Aktif" else "Sistem Seçilmedi",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Aktif PC Sihirbazı sisteminizdeki donanımları, doğrulanmış topluluk fiyat defteri ve amortisman algoritmasıyla 3 farklı piyasa kademesinde otomatik değerler.",
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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
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
                    text = "SİSTEM TOPLAM 2. EL PİYASA DEĞERİ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                )
                Text(
                    text = "Sıfır Liste: ${formatTry(totalNew)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
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
                    title = "⚡ Hızlı Nakit",
                    subtitle = "24-48 Saat İçi",
                    price = quickSell,
                    color = NeonAmber,
                    modifier = Modifier.weight(1f),
                )

                // 2. Gerçekçi Piyasa Medyanı (Önerilen)
                TierColumnBox(
                    title = "🎯 Gerçekçi Pazar",
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
        color = if (isPrimary) color.copy(alpha = 0.15f) else CardSurfaceElevated,
        border = BorderStroke(if (isPrimary) 1.5.dp else 0.5.dp, if (isPrimary) color else GlassBorder),
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
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurfaceElevated.copy(alpha = 0.5f),
        border = BorderStroke(0.5.dp, GlassBorder),
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
                        text = "${valItem.item.category.displayName} • Sıfır Fiyatı: ${formatTry(valItem.newPriceTry)}",
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
                    Text("Hızlı Nakit", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                    Text(formatTry(valItem.quickSellTry), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = NeonAmber)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Piyasa Değeri", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
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
                    text = "✓ ${valItem.verifiedCommunityDealsCount} doğrulanmış 2. el topluluk işlemi baz alındı",
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
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, GlassBorder),
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
                    text = "2. EL SATIŞ TAKTİKLERİ & PÜF NOKTALARI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonAmber,
                )
            }

            SellingTipRow(
                icon = "📦",
                title = "Kutu & Fatura Etkisi (+%10-15)",
                description = "Kutusu, faturası ve TR distribütör garantisi bulunan donanımlar hem çok daha hızlı satılır hem de vitrin değerinde alıcı bulur.",
            )

            SellingTipRow(
                icon = "📊",
                title = "Stres Testi Ekran Görüntüleri",
                description = "İlanınıza 15 dakikalık FurMark, Cinebench R23 ve CrystalDiskInfo SSD sağlık ekran görüntülerini eklemek alıcı güvenini 3 katına çıkarır.",
            )

            SellingTipRow(
                icon = "✨",
                title = "Temizlik ve Termal Macun",
                description = "Kasa içi kablolaması düzenli, tozları temizlenmiş ve yeni termal macun uygulanmış sistemler fotoğraflarda anında fark yaratır.",
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
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, GlassBorder),
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
                text = "Aktif Sistem Listeniz Boş",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sisteminizin 2. el değerini hesaplamak için PC Sihirbazı'ndan parçalarınızı seçin veya kataloğu gezin.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToWizard,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
            ) {
                Text(
                    text = "PC Sihirbazı'na Git ve Parça Ekle",
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
