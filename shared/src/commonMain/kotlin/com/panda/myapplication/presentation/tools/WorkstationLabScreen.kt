package com.panda.myapplication.presentation.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.engine.HardwareRecommendation
import com.panda.myapplication.domain.engine.HardwareTier
import com.panda.myapplication.domain.engine.WorkloadPhase
import com.panda.myapplication.domain.engine.WorkloadResolution
import com.panda.myapplication.domain.engine.WorkstationApp
import com.panda.myapplication.domain.engine.WorkstationCategory
import com.panda.myapplication.domain.engine.WorkstationRequirementEngine
import com.panda.myapplication.domain.engine.WorkstationRequirementProfile
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.presentation.theme.CardSurface
import com.panda.myapplication.presentation.theme.CardSurfaceElevated
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GlassBorder
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.theme.SlateDarkBackground
import com.panda.myapplication.presentation.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkstationLabScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToCatalogWithFilter: (query: String, category: HardwareCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish

    var selectedCategory by remember { mutableStateOf(WorkstationCategory.VIDEO_EDITING) }
    var selectedAppId by remember { mutableStateOf("premiere_pro") }
    var selectedResolution by remember { mutableStateOf(WorkloadResolution.UHD_4K) }
    var selectedPhase by remember { mutableStateOf(WorkloadPhase.REALTIME_PREVIEW) }

    val profile = remember(selectedAppId, selectedResolution, selectedPhase) {
        WorkstationRequirementEngine.calculateRequirements(
            appId = selectedAppId,
            resolution = selectedResolution,
            phase = selectedPhase,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SlateDarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTr) "Workstation & Creator Lab" else "Workstation & Creator Lab",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (isTr) "Adobe, 3D, CAD & AI Donanım Kılavuzu" else "Hardware Matrix for Pro Workloads",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Menü",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDarkBackground,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
        ) {
            // 1. Kategori Seçici
            item {
                CategorySelectorTabs(
                    selectedCategory = selectedCategory,
                    onCategorySelect = { category ->
                        selectedCategory = category
                        val firstAppInCat = WorkstationRequirementEngine.supportedApps.firstOrNull { it.category == category }
                        if (firstAppInCat != null) {
                            selectedAppId = firstAppInCat.id
                        }
                    },
                    isTr = isTr,
                )
            }

            // 2. Uygulama Seçim Çipleri
            item {
                AppSelectorChips(
                    category = selectedCategory,
                    selectedAppId = selectedAppId,
                    onAppSelect = { appId ->
                        selectedAppId = appId
                        val targetApp = WorkstationRequirementEngine.supportedApps.find { it.id == appId }
                        if (targetApp != null) {
                            if (!targetApp.supportedResolutions.contains(selectedResolution)) {
                                selectedResolution = targetApp.supportedResolutions.first()
                            }
                            if (!targetApp.supportedPhases.contains(selectedPhase)) {
                                selectedPhase = targetApp.supportedPhases.first()
                            }
                        }
                    },
                )
            }

            // 3. Çözünürlük ve İş Akışı Aşaması Seçici
            item {
                WorkloadConfigCard(
                    app = profile.app,
                    selectedResolution = selectedResolution,
                    selectedPhase = selectedPhase,
                    onResolutionSelect = { selectedResolution = it },
                    onPhaseSelect = { selectedPhase = it },
                    isTr = isTr,
                )
            }

            // 4. Kritik Darboğaz & Veri Güvenilirliği Başlığı
            item {
                DataCitationBanner(
                    app = profile.app,
                    citation = profile.dataCitation,
                    isTr = isTr,
                )
            }

            // 5. Üçlü Donanım Kademesi (Minimum, Önerilen Pro, Ağır Stüdyo)
            item {
                Text(
                    text = if (isTr) "🎯 HESAPLANAN SİSTEM GEREKSİNİMLERİ" else "🎯 CALCULATED SYSTEM TIERS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = NeonEmerald,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            item {
                HardwareTierCard(
                    recommendation = profile.minimumTier,
                    isFeatured = false,
                    isTr = isTr,
                    onSearchCpu = { query -> onNavigateToCatalogWithFilter(query, HardwareCategory.CPU) },
                    onSearchGpu = { query -> onNavigateToCatalogWithFilter(query, HardwareCategory.GPU) },
                )
            }

            item {
                HardwareTierCard(
                    recommendation = profile.recommendedTier,
                    isFeatured = true,
                    isTr = isTr,
                    onSearchCpu = { query -> onNavigateToCatalogWithFilter(query, HardwareCategory.CPU) },
                    onSearchGpu = { query -> onNavigateToCatalogWithFilter(query, HardwareCategory.GPU) },
                )
            }

            item {
                HardwareTierCard(
                    recommendation = profile.studioTier,
                    isFeatured = false,
                    isTr = isTr,
                    onSearchCpu = { query -> onNavigateToCatalogWithFilter(query, HardwareCategory.CPU) },
                    onSearchGpu = { query -> onNavigateToCatalogWithFilter(query, HardwareCategory.GPU) },
                )
            }

            // 6. Mühendislik Notu & Mimari Analiz
            item {
                TechnicalTakeawayCard(
                    takeaway = if (isTr) profile.keyTechnicalTakeawayTr else profile.keyTechnicalTakeawayEn,
                    isTr = isTr,
                )
            }
        }
    }
}

@Composable
private fun CategorySelectorTabs(
    selectedCategory: WorkstationCategory,
    onCategorySelect: (WorkstationCategory) -> Unit,
    isTr: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WorkstationCategory.entries.forEach { category ->
            val isSelected = category == selectedCategory
            val title = if (isTr) category.titleTr else category.titleEn
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) NeonCyan.copy(alpha = 0.18f) else CardSurfaceElevated,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) NeonCyan else GlassBorder,
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCategorySelect(category) },
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                    color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                )
            }
        }
    }
}

@Composable
private fun AppSelectorChips(
    category: WorkstationCategory,
    selectedAppId: String,
    onAppSelect: (String) -> Unit,
) {
    val apps = remember(category) {
        WorkstationRequirementEngine.supportedApps.filter { it.category == category }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        apps.forEach { app ->
            val isSelected = app.id == selectedAppId
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) ElectricBlue.copy(alpha = 0.22f) else CardSurface,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) ElectricBlue else GlassBorder.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAppSelect(app.id) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ElectricBlue else Color.Gray),
                    )
                    Text(
                        text = app.shortName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkloadConfigCard(
    app: WorkstationApp,
    selectedResolution: WorkloadResolution,
    selectedPhase: WorkloadPhase,
    onResolutionSelect: (WorkloadResolution) -> Unit,
    onPhaseSelect: (WorkloadPhase) -> Unit,
    isTr: Boolean,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonEmerald.copy(alpha = 0.14f),
                ) {
                    Text(
                        text = if (isTr) "PRO İŞ YÜKÜ" else "PRO WORKLOAD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = NeonEmerald,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))

            // Çözünürlük Seçimi
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isTr) "1. Çözünürlük ve Veri Formatı" else "1. Resolution & Media Format",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    app.supportedResolutions.forEach { res ->
                        val isSelected = res == selectedResolution
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardSurface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) NeonCyan else GlassBorder.copy(alpha = 0.6f),
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onResolutionSelect(res) },
                        ) {
                            Text(
                                text = if (isTr) res.labelTr else res.labelEn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
                Text(
                    text = if (isTr) selectedResolution.descriptionTr else selectedResolution.descriptionEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))

            // İş Akışı Aşaması (Playback vs Render)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isTr) "2. İş Akışı Aşaması (Anlık Kullanım vs Dışa Aktarma)" else "2. Pipeline Stage (Playback vs Export)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonAmber,
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    app.supportedPhases.forEach { phase ->
                        val isSelected = phase == selectedPhase
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NeonAmber.copy(alpha = 0.16f) else CardSurface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) NeonAmber else GlassBorder.copy(alpha = 0.6f),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onPhaseSelect(phase) },
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isTr) phase.labelTr else phase.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonAmber else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (isTr) phase.descriptionTr else phase.descriptionEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataCitationBanner(
    app: WorkstationApp,
    citation: String,
    isTr: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = NeonEmerald,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = if (isTr) "DOĞRULANMIŞ ENDÜSTRİYEL VERİ KAYNAĞI" else "VERIFIED BENCHMARK PROTOCOL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonEmerald,
                    fontSize = 10.sp,
                )
            }
            Text(
                text = citation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (isTr) "Temel Darboğaz:" else "Primary Bottleneck:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonRose,
                    fontSize = 10.sp,
                )
                Text(
                    text = if (isTr) app.primaryBottleneckTr else app.primaryBottleneckEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun HardwareTierCard(
    recommendation: HardwareRecommendation,
    isFeatured: Boolean,
    isTr: Boolean,
    onSearchCpu: (String) -> Unit,
    onSearchGpu: (String) -> Unit,
) {
    val tierColor = Color(recommendation.tier.badgeColorHex)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFeatured) CardSurfaceElevated else CardSurface,
        ),
        border = BorderStroke(
            width = if (isFeatured) 2.dp else 1.dp,
            color = if (isFeatured) NeonCyan else GlassBorder,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Başlık & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = tierColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(10.dp),
                    ) {}
                    Text(
                        text = if (isTr) recommendation.tier.titleTr else recommendation.tier.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = tierColor,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tierColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = "${recommendation.benchmarkConfidenceScore}% Uyum",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = tierColor,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
            }

            // Gerçek Dünya Deneyimi Notu
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SlateDarkBackground.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "💡 ${if (isTr) recommendation.estimatedExperienceNoteTr else recommendation.estimatedExperienceNoteEn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(10.dp),
                )
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.4f))

            // 1. İşlemci (CPU) Satırı + Katalog Arama Butonu
            ComponentSpecRowWithCatalogJump(
                icon = Icons.Rounded.DeveloperBoard,
                iconColor = NeonCyan,
                label = if (isTr) "Önerilen İşlemci Yonga Ailesi" else "Target CPU Architecture",
                chipsetName = recommendation.targetCpuChipset,
                specs = recommendation.cpuSpecsSummary,
                buttonText = if (isTr) "Tüm Modelleri Gör (${recommendation.targetCpuSearchQuery})" else "Browse All (${recommendation.targetCpuSearchQuery})",
                onSearchClick = { onSearchCpu(recommendation.targetCpuSearchQuery) },
            )

            // 2. Ekran Kartı (GPU) Satırı + Katalog Arama Butonu
            ComponentSpecRowWithCatalogJump(
                icon = Icons.Rounded.Speed,
                iconColor = NeonEmerald,
                label = if (isTr) "Önerilen GPU & VRAM" else "Target GPU & VRAM",
                chipsetName = recommendation.targetGpuChipset,
                specs = "${recommendation.vramRequirementGb}GB VRAM · ${recommendation.gpuSpecsSummary}",
                buttonText = if (isTr) "Tüm Modelleri Gör (${recommendation.targetGpuSearchQuery})" else "Browse All (${recommendation.targetGpuSearchQuery})",
                onSearchClick = { onSearchGpu(recommendation.targetGpuSearchQuery) },
            )

            // 3. Bellek (RAM)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Memory,
                    contentDescription = null,
                    tint = NeonAmber,
                    modifier = Modifier.size(18.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTr) "Sistem Belleği (RAM)" else "System RAM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = "${recommendation.targetRamCapacityGb} GB RAM",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = recommendation.targetRamTypeAndSpeed,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                }
            }

            // 4. Depolama (Storage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Storage,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(18.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTr) "Depolama & Scratch Disk" else "Storage & Scratch",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = recommendation.targetStorageType,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentSpecRowWithCatalogJump(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    chipsetName: String,
    specs: String,
    buttonText: String,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
                Text(
                    text = chipsetName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = iconColor,
                )
                Text(
                    text = specs,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }

        // Kullanıcının özellikle istediği: "sadece bir model seçme, tüm o çipi dahil et"
        Button(
            onClick = onSearchClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = iconColor.copy(alpha = 0.14f),
                contentColor = iconColor,
            ),
            border = BorderStroke(1.dp, iconColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp),
            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun TechnicalTakeawayCard(
    takeaway: String,
    isTr: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PrecisionManufacturing,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = if (isTr) "🔬 MÜHENDİSLİK & MİMARİ ANALİZ NOTU" else "🔬 ENGINEERING ARCHITECTURE INSIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    fontSize = 10.sp,
                )
            }
            Text(
                text = takeaway,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                lineHeight = 17.sp,
            )
        }
    }
}
