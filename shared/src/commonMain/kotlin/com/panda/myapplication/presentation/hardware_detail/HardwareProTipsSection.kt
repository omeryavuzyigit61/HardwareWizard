package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.model.HardwareProTip
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.theme.Spacing
import kotlin.math.roundToInt

@Composable
fun HardwareCommentsAndReviewsSection(
    tips: List<HardwareProTip>,
    onAddTipClick: () -> Unit,
    onUpvoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hardwareName: String = "",
    currentUserId: String? = null,
    currentUsername: String? = null,
    onReportClick: (HardwareProTip) -> Unit = {},
) {
    val avgRating = if (tips.isNotEmpty()) tips.map { it.rating.toDouble() }.average() else 5.0
    val formattedAvg = ((avgRating * 10).roundToInt() / 10.0).toString()

    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = NeonCyan,
        secondaryColor = NeonEmerald,
        contentPadding = Spacing.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Forum,
                            contentDescription = "Yorumlar",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "💬 KULLANICI YORUMLARI & DEĞERLENDİRMELERİ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan,
                                letterSpacing = 0.6.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "Gerçek kullanıcı deneyimleri, tavsiyeler ve kullanım notları",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Yorum Ekle Butonu
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonEmerald.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAddTipClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Yorum Ekle",
                            tint = NeonEmerald,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Yorum Yaz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonEmerald,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rating Summary Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "⭐ $formattedAvg / 5.0",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber,
                        )
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${tips.size} Topluluk Değerlendirmesi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonCyan.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = "+10 Puan Kazandırır",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            if (tips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .padding(vertical = Spacing.large, horizontal = Spacing.medium),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Henüz bu donanım için bir yorum yazılmamış.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "İlk deneyimi siz paylaşın ve +10 itibar puanı kazanın!",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    tips.forEach { tip ->
                        val isSelf = currentUserId == tip.userId || (currentUsername != null && tip.userDisplayName == currentUsername)
                        ProductReviewCardItem(
                            tip = tip,
                            isSelf = isSelf,
                            onUpvote = { onUpvoteClick(tip.id) },
                            onReport = { onReportClick(tip) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Geriye dönük uyumluluk için alias fonksiyon
 */
@Composable
fun HardwareProTipsSection(
    tips: List<HardwareProTip>,
    onAddTipClick: () -> Unit,
    onUpvoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    HardwareCommentsAndReviewsSection(
        tips = tips,
        onAddTipClick = onAddTipClick,
        onUpvoteClick = onUpvoteClick,
        modifier = modifier,
    )
}

@Composable
private fun ProductReviewCardItem(
    tip: HardwareProTip,
    isSelf: Boolean,
    onUpvote: () -> Unit,
    onReport: () -> Unit,
) {
    val upvoteBg by animateColorAsState(
        if (tip.isUpvotedByMe) NeonEmerald.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
    )
    val upvoteBorder by animateColorAsState(
        if (tip.isUpvotedByMe) NeonEmerald else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // User Header & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tip.userDisplayName.take(1).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "@${tip.userDisplayName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeonAmber.copy(alpha = 0.15f),
                            ) {
                                Text(
                                    text = tip.userBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = NeonAmber,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                }

                // Stars Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < tip.rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = null,
                            tint = if (index < tip.rating) NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(13.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment Text
            Text(
                text = "\"${tip.tipText}\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Upvote & Report/Retract Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Upvote Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = upvoteBg,
                    border = BorderStroke(1.dp, upvoteBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onUpvote() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ThumbUp,
                            contentDescription = "Faydalı",
                            tint = if (tip.isUpvotedByMe) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = if (tip.upvotesCount > 0) "Faydalı (${tip.upvotesCount})" else "Faydalı",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (tip.isUpvotedByMe) NeonEmerald else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // Report / Retract Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelf) NeonCyan.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(0.5.dp, if (isSelf) NeonCyan.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onReport() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = if (isSelf) "Kaldır" else "Raporla",
                            tint = if (isSelf) NeonCyan else NeonRose,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = if (isSelf) "Yorumumu Kaldır" else "Bildir",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelf) NeonCyan else NeonRose,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductReviewDialog(
    hardwareName: String,
    onDismiss: () -> Unit,
    onSubmit: (String, Int) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }
    val maxChars = 500

    val ratingLabels = mapOf(
        5 to "Mükemmel (5/5) 🌟",
        4 to "Çok İyi (4/5) 👍",
        3 to "Ortalama (3/5) 😐",
        2 to "Beklentimin Altında (2/5) 👎",
        1 to "Tavsiye Etmiyorum (1/5) ⚠️"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💬 Yorum & Deneyim Paylaş",
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "$hardwareName hakkındaki gerçek deneyiminizi, artı/eksi yönlerini veya kullanım tavsiyelerinizi paylaşın:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Star Rating Selector
                Text(
                    text = "Puanınız: ${ratingLabels[rating]}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonAmber,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { starIndex ->
                        Icon(
                            imageVector = if (starIndex <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "$starIndex Yıldız",
                            tint = if (starIndex <= rating) NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { rating = starIndex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= maxChars) text = it },
                    placeholder = { Text("Ör: 2 aydır kullanıyorum, 1440p ultra ayarlarda 65 dereceyi geçmedi. Fan sesi fısıltı gibi, kesinlikle tavsiye ederim.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 4,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${text.length}/$maxChars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onSubmit(text.trim(), rating)
                    }
                },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Yayınla (+10 Puan)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

/**
 * Geriye dönük uyumluluk için alias
 */
@Composable
fun AddProTipDialog(
    hardwareName: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    AddProductReviewDialog(
        hardwareName = hardwareName,
        onDismiss = onDismiss,
        onSubmit = { text, _ -> onSubmit(text) }
    )
}
