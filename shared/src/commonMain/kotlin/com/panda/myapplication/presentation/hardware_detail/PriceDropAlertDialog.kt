package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald

@Composable
fun PriceDropAlertDialog(
    hardwareName: String,
    currentPriceTry: Double?,
    onDismiss: () -> Unit,
    onSaveAlert: (Double) -> Unit,
) {
    var targetPriceStr by remember {
        mutableStateOf(currentPriceTry?.let { (it * 0.90).toInt().toString() } ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = NeonAmber,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Fiyat Düşüş Alarmı Kur",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "$hardwareName için hedeflediğiniz fiyatı girin. Fiyat bu seviyeye indiğinde anında bildirim alırsınız.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = targetPriceStr,
                    onValueChange = { targetPriceStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Hedef Fiyat (₺)") },
                    placeholder = { Text("Ör: 12500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                    ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Strict retail-only filter guarantee
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonEmerald.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Yalnızca sıfır/perakende mağaza ve resmi indirim kampanyalarında alarm tetiklenir. İkinci el ilanlar alarmınızı tetiklemez.",
                            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 15.sp),
                            color = NeonEmerald,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = targetPriceStr.toDoubleOrNull()
                    if (price != null && price > 0) {
                        onSaveAlert(price)
                    }
                },
                enabled = (targetPriceStr.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Alarmı Kur", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}
