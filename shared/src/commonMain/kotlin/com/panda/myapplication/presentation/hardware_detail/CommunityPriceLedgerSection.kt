package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.DealStatus
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
    modifier: Modifier = Modifier,
) {
    val displayDeals = if (isAdmin) deals else deals.filter { it.status == DealStatus.APPROVED }
    val approvedDeals = deals.filter { it.status == DealStatus.APPROVED }

    val minPrice = approvedDeals.minOfOrNull { it.priceTry }
    val avgPrice = if (approvedDeals.isNotEmpty()) approvedDeals.map { it.priceTry }.average() else null

    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = NeonCyan,
        secondaryColor = ElectricBlue,
        contentPadding = Spacing.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Binance Ticker & Live Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Text(
                        text = "CANLI TOPLULUK FİYAT DEFTERİ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                        letterSpacing = 0.8.sp,
                    )
                }

                if (isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonAmber.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, NeonAmber),
                    ) {
                        Text(
                            text = "🛡️ YÖNETİCİ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ) {
                        Text(
                            text = "${approvedDeals.size} Fırsat",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Kullanıcıların kendi aldıkları, 2. el, yurtdışı veya mağaza kampanyalarında yakaladıkları fiyat kayıtları.",
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
                                text = "En İyi Fırsat",
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
                                text = "Topluluk Ort.",
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
                                text = "Bildirim Sayısı",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${approvedDeals.size} Doğrulanmış",
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
                            text = "Henüz onaylanmış topluluk fiyatı bildirilmedi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bu ürünü kaça aldığınızı veya gördüğünüz fırsatı ilk siz paylaşın!",
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

            // Bottom Actions: Submit Deal + Admin Mode Toggle
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
                        text = "+ Fiyat Bildir / Fırsat Paylaş",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (isAdmin) {
                    TextButton(onClick = onAdminLogoutClick) {
                        Text(
                            text = "Admin Çıkışı",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonRose,
                        )
                    }
                } else {
                    TextButton(onClick = onAdminLoginClick) {
                        Text(
                            text = "🛡️ Yönetici",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                        text = "$typeEmoji ${deal.dealType.badgeLabel}",
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
                            text = "🔗 Kanıt Mevcut",
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
                            text = "⏳ Onay Bekliyor",
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
@Composable
fun CommunityDealDetailDialog(
    deal: CommunityDeal,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            ) {
                Text(deal.dealType.emoji)
                Text(
                    text = "Topluluk Fiyat Bildirimi",
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
                                text = "Bildirilen Fiyat",
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
                        text = "Paylaşan: ${deal.userDisplayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = when (deal.status) {
                            DealStatus.APPROVED -> "✓ Doğrulandı"
                            DealStatus.PENDING -> "⏳ İncelemede"
                            DealStatus.REJECTED -> "✕ Reddedildi"
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
                                text = "💬 Kullanıcı Notu:",
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
                            text = "🔗 Kanıt Linkini İncele / Aç",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricBlue,
                        )
                    }
                }

                // Admin Inline Moderation Controls
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "🛡️ Yönetici Moderasyon Kararı:",
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
                            Text("✓ Onayla", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonRose),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("✕ Reddet", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        },
    )
}

/**
 * Yeni Topluluk Fiyatı / Fırsatı Bildirme Bottom Sheet Ekranı.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitCommunityDealSheet(
    hardwareName: String,
    onDismiss: () -> Unit,
    onSubmit: (priceTry: Double, dealType: CommunityDealType, note: String?, proofUrl: String?, userDisplayName: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var priceText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CommunityDealType.OWN_PURCHASE) }
    var noteText by remember { mutableStateOf("") }
    var proofUrlText by remember { mutableStateOf("") }
    var userDisplayName by remember { mutableStateOf("") }
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
                .padding(bottom = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            Text(
                text = "⚡ Topluluk Fiyatı / Fırsatı Bildir",
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
                text = "Aldığınız fiyatı veya piyasada gördüğünüz bir fırsatı bildirin. Yönetici onayının ardından canlı deftere eklenecektir.",
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
                label = { Text("Fiyat (TL) *") },
                placeholder = { Text("Örn: 24999") },
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
                text = "İşlem Türü Seçin *",
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

            // Proof URL (Kanıt Linki)
            OutlinedTextField(
                value = proofUrlText,
                onValueChange = { proofUrlText = it },
                label = { Text("Kanıt Linki (İlan / Fatura / Kampanya)") },
                placeholder = { Text("https://www.sahibinden.com/... veya mağaza linki") },
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
                label = { Text("Açıklama / Deneyim Notu") },
                placeholder = { Text("Örn: Sarı siteden kutulu garantili alındı...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )

            // Submitter Nickname
            OutlinedTextField(
                value = userDisplayName,
                onValueChange = { userDisplayName = it },
                label = { Text("Rumuzunuz / Adınız") },
                placeholder = { Text("Örn: Mert K.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

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
                        errorMessage = "Lütfen geçerli bir fiyat (TL) girin."
                        return@Button
                    }
                    onSubmit(
                        parsedPrice,
                        selectedType,
                        noteText.takeIf { it.isNotBlank() },
                        proofUrlText.takeIf { it.isNotBlank() },
                        userDisplayName.ifBlank { "Topluluk Üyesi" },
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            ) {
                Text(
                    text = "🚀 Fiyatı Bildir (Yönetici Onayına Sun)",
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
    var pinText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🛡️ Yönetici Girişi",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                Text(
                    text = "Topluluk fiyat bildirimlerini doğrudan onaylamak veya reddetmek için yönetici PIN kodunuzu girin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        pinText = it
                        isError = false
                    },
                    label = { Text("Yönetici PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isError) {
                    Text(
                        text = "Geçersiz PIN kodu! (Varsayılan: 1923)",
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
                Text("Giriş Yap", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
    )
}

private fun formatTry(amount: Double): String {
    val rounded = amount.roundToInt()
    val formatted = rounded.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$formatted ₺"
}
