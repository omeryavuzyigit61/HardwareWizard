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
import androidx.compose.material.icons.rounded.Lightbulb
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
import androidx.compose.ui.graphics.Brush
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
import com.panda.myapplication.presentation.theme.Spacing

@Composable
fun HardwareProTipsSection(
    tips: List<HardwareProTip>,
    onAddTipClick: () -> Unit,
    onUpvoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FluidOrganicCard(
        modifier = modifier.fillMaxWidth(),
        accentColor = NeonAmber,
        secondaryColor = NeonEmerald,
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonAmber.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Tips",
                            tint = NeonAmber,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💡 TOPLULUK MİKRO İPUÇLARI",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonAmber,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Deneyimli kullanıcıların pratik hap bilgileri",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonEmerald.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onAddTipClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Ekle",
                            tint = NeonEmerald,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "İpucu Paylaş",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald,
                            maxLines = 1,
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
                            text = "Henüz bir mikro ipucu paylaşılmamış.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bu donanım hakkında ilk hap bilgiyi siz paylaşın!",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tips.forEach { tip ->
                        ProTipCardItem(tip = tip, onUpvote = { onUpvoteClick(tip.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProTipCardItem(
    tip: HardwareProTip,
    onUpvote: () -> Unit,
) {
    val upvoteBg by animateColorAsState(
        if (tip.isUpvotedByMe) NeonEmerald.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
    )
    val upvoteBorder by animateColorAsState(
        if (tip.isUpvotedByMe) NeonEmerald else MaterialTheme.colorScheme.outlineVariant
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "\"${tip.tipText}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = upvoteBg,
                border = BorderStroke(1.dp, upvoteBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onUpvote() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbUp,
                        contentDescription = "Faydalı",
                        tint = if (tip.isUpvotedByMe) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${tip.upvotesCount}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (tip.isUpvotedByMe) NeonEmerald else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun AddProTipDialog(
    hardwareName: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val maxChars = 200

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "💡 Mikro İpucu Paylaş",
                fontWeight = FontWeight.Bold,
                color = NeonAmber,
            )
        },
        text = {
            Column {
                Text(
                    text = "$hardwareName için pratik bir hap bilgi yazın (Maks. $maxChars karakter):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= maxChars) text = it },
                    placeholder = { Text("Ör: BIOS güncellemesi yapmadan bu işlemci görüntü vermez.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonAmber,
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
                        onSubmit(text.trim())
                    }
                },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Paylaş (+10 Puan)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}
