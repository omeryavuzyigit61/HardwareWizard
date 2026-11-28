package com.panda.myapplication.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.config.FeatureFlags
import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.DealStatus
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.repository.CommunityPriceRepository
import com.panda.myapplication.domain.repository.UserRepository
import com.panda.myapplication.presentation.components.FluidOrganicCard
import com.panda.myapplication.presentation.hardware_list.Spacing
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GoldAccent
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.LocalAppStrings
import com.panda.myapplication.presentation.util.openSafeHttpsUrl
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
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by userRepository.currentUser.collectAsState()
    val isLoggedIn by userRepository.isLoggedIn.collectAsState()
    val allDeals by communityPriceRepository.observeAllDeals().collectAsState(initial = emptyList())
    val isAdminSession by communityPriceRepository.isAdminLoggedIn.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    val user = currentUser
    val isMasterAdmin = isAdminSession

    LaunchedEffect(user?.id) {
        if (user != null) {
            userRepository.refreshCurrentUserStats()
        }
    }

    val userDeals = remember(user, allDeals) {
        if (user == null) emptyList()
        else allDeals.filter { deal ->
            deal.userDisplayName.equals(user.username, ignoreCase = true) ||
                deal.userEmail?.equals(user.email, ignoreCase = true) == true
        }
    }

    val approvedCount = userDeals.count { it.status == DealStatus.APPROVED }
    val pendingCount = userDeals.count { it.status == DealStatus.PENDING }
    val rejectedCount = userDeals.count { it.status == DealStatus.REJECTED }
    val totalCount = userDeals.size

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            ProfileTopBar(
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings,
            )
        },
        modifier = modifier.statusBarsPadding().navigationBarsPadding(),
    ) { innerPadding ->
        if (user == null || !isLoggedIn) {
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
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(90.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(44.dp),
                        )
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
                    text = if (FeatureFlags.IS_PRICING_ENABLED) {
                        tr(isTr, "Fiyat bildirimlerinizi takip etmek, onay/ret istatistiklerinizi görmek ve donanım ruhunuzu belirlemek için giriş yapın veya kayıt olun.", "Sign in or register to track price reports, view approval stats, and customize your hardware profile.")
                    } else {
                        tr(isTr, "Donanım ruhunuzu belirlemek, itibar rütbenizi yükseltmek, topluluk tuning profillerine ve sistem vitrinine katılmak için giriş yapın veya kayıt olun.", "Sign in or register to customize your hardware profile, earn reputation ranks, and join community tuning profiles and rig showcases.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (user.isSuspended) {
                    item(key = "suspension_banner") {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonRose.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.55f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GppMaybe,
                                    contentDescription = null,
                                    tint = NeonRose,
                                    modifier = Modifier.size(22.dp),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tr(isTr, "Topluluk Erişimi Kısıtlı (${user.remainingSuspensionText})", "Community Access Restricted (${user.remainingSuspensionText})"),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonRose,
                                    )
                                    user.banReason?.takeIf { it.isNotBlank() }?.let { reason ->
                                        Text(
                                            text = reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 1. Profile Hero Card + Edit Profile Trigger
                item(key = "profile_hero") {
                    ProfileHeroCard(
                        user = user,
                        isMasterAdmin = isMasterAdmin,
                        onEditClick = { showEditDialog = true },
                    )
                }

                // 2. Reputation XP, Rank & Community Contributions Card
                item(key = "reputation_card") {
                    ReputationAndContributionsCard(
                        user = user,
                        isTr = isTr,
                    )
                }

                // 3. Spirit Hardware Card ("Ruh Donanımı")
                item(key = "spirit_hardware_card") {
                    SpiritHardwareShowcaseCard(
                        user = user,
                        onEditSpiritClick = { showEditDialog = true },
                    )
                }

                // 4. Price Submission Statistics Grid (Only shown when pricing engine is active)
                if (FeatureFlags.IS_PRICING_ENABLED) {
                    item(key = "stats_grid") {
                        CommunityStatsSection(
                            totalCount = totalCount,
                            approvedCount = approvedCount,
                            pendingCount = pendingCount,
                            rejectedCount = rejectedCount,
                        )
                    }
                }

                // 5. Admin Portal Info (PC Web Portal Notice)
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
                                    Icon(
                                        imageVector = Icons.Rounded.Security,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(20.dp),
                                    )
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
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = tr(isTr, "Uygulama Ayarları", "Application Settings"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = tr(isTr, "Tema, 7 Dil, Birimler, Uzman Modu, Kaynakça", "Theme, 7 Languages, Units, Expert Mode, Sources"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }

                // User Submission History & Deals List (Only shown when pricing engine is active)
                if (FeatureFlags.IS_PRICING_ENABLED) {
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

                    if (userDeals.isEmpty()) {
                        item(key = "no_deals") {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalOffer,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(30.dp),
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = tr(isTr, "Henüz fiyat bildiriminde bulunmadınız.", "You have not submitted any price reports yet."),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = tr(isTr, "Ürün detay sayfalarındaki 'Fiyat Bildir' butonunu kullanarak ilk fırsatınızı paylaşabilirsiniz.", "Use the 'Report Price' button on product detail pages to share your first deal."),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NeonCyan,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    } else {
                        items(userDeals, key = { it.id }) { deal ->
                            UserDealItemCard(deal = deal, onOpenUrl = { url ->
                                uriHandler.openSafeHttpsUrl(url)
                            })
                        }
                    }
                }

                // Logout Action
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

    if (showEditDialog && user != null) {
        EditUserProfileDialog(
            user = user,
            isTr = isTr,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                coroutineScope.launch {
                    when (val res = userRepository.updateProfile(updated)) {
                        is DomainResult.Success -> {
                            showEditDialog = false
                            snackbarHostState.showSnackbar(
                                tr(isTr, "Profil bilgileriniz güncellendi.", "Profile updated successfully.")
                            )
                        }
                        is DomainResult.Failure -> {
                            val msg = (res.error as? DomainError.Unknown)?.cause?.message
                                ?: tr(isTr, "Profil güncellenemedi.", "Failed to update profile.")
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun ProfileTopBar(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
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
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
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
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = tr(isTr, "Ayarlar", "Settings"),
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private fun SpiritHardware.iconVector(): ImageVector = when (this) {
    SpiritHardware.OVERCLOCKED_CPU -> Icons.Rounded.Memory
    SpiritHardware.FLAGSHIP_GPU -> Icons.Rounded.SportsEsports
    SpiritHardware.LOW_LATENCY_RAM -> Icons.Rounded.Bolt
    SpiritHardware.LIQUID_COOLER -> Icons.Rounded.AcUnit
    SpiritHardware.TITANIUM_PSU -> Icons.Rounded.Shield
    SpiritHardware.GEN5_NVME_SSD -> Icons.Rounded.RocketLaunch
}

private fun Gender.localizedLabel(isTr: Boolean): String = when (this) {
    Gender.MALE -> if (isTr) "Erkek" else "Male"
    Gender.FEMALE -> if (isTr) "Kadın" else "Female"
    Gender.UNSPECIFIED -> if (isTr) "Belirtilmedi" else "Unspecified"
}

private fun SpiritHardware.localizedTitle(isTr: Boolean): String = if (isTr) title else when (this) {
    SpiritHardware.OVERCLOCKED_CPU -> "Overclock Beast Processor (CPU)"
    SpiritHardware.FLAGSHIP_GPU -> "Flagship Graphics Card (GPU)"
    SpiritHardware.LOW_LATENCY_RAM -> "Low-Latency Memory Kit (RAM)"
    SpiritHardware.LIQUID_COOLER -> "360mm Custom Liquid Cooler (AIO)"
    SpiritHardware.TITANIUM_PSU -> "80+ Titanium Power Supply (PSU)"
    SpiritHardware.GEN5_NVME_SSD -> "PCIe 5.0 NVMe SSD"
}

private fun SpiritHardware.localizedMotto(isTr: Boolean): String = if (isTr) motto else when (this) {
    SpiritHardware.OVERCLOCKED_CPU -> "Pure compute power, brain and leader of the entire rig."
    SpiritHardware.FLAGSHIP_GPU -> "Ultra graphics, zero latency, and pure visual fidelity."
    SpiritHardware.LOW_LATENCY_RAM -> "Lightning-fast reflexes, agile multitasking specialist."
    SpiritHardware.LIQUID_COOLER -> "Cool-headed and whisper-quiet even under extreme stress."
    SpiritHardware.TITANIUM_PSU -> "Unshakable energy backbone, full protection and stability."
    SpiritHardware.GEN5_NVME_SSD -> "Zero patience for loading screens — instant read and write."
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileHeroCard(
    user: UserProfile,
    isMasterAdmin: Boolean,
    onEditClick: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val spiritColor = Color(user.spiritHardware.colorHex)

    FluidOrganicCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = if (isMasterAdmin) NeonAmber else spiritColor,
        secondaryColor = ElectricBlue,
        contentPadding = Spacing.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = spiritColor.copy(alpha = 0.16f),
                    border = BorderStroke(2.dp, spiritColor),
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = user.spiritHardware.iconVector(),
                            contentDescription = null,
                            tint = spiritColor,
                            modifier = Modifier.size(30.dp),
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (isMasterAdmin) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonAmber.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, NeonAmber),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Security,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(11.dp),
                                    )
                                    Text(
                                        text = tr(isTr, "YÖNETİCİ", "ADMIN"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonAmber,
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
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

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Text(
                                text = user.gender.localizedLabel(isTr),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Surface(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(10.dp),
                    color = NeonCyan.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = tr(isTr, "Düzenle", "Edit"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReputationAndContributionsCard(
    user: UserProfile,
    isTr: Boolean,
) {
    val score = user.reputationScore.coerceAtLeast(0)
    val (nextThreshold, nextRankName) = when {
        score < 51 -> 51 to tr(isTr, "Silikon Meraklısı", "Silicon Enthusiast")
        score < 201 -> 201 to tr(isTr, "Undervolt Ustası", "Undervolt Master")
        score < 501 -> 501 to tr(isTr, "Overclock Şampiyonu", "Overclock Champion")
        score < 1200 -> 1200 to tr(isTr, "Efsanevi Mühendis", "Legendary Engineer")
        else -> 1200 to tr(isTr, "Maksimum Rütbe", "Max Rank Reached")
    }
    val progress = if (score >= 1200) 1f else (score.toFloat() / nextThreshold.toFloat()).coerceIn(0.04f, 1f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = tr(isTr, "İTİBAR PUANI & RÜTBE", "REPUTATION & RANK"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent,
                            letterSpacing = 0.7.sp,
                        )
                        Text(
                            text = user.effectiveBadge,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldAccent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = "$score XP",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GoldAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (score >= 1200) {
                    tr(isTr, "En yüksek topluluk mühendislik rütbesine ulaştınız!", "You have reached the highest community engineering rank!")
                } else {
                    tr(
                        isTr,
                        "Sonraki rütbe: $nextRankName (${(nextThreshold - score).coerceAtLeast(1)} XP kaldı • OC/UV +15 XP, İnceleme +10 XP)",
                        "Next rank: $nextRankName (${(nextThreshold - score).coerceAtLeast(1)} XP left • OC/UV +15 XP, Pro Tip +10 XP)",
                    )
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatMetricColumn(
                    title = tr(isTr, "OC / UV Profili", "OC / UV Profiles"),
                    count = user.totalTuningsCount.toString(),
                    color = NeonCyan,
                )
                StatMetricColumn(
                    title = tr(isTr, "Ürün İpucu", "Pro Tips"),
                    count = user.totalTipsCount.toString(),
                    color = NeonEmerald,
                )
                if (FeatureFlags.IS_PRICING_ENABLED) {
                    StatMetricColumn(
                        title = tr(isTr, "Fırsat Bildirimi", "Deal Reports"),
                        count = user.totalDealsCount.toString(),
                        color = NeonPurple,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpiritHardwareShowcaseCard(
    user: UserProfile,
    onEditSpiritClick: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val spirit = user.spiritHardware
    val spiritColor = Color(spirit.colorHex)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, spiritColor.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onEditSpiritClick() },
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
                Icon(
                    imageVector = spirit.iconVector(),
                    contentDescription = null,
                    tint = spiritColor,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = spirit.localizedTitle(isTr),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "“${spirit.localizedMotto(isTr)}”",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EditUserProfileDialog(
    user: UserProfile,
    isTr: Boolean,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit,
) {
    var username by remember(user.username) { mutableStateOf(user.username) }
    var birthDateIso by remember(user.birthDateIso) { mutableStateOf(user.birthDateIso) }
    var selectedGender by remember(user.gender) { mutableStateOf(user.gender) }
    var selectedSpirit by remember(user.spiritHardware) { mutableStateOf(user.spiritHardware) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = tr(isTr, "Profili Düzenle", "Edit Profile"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                validationError?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRose,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        validationError = null
                    },
                    label = { Text(tr(isTr, "Kullanıcı Adı", "Username")) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = birthDateIso,
                    onValueChange = {
                        birthDateIso = it
                        validationError = null
                    },
                    label = { Text(tr(isTr, "Doğum Tarihi (YYYY-AA-GG)", "Birth Date (YYYY-MM-DD)")) },
                    placeholder = { Text("2000-01-15") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = tr(isTr, "Cinsiyet", "Gender"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Gender.entries.forEach { g ->
                        val isSelected = selectedGender == g
                        Surface(
                            onClick = { selectedGender = g },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Text(
                                text = g.localizedLabel(isTr),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                Text(
                    text = tr(isTr, "Ruh Donanımın", "Spirit Hardware"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SpiritHardware.entries.forEach { sp ->
                        val isSelected = selectedSpirit == sp
                        val spColor = Color(sp.colorHex)
                        Surface(
                            onClick = { selectedSpirit = sp },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) spColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) spColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = sp.iconVector(),
                                    contentDescription = null,
                                    tint = spColor,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = sp.localizedTitle(isTr),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) spColor else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanUsername = username.trim().removePrefix("@")
                    if (cleanUsername.length < 3) {
                        validationError = tr(isTr, "Kullanıcı adı en az 3 karakter olmalıdır.", "Username must be at least 3 characters.")
                        return@Button
                    }
                    val dateParts = birthDateIso.trim().split("-")
                    val year = dateParts.getOrNull(0)?.toIntOrNull()
                    val month = dateParts.getOrNull(1)?.toIntOrNull()
                    val day = dateParts.getOrNull(2)?.toIntOrNull()
                    val (curYear, _, _) = UserProfile.currentCalendarDate()
                    if (dateParts.size != 3 || year == null || month == null || day == null ||
                        month !in 1..12 || day !in 1..31 || year !in (curYear - 110)..(curYear - 13)
                    ) {
                        validationError = tr(
                            isTr,
                            "Lütfen geçerli bir doğum tarihi girin (YYYY-AA-GG, en az 13 yaş).",
                            "Please enter a valid birth date (YYYY-MM-DD, minimum age 13).",
                        )
                        return@Button
                    }
                    val normalizedDate = "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                    onSave(
                        user.copy(
                            username = cleanUsername,
                            birthDateIso = normalizedDate,
                            gender = selectedGender,
                            spiritHardware = selectedSpirit,
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = tr(isTr, "Kaydet", "Save"),
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr(isTr, "Vazgeç", "Cancel"))
            }
        },
    )
}

@Composable
private fun CommunityStatsSection(
    totalCount: Int,
    approvedCount: Int,
    pendingCount: Int,
    rejectedCount: Int,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                    title = tr(isTr, "Onaylanan", "Approved"),
                    count = approvedCount.toString(),
                    color = NeonEmerald,
                )
                StatMetricColumn(
                    title = tr(isTr, "İncelemede", "In Review"),
                    count = pendingCount.toString(),
                    color = NeonAmber,
                )
                StatMetricColumn(
                    title = tr(isTr, "Reddedilen", "Rejected"),
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun UserDealItemCard(
    deal: CommunityDeal,
    onOpenUrl: (String) -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val (statusColor, statusText) = when (deal.status) {
        DealStatus.APPROVED -> NeonEmerald to tr(isTr, "Onaylandı", "Approved")
        DealStatus.PENDING -> NeonAmber to tr(isTr, "İncelemede", "In Review")
        DealStatus.REJECTED -> NeonRose to tr(isTr, "Reddedildi", "Rejected")
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
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
                    text = deal.dealType.localizedTitle(isTr),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onOpenUrl(deal.proofUrl) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Link,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = tr(isTr, "Kanıt Linkini Aç", "Open Proof Link"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                    )
                }
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
