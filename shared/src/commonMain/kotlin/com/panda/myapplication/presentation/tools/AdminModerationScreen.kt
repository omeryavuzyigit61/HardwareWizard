package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.panda.myapplication.presentation.theme.NeonPurple
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

import com.panda.myapplication.domain.model.ContentReport
import com.panda.myapplication.domain.model.ReportStatus
import com.panda.myapplication.domain.model.ReportTargetType
import com.panda.myapplication.domain.repository.CommunityReportRepository

@Composable
fun AdminModerationScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    repository: CommunityPriceRepository = koinInject(),
    tuningVaultRepository: com.panda.myapplication.domain.repository.TuningVaultRepository = koinInject(),
    reportRepository: CommunityReportRepository = koinInject(),
    modifier: Modifier = Modifier,
) {
    val pendingTunings by tuningVaultRepository.observePendingTunings().collectAsState(initial = emptyList())
    val pendingReports by reportRepository.observePendingReports().collectAsState(initial = emptyList())
    var selectedModerationTab by remember { mutableStateOf(0) } // 0: Deals, 1: Tuning, 2: Reports
    var tuningRejectDialogProfileId by remember { mutableStateOf<String?>(null) }
    var tuningRejectReasonText by remember { mutableStateOf("") }
    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
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
                title = tr(isTr, "Yönetici Moderasyon Paneli", "Admin Moderation Panel"),
                subtitle = "${tr(isTr, "Topluluk Fiyat Bildirimleri", "Community Price Submissions")} ($pendingCount ${tr(isTr, "Bekleyen", "Pending")})",
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
                                    text = tr(isTr, "SÜPER YÖNETİCİ: Ömer Admin", "SUPER ADMIN: Ömer Admin"),
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
                                    text = tr(isTr, "Çıkış Yap", "Sign Out"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonRose,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                // Main Moderation Tab Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedModerationTab == 0) NeonCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (selectedModerationTab == 0) NeonCyan else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedModerationTab = 0 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "🏷️ Fiyat Fırsatları ($pendingCount)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedModerationTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedModerationTab == 0) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedModerationTab == 1) NeonPurple.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (selectedModerationTab == 1) NeonPurple else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedModerationTab = 1 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "⚡ OC/UV (${pendingTunings.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedModerationTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedModerationTab == 1) NeonPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedModerationTab == 2) NeonRose.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (selectedModerationTab == 2) NeonRose else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedModerationTab = 2 }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "🚨 Raporlar (${pendingReports.size})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedModerationTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedModerationTab == 2) NeonRose else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.small))

                if (selectedModerationTab == 1) {
                    // Tuning Moderation List
                    if (pendingTunings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚡", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Onay bekleyen OC / UV ayarı bulunmuyor.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(pendingTunings, key = { it.id }) { tuning ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column {
                                                Text(
                                                    text = tuning.hardwareName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Text(
                                                    text = "${tuning.profileTitle} (${tuning.tuningType.name})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = NeonCyan,
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surface,
                                            ) {
                                                Text(
                                                    text = "🌐 ${tuning.languageCode.uppercase()}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeonAmber,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            tuning.voltageOffsetMv?.let {
                                                Text("Voltaj: ${it.toInt()}mV", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            tuning.clockMhz?.let {
                                                Text("Saat: ${it.toInt()}MHz", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            tuning.tempDelta?.let {
                                                Text("Sıcaklık: ${it.toInt()}°C", style = MaterialTheme.typography.labelSmall, color = NeonEmerald)
                                            }
                                        }

                                        val sysParts = listOfNotNull(
                                            tuning.motherboardName?.let { "🖥️ $it" },
                                            tuning.coolerName?.let { "❄️ $it" },
                                            tuning.ramSpec?.let { "🧠 $it" },
                                            tuning.secondaryHardware?.let { "🎮 $it" },
                                        )
                                        if (sysParts.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Sistem: ${sysParts.joinToString(" • ")}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NeonCyan,
                                            )
                                        }

                                        if (!tuning.guideNotes.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Rehber: ${tuning.guideNotes}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "Yazar: @${tuning.userDisplayName} (${tuning.userBadge})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f),
                                            )
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        tuningVaultRepository.approveTuningProfile(tuning.id)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = Color.Black),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            ) {
                                                Text("✓ Onayla (+50p)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Button(
                                                onClick = {
                                                    tuningRejectDialogProfileId = tuning.id
                                                    tuningRejectReasonText = "Seçilen dil ile metin uyuşmuyor."
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonRose, contentColor = Color.White),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            ) {
                                                Text("✕ Reddet", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedModerationTab == 2) {
                    // User Reports Moderation List
                    if (pendingReports.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🛡️", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "İncelenmeyi bekleyen kullanıcı raporu bulunmuyor.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(pendingReports, key = { it.id }) { report ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, if (report.isSelfReport) NeonCyan.copy(alpha = 0.5f) else NeonRose.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                ) {
                                                    Text(
                                                        text = report.targetType.localizedTitle(isTr),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NeonPurple,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }

                                                if (report.isSelfReport) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = NeonCyan.copy(alpha = 0.2f),
                                                    ) {
                                                        Text(
                                                            text = "👤 KENDİ PAYLAŞIMI",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Black,
                                                            color = NeonCyan,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        )
                                                    }
                                                }
                                            }

                                            Text(
                                                text = report.reason.localizedTitle(isTr),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (report.isSelfReport) NeonCyan else NeonRose,
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = report.targetTitle,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Hedef Kullanıcı: @" + report.reportedUserDisplayName + " • Bildiren: @" + report.reporterUserDisplayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )

                                        if (report.details.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                Text(
                                                    text = "Açıklama: \"" + report.details + "\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(8.dp),
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        reportRepository.resolveReport(report.id, ReportStatus.RESOLVED_REMOVED)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonRose, contentColor = Color.White),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            ) {
                                                Text("🗑️ İçeriği Kaldır", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        reportRepository.resolveReport(report.id, ReportStatus.RESOLVED_DISMISSED)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            ) {
                                                Text("✓ Raporu Kapat", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(tr(isTr, "Donanım veya kullanıcı ara...", "Search hardware or user...")) },
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
                        label = { Text("${tr(isTr, "⏳ Bekleyenler", "⏳ Pending")} ($pendingCount)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonAmber.copy(alpha = 0.2f),
                            selectedLabelColor = NeonAmber,
                        ),
                    )
                    FilterChip(
                        selected = selectedFilter == DealStatus.APPROVED,
                        onClick = { selectedFilter = DealStatus.APPROVED },
                        label = { Text(tr(isTr, "✓ Onaylı", "✓ Approved")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = NeonEmerald,
                        ),
                    )
                    FilterChip(
                        selected = selectedFilter == DealStatus.REJECTED,
                        onClick = { selectedFilter = DealStatus.REJECTED },
                        label = { Text(tr(isTr, "✕ Reddedilen", "✕ Rejected")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonRose.copy(alpha = 0.2f),
                            selectedLabelColor = NeonRose,
                        ),
                    )
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("${tr(isTr, "Tümü", "All")} (${allDeals.size})") },
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
                            text = tr(isTr, "Bu filtreye uygun bildirim bulunamadı.", "No submissions found for this filter."),
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
                                        repository.rejectDeal(deal.id, tr(isTr, "Geçersiz kanıt veya piyasa dışı bildirim.", "Invalid proof or out-of-market submission."))
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
                } // End of selectedModerationTab == 0
            }
        }
    }

    tuningRejectDialogProfileId?.let { profileId ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { tuningRejectDialogProfileId = null },
            title = { Text("Ayar Paylaşımını Reddet", fontWeight = FontWeight.Bold, color = NeonRose) },
            text = {
                Column {
                    Text("Reddetme gerekçesini belirtin (Kullanıcı profilinde görecektir):", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tuningRejectReasonText,
                        onValueChange = { tuningRejectReasonText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            tuningVaultRepository.rejectTuningProfile(profileId, tuningRejectReasonText.trim())
                            tuningRejectDialogProfileId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRose, contentColor = Color.White),
                ) {
                    Text("Reddet")
                }
            },
            dismissButton = {
                TextButton(onClick = { tuningRejectDialogProfileId = null }) {
                    Text("Vazgeç")
                }
            },
        )
    }
}

@Suppress("DEPRECATION")
@Composable
private fun AdminDealItemCard(
    deal: CommunityDeal,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDelete: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

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
                        text = "${tr(isTr, "Kanıt", "Proof")}: ${deal.proofUrl}",
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
                        Text(tr(isTr, "Aç / Kopyala", "Open / Copy"), style = MaterialTheme.typography.labelSmall, color = ElectricBlue)
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
                    Text(tr(isTr, "🗑️ Sil", "🗑️ Delete"), style = MaterialTheme.typography.labelSmall, color = NeonRose)
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
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"

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
                    text = tr(isTr, "Yönetici Moderasyon Girişi", "Admin Moderation Login"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = tr(isTr, "Topluluk fiyat bildirimlerini incelemek, onaylamak veya reddetmek için yönetici PIN kodunuzu girin.", "Enter your admin PIN to review, approve, or reject community price submissions."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        pinText = it
                        isError = false
                    },
                    label = { Text(tr(isTr, "Yönetici PIN Kodu", "Admin PIN Code")) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (isError) {
                    Text(
                        text = tr(isTr, "Hatalı PIN kodu! (Varsayılan PIN: 1923)", "Invalid PIN code! (Default PIN: 1923)"),
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
                    val strings = com.panda.myapplication.presentation.util.LocalAppStrings.current
                    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
                    Text(
                        text = tr(isTr, "🛡️ Yönetici Olarak Giriş Yap", "🛡️ Sign In as Admin"),
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

private fun tr(isTr: Boolean, trText: String, enText: String): String = if (isTr) trText else enText
