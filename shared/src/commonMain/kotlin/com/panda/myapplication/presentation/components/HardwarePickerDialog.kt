package com.panda.myapplication.presentation.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.engine.HardwareSearchEngine
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.usecase.GetCompatibleHardwareForSlotUseCase
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.util.LocalAppStrings

@Composable
fun HardwarePickerDialog(
    title: String,
    targetCategories: List<HardwareCategory>,
    hardwareRepository: HardwareRepository,
    activeBuild: ActiveBuild? = null,
    onDismiss: () -> Unit,
    onSelectItem: (HardwareItem) -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    var searchQuery by remember { mutableStateOf("") }
    val compatibilityUseCase = remember { GetCompatibleHardwareForSlotUseCase() }

    val allItemsResult by remember(hardwareRepository) {
        hardwareRepository.observeHardwareItems()
    }.collectAsState(initial = DomainResult.Success(emptyList()))

    val (filteredItems, constraintDescription) = remember(allItemsResult, targetCategories, activeBuild, searchQuery) {
        val list = when (val res = allItemsResult) {
            is DomainResult.Success -> res.data
            else -> emptyList()
        }
        val categoryMatches = list.filter { item ->
            targetCategories.isEmpty() || targetCategories.contains(item.category)
        }

        val (compatFiltered, constraintDesc) = if (activeBuild != null && targetCategories.size == 1) {
            val slotCategory = targetCategories.first()
            val compatRes = compatibilityUseCase(slotCategory, activeBuild, categoryMatches)
            compatRes.items to compatRes.filterConstraintDescription
        } else {
            categoryMatches to null
        }

        val queryFiltered = if (searchQuery.isBlank()) {
            compatFiltered
        } else {
            compatFiltered
                .filter { HardwareSearchEngine.matches(it, searchQuery) }
                .sortedByDescending { HardwareSearchEngine.calculateScore(it, searchQuery) }
        }
        queryFiltered to constraintDesc
    }

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
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Devices,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = strings.closeButton,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
            ) {
                if (!constraintDescription.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NeonAmber.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = constraintDescription,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (isTr) "Model veya marka ara..." else "Search model or brand...",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = strings.closeButton,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isTr) "${filteredItems.size} uyumlu donanım listelendi" else "${filteredItems.size} compatible items listed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isTr) "Aradığınız kriterde uyumlu donanım bulunamadı." else "No compatible hardware found matching your criteria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(filteredItems, key = { it.id.value }) { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectItem(item) },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        if (item.effectiveImageUrl != null) {
                                            AsyncImage(
                                                model = item.effectiveImageUrl,
                                                contentDescription = item.displayName,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White.copy(alpha = 0.05f)),
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(ElectricBlue.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = item.brand.take(2).uppercase(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ElectricBlue,
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = item.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = NeonPurple.copy(alpha = 0.2f),
                                                ) {
                                                    Text(
                                                        text = item.category.displayName,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                        color = NeonCyan,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    )
                                                }
                                                val priceTry = item.currentPriceTry
                                                if (priceTry != null && priceTry > 0) {
                                                    Text(
                                                        text = "₺${priceTry.toInt()}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        color = NeonEmerald,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonCyan.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                                    ) {
                                        Text(
                                            text = if (isTr) "Seç" else "Select",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.closeButton, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}
