package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.TuningProfile
import com.panda.myapplication.domain.model.TuningType
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.repository.TuningVaultRepository
import com.panda.myapplication.presentation.components.HardwarePickerDialog
import com.panda.myapplication.presentation.components.rememberProofImagePicker
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.LocalAppStrings
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class ComponentPickerSlot {
    TARGET_HARDWARE,
    MOTHERBOARD,
    COOLER,
    RAM,
    SECONDARY_HARDWARE,
}

@Composable
fun TuningShareDialog(
    hardwareId: String = "",
    hardwareName: String = "",
    hardwareCategory: String = "CPU",
    userId: String,
    userDisplayName: String,
    userBadge: String = "Lehim Çırağı",
    hardwareRepository: HardwareRepository = koinInject(),
    activeBuildRepository: ActiveBuildRepository = koinInject(),
    tuningVaultRepository: TuningVaultRepository = koinInject(),
    onDismiss: () -> Unit,
    onSubmit: (TuningProfile) -> Unit,
) {
    val strings = LocalAppStrings.current
    val isTr = strings.activeStatusBadge == "AKTİF" || strings.tabCatalog == "Keşfet & Katalog"
    val coroutineScope = rememberCoroutineScope()

    var selectedHardwareId by remember(hardwareId) { mutableStateOf(hardwareId) }
    var selectedHardwareName by remember(hardwareName) { mutableStateOf(hardwareName) }
    var selectedCategory by remember(hardwareCategory) {
        mutableStateOf(if (hardwareCategory.isNotBlank()) hardwareCategory else "CPU")
    }

    // Uyumluluk motoru için seçilen gerçek donanım nesneleri
    var selectedTargetItem by remember { mutableStateOf<HardwareItem?>(null) }
    var selectedMotherboardItem by remember { mutableStateOf<HardwareItem?>(null) }
    var selectedCoolerItem by remember { mutableStateOf<HardwareItem?>(null) }
    var selectedRamItem by remember { mutableStateOf<HardwareItem?>(null) }
    var selectedSecondaryItem by remember { mutableStateOf<HardwareItem?>(null) }

    var motherboardName by remember { mutableStateOf("") }
    var coolerName by remember { mutableStateOf("") }
    var ramSpec by remember { mutableStateOf("") }
    var secondaryHardware by remember { mutableStateOf("") }
    var compatibilityResetWarning by remember { mutableStateOf<String?>(null) }

    var title by remember { mutableStateOf("") }
    // Çoklu seçim (Multi-select): İşlemci için aynı anda Undervolt + PBO vb. seçilebilir
    var selectedTypes by remember { mutableStateOf(setOf(TuningType.UNDERVOLT)) }
    var selectedLanguage by remember { mutableStateOf("tr") }

    var voltageOffsetStr by remember { mutableStateOf("") }
    var clockMhzStr by remember { mutableStateOf("") }
    var tempBeforeStr by remember { mutableStateOf("") }
    var tempAfterStr by remember { mutableStateOf("") }
    var benchName by remember { mutableStateOf("Cinebench R23") }
    var scoreBeforeStr by remember { mutableStateOf("") }
    var scoreAfterStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var proofImageUrl by remember { mutableStateOf("") }
    var showUrlInputField by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadedImageStatus by remember { mutableStateOf<String?>(null) }
    var uploadErrorText by remember { mutableStateOf<String?>(null) }

    var activePickerSlot by remember { mutableStateOf<ComponentPickerSlot?>(null) }

    val activeBuild by activeBuildRepository.activeBuild.collectAsState()
    val allCatalogResult by remember(hardwareRepository) {
        hardwareRepository.observeHardwareItems()
    }.collectAsState(initial = DomainResult.Success(emptyList()))

    // Başlangıçta hardwareId verilmişse katalogdan gerçek HardwareItem nesnesini bul (Soket/DDR kilidi için)
    LaunchedEffect(hardwareId, allCatalogResult) {
        if (hardwareId.isNotBlank() && selectedTargetItem == null) {
            val items = (allCatalogResult as? DomainResult.Success)?.data.orEmpty()
            val match = items.firstOrNull { it.id.value == hardwareId }
            if (match != null) {
                selectedTargetItem = match
                selectedCategory = match.category.name
            }
        }
    }

    // Sanal ActiveBuild: PC Sihirbazı ile birebir aynı uyumluluk kilidini çalıştırır
    val virtualBuildForCompatibility = remember(
        selectedTargetItem,
        selectedCategory,
        selectedMotherboardItem,
        selectedCoolerItem,
        selectedRamItem,
        selectedSecondaryItem,
    ) {
        val cpuItem = if (selectedCategory == "CPU") selectedTargetItem else selectedSecondaryItem
        val gpuItem = if (selectedCategory == "GPU") selectedTargetItem else selectedSecondaryItem
        ActiveBuild(
            cpu = cpuItem,
            gpu = gpuItem,
            motherboard = selectedMotherboardItem,
            cooler = selectedCoolerItem,
            ram = selectedRamItem,
        )
    }

    // Galeri & Kamera başlatıcısı (<= 150 KB akıllı sıkıştırma + Supabase Storage yüklemesi)
    val imagePickerLauncher = rememberProofImagePicker(
        onImageCompressed = { compressedBytes ->
            isUploadingImage = true
            uploadErrorText = null
            val kbSize = (compressedBytes.size / 1024).coerceAtLeast(1)
            val fileName = "proof_${userId}_${System.currentTimeMillis()}.jpg"
            coroutineScope.launch {
                when (val res = tuningVaultRepository.uploadProofImage(compressedBytes, fileName)) {
                    is DomainResult.Success -> {
                        proofImageUrl = res.data
                        uploadedImageStatus = if (isTr) {
                            "Görsel sıkıştırıldı ($kbSize KB) ve buluta yüklendi"
                        } else {
                            "Compressed ($kbSize KB) & uploaded to cloud"
                        }
                        isUploadingImage = false
                    }
                    is DomainResult.Failure -> {
                        uploadErrorText = if (isTr) {
                            "Bulut yüklemesi başarısız oldu, lütfen tekrar deneyin veya URL kullanın."
                        } else {
                            "Cloud upload failed, please retry or use URL."
                        }
                        isUploadingImage = false
                    }
                }
            }
        },
        onError = { err ->
            uploadErrorText = err
            isUploadingImage = false
        },
    )

    val languages = listOf(
        "tr" to "TR Türkçe",
        "en" to "EN English",
        "de" to "DE Deutsch",
        "es" to "ES Español",
        "fr" to "FR Français",
        "ru" to "RU Русский",
        "ja" to "JA 日本語",
    )

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
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isTr) "Silikon Kasası: Sistem & Ayar Paylaş" else "Silicon Vault: Share Rig & Tuning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (isTr) {
                                "Uyumlu sistem parçalarınızı seçin ve OC/UV deneyiminizi ekleyin"
                            } else {
                                "Select compatible rig parts and share your OC/UV profile"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 1. Aktif PC Sihirbazı Sisteminden Tek Tıkla Doldurma
                if (activeBuild.items.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonCyan.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (selectedHardwareName.isBlank()) {
                                    activeBuild.cpu?.let {
                                        selectedTargetItem = it
                                        selectedHardwareId = it.id.value
                                        selectedHardwareName = it.displayName
                                        selectedCategory = "CPU"
                                    } ?: activeBuild.gpu?.let {
                                        selectedTargetItem = it
                                        selectedHardwareId = it.id.value
                                        selectedHardwareName = it.displayName
                                        selectedCategory = "GPU"
                                    }
                                }
                                activeBuild.motherboard?.let {
                                    selectedMotherboardItem = it
                                    motherboardName = it.displayName
                                }
                                activeBuild.cooler?.let {
                                    selectedCoolerItem = it
                                    coolerName = it.displayName
                                }
                                activeBuild.ram?.let {
                                    selectedRamItem = it
                                    ramSpec = it.displayName
                                }
                                if (selectedCategory == "CPU") {
                                    activeBuild.gpu?.let {
                                        selectedSecondaryItem = it
                                        secondaryHardware = it.displayName
                                    }
                                } else {
                                    activeBuild.cpu?.let {
                                        selectedSecondaryItem = it
                                        secondaryHardware = it.displayName
                                    }
                                }
                                compatibilityResetWarning = null
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoFixHigh,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTr) "Aktif PC Sihirbazı Sistemimden Doldur" else "Autofill from Active PC Wizard Build",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                )
                                Text(
                                    text = if (isTr) {
                                        "Kayıtlı sisteminizdeki uyumlu CPU, GPU, Anakart, Soğutucu ve RAM tek tıkla aktarılır."
                                    } else {
                                        "Transfers your compatible CPU, GPU, Motherboard, Cooler and RAM in one tap."
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // 2. Sistem & Donanım Parçaları Seçimi (Uyumluluk Kilitli)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Devices,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (isTr) "Sistem & Donanım Parçaları (Uyumluluk Korumalı):" else "System & Rig Components (Compatibility Locked):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber,
                    )
                }

                AnimatedVisibility(visible = compatibilityResetWarning != null) {
                    compatibilityResetWarning?.let { warnText ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonAmber.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = NeonAmber,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = warnText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = NeonAmber,
                                )
                            }
                        }
                    }
                }

                // Slot 1: Ana Donanım
                HardwareSlotCard(
                    slotIcon = Icons.Rounded.Memory,
                    imageUrl = selectedTargetItem?.effectiveImageUrl,
                    slotLabel = if (isTr) "Ayar Yapılan Ana Donanım *" else "Target Hardware *",
                    selectedName = selectedHardwareName.ifBlank {
                        if (isTr) "Donanım Seçilmedi (Zorunlu)" else "No Hardware Selected (Required)"
                    },
                    badgeText = selectedCategory,
                    badgeColor = NeonCyan,
                    isPlaceholder = selectedHardwareName.isBlank(),
                    isTr = isTr,
                    onSelectClick = { activePickerSlot = ComponentPickerSlot.TARGET_HARDWARE },
                )

                // Slot 2: Anakart
                HardwareSlotCard(
                    slotIcon = Icons.Rounded.Layers,
                    imageUrl = selectedMotherboardItem?.effectiveImageUrl,
                    slotLabel = if (isTr) "Anakart Modeli" else "Motherboard Model",
                    selectedName = motherboardName.ifBlank {
                        if (isTr) "Uyumlu Anakart Seçin (Örn: X870E / B650 / Z790)" else "Select Compatible Motherboard"
                    },
                    badgeText = if (isTr) "ANAKART" else "MOTHERBOARD",
                    badgeColor = ElectricBlue,
                    isPlaceholder = motherboardName.isBlank(),
                    isTr = isTr,
                    onSelectClick = { activePickerSlot = ComponentPickerSlot.MOTHERBOARD },
                )

                // Slot 3: RAM
                HardwareSlotCard(
                    slotIcon = Icons.Rounded.DeveloperBoard,
                    imageUrl = selectedRamItem?.effectiveImageUrl,
                    slotLabel = if (isTr) "RAM Kiti & Frekansı" else "RAM Kit & Frequency",
                    selectedName = ramSpec.ifBlank {
                        if (isTr) "Uyumlu RAM Seçin (Örn: 64GB DDR5-6400 CL32)" else "Select Compatible RAM Kit"
                    },
                    badgeText = if (isTr) "BELLEK" else "MEMORY",
                    badgeColor = NeonPurple,
                    isPlaceholder = ramSpec.isBlank(),
                    isTr = isTr,
                    onSelectClick = { activePickerSlot = ComponentPickerSlot.RAM },
                )

                // Slot 4: Soğutucu
                HardwareSlotCard(
                    slotIcon = Icons.Rounded.AcUnit,
                    imageUrl = selectedCoolerItem?.effectiveImageUrl,
                    slotLabel = if (isTr) "İşlemci / Kasa Soğutması" else "CPU / System Cooler",
                    selectedName = coolerName.ifBlank {
                        if (isTr) "Uyumlu Soğutucu Seçin (Örn: 360mm Sıvı Soğutma)" else "Select Compatible Cooler"
                    },
                    badgeText = if (isTr) "SOĞUTMA" else "COOLER",
                    badgeColor = NeonEmerald,
                    isPlaceholder = coolerName.isBlank(),
                    isTr = isTr,
                    onSelectClick = { activePickerSlot = ComponentPickerSlot.COOLER },
                )

                // Slot 5: Tamamlayıcı Donanım
                HardwareSlotCard(
                    slotIcon = Icons.Rounded.VideogameAsset,
                    imageUrl = selectedSecondaryItem?.effectiveImageUrl,
                    slotLabel = if (selectedCategory == "CPU") {
                        if (isTr) "Sistemdeki Ekran Kartı (GPU)" else "System Graphics Card (GPU)"
                    } else {
                        if (isTr) "Sistemdeki İşlemci (CPU)" else "System Processor (CPU)"
                    },
                    selectedName = secondaryHardware.ifBlank {
                        if (isTr) "Tamamlayıcı Donanım Seçin (Opsiyonel)" else "Select Secondary Hardware (Optional)"
                    },
                    badgeText = if (selectedCategory == "CPU") "GPU" else "CPU",
                    badgeColor = NeonRose,
                    isPlaceholder = secondaryHardware.isBlank(),
                    isTr = isTr,
                    onSelectClick = { activePickerSlot = ComponentPickerSlot.SECONDARY_HARDWARE },
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 3. Ayar Türü Seçimi: Çoklu Seçim (Multi-Select) + 2x2 Simetrik Eşit Yükseklikli (52.dp) Grid + Yumuşak Vektör İkonlar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = if (isTr) "Ayar Türü (Birden Fazla Seçilebilir):" else "Tuning Type (Multi-Select):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = if (isTr) "${selectedTypes.size} seçili" else "${selectedTypes.size} selected",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                    )
                }

                val availableTuningTypes = remember(selectedCategory) {
                    if (selectedCategory.equals("GPU", ignoreCase = true)) {
                        TuningType.entries.filter { it != TuningType.PBO }
                    } else {
                        TuningType.entries.toList()
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableTuningTypes.chunked(2).forEach { rowTypes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowTypes.forEach { type ->
                                val isSelected = selectedTypes.contains(type)
                                TuningTypeGridCard(
                                    type = type,
                                    isSelected = isSelected,
                                    isGpuCategory = selectedCategory.equals("GPU", ignoreCase = true),
                                    onClick = {
                                        selectedTypes = if (isSelected) {
                                            if (selectedTypes.size > 1) selectedTypes - type else selectedTypes
                                        } else {
                                            selectedTypes + type
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (rowTypes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // 4. İçerik Dili Seçimi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (isTr) "İçerik Dili (Zorunlu Seçim):" else "Content Language (Required):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    languages.take(4).forEach { (code, label) ->
                        val isSelected = selectedLanguage == code
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NeonAmber.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) NeonAmber else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedLanguage = code },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonRose.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NeonRose.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = NeonRose,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTr) {
                                "Uyarı: Açıklama metni ile seçtiğiniz dil uyuşmazsa moderasyon tarafından reddedilecektir."
                            } else {
                                "Warning: Submissions with mismatched text and selected language will be rejected."
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 13.sp),
                            color = NeonRose,
                        )
                    }
                }

                // 5. Ayar Başlığı & Teknik Parametreler
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isTr) "Ayar Başlığı *" else "Profile Title *") },
                    placeholder = {
                        Text(
                            if (isTr) "Ör: Günlük Stabil -30mV PBO + 6400MHz CL32" else "e.g. Daily Stable -30mV PBO + 6400MHz CL32"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = voltageOffsetStr,
                        onValueChange = { voltageOffsetStr = it },
                        label = { Text(if (isTr) "Voltaj / CO (mV)" else "Voltage (mV)", maxLines = 1) },
                        placeholder = { Text("-30") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = clockMhzStr,
                        onValueChange = { clockMhzStr = it },
                        label = { Text(if (isTr) "Saat Hızı (MHz)" else "Clock (MHz)", maxLines = 1) },
                        placeholder = { Text("5450") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempBeforeStr,
                        onValueChange = { tempBeforeStr = it },
                        label = { Text(if (isTr) "Önceki (°C)" else "Before (°C)", maxLines = 1) },
                        placeholder = { Text("85") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = tempAfterStr,
                        onValueChange = { tempAfterStr = it },
                        label = { Text(if (isTr) "Sonraki (°C)" else "After (°C)", maxLines = 1) },
                        placeholder = { Text("71") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                }

                // 6. Deneyim & Rehber Notları
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.AutoMirrored.Rounded.Notes, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(15.dp))
                            Text(if (isTr) "Deneyiminiz ve Rehber Notlarınız" else "Your Experience & Guide Notes")
                        }
                    },
                    placeholder = {
                        Text(
                            if (isTr) {
                                "Voltaj düşürme adımlarınızı, test ettiğiniz stres yazılımını (Prime95, Cinebench, OCCT) ve diğer kullanıcılara BIOS önerilerinizi yazın..."
                            } else {
                                "Describe your BIOS steps, stress testing software (OCCT, Cinebench, Prime95), and stability tips..."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    maxLines = 5,
                )

                // 7. Kanıt Görseli: Galeri + Kamera + URL Hibrit Seçici
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (isTr) "Kanıt Görseli (Galeri, Kamera veya Link):" else "Proof Image (Gallery, Camera or URL):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Galeriden Seç
                    ProofSourceActionButton(
                        icon = Icons.Rounded.PhotoLibrary,
                        label = if (isTr) "Galeri" else "Gallery",
                        accentColor = NeonCyan,
                        onClick = { imagePickerLauncher.launchGallery() },
                        modifier = Modifier.weight(1f),
                    )
                    // Kamera ile Çek
                    ProofSourceActionButton(
                        icon = Icons.Rounded.PhotoCamera,
                        label = if (isTr) "Kamera" else "Camera",
                        accentColor = NeonEmerald,
                        onClick = { imagePickerLauncher.launchCamera() },
                        modifier = Modifier.weight(1f),
                    )
                    // URL Gir
                    ProofSourceActionButton(
                        icon = Icons.Rounded.Link,
                        label = if (isTr) "Link (URL)" else "Paste URL",
                        accentColor = NeonPurple,
                        isSelected = showUrlInputField,
                        onClick = { showUrlInputField = !showUrlInputField },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (isUploadingImage) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = if (isTr) "Görsel <= 150 KB sıkıştırılıyor ve buluta yükleniyor..." else "Compressing <= 150 KB and uploading...",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                        )
                    }
                }

                uploadedImageStatus?.let { statusMsg ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonEmerald.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                Text(text = statusMsg, style = MaterialTheme.typography.labelSmall, color = NeonEmerald, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = {
                                    proofImageUrl = ""
                                    uploadedImageStatus = null
                                },
                                modifier = Modifier.size(22.dp),
                            ) {
                                Icon(imageVector = Icons.Rounded.Close, contentDescription = null, tint = NeonRose, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                uploadErrorText?.let { errMsg ->
                    Text(
                        text = errMsg,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRose,
                    )
                }

                AnimatedVisibility(visible = showUrlInputField || (proofImageUrl.isNotBlank() && uploadedImageStatus == null)) {
                    OutlinedTextField(
                        value = proofImageUrl,
                        onValueChange = {
                            proofImageUrl = it
                            uploadedImageStatus = null
                        },
                        label = { Text(if (isTr) "Doğrudan Görsel Linki (URL)" else "Direct Image URL") },
                        placeholder = { Text("https://i.imgur.com/...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = {
                            if (proofImageUrl.isNotEmpty()) {
                                IconButton(onClick = { proofImageUrl = "" }) {
                                    Icon(imageVector = Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                    )
                }

                if (proofImageUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = proofImageUrl.trim(),
                            contentDescription = "Proof Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedHardwareName.isNotBlank()) {
                        val orderedTypes = selectedTypes.toList().ifEmpty { listOf(TuningType.UNDERVOLT) }
                        val newProfile = TuningProfile(
                            id = "tune-${System.currentTimeMillis()}-${(1000..9999).random()}",
                            hardwareId = selectedHardwareId.ifBlank { "hw-custom-${System.currentTimeMillis()}" },
                            hardwareName = selectedHardwareName.trim(),
                            hardwareCategory = selectedCategory,
                            tuningType = orderedTypes.first(),
                            tuningTypes = orderedTypes,
                            profileTitle = title.trim(),
                            languageCode = selectedLanguage,
                            voltageOffsetMv = voltageOffsetStr.toDoubleOrNull(),
                            clockMhz = clockMhzStr.toDoubleOrNull(),
                            tempBeforeC = tempBeforeStr.toDoubleOrNull(),
                            tempAfterC = tempAfterStr.toDoubleOrNull(),
                            scoreBefore = scoreBeforeStr.toDoubleOrNull(),
                            scoreAfter = scoreAfterStr.toDoubleOrNull(),
                            benchmarkName = benchName,
                            proofImageUrl = proofImageUrl.trim().ifBlank { null },
                            guideNotes = notes.trim().ifBlank { null },
                            userId = userId,
                            userDisplayName = userDisplayName,
                            userBadge = userBadge,
                            createdAtEpochMs = System.currentTimeMillis(),
                            motherboardName = motherboardName.trim().ifBlank { null },
                            coolerName = coolerName.trim().ifBlank { null },
                            ramSpec = ramSpec.trim().ifBlank { null },
                            secondaryHardware = secondaryHardware.trim().ifBlank { null },
                        )
                        onSubmit(newProfile)
                    }
                },
                enabled = title.isNotBlank() && selectedHardwareName.isNotBlank() && !isUploadingImage,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = if (isTr) "Onaya Gönder (+50 Puan)" else "Submit for Review (+50 Pts)",
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (isTr) "Vazgeç" else "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )

    // Uyumluluk Korumalı Donanım Seçici Diyalog
    activePickerSlot?.let { slot ->
        val categories = when (slot) {
            ComponentPickerSlot.TARGET_HARDWARE -> listOf(HardwareCategory.CPU, HardwareCategory.GPU)
            ComponentPickerSlot.MOTHERBOARD -> listOf(HardwareCategory.MOTHERBOARD)
            ComponentPickerSlot.COOLER -> listOf(HardwareCategory.COOLER)
            ComponentPickerSlot.RAM -> listOf(HardwareCategory.RAM)
            ComponentPickerSlot.SECONDARY_HARDWARE -> if (selectedCategory == "CPU") listOf(HardwareCategory.GPU) else listOf(HardwareCategory.CPU)
        }
        val slotTitle = when (slot) {
            ComponentPickerSlot.TARGET_HARDWARE -> if (isTr) "Ayar Yapılacak Donanımı Seç" else "Select Target Hardware"
            ComponentPickerSlot.MOTHERBOARD -> if (isTr) "Uyumlu Anakart Seçin" else "Select Compatible Motherboard"
            ComponentPickerSlot.COOLER -> if (isTr) "Uyumlu Soğutucu Seçin" else "Select Compatible Cooler"
            ComponentPickerSlot.RAM -> if (isTr) "Uyumlu RAM Kiti Seçin" else "Select Compatible RAM Kit"
            ComponentPickerSlot.SECONDARY_HARDWARE -> if (selectedCategory == "CPU") {
                if (isTr) "Ekran Kartı Seçin" else "Select Graphics Card"
            } else {
                if (isTr) "Uyumlu İşlemci Seçin" else "Select Compatible Processor"
            }
        }

        HardwarePickerDialog(
            title = slotTitle,
            targetCategories = categories,
            hardwareRepository = hardwareRepository,
            activeBuild = virtualBuildForCompatibility,
            onDismiss = { activePickerSlot = null },
            onSelectItem = { item ->
                compatibilityResetWarning = null
                when (slot) {
                    ComponentPickerSlot.TARGET_HARDWARE -> {
                        selectedTargetItem = item
                        selectedHardwareId = item.id.value
                        selectedHardwareName = item.displayName
                        selectedCategory = item.category.name
                        if (selectedCategory == "GPU") {
                            selectedTypes = selectedTypes - TuningType.PBO
                            if (selectedTypes.isEmpty()) selectedTypes = setOf(TuningType.UNDERVOLT)
                        }
                        // Yeni CPU seçildiyse mevcut Anakart ve RAM soket/DDR uyumunu denetle
                        val newCpuSpec = item.spec as? CpuSpec
                        if (newCpuSpec != null) {
                            val curMoboSpec = selectedMotherboardItem?.spec as? MotherboardSpec
                            if (curMoboSpec != null && !curMoboSpec.socket.equals(newCpuSpec.socket, ignoreCase = true)) {
                                selectedMotherboardItem = null
                                motherboardName = ""
                                compatibilityResetWarning = if (isTr) {
                                    "Seçilen işlemci (${newCpuSpec.socket}) ile uyumsuz olan önceki anakart sıfırlandı."
                                } else {
                                    "Previous motherboard reset due to socket mismatch (${newCpuSpec.socket})."
                                }
                            }
                            val inferredDdr = when (newCpuSpec.socket.uppercase().trim()) {
                                "AM5", "LGA1851" -> DdrGeneration.DDR5
                                "AM4", "LGA1200", "LGA1151" -> DdrGeneration.DDR4
                                else -> null
                            }
                            val curRamSpec = selectedRamItem?.spec as? RamSpec
                            if (inferredDdr != null && curRamSpec != null && curRamSpec.generation != inferredDdr) {
                                selectedRamItem = null
                                ramSpec = ""
                                compatibilityResetWarning = if (isTr) {
                                    "Seçilen platform ($inferredDdr) ile uyumsuz olan önceki RAM sıfırlandı."
                                } else {
                                    "Previous RAM reset due to generation mismatch ($inferredDdr)."
                                }
                            }
                        }
                    }
                    ComponentPickerSlot.MOTHERBOARD -> {
                        selectedMotherboardItem = item
                        motherboardName = item.displayName
                        // Yeni Anakartın DDR nesli mevcut RAM ile uyumsuzsa RAM'i otomatik sıfırla
                        val newMoboSpec = item.spec as? MotherboardSpec
                        val curRamSpec = selectedRamItem?.spec as? RamSpec
                        if (newMoboSpec != null && curRamSpec != null && newMoboSpec.memoryGeneration != curRamSpec.generation) {
                            selectedRamItem = null
                            ramSpec = ""
                            compatibilityResetWarning = if (isTr) {
                                "Seçilen anakart (${newMoboSpec.memoryGeneration.name}) ile uyumsuz olan önceki RAM sıfırlandı."
                            } else {
                                "Previous RAM reset because motherboard requires ${newMoboSpec.memoryGeneration.name}."
                            }
                        }
                    }
                    ComponentPickerSlot.COOLER -> {
                        selectedCoolerItem = item
                        coolerName = item.displayName
                    }
                    ComponentPickerSlot.RAM -> {
                        selectedRamItem = item
                        ramSpec = item.displayName
                    }
                    ComponentPickerSlot.SECONDARY_HARDWARE -> {
                        selectedSecondaryItem = item
                        secondaryHardware = item.displayName
                    }
                }
                activePickerSlot = null
            },
        )
    }
}

/**
 * Yumuşak hatlı vektörel ikon eşlemesi ve rengi.
 */
fun TuningType.iconVector(): ImageVector = when (this) {
    TuningType.UNDERVOLT -> Icons.Rounded.AcUnit
    TuningType.OVERCLOCK -> Icons.Rounded.LocalFireDepartment
    TuningType.PBO -> Icons.Rounded.Bolt
    TuningType.MEMORY -> Icons.Rounded.Memory
}

fun TuningType.accentColor(): Color = when (this) {
    TuningType.UNDERVOLT -> NeonCyan
    TuningType.OVERCLOCK -> NeonRose
    TuningType.PBO -> NeonAmber
    TuningType.MEMORY -> NeonPurple
}

fun TuningType.tintColor(): Color = accentColor()

val TuningType.shortBadge: String
    get() = when (this) {
        TuningType.UNDERVOLT -> "Undervolt"
        TuningType.OVERCLOCK -> "Overclock"
        TuningType.PBO -> "PBO Curve"
        TuningType.MEMORY -> "RAM Timing"
    }

@Composable
private fun TuningTypeGridCard(
    type: TuningType,
    isSelected: Boolean,
    isGpuCategory: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = type.accentColor()
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.97f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "tuning_type_scale",
    )

    val labelText = when (type) {
        TuningType.UNDERVOLT -> "Undervolt"
        TuningType.OVERCLOCK -> "Overclock"
        TuningType.PBO -> "PBO Curve"
        TuningType.MEMORY -> if (isGpuCategory) "VRAM Timing" else "RAM Timing"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
        modifier = modifier
            .height(52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
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
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) accent.copy(alpha = 0.24f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = type.iconVector(),
                        contentDescription = labelText,
                        tint = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp),
                    )
                }

                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ProofSourceActionButton(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.22f) else accentColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, if (isSelected) accentColor else accentColor.copy(alpha = 0.45f)),
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun HardwareSlotCard(
    slotIcon: ImageVector,
    imageUrl: String? = null,
    slotLabel: String,
    selectedName: String,
    badgeText: String,
    badgeColor: Color,
    isPlaceholder: Boolean,
    isTr: Boolean,
    onSelectClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            1.dp,
            if (isPlaceholder) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f) else badgeColor.copy(alpha = 0.5f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onSelectClick() },
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
                if (!imageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = selectedName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp)),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = slotIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = slotLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.16f),
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.SemiBold,
                        color = if (isPlaceholder) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = if (isPlaceholder) {
                            if (isTr) "Seç" else "Select"
                        } else {
                            if (isTr) "Değiştir" else "Change"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
