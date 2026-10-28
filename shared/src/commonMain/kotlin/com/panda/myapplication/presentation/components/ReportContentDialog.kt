package com.panda.myapplication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.model.ReportReason
import com.panda.myapplication.domain.model.ReportTargetType
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportContentDialog(
    targetType: ReportTargetType,
    targetTitle: String,
    reportedUserName: String,
    isSelfReport: Boolean,
    isTr: Boolean,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: ReportReason, details: String) -> Unit,
) {
    var selectedReason by remember {
        mutableStateOf(if (isSelfReport) ReportReason.AUTHOR_RETRACTION else ReportReason.OFFENSIVE_OR_INAPPROPRIATE)
    }
    var detailsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = if (isSelfReport) Icons.Rounded.Info else Icons.Rounded.ReportProblem,
                        contentDescription = null,
                        tint = if (isSelfReport) NeonCyan else NeonRose,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = if (isSelfReport) {
                            if (isTr) "Paylaşımımı Raporla / Geri Çek" else "Report My Own Post"
                        } else {
                            if (isTr) "İçeriği Raporla & Şikayet Et" else "Report Content"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Kapat", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Target Information Card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${targetType.localizedTitle(isTr)} • @$reportedUserName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = targetTitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                        )
                    }
                }

                // Self-Report Notification Banner
                if (isSelfReport) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(imageVector = Icons.Rounded.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isTr) {
                                    "ℹ️ Bu sizin kendi paylaşımınızdır. Gönderinizi hatalı paylaştıysanız veya yayından kaldırmak/düzeltilmesini istiyorsanız 'Kendi Paylaşımımı Geri Çekme Talebi' ile doğrudan moderatör ekibine bildirebilirsiniz."
                                } else {
                                    "ℹ️ This is your own post. If you posted erroneous data or wish to have it retracted/reviewed, you can submit a self-report retraction request to moderators."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }

                Text(
                    text = if (isTr) "Raporlama Nedeni:" else "Reason for Report:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Selectable Reasons
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ReportReason.entries.forEach { reason ->
                        val isSelected = selectedReason == reason
                        val accentColor = when (reason) {
                            ReportReason.AUTHOR_RETRACTION -> NeonCyan
                            ReportReason.DANGEROUS_VOLTAGE -> NeonRose
                            ReportReason.OFFENSIVE_OR_INAPPROPRIATE -> NeonRose
                            ReportReason.MISLEADING_OR_FAKE -> NeonAmber
                            else -> NeonPurple
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedReason = reason }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                                Text(
                                    text = reason.localizedTitle(isTr),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                // Details Text Field
                Text(
                    text = if (isTr) "Ek Açıklama (İsteğe Bağlı):" else "Additional Details (Optional):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                OutlinedTextField(
                    value = detailsText,
                    onValueChange = { detailsText = it },
                    placeholder = {
                        Text(
                            text = if (isTr) "Lütfen karşılaştığınız sorunu veya talebinizi kısaca açıklayın..." else "Please briefly explain the issue...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isSelfReport) NeonCyan else NeonRose,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitReport(selectedReason, detailsText.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelfReport) NeonCyan else NeonRose,
                    contentColor = Color.Black,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(imageVector = Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isTr) "Raporu Gönder" else "Submit Report",
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = if (isTr) "Vazgeç" else "Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
