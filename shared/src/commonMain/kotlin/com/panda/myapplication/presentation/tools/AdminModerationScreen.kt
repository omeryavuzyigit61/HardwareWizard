package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.LocalAppCurrency
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.DealStatus
import com.panda.myapplication.domain.repository.CommunityPriceRepository
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun AdminModerationScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    repository: CommunityPriceRepository = koinInject(),
    modifier: Modifier = Modifier,
) {
    val isAdminLoggedIn by repository.isAdminLoggedIn.collectAsState()
    val allDeals by repository.observeAllDeals().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.refreshDeals()
    }

    var selectedFilter by remember { mutableStateOf<DealStatus?>(DealStatus.PENDING) }
    var searchQuery by remember { mutableStateOf("") }

    val pendingCount = allDeals.count { it.status == DealStatus.PENDING }

    val filteredDeals = remember(allDeals, selectedFilter, searchQuery) {
        allDeals.filter { deal ->
            val matchesFilter = selectedFilter == null || deal.status == selectedFilter
            val matchesSearch = searchQuery.isBlank() ||
                    deal.hardwareName.contains(searchQuery, ignoreCase = true) ||
                    deal.userDisplayName.contains(searchQuery, ignoreCase = true) ||
                    deal.dealType.title.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ToolTopBar(
                title = "Yönetici Moderasyon Paneli",
                subtitle = "Topluluk Fiyat Bildirimleri ($pendingCount Bekleyen)",
                icon = "🛡️",
                onNavigateBack = onNavigateBack,
                onOpenDrawer = onOpenDrawer,
            )
        },
    ) { innerPadding ->
        if (!isAdminLoggedIn) {
            AdminAuthScreenContent(
                onLogin = { pin -> repository.loginAdmin(pin) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Spacing.medium),
            ) {
                Spacer(modifier = Modifier.height(Spacing.small))

                // Admin Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(NeonEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SÜPER YÖNETİCİ: Ömer Admin",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonAmber,
                                )
                            }
                            Text(
                                text = "Toplam ${allDeals.size} bildirim ($pendingCount bekleyen)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        repository.refreshDeals()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            ) {
                                Text(
                                    text = "🔄 Yenile",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                )
                            }
                            OutlinedButton(
                                onClick = { repository.logoutAdmin() },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.5f)),
                            ) {
                                Text(
                                    text = "Çıkış Yap",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonRose,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Donanım veya kullanıcı ara...") },
                    leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 8.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        focusedLabelColor = NeonCyan,
                    ),
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FilterChip(
                        selected = selectedFilter == DealStatus.PENDING,
                        onClick = { selectedFilter = DealStatus.PENDING },
                        label = { Text("⏳ Bekleyenler ($pendingCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonAmber.copy(alpha = 0.2f),
                            selectedLabelColor = NeonAmber,
                        ),
                    )
                    FilterChip(
                        selected = selectedFilter == DealStatus.APPROVED,
                        onClick = { selectedFilter = DealStatus.APPROVED },
                        label = { Text("✓ Onaylı") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = NeonEmerald,
                        ),
                    )
                    FilterChip(
                        selected = selectedFilter == DealStatus.REJECTED,
                        onClick = { selectedFilter = DealStatus.REJECTED },
                        label = { Text("✕ Reddedilen") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonRose.copy(alpha = 0.2f),
                            selectedLabelColor = NeonRose,
                        ),
                    )
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("Tümü (${allDeals.size})") },
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                // Moderation Items List
                if (filteredDeals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Bu filtreye uygun bildirim bulunamadı.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = Spacing.large),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        items(filteredDeals, key = { it.id }) { deal ->
                            AdminDealItemCard(
                                deal = deal,
                                onApprove = {
                                    coroutineScope.launch {
                                        repository.approveDeal(deal.id)
                                    }
                                },
                                onReject = {
                                    coroutineScope.launch {
                                        repository.rejectDeal(deal.id, "Geçersiz kanıt veya piyasa dışı bildirim.")
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        repository.deleteDeal(deal.id)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDealItemCard(
    deal: CommunityDeal,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current

    val (typeColor, typeEmoji) = when (deal.dealType) {
        CommunityDealType.OWN_PURCHASE -> ElectricBlue to "🛍️"
        CommunityDealType.SECOND_HAND -> NeonRose to "🔄"
        CommunityDealType.DISCOUNT_CAMPAIGN -> NeonAmber to "🔥"
        CommunityDealType.FOREIGN_PURCHASE -> NeonEmerald to "✈️"
    }

    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = when (deal.status) {
            DealStatus.PENDING -> NeonAmber
            DealStatus.APPROVED -> NeonEmerald
            DealStatus.REJECTED -> NeonRose
        },
        secondaryColor = typeColor,
        contentPadding = 12.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, typeColor.copy(alpha = 0.6f)),
                ) {
                    Text(
                        text = "$typeEmoji ${deal.dealType.title}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (deal.status) {
                        DealStatus.APPROVED -> NeonEmerald.copy(alpha = 0.15f)
                        DealStatus.PENDING -> NeonAmber.copy(alpha = 0.15f)
                        DealStatus.REJECTED -> NeonRose.copy(alpha = 0.15f)
                    },
                ) {
                    Text(
                        text = deal.status.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (deal.status) {
                            DealStatus.APPROVED -> NeonEmerald
                            DealStatus.PENDING -> NeonAmber
                            DealStatus.REJECTED -> NeonRose
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = deal.hardwareName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = formatTry(deal.priceTry),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NeonEmerald,
                )
                Text(
                    text = "Bildiren: ${deal.userDisplayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!deal.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "💬 ${deal.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }

            if (!deal.proofUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Kanıt: ${deal.proofUrl}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            runCatching { uriHandler.openUri(deal.proofUrl) }
                                .onFailure { clipboardManager.setText(AnnotatedString(deal.proofUrl)) }
                        },
                    ) {
                        Text("Aç / Kopyala", style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (deal.status != DealStatus.APPROVED) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("✓ Onayla", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                if (deal.status != DealStatus.REJECTED) {
                    Button(
                        onClick = onReject,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRose.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("✕ Reddet", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = onDelete,
                ) {
                    Text("🗑️ Sil", style = MaterialTheme.typography.labelSmall, color = NeonRose)
                }
            }
        }
    }
}

@Composable
private fun AdminAuthScreenContent(
    onLogin: (pin: String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var pinText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(Spacing.medium),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                Surface(
                    shape = CircleShape,
                    color = NeonAmber.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🔒", fontSize = 28.sp)
                    }
                }

                Text(
                    text = "Yönetici Moderasyon Girişi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Topluluk fiyat bildirimlerini incelemek, onaylamak veya reddetmek için yönetici PIN kodunuzu girin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        pinText = it
                        isError = false
                    },
                    label = { Text("Yönetici PIN Kodu") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (isError) {
                    Text(
                        text = "Hatalı PIN kodu! (Varsayılan PIN: 1923)",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonRose,
                    )
                }

                Button(
                    onClick = {
                        val success = onLogin(pinText)
                        if (!success) {
                            isError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "🛡️ Yönetici Olarak Giriş Yap",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }
            }
        }
    }
}

private fun formatTry(amount: Double): String {
    val rounded = amount.roundToInt()
    val formatted = rounded.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$formatted ₺"
}
