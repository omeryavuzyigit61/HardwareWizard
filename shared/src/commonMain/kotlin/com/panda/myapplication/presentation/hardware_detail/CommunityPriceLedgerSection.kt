package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.DealStatus
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.theme.CardSurface
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GlassBorder
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.hardware_list.Spacing
import kotlin.math.roundToInt

/**
 * Binance / Kripto order book & trade akışı tarzı Canlı Topluluk Fiyat Defteri.
 */
@Composable
fun CommunityPriceLedgerSection(
    deals: List<CommunityDeal>,
    isAdmin: Boolean,
    onDealClick: (CommunityDeal) -> Unit,
    onSubmitClick: () -> Unit,
    onApproveDeal: (String) -> Unit,
    onRejectDeal: (String) -> Unit,
    onAdminLoginClick: () -> Unit,
    onAdminLogoutClick: () -> Unit,
    onRefreshClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val displayDeals = if (isAdmin) deals else deals.filter { it.status == DealStatus.APPROVED }
    val approvedDeals = deals.filter { it.status == DealStatus.APPROVED }

    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    val minPrice = approvedDeals.minOfOrNull { it.priceTry }
    val avgPrice = if (approvedDeals.isNotEmpty()) approvedDeals.map { it.priceTry }.average() else null

    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = NeonCyan,
        secondaryColor = ElectricBlue,
        contentPadding = Spacing.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Binance Ticker & Live Indicator (Responsive FlowRow prevents squishing & letter-by-letter wrapping)
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = strings.detailCommunityLedger,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 0.8.sp,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (onRefreshClick != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onRefreshClick() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.ledgerRefresh,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }

                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonAmber.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, NeonAmber),
                        ) {
                            Text(
                                text = tr(isTr, "🛡️ YÖNETİCİ", "🛡️ ADMIN"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ) {
                            Text(
                                text = if (isTr) "${approvedDeals.size} Fırsat" else "${approvedDeals.size} Deals",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tr(isTr, "Kullanıcıların kendi aldıkları, 2. el, yurtdışı veya mağaza kampanyalarında yakaladıkları fiyat kayıtları.", "Community records of user purchase prices, second-hand deals, imports, or retail discounts."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Market Summary Stat Bar (Binance Market Stats)
            if (approvedDeals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardSurfaceElevated.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = tr(isTr, "En İyi Fırsat", "Best Deal"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = minPrice?.let { formatTry(it) } ?: "-",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald,
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tr(isTr, "Topluluk Ort.", "Community Avg."),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = avgPrice?.let { formatTry(it) } ?: "-",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = tr(isTr, "Bildirim Sayısı", "Reports Count"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${approvedDeals.size} ${tr(isTr, "Doğrulanmış", "Verified")}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Ticker Rows (Compact Binance Trades Layout)
            if (displayDeals.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardSurface.copy(alpha = 0.4f),
                    border = BorderStroke(0.5.dp, GlassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = tr(isTr, "Henüz onaylanmış topluluk fiyatı bildirilmedi.", "No approved community price reported yet."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tr(isTr, "Bu ürünü kaça aldığınızı veya gördüğünüz fırsatı ilk siz paylaşın!", "Share what you paid or report a discount deal you spotted!"),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    displayDeals.take(5).forEach { deal ->
                        CommunityDealTickerRow(
                            deal = deal,
                            isAdmin = isAdmin,
                            onClick = { onDealClick(deal) },
                            onApprove = { onApproveDeal(deal.id) },
                            onReject = { onRejectDeal(deal.id) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Bottom Actions: Submit Deal (+ Admin Mode Logout if admin)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onSubmitClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan.copy(alpha = 0.16f),
                        contentColor = NeonCyan,
                    ),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                ) {
                    Text(
                        text = strings.ledgerSubmitDealBtn,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (isAdmin) {
                    TextButton(onClick = onAdminLogoutClick) {
                        Text(
                            text = strings.ledgerAdminLogout,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonRose,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tek bir topluluk fiyat satırı (Binance son işlemler stili).
 */
@Composable
private fun CommunityDealTickerRow(
    deal: CommunityDeal,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val (typeColor, typeEmoji) = when (deal.dealType) {
        CommunityDealType.OWN_PURCHASE -> ElectricBlue to "🛍️"
        CommunityDealType.SECOND_HAND -> NeonRose to "🔄"
        CommunityDealType.DISCOUNT_CAMPAIGN -> NeonAmber to "🔥"
        CommunityDealType.FOREIGN_PURCHASE -> NeonEmerald to "✈️"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CardSurfaceElevated.copy(alpha = 0.45f),
        border = BorderStroke(0.5.dp, GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left: Deal Type Badge + Submitter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, typeColor.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = "$typeEmoji ${deal.dealType.localizedBadgeLabel(isTr)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.small))

                Column {
                    Text(
                        text = deal.userDisplayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (deal.proofUrl != null) {
                        Text(
                            text = tr(isTr, "🔗 Kanıt Mevcut", "🔗 Proof Available"),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                        )
                    }
                }
            }

            // Right: Price & Inline Admin Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatTry(deal.priceTry),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Black,
                        color = if (deal.dealType == CommunityDealType.DISCOUNT_CAMPAIGN) NeonAmber else NeonEmerald,
                    )
                    if (deal.status == DealStatus.PENDING) {
                        Text(
                            text = tr(isTr, "⏳ Onay Bekliyor", "⏳ Awaiting Review"),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonAmber,
                            fontSize = 10.sp,
                        )
                    }
                }

                if (isAdmin && deal.status == DealStatus.PENDING) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onApprove,
                        modifier = Modifier.size(26.dp),
                    ) {
                        Text(
                            text = "✓",
                            color = NeonEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                    IconButton(
                        onClick = onReject,
                        modifier = Modifier.size(26.dp),
                    ) {
                        Text(
                            text = "✕",
                            color = NeonRose,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tıklanan fiyat bildiriminin tam detay popup ekranı (Modal Dialog).
 */
@Suppress("DEPRECATION")
@Composable
fun CommunityDealDetailDialog(
    deal: CommunityDeal,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Text(deal.dealType.emoji)
                Text(
                    text = tr(isTr, "Topluluk Fiyat Bildirimi", "Community Price Submission"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                // Product Name
                Text(
                    text = deal.hardwareName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Price & Type Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = strings.ledgerReportedPrice,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = formatTry(deal.priceTry),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NeonEmerald,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, NeonCyan),
                        ) {
                            Text(
                                text = deal.dealType.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                // Submitter & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${tr(isTr, "Paylaşan", "Reported by")}: ${deal.userDisplayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = when (deal.status) {
                            DealStatus.APPROVED -> tr(isTr, "✓ Doğrulandı", "✓ Verified")
                            DealStatus.PENDING -> tr(isTr, "⏳ İncelemede", "⏳ In Review")
                            DealStatus.REJECTED -> tr(isTr, "✕ Reddedildi", "✕ Rejected")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (deal.status) {
                            DealStatus.APPROVED -> NeonEmerald
                            DealStatus.PENDING -> NeonAmber
                            DealStatus.REJECTED -> NeonRose
                        },
                    )
                }

                // Note
                if (!deal.note.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = strings.ledgerUserNotePrefix,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = deal.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                // Proof URL Link
                if (!deal.proofUrl.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                uriHandler.openUri(deal.proofUrl)
                            }.onFailure {
                                clipboardManager.setText(AnnotatedString(deal.proofUrl))
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ElectricBlue),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = strings.ledgerProofLinkBtn,
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricBlue,
                        )
                    }
                }

                // Admin Inline Moderation Controls
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = tr(isTr, "🛡️ Yönetici Moderasyon Kararı:", "🛡️ Admin Moderation Decision:"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(tr(isTr, "✓ Onayla", "✓ Approve"), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRose),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(tr(isTr, "✕ Reddet", "✕ Reject"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isTr) "Kapat" else "Close")
            }
        },
    )
}

/**
 * Yeni Topluluk Fiyatı / Fırsatı Bildirme Bottom Sheet Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun SubmitCommunityDealSheet(
    hardwareName: String,
    currentUser: UserProfile? = null,
    onDismiss: () -> Unit,
    onSubmit: (priceTry: Double, dealType: CommunityDealType, note: String?, proofUrl: String?, userDisplayName: String) -> Unit,
    defaultUserDisplayName: String = "",
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var priceText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CommunityDealType.OWN_PURCHASE) }
    var noteText by remember { mutableStateOf("") }
    var proofUrlText by remember { mutableStateOf("") }
    var userDisplayName by remember { mutableStateOf(defaultUserDisplayName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Text(
                text = strings.ledgerSubmitSheetTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
            )

            Text(
                text = hardwareName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = tr(isTr, "Aldığınız fiyatı veya piyasada gördüğünüz bir fırsatı bildirin. Güvenilirlik için kanıt linki (ilan/fatura) zorunludur. Yönetici onayının ardından canlı deftere eklenecektir.", "Submit your purchase price or a found deal. A proof link (receipt/listing) is mandatory. Approved entries appear in the live ledger."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Price Field
            OutlinedTextField(
                value = priceText,
                onValueChange = {
                    priceText = it.filter { char -> char.isDigit() || char == '.' || char == ',' }
                    errorMessage = null
                },
                label = { Text(strings.ledgerPriceLabel) },
                placeholder = { Text(tr(isTr, "Örn: 24999", "e.g. 24999")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    focusedLabelColor = NeonCyan,
                ),
            )

            // 4 Deal Types
            Text(
                text = strings.ledgerDealTypeSelect,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CommunityDealType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                text = "${type.emoji} ${type.badgeLabel}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan,
                        ),
                    )
                }
            }

            // Proof URL (Kanıt Linki - ZORUNLU)
            OutlinedTextField(
                value = proofUrlText,
                onValueChange = {
                    proofUrlText = it
                    errorMessage = null
                },
                label = { Text(strings.ledgerProofUrlLabel) },
                placeholder = { Text(tr(isTr, "https://www.sahibinden.com/... veya fatura/mağaza linki", "https://... proof link, receipt or listing URL")) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    focusedLabelColor = ElectricBlue,
                ),
            )

            // Note Field
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text(strings.ledgerNoteFieldLabel) },
                placeholder = { Text(tr(isTr, "Örn: İkinci elden kutulu garantili alındı...", "e.g. Bought second-hand with box & warranty...")) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )

            // Authenticated Submitter Badge
            if (currentUser != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tr(isTr, "👤 Bildiren Üye: ", "👤 Submitter: "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "@${currentUser.username}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = userDisplayName,
                    onValueChange = { userDisplayName = it },
                    label = { Text(tr(isTr, "Rumuzunuz / Adınız", "Nickname / Name")) },
                    placeholder = { Text(tr(isTr, "Örn: DonanimSever", "e.g. TechEnthusiast")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonRose,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            // Submit Button
            Button(
                onClick = {
                    val parsedPrice = priceText.replace(',', '.').toDoubleOrNull()
                    if (parsedPrice == null || parsedPrice <= 0) {
                        errorMessage = tr(isTr, "Lütfen geçerli bir fiyat (TL) girin.", "Please enter a valid price.")
                        return@Button
                    }
                    val cleanProof = proofUrlText.trim()
                    if (cleanProof.isBlank() || (!cleanProof.startsWith("http://", ignoreCase = true) && !cleanProof.startsWith("https://", ignoreCase = true))) {
                        errorMessage = tr(isTr, "Kanıt linki zorunludur! Lütfen geçerli bir web bağlantısı girin (https://...).", "Proof link is required! Please enter a valid URL (https://...).")
                        return@Button
                    }
                    val finalDisplayName = currentUser?.username?.let { "@$it" } ?: userDisplayName.ifBlank { tr(isTr, "Topluluk Üyesi", "Community Member") }
                    onSubmit(
                        parsedPrice,
                        selectedType,
                        noteText.takeIf { it.isNotBlank() },
                        cleanProof,
                        finalDisplayName,
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            ) {
                Text(
                    text = strings.ledgerSubmitAction,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
            }
        }
    }
}

/**
 * Yönetici PIN Giriş Dialogu (Tek yetkili admin için hızlı doğrulama).
 */
@Composable
fun AdminLoginPinDialog(
    onDismiss: () -> Unit,
    onLogin: (pin: String) -> Boolean,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    var pinText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = tr(isTr, "🛡️ Yönetici Girişi", "🛡️ Admin Authentication"),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Text(
                    text = tr(isTr, "Topluluk fiyat bildirimlerini doğrudan onaylamak veya reddetmek için yönetici PIN kodunuzu girin.", "Enter your admin PIN to moderate community deal submissions directly."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        pinText = it
                        isError = false
                    },
                    label = { Text(tr(isTr, "Yönetici PIN", "Admin PIN")) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isError) {
                    Text(
                        text = tr(isTr, "Geçersiz PIN kodu! (Varsayılan: 1923)", "Invalid PIN code! (Default: 1923)"),
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonRose,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val success = onLogin(pinText)
                    if (success) {
                        onDismiss()
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
            ) {
                Text(strings.authTabLogin, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isTr) "İptal" else "Cancel")
            }
        },
    )
}

private fun formatTry(amount: Double): String {
    val rounded = amount.roundToInt()
    val formatted = rounded.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$formatted ₺"
}

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText
