package com.panda.myapplication.presentation.tools

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.engine.HardwareRecommendation
import com.panda.myapplication.domain.engine.WorkloadPhase
import com.panda.myapplication.domain.engine.WorkloadResolution
import com.panda.myapplication.domain.engine.WorkstationApp
import com.panda.myapplication.domain.engine.WorkstationCategory
import com.panda.myapplication.domain.engine.WorkstationRequirementEngine
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

    var appSearchQuery by remember { mutableStateOf("") }
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

    val filteredApps = remember(appSearchQuery) {
        if (appSearchQuery.isBlank()) {
            emptyList()
        } else {
            val q = appSearchQuery.trim().lowercase()
            WorkstationRequirementEngine.supportedApps.filter {
                it.name.lowercase().contains(q) ||
                it.shortName.lowercase().contains(q) ||
                it.category.titleTr.lowercase().contains(q) ||
                it.category.titleEn.lowercase().contains(q)
            }
        }
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
                            text = strings.workstationMatrixTitle,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
        ) {
            // 0. Anlık Uygulama Arama Çubuğu
            item {
                OutlinedTextField(
                    value = appSearchQuery,
                    onValueChange = { appSearchQuery = it },
                    placeholder = {
                        Text(
                            text = strings.workstationSearchPlaceholder,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
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
                        if (appSearchQuery.isNotBlank()) {
                            IconButton(onClick = { appSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Temizle",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardSurfaceElevated,
                        unfocusedContainerColor = CardSurface,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassBorder,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 1. Kategori Seçici veya Arama Sonuçları
            if (appSearchQuery.isBlank()) {
                item {
                    CategorySelectorTabs(
                        selectedCategory = selectedCategory,
                        onCategorySelect = { category ->
                            selectedCategory = category
                            val firstAppInCat = WorkstationRequirementEngine.supportedApps.firstOrNull { it.category == category }
                            if (firstAppInCat != null) {
                                selectedAppId = firstAppInCat.id
                                if (!firstAppInCat.supportedResolutions.contains(selectedResolution)) {
                                    selectedResolution = firstAppInCat.supportedResolutions.first()
                                }
                                if (!firstAppInCat.supportedPhases.contains(selectedPhase)) {
                                    selectedPhase = firstAppInCat.supportedPhases.first()
                                }
                            }
                        },
                        isTr = isTr,
                    )
                }

                // 2. Kategoriye Ait Uygulama Çipleri
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
            } else {
                // Arama Filtresi Aktifken Eşleşen Uygulamalar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isTr) "BULUNAN UYGULAMALAR (${filteredApps.size})" else "MATCHED APPS (${filteredApps.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                        )

                        if (filteredApps.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CardSurface,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = strings.workstationNoResults,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp),
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                filteredApps.forEach { app ->
                                    val isSelected = app.id == selectedAppId
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) ElectricBlue.copy(alpha = 0.24f) else CardSurface,
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) ElectricBlue else GlassBorder,
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedAppId = app.id
                                                selectedCategory = app.category
                                                if (!app.supportedResolutions.contains(selectedResolution)) {
                                                    selectedResolution = app.supportedResolutions.first()
                                                }
                                                if (!app.supportedPhases.contains(selectedPhase)) {
                                                    selectedPhase = app.supportedPhases.first()
                                                }
                                            },
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(text = app.category.emoji, fontSize = 14.sp)
                                            Text(
                                                text = app.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
            val appCount = remember(category) {
                WorkstationRequirementEngine.supportedApps.count { it.category == category }
            }

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
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = category.emoji, fontSize = 14.sp)
                    Text(
                        text = "$title ($appCount)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = app.category.emoji, fontSize = 20.sp)
                    Column {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (isTr) app.category.titleTr else app.category.titleEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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

            // Kritik Darboğaz Kutusu
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NeonRose.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = NeonRose,
                        modifier = Modifier.size(18.dp),
                    )
                    Column {
                        Text(
                            text = if (isTr) "KRİTİK DONANIM DARBOĞAZI" else "PRIMARY PERFORMANCE BOTTLENECK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonRose,
                            fontSize = 9.sp,
                        )
                        Text(
                            text = if (isTr) app.primaryBottleneckTr else app.primaryBottleneckEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))

            // Çözünürlük ve Proje Ölçeği Seçimi
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isTr) "1. Proje Çözünürlüğü ve Veri Ölçeği" else "1. Project Resolution & Scale",
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
                Text(
                    text = if (isTr) selectedResolution.descriptionTr else selectedResolution.descriptionEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }

            // İş Akışı Aşaması Seçimi
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isTr) "2. İş Akışı Aşaması / Kalite Hedefi" else "2. Workflow Phase / Quality Target",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    app.supportedPhases.forEach { phase ->
                        val isSelected = phase == selectedPhase
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ElectricBlue.copy(alpha = 0.2f) else CardSurface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ElectricBlue else GlassBorder.copy(alpha = 0.6f),
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onPhaseSelect(phase) },
                        ) {
                            Text(
                                text = if (isTr) phase.labelTr else phase.labelEn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
                Text(
                    text = if (isTr) selectedPhase.descriptionTr else selectedPhase.descriptionEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
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
        shape = RoundedCornerShape(12.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isTr) "Şeffaf ve Onaylı Test Verisi" else "Verified Benchmark Protocol",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonEmerald,
                )
                Text(
                    text = citation,
                    style = MaterialTheme.typography.labelSmall,
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
            width = if (isFeatured) 1.5.dp else 1.dp,
            color = if (isFeatured) tierColor else GlassBorder,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Başlık Çubuğu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(tierColor),
                    )
                    Text(
                        text = if (isTr) recommendation.tier.titleTr else recommendation.tier.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = tierColor,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonEmerald.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, NeonEmerald.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = "${recommendation.benchmarkConfidenceScore}% GÜVEN SKORU",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = NeonEmerald,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.4f))

            // 1. İşlemci (CPU) Bölümü
            HardwareComponentRow(
                icon = Icons.Rounded.DeveloperBoard,
                iconTint = NeonCyan,
                label = if (isTr) "İşlemci (CPU)" else "Processor (CPU)",
                chipset = recommendation.targetCpuChipset,
                specs = recommendation.cpuSpecsSummary,
                onSearchCatalog = { onSearchCpu(recommendation.targetCpuSearchQuery) },
                isTr = isTr,
            )

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))

            // 2. Ekran Kartı (GPU) Bölümü
            HardwareComponentRow(
                icon = Icons.Rounded.Computer,
                iconTint = NeonEmerald,
                label = if (isTr) "Ekran Kartı (GPU)" else "Graphics Card (GPU)",
                chipset = recommendation.targetGpuChipset,
                specs = "${recommendation.gpuSpecsSummary} · ${recommendation.vramRequirementGb}GB VRAM",
                onSearchCatalog = { onSearchGpu(recommendation.targetGpuSearchQuery) },
                isTr = isTr,
            )

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))

            // 3. Bellek (RAM) ve Depolama (SSD)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // RAM
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(0.5.dp, GlassBorder),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Memory,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${recommendation.targetRamCapacityGb} GB RAM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = recommendation.targetRamTypeAndSpeed,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // SSD
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(0.5.dp, GlassBorder),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = if (isTr) "Depolama" else "Storage",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = recommendation.targetStorageType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Deneyim ve Akıcılık Analiz Notu
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = tierColor.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (isTr) recommendation.estimatedExperienceNoteTr else recommendation.estimatedExperienceNoteEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
    }
}

@Composable
private fun HardwareComponentRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    chipset: String,
    specs: String,
    onSearchCatalog: () -> Unit,
    isTr: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.16f),
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
                Text(
                    text = chipset,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = specs,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onSearchCatalog,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = iconTint.copy(alpha = 0.16f),
                contentColor = iconTint,
            ),
            border = BorderStroke(0.5.dp, iconTint.copy(alpha = 0.4f)),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (isTr) "Tüm Modeller" else "All Models",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                )
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
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceElevated),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = NeonCyan.copy(alpha = 0.16f),
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.PrecisionManufacturing,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = if (isTr) "Mühendislik Analizi & Mimari Not" else "Engineering Architecture & Bottleneck Takeaway",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                )
                Text(
                    text = takeaway,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}
