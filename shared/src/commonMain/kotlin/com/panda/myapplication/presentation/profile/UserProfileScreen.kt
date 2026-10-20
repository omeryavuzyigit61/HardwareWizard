package com.panda.myapplication.presentation.profile

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.DealStatus
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.repository.CommunityPriceRepository
import com.panda.myapplication.domain.repository.UserRepository
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun UserProfileScreen(
    userRepository: UserRepository,
    communityPriceRepository: CommunityPriceRepository,
    onNavigateBack: () -> Unit,
    onOpenAuthSheet: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val currentUser by userRepository.currentUser.collectAsState()
    val isLoggedIn by userRepository.isLoggedIn.collectAsState()
    val allDeals by communityPriceRepository.observeAllDeals().collectAsState(initial = emptyList())

    val user = currentUser
    val isMasterAdmin = user?.email?.equals("yavuzyigitomer52@gmail.com", ignoreCase = true) == true ||
            user?.username?.equals("dombikpanda", ignoreCase = true) == true

    // Kullanıcıya ait bildirimler
    val userDeals = remember(user, allDeals) {
        if (user == null) emptyList()
        else allDeals.filter { deal ->
            deal.userDisplayName.equals(user.username, ignoreCase = true) ||
                    deal.userEmail?.equals(user.email, ignoreCase = true) == true ||
                    (isMasterAdmin && (deal.userDisplayName.contains("dombik", ignoreCase = true) || deal.userDisplayName.contains("Ömer", ignoreCase = true)))
        }
    }

    val approvedCount = userDeals.count { it.status == DealStatus.APPROVED }
    val pendingCount = userDeals.count { it.status == DealStatus.PENDING }
    val rejectedCount = userDeals.count { it.status == DealStatus.REJECTED }
    val totalCount = userDeals.size

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ProfileTopBar(
                isLoggedIn = isLoggedIn,
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
        modifier = modifier.statusBarsPadding().navigationBarsPadding(),
    ) { innerPadding ->
        if (user == null || !isLoggedIn) {
            // Not Logged In State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.size(90.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "👤", fontSize = 42.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = strings.profileNotLoggedIn,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tr(isTr, "Fiyat bildirimlerinizi takip etmek, onay/ret istatistiklerinizi görmek ve donanım ruhunuzu belirlemek için giriş yapın veya kayıt olun.", "Sign in or register to track price reports, view approval stats, and customize your hardware profile."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onOpenAuthSheet,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth(0.8f),
                ) {
                    Text(
                        text = strings.profileLoginBtn,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }
            }
        } else {
            // Logged In Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // 1. Profile Hero Card
                item(key = "profile_hero") {
                    ProfileHeroCard(
                        user = user,
                        isMasterAdmin = isMasterAdmin,
                    )
                }

                // 2. Spirit Hardware Card ("Ruh Donanımı")
                item(key = "spirit_hardware_card") {
                    SpiritHardwareShowcaseCard(user = user)
                }

                // 3. Price Submission Statistics Grid (Onaylanan, Bekleyen, Reddedilen)
                item(key = "stats_grid") {
                    CommunityStatsSection(
                        totalCount = totalCount,
                        approvedCount = approvedCount,
                        pendingCount = pendingCount,
                        rejectedCount = rejectedCount,
                    )
                }

                // 4. Admin Portal Info (PC Web Portal Notice)
                if (isMasterAdmin) {
                    item(key = "admin_portal_notice") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonAmber.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🛡️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tr(isTr, "Yönetici Portalı Bilgilendirmesi", "Administrator Portal Notice"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = NeonAmber,
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr(isTr, "Yönetici onay ve ret işlemleri, mobil uygulamayı sade ve güvenli tutmak adına PC Web Portalı üzerinden yürütülmektedir.\nTarayıcınızdan 'admin_portal.html' dosyasını açarak 2FA korumalı yönetici panelinize erişebilirsiniz.", "Admin review and approval operations are managed via the PC Web Portal to keep the mobile experience streamlined and secure.\nOpen 'admin_portal.html' in your browser to access the 2FA-secured dashboard."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }

                // Settings Navigation Card
                item(key = "profile_settings_nav") {
                    Surface(
                        onClick = onNavigateToSettings,
                        shape = RoundedCornerShape(14.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚙️", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tr(isTr, "Uygulama Ayarları", "Application Settings"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = tr(isTr, "Tema, 7 Dil, Para Birimi, Uzman Modu, Kaynakça", "Theme, 7 Languages, Currency, Expert Mode, Sources"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                            }
                            Text(
                                text = "›",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                            )
                        }
                    }
                }

                // 5. User Submission History Header
                item(key = "history_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${tr(isTr, "FİYAT BİLDİRİM GEÇMİŞİM", "MY PRICE SUBMISSION HISTORY")} (${userDeals.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                            letterSpacing = 0.8.sp,
                        )
                    }
                }

                // 6. User Deals List
                if (userDeals.isEmpty()) {
                    item(key = "no_deals") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardSurfaceElevated,
                            border = BorderStroke(0.5.dp, GlassBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("🏷️", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tr(isTr, "Henüz fiyat bildiriminde bulunmadınız.", "You have not submitted any price reports yet."),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                )
                                Text(
                                    text = tr(isTr, "Ürün detay sayfalarındaki 'Fiyat Bildir' butonunu kullanarak ilk fırsatınızı paylaşabilirsiniz.", "Use the 'Report Price' button on product detail pages to share your first deal."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonCyan,
                                )
                            }
                        }
                    }
                } else {
                    items(userDeals, key = { it.id }) { deal ->
                        UserDealItemCard(deal = deal, onOpenUrl = { url ->
                            runCatching { uriHandler.openUri(url) }
                        })
                    }
                }

                // 7. Logout Action
                item(key = "logout_section") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                userRepository.logout()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRose),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = strings.profileLogoutBtn,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileTopBar(
    isLoggedIn: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
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
            Surface(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(10.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(1.dp, GlassBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "←",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tr(isTr, "Geri", "Back"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Text(
                text = strings.profileTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Surface(
                onClick = onNavigateToSettings,
                shape = RoundedCornerShape(10.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(1.dp, GlassBorder),
            ) {
                Text(
                    text = "⚙️",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun ProfileHeroCard(
    user: UserProfile,
    isMasterAdmin: Boolean,
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (isMasterAdmin) NeonAmber else Color(user.spiritHardware.colorHex),
        secondaryColor = ElectricBlue,
        contentPadding = Spacing.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Spirit Avatar
                Surface(
                    shape = CircleShape,
                    color = CardSurfaceElevated,
                    border = BorderStroke(2.dp, Color(user.spiritHardware.colorHex)),
                    modifier = Modifier.size(68.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.spiritHardware.emoji,
                            fontSize = 34.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (isMasterAdmin) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonAmber.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, NeonAmber),
                            ) {
                                Text(
                                    text = tr(isTr, "🛡️ YÖNETİCİ", "🛡️ ADMIN"),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonAmber,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badges Row: Otomatik Hesaplanmış YAŞ + Cinsiyet
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // YAŞ (Doğum tarihi değil, doğrudan kullanıcı yaşı)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.4f)),
                        ) {
                            Text(
                                text = "${tr(isTr, "Yaş", "Age")}: ${user.age}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }

                        // Cinsiyet
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CardSurfaceElevated,
                            border = BorderStroke(0.5.dp, GlassBorder),
                        ) {
                            Text(
                                text = "${user.gender.emoji} ${user.gender.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpiritHardwareShowcaseCard(user: UserProfile) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val spirit = user.spiritHardware
    val spiritColor = Color(spirit.colorHex)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurfaceElevated.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, spiritColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = tr(isTr, "RUH DONANIMIN", "YOUR SPIRIT HARDWARE"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = spiritColor,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    text = spirit.emoji,
                    fontSize = 18.sp,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = spirit.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "“${spirit.motto}”",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun CommunityStatsSection(
    totalCount: Int,
    approvedCount: Int,
    pendingCount: Int,
    rejectedCount: Int,
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = tr(isTr, "FİYAT BİLDİRİM İSTATİSTİKLERİ", "PRICE SUBMISSION STATISTICS"),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = 0.8.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatMetricColumn(
                    title = strings.wizardTotalPrefix,
                    count = totalCount.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                StatMetricColumn(
                    title = tr(isTr, "✓ Onaylanan", "✓ Approved"),
                    count = approvedCount.toString(),
                    color = NeonEmerald,
                )
                StatMetricColumn(
                    title = tr(isTr, "⏳ İncelemede", "⏳ In Review"),
                    count = pendingCount.toString(),
                    color = NeonAmber,
                )
                StatMetricColumn(
                    title = tr(isTr, "✕ Reddedilen", "✕ Rejected"),
                    count = rejectedCount.toString(),
                    color = NeonRose,
                )
            }
        }
    }
}

@Composable
private fun StatMetricColumn(
    title: String,
    count: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = color,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun UserDealItemCard(
    deal: CommunityDeal,
    onOpenUrl: (String) -> Unit,
) {
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog" 
    val (statusColor, statusText) = when (deal.status) {
        DealStatus.APPROVED -> NeonEmerald to tr(isTr, "✓ Onaylandı", "✓ Approved")
        DealStatus.PENDING -> NeonAmber to tr(isTr, "⏳ İncelemede", "⏳ In Review")
        DealStatus.REJECTED -> NeonRose to tr(isTr, "✕ Reddedildi", "✕ Rejected")
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(0.5.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = deal.hardwareName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${deal.dealType.emoji} ${deal.dealType.localizedTitle(isTr)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Text(
                    text = "${deal.priceTry.roundToInt()} ₺",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonEmerald,
                )
            }

            if (!deal.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${tr(isTr, "Not", "Note")}: ${deal.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!deal.proofUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tr(isTr, "🔗 Kanıt Linkini Aç", "🔗 Open Proof Link"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                    modifier = Modifier.clickable { onOpenUrl(deal.proofUrl) },
                )
            }

            if (deal.status == DealStatus.REJECTED && !deal.reviewNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonRose.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, NeonRose.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "${tr(isTr, "Reddetme Nedeni", "Rejection Reason")}: ${deal.reviewNote}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRose,
                        modifier = Modifier.padding(6.dp),
                    )
                }
            }
        }
    }
}

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText
