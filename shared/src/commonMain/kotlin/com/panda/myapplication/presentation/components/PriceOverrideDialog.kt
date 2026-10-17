package com.panda.myapplication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.theme.SlateDarkBackground
import com.panda.myapplication.presentation.util.formatPrice
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PriceOverrideDialog(
    item: HardwareItem,
    onDismiss: () -> Unit,
    onSaveCustomPrice: (Double, String?) -> Unit,
    onClearCustomPrice: () -> Unit,
) {
    val initialPriceText = remember(item.id, item.customPriceTry) {
        item.customPriceTry?.roundToLong()?.toString()
            ?: item.storePriceTry?.roundToLong()?.toString()
            ?: ""
    }
    var priceText by remember(item.id) { mutableStateOf(initialPriceText) }
    var noteText by remember(item.id) { mutableStateOf(item.customPriceNote ?: "") }
    var hasError by remember { mutableStateOf(false) }

    val notePresets = listOf("Kendi Aldığım Fiyat", "İkinci El", "Kampanya / İndirim", "Yurt Dışı")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "🏷️ Yerel Özel Fiyat Belirle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan,
                    )
                    if (item.isCustomPrice) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonAmber.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.6f)),
                        ) {
                            Text(
                                text = "Özel Fiyat Aktif",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                            )
                        }
                    }
                }

                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Mağaza Referans Fiyatı:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = item.storePriceTry.formatPriceWithCurrency("Stok / Fiyat Yok"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (item.storePriceTry != null) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() || it == '.' || it == ',' }
                        priceText = digitsOnly
                        hasError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Özel Fiyatınız (₺ TL)") },
                    placeholder = { Text("Örn: 14500") },
                    singleLine = true,
                    isError = hasError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = CardSurfaceElevated,
                        unfocusedContainerColor = CardSurfaceElevated,
                    ),
                )

                if (hasError) {
                    Text(
                        text = "Lütfen geçerli bir TL tutarı girin (Örn: 5499).",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRose,
                    )
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Not / Kaynak (İsteğe Bağlı)") },
                    placeholder = { Text("Örn: İkinci El, Amazon İndirimi...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = CardSurfaceElevated,
                        unfocusedContainerColor = CardSurfaceElevated,
                    ),
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    notePresets.forEach { preset ->
                        val selected = noteText.equals(preset, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { noteText = if (selected) "" else preset },
                            label = {
                                Text(
                                    text = preset,
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

                Text(
                    text = "Girilen özel fiyat Room veritabanına kaydedilir; hem Katalog hem VS Arena F/P hesabı hem de PC Sihirbazı toplam bütçesinde öncelikli olarak kullanılır.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.isCustomPrice) {
                        OutlinedButton(
                            onClick = {
                                onClearCustomPrice()
                                onDismiss()
                            },
                            border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                text = "Sıfırla",
                                color = NeonRose,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = {
                            val normalized = priceText.replace(".", "").replace(",", ".").trim()
                            val parsed = normalized.toDoubleOrNull()
                            if (parsed == null || parsed < 100.0) {
                                hasError = true
                            } else {
                                onSaveCustomPrice(parsed, noteText.trim().takeIf { it.isNotBlank() })
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    ) {
                        Text(
                            text = "Kaydet",
                            color = SlateDarkBackground,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        }
    }
}
