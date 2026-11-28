package com.panda.myapplication.presentation.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panda.myapplication.domain.model.GamingChairSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MicrophoneSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.MousepadSpec
import com.panda.myapplication.domain.model.WebcamSpec
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.presentation.components.HardwarePickerDialog
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.NeonRose
import com.panda.myapplication.presentation.util.LocalAppStrings
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.tan

@Composable
fun StudioAndSurfaceLabScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    hardwareRepository: HardwareRepository = koinInject(),
    activeBuildRepository: ActiveBuildRepository = koinInject(),
) {
    val strings = LocalAppStrings.current
    val isTr = strings.isTurkish
    val activeBuild by activeBuildRepository.activeBuild.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Catalog Hardware Selections (initialized from ActiveBuild if available)
    var selectedMic by remember(activeBuild.microphone) { mutableStateOf(activeBuild.microphone) }
    var selectedMouse by remember(activeBuild.mouse) { mutableStateOf(activeBuild.mouse) }
    var selectedMousepad by remember(activeBuild.mousepad) { mutableStateOf(activeBuild.mousepad) }
    var selectedWebcam by remember(activeBuild.webcam) { mutableStateOf(activeBuild.webcam) }
    var selectedChair by remember(activeBuild.gamingChair) { mutableStateOf(activeBuild.gamingChair) }

    var activePickerCategory by remember { mutableStateOf<HardwareCategory?>(null) }

    activePickerCategory?.let { cat ->
        HardwarePickerDialog(
            title = if (isTr) "Katalogdan Donanım Seçin" else "Select Hardware from Catalog",
            targetCategories = listOf(cat),
            hardwareRepository = hardwareRepository,
            activeBuild = activeBuild,
            onDismiss = { activePickerCategory = null },
            onSelectItem = { item ->
                when (cat) {
                    HardwareCategory.MICROPHONE -> selectedMic = item
                    HardwareCategory.MOUSE -> selectedMouse = item
                    HardwareCategory.MOUSEPAD -> selectedMousepad = item
                    HardwareCategory.WEBCAM -> selectedWebcam = item
                    HardwareCategory.GAMING_CHAIR -> selectedChair = item
                    else -> Unit
                }
                activePickerCategory = null
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Geri",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menü",
                                tint = NeonCyan,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.drawerStudioLabTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = strings.drawerStudioLabSubtitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4-Tab Horizontal Pill Selector
                    val tabs = listOf(
                        Triple(0, "🎙️ " + (if (isTr) "Mikrofon Akustiği" else "Mic Acoustics"), NeonAmber),
                        Triple(1, "🎯 " + (if (isTr) "Mousepad Sürtünme" else "Pad Friction"), NeonCyan),
                        Triple(2, "📷 " + (if (isTr) "Webcam FOV & Işık" else "Webcam FOV"), ElectricBlue),
                        Triple(3, "🪑 " + (if (isTr) "Koltuk Ergonomisi" else "Chair Ergonomics"), NeonPurple),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        tabs.forEach { (idx, label, accent) ->
                            val isSelected = selectedTab == idx
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedTab = idx }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (selectedTab) {
                0 -> MicrophoneAcousticSimulatorTab(
                    selectedMic = selectedMic,
                    isTr = isTr,
                    onPickMic = { activePickerCategory = HardwareCategory.MICROPHONE },
                )
                1 -> MousepadFrictionPhysicsTab(
                    selectedMouse = selectedMouse,
                    selectedMousepad = selectedMousepad,
                    isTr = isTr,
                    onPickMouse = { activePickerCategory = HardwareCategory.MOUSE },
                    onPickMousepad = { activePickerCategory = HardwareCategory.MOUSEPAD },
                )
                2 -> WebcamFovAndLightSimulatorTab(
                    selectedWebcam = selectedWebcam,
                    isTr = isTr,
                    onPickWebcam = { activePickerCategory = HardwareCategory.WEBCAM },
                )
                3 -> ChairErgonomicsWizardTab(
                    selectedChair = selectedChair,
                    isTr = isTr,
                    onPickChair = { activePickerCategory = HardwareCategory.GAMING_CHAIR },
                )
            }
        }
    }
}

// ============================================================================================
// TAB 1: MICROPHONE & STUDIO ACOUSTIC SIMULATOR
// ============================================================================================
@Composable
private fun MicrophoneAcousticSimulatorTab(
    selectedMic: HardwareItem?,
    isTr: Boolean,
    onPickMic: () -> Unit,
) {
    val micSpec = selectedMic?.spec as? MicrophoneSpec
    val isDynamicDefault = micSpec?.capsuleType?.let { cap ->
        cap.contains("Dynamic", ignoreCase = true) || cap.contains("Dinamik", ignoreCase = true)
    } ?: true

    var micDistanceCm by remember { mutableStateOf(12f) }
    var roomTreatmentLevel by remember { mutableStateOf(1) } // 0: Boş Yankılı Oda, 1: Standart Ev Odası, 2: Akustik Yalıtımlı
    var keyboardLoudness by remember { mutableStateOf(1) } // 0: Sessiz/Linear, 1: Mekanik/Manyetik, 2: Clicky/Gürültülü

    val isDynamic = isDynamicDefault
    val connUpper = micSpec?.connectivity?.uppercase().orEmpty()
    val isXlr = if (micSpec == null) true else connUpper.contains("XLR")
    val isUsbOrWireless = connUpper.contains("USB") || connUpper.contains("WIRELESS") || connUpper.contains("2.4") || connUpper.contains("BLUETOOTH")
    val hasBuiltInPreampOrDsp = (micSpec?.hasBuiltInDsp == true) ||
        (selectedMic?.displayName?.contains("SM7dB", ignoreCase = true) == true)
    val needsPhantom48v = (micSpec?.requiresPhantomPower == true) || (isXlr && !isDynamic)

    // Polar pattern off-axis rejection + Logarithmic Inverse Square Law: ΔL = 20 * log10(20 / d)
    val polarUpper = micSpec?.polarPattern?.uppercase().orEmpty()
    val capsuleRejectionDb = when {
        polarUpper.contains("OMNI") -> 3f
        polarUpper.contains("SHOTGUN") -> 22f
        polarUpper.contains("SUPERCARDIOID") || polarUpper.contains("HYPERCARDIOID") -> if (isDynamic) 20f else 13f
        isDynamic -> 18f
        else -> 9f
    }
    val proximityBonusDb = (20.0 * kotlin.math.log10(20.0 / micDistanceCm.coerceAtLeast(5f).toDouble())).toFloat()
    val roomEchoPenaltyDb = when (roomTreatmentLevel) {
        0 -> if (isDynamic) 6f else 15f
        1 -> if (isDynamic) 3f else 8f
        else -> 1f
    }
    val rawKeyboardSpl = when (keyboardLoudness) {
        0 -> 38f
        1 -> 48f
        else -> 58f
    }
    val keyboardNoiseDb = (rawKeyboardSpl - capsuleRejectionDb).coerceAtLeast(16f)

    val snrScore = (76f + proximityBonusDb * 0.7f + capsuleRejectionDb * 0.65f - roomEchoPenaltyDb - (keyboardNoiseDb - 18f) * 0.45f)
        .roundToInt()
        .coerceIn(35, 99)

    val recommendedGateThresholdDb = (-48 + (keyboardNoiseDb.roundToInt() - 25).coerceIn(-6, 14)).coerceIn(-54, -26)

    LabPickerCard(
        emoji = "🎙️",
        label = if (isTr) "Test Edilen Stüdyo Mikrofonu" else "Tested Studio Microphone",
        selectedName = selectedMic?.displayName ?: (if (isTr) "Shure SM7dB / Dinamik Referans (Katalogdan Seç)" else "Shure SM7dB Reference (Tap to Pick)"),
        selectedSub = micSpec?.let { "${it.capsuleType} • ${it.polarPattern} • ${it.connectivity}" }
            ?: (if (isTr) "Katalogdan mikrofon seçerek gerçek kapsül verileriyle simüle edin" else "Pick a microphone from catalog to simulate real capsule specs"),
        accentColor = NeonAmber,
        isTr = isTr,
        onClick = onPickMic,
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isTr) "🎛️ Akustik Ortam & Mesafe Parametreleri" else "🎛️ Acoustic Room & Distance Parameters",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NeonAmber,
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (isTr) "Ağız - Mikrofon Mesafesi (Proximity)" else "Mouth-to-Mic Distance",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${micDistanceCm.roundToInt()} cm",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonAmber,
                    )
                }
                Slider(
                    value = micDistanceCm,
                    onValueChange = { micDistanceCm = it },
                    valueRange = 5f..45f,
                    colors = SliderDefaults.colors(thumbColor = NeonAmber, activeTrackColor = NeonAmber),
                )
            }

            Text(
                text = if (isTr) "Oda Yalıtımı & Yankı Durumu" else "Room Acoustic Treatment",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val roomLabels = if (isTr) listOf("Boş / Yankılı", "Ev Odası", "Stüdyo Süngerli") else listOf("Untreated", "Home Room", "Treated Studio")
                roomLabels.forEachIndexed { idx, txt ->
                    FilterChip(
                        selected = roomTreatmentLevel == idx,
                        onClick = { roomTreatmentLevel = idx },
                        label = { Text(txt, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonAmber.copy(alpha = 0.2f),
                            selectedLabelColor = NeonAmber,
                        )
                    )
                }
            }

            Text(
                text = if (isTr) "Klavye & Ortam Gürültü Şiddeti" else "Keyboard & Ambient Noise",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val kbLabels = if (isTr) listOf("Sessiz Linear", "Mekanik / HE", "Clicky Blue") else listOf("Silent Linear", "Mechanical/HE", "Loud Clicky")
                kbLabels.forEachIndexed { idx, txt ->
                    FilterChip(
                        selected = keyboardLoudness == idx,
                        onClick = { keyboardLoudness = idx },
                        label = { Text(txt, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan,
                        )
                    )
                }
            }
        }
    }

    // Results Card
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isTr) "📊 Yayın Ses Netliği (SNR) Skoru" else "📊 Broadcast Voice Clarity (SNR)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$snrScore / 100",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (snrScore >= 80) NeonEmerald else if (snrScore >= 65) NeonAmber else NeonRose,
                    softWrap = false,
                )
            }

            LinearProgressIndicator(
                progress = { snrScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (snrScore >= 80) NeonEmerald else if (snrScore >= 65) NeonAmber else NeonRose,
            )

            LabMetricRow(
                label = if (isTr) "Kapsül Arka Plan Reddi (Off-Axis Rejection)" else "Capsule Off-Axis Noise Rejection",
                value = if (isDynamic) {
                    if (isTr) "-${capsuleRejectionDb.roundToInt()} dB (Dinamik İzolasyon)" else "-${capsuleRejectionDb.roundToInt()} dB (Dynamic Isolation)"
                } else {
                    if (isTr) "-${capsuleRejectionDb.roundToInt()} dB (Geniş Kondenser)" else "-${capsuleRejectionDb.roundToInt()} dB (Wide Condenser)"
                },
                valueColor = if (isDynamic) NeonEmerald else NeonAmber,
            )
            LabMetricRow(
                label = if (isTr) "Tahmini Klavye Ses Sızıntısı" else "Estimated Keyboard Bleed",
                value = "${keyboardNoiseDb.roundToInt()} dB SPL",
                valueColor = if (keyboardNoiseDb <= 22f) NeonEmerald else NeonAmber,
            )
            LabMetricRow(
                label = if (isTr) "Önerilen OBS Noise Gate Eşiği" else "Recommended OBS Noise Gate Threshold",
                value = "$recommendedGateThresholdDb dBFS",
                valueColor = NeonCyan,
            )
            LabMetricRow(
                label = if (isTr) "Ses Kartı & Preamp Gereksinimi" else "Audio Interface & Preamp Advice",
                value = when {
                    isXlr && isUsbOrWireless ->
                        if (isTr) "Hibrit XLR / USB-C (Her İki Mod Destekli)" else "Hybrid XLR / USB-C Dual Mode"
                    isXlr && hasBuiltInPreampOrDsp ->
                        if (isTr) "Dahili Preamp Aktif (+48V Standart XLR Yeterli)" else "Built-in Preamp Active (+48V XLR OK)"
                    isXlr && isDynamic ->
                        if (isTr) "60dB+ Gain Ses Kartı veya Inline Preamp Önerilir" else "60dB+ Gain Interface or Inline Preamp Recommended"
                    isXlr && needsPhantom48v ->
                        if (isTr) "+48V Phantom Power Destekli XLR Ses Kartı Zorunlu" else "XLR Audio Interface with +48V Phantom Power Required"
                    else ->
                        if (isTr) "Tak-Çalıştır USB-C Dijital ADC" else "Plug & Play USB-C Digital ADC"
                },
                valueColor = ElectricBlue,
            )
        }
    }
}

// ============================================================================================
// TAB 2: MOUSEPAD DYNAMIC FRICTION & GLIDE PHYSICS LAB
// ============================================================================================
@Composable
private fun MousepadFrictionPhysicsTab(
    selectedMouse: HardwareItem?,
    selectedMousepad: HardwareItem?,
    isTr: Boolean,
    onPickMouse: () -> Unit,
    onPickMousepad: () -> Unit,
) {
    val mouseSpec = selectedMouse?.spec as? MouseSpec
    val padSpec = selectedMousepad?.spec as? MousepadSpec

    val mouseWeightG = mouseSpec?.weightG?.takeIf { it > 0 } ?: 54
    var skateMaterialIdx by remember { mutableStateOf(0) } // 0: %100 Virgin PTFE, 1: Dot PTFE, 2: Temperli Cam Skatez

    val isGlassPad = padSpec?.surfaceType?.contains("Cam", ignoreCase = true) == true ||
        padSpec?.surfaceType?.contains("Glass", ignoreCase = true) == true
    val isSpeedHybrid = padSpec?.speedGlideTier?.contains("Hız", ignoreCase = true) == true ||
        padSpec?.speedGlideTier?.contains("Speed", ignoreCase = true) == true

    // Physics Friction Coefficients (mu_s static, mu_k kinetic)
    val baseMuS = when {
        isGlassPad -> 0.09f
        isSpeedHybrid -> 0.16f
        else -> 0.23f
    }
    val baseMuK = when {
        isGlassPad -> 0.07f
        isSpeedHybrid -> 0.13f
        else -> 0.19f
    }

    val skateMod = when (skateMaterialIdx) {
        0 -> 1.0f
        1 -> 0.88f // Less surface contact area with Dots
        else -> 0.78f
    }

    val muStatic = (baseMuS * skateMod * (1f + (mouseWeightG - 55) * 0.002f)).coerceIn(0.05f, 0.38f)
    val muKinetic = (baseMuK * skateMod * (1f + (mouseWeightG - 55) * 0.0015f)).coerceIn(0.04f, 0.32f)

    val baseStop = padSpec?.stoppingPowerScore?.takeIf { it > 0 } ?: (if (isGlassPad) 4 else if (isSpeedHybrid) 7 else 9)
    val skateStopDelta = when (skateMaterialIdx) {
        0 -> 0
        1 -> -1
        else -> -2
    }
    val stoppingPower = (baseStop + skateStopDelta).coerceIn(2, 10)
    val microAdjustScore = ((1f - (muStatic / 0.38f)) * 100f).roundToInt().coerceIn(55, 99)
    val glassOnGlassWarning = isGlassPad && skateMaterialIdx == 2

    LabPickerCard(
        emoji = "🖱️",
        label = if (isTr) "Seçili Oyuncu Mouse (${mouseWeightG}g)" else "Selected Gaming Mouse (${mouseWeightG}g)",
        selectedName = selectedMouse?.displayName ?: (if (isTr) "54g Ultra-Hafif Referans Mouse (Katalogdan Seç)" else "54g Ultra-Light Reference Mouse (Tap to Pick)"),
        selectedSub = mouseSpec?.let {
            listOfNotNull(
                it.sensor.ifBlank { null },
                it.weightG.takeIf { w -> w > 0 }?.let { w -> "${w}g" },
                it.pollingRateHz.takeIf { hz -> hz > 0 }?.let { hz -> "${hz}Hz" },
            ).joinToString(" • ").ifBlank { if (isTr) "Referans 54g profili kullanılıyor" else "Using 54g reference profile" }
        }
            ?: (if (isTr) "Mouse ağırlığını hesaba katmak için katalogdan seçin" else "Pick mouse from catalog to factor weight into friction"),
        accentColor = NeonCyan,
        isTr = isTr,
        onClick = onPickMouse,
    )

    LabPickerCard(
        emoji = "🎯",
        label = if (isTr) "Seçili Oyuncu Mousepad Yüzeyi" else "Selected Gaming Mousepad Surface",
        selectedName = selectedMousepad?.displayName ?: (if (isTr) "Artisan Ninja FX Zero XSoft XL (Katalogdan Seç)" else "Artisan Ninja FX Zero XSoft XL (Tap to Pick)"),
        selectedSub = padSpec?.let {
            listOfNotNull(
                it.surfaceType.ifBlank { null },
                it.speedGlideTier.ifBlank { null },
                it.baseMaterial.ifBlank { null },
            ).joinToString(" • ").ifBlank { if (isTr) "Standart kumaş kontrol yüzeyi" else "Standard cloth control surface" }
        }
            ?: (if (isTr) "Yüzey dokusu ve durdurma gücünü test etmek için seçin" else "Pick mousepad from catalog to test glide & stopping power"),
        accentColor = NeonEmerald,
        isTr = isTr,
        onClick = onPickMousepad,
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (isTr) "⛸️ Skatez (Mouse Ayak) Materyali" else "⛸️ Mouse Skate Material",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NeonCyan,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val skateLabels = if (isTr) listOf("%100 Saf PTFE", "Dot (Nokta) PTFE", "Temperli Cam Skatez") else listOf("100% Virgin PTFE", "Dot PTFE", "Glass Skates")
                skateLabels.forEachIndexed { idx, txt ->
                    FilterChip(
                        selected = skateMaterialIdx == idx,
                        onClick = { skateMaterialIdx = idx },
                        label = { Text(txt, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan,
                        )
                    )
                }
            }

            if (glassOnGlassWarning) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonRose.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonRose),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isTr) "⚠️ UYARI: Cam Mousepad üzerinde Cam Skatez kullanımı her iki yüzeyi de kalıcı olarak çizer! Cam pad ile mutlaka Saf PTFE veya Dot PTFE kullanın."
                        else "⚠️ WARNING: Using Glass Skates on a Glass Mousepad permanently scratches both surfaces! Always use Virgin PTFE on glass pads.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonRose,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isTr) "🔬 Triboloji & Sürtünme Fiziği Raporu" else "🔬 Tribology & Friction Physics Report",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NeonCyan,
            )

            LabMetricRow(
                label = if (isTr) "Statik Sürtünme Katsayısı (μs - İlk Kalkış)" else "Static Friction Coefficient (μs)",
                value = "${(muStatic * 100).roundToInt() / 100.0} μs",
                valueColor = NeonCyan,
            )
            LabMetricRow(
                label = if (isTr) "Kinetik Sürtünme Katsayısı (μk - Kayma)" else "Kinetic Friction Coefficient (μk)",
                value = "${(muKinetic * 100).roundToInt() / 100.0} μk",
                valueColor = NeonEmerald,
            )
            LabMetricRow(
                label = if (isTr) "Mikro-Nişan (Micro-Adjustment) Çevikliği" else "Micro-Adjustment Agility",
                value = "$microAdjustScore / 100",
                valueColor = NeonEmerald,
            )
            LabMetricRow(
                label = if (isTr) "Flick Durdurma Gücü (Stopping Power)" else "Flick Stopping Power",
                value = if (isTr) "$stoppingPower / 10 Puan" else "$stoppingPower / 10 pts",
                valueColor = NeonAmber,
            )
            LabMetricRow(
                label = if (isTr) "İdeal Espor Oyun Profili" else "Ideal Esports Genre Profile",
                value = when {
                    isGlassPad -> if (isTr) "Apex Legends / Overwatch 2 / Saf Tracking" else "Apex / OW2 / Pure Tracking"
                    isSpeedHybrid -> if (isTr) "Hibrit FPS (Valorant + CoD + Apex)" else "Hybrid FPS (Valorant + CoD + Apex)"
                    else -> if (isTr) "Taktiksel FPS (CS2 & Valorant Keskin Flick)" else "Tactical FPS (CS2 & Valorant Precision)"
                },
                valueColor = NeonPurple,
            )
        }
    }
}

// ============================================================================================
// TAB 3: WEBCAM FIELD OF VIEW (FOV) & LOW-LIGHT LUX SIMULATOR
// ============================================================================================
@Composable
private fun WebcamFovAndLightSimulatorTab(
    selectedWebcam: HardwareItem?,
    isTr: Boolean,
    onPickWebcam: () -> Unit,
) {
    val camSpec = selectedWebcam?.spec as? WebcamSpec

    var fovDeg by remember(camSpec) {
        val rawFov = camSpec?.fieldOfViewDeg?.takeIf { it > 0 } ?: 82
        mutableStateOf(rawFov.toFloat().coerceIn(60f, 120f))
    }
    var distanceCm by remember { mutableStateOf(60f) }
    var roomLuxIdx by remember { mutableStateOf(1) } // 0: Karanlık (30 Lux), 1: Oda Işığı (150 Lux), 2: Stüdyo Key Light (500 Lux)

    // Frame width math: W = 2 * d * tan(FOV / 2)
    val halfAngleRad = (fovDeg / 2.0) * (PI / 180.0)
    val horizontalFrameWidthCm = (2.0 * distanceCm * tan(halfAngleRad)).roundToInt()

    val isLargeSensor = camSpec?.sensorSizeInch?.let { s ->
        s.contains("APS-C", ignoreCase = true) ||
            s.contains("Full", ignoreCase = true) ||
            s.contains("1\"", ignoreCase = true) ||
            s.contains("1/1.2") ||
            s.contains("1/1.3") ||
            s.contains("1/1.5") ||
            s.contains("1/1.8") ||
            s.contains("1/2\"")
    } ?: false

    val noiseLevelText = when (roomLuxIdx) {
        0 -> if (isLargeSensor) {
            if (isTr) "Düşük Kumlanma (Büyük Sensör Avantajı)" else "Low Grain (Large Sensor Advantage)"
        } else {
            if (isTr) "Yüksek ISO Kumlanması (Key Light Önerilir)" else "High ISO Grain (Key Light Recommended)"
        }
        1 -> if (isTr) "Temiz Görüntü (Dengeli ISO 200-400)" else "Clean Image (Balanced ISO 200-400)"
        else -> if (isTr) "Stüdyo Referans Netliği (ISO 100 + 1/120s)" else "Studio Reference Clarity (ISO 100 + 1/120s)"
    }

    LabPickerCard(
        emoji = "📷",
        label = if (isTr) "Test Edilen Yayıncı Kamerası (Webcam)" else "Tested Streaming Webcam",
        selectedName = selectedWebcam?.displayName ?: (if (isTr) "Elgato Facecam Pro / Razer Kiyo Pro Ultra (Seç)" else "Elgato Facecam Pro / Kiyo Pro Ultra (Tap to Pick)"),
        selectedSub = camSpec?.let { "${it.maxResolution} @ ${it.maxFrameRateFps}FPS • ${it.sensorModel} (${it.sensorSizeInch}) • ${it.fieldOfViewDeg}°" }
            ?: (if (isTr) "Gerçek sensör boyutu ve lens açısını test etmek için seçin" else "Pick webcam from catalog to test real sensor & FOV"),
        accentColor = ElectricBlue,
        isTr = isTr,
        onClick = onPickWebcam,
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (isTr) "Görüş Açısı (FOV): ${fovDeg.roundToInt()}°" else "Field of View (FOV): ${fovDeg.roundToInt()}°",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue,
                )
                Text(
                    text = if (isTr) "Mesafe: ${distanceCm.roundToInt()} cm" else "Distance: ${distanceCm.roundToInt()} cm",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                )
            }

            Slider(
                value = fovDeg,
                onValueChange = { fovDeg = it },
                valueRange = 60f..120f,
                colors = SliderDefaults.colors(thumbColor = ElectricBlue, activeTrackColor = ElectricBlue),
            )

            Slider(
                value = distanceCm,
                onValueChange = { distanceCm = it },
                valueRange = 40f..100f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
            )

            // 2D FOV Cone Visualization Canvas (Dark viewport ensures high contrast in both Light & Dark themes)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val camPos = Offset(w / 2f, 14f)
                    val depthRatio = ((distanceCm - 40f) / 60f).coerceIn(0f, 1f)
                    val coneBottomY = 58f + depthRatio * (h - 74f)
                    val spreadRatio = ((fovDeg - 60f) / 60f).coerceIn(0f, 1f)
                    val halfSpanFactor = (0.18f + spreadRatio * 0.30f) * (0.68f + depthRatio * 0.32f)
                    val leftEnd = Offset(w * (0.5f - halfSpanFactor), coneBottomY)
                    val rightEnd = Offset(w * (0.5f + halfSpanFactor), coneBottomY)

                    val conePath = Path().apply {
                        moveTo(camPos.x, camPos.y)
                        lineTo(leftEnd.x, leftEnd.y)
                        lineTo(rightEnd.x, rightEnd.y)
                        close()
                    }
                    drawPath(conePath, color = ElectricBlue.copy(alpha = 0.22f))
                    drawPath(conePath, color = ElectricBlue, style = Stroke(width = 3f))
                    drawCircle(color = NeonCyan, radius = 8f, center = camPos)
                }

                Text(
                    text = if (isTr) "Kadraj Genişliği: $horizontalFrameWidthCm cm" else "Frame Width: $horizontalFrameWidthCm cm",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                )
            }

            Text(
                text = if (isTr) "Oda Aydınlatma Şiddeti (Lux)" else "Room Lighting Intensity (Lux)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val luxLabels = if (isTr) listOf("Loş Oda (30 lx)", "Ev Lambası (150 lx)", "Stüdyo Işığı (500 lx)") else listOf("Dim (30 lx)", "Room (150 lx)", "Key Light (500 lx)")
                luxLabels.forEachIndexed { idx, txt ->
                    FilterChip(
                        selected = roomLuxIdx == idx,
                        onClick = { roomLuxIdx = idx },
                        label = { Text(txt, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                            selectedLabelColor = ElectricBlue,
                        )
                    )
                }
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LabMetricRow(
                label = if (isTr) "Yatay Arka Plan Kapsamı" else "Horizontal Background Span",
                value = "$horizontalFrameWidthCm cm (${if (horizontalFrameWidthCm > 115) (if (isTr) "Geniş Oda" else "Wide Room") else (if (isTr) "Sıkı Portre" else "Tight Portrait")})",
                valueColor = ElectricBlue,
            )
            LabMetricRow(
                label = if (isTr) "Sensör Düşük Işık & Kumlanma" else "Sensor Low-Light & Grain",
                value = noiseLevelText,
                valueColor = if (roomLuxIdx == 0 && !isLargeSensor) NeonAmber else NeonEmerald,
            )
            LabMetricRow(
                label = if (isTr) "Akıcı Yayın Enstantane & Anti-Flicker" else "Recommended Shutter & Anti-Flicker",
                value = if (isTr) "50Hz Şebeke: 1/100 sn • 60Hz: 1/120 sn" else "50Hz Mains: 1/100s • 60Hz: 1/120s",
                valueColor = NeonCyan,
            )
        }
    }
}

// ============================================================================================
// TAB 4: GAMING CHAIR & DESK BIOMECHANICS ERGONOMICS WIZARD
// ============================================================================================
@Composable
private fun ChairErgonomicsWizardTab(
    selectedChair: HardwareItem?,
    isTr: Boolean,
    onPickChair: () -> Unit,
) {
    val chairSpec = selectedChair?.spec as? GamingChairSpec

    var userHeightCm by remember { mutableStateOf(178f) }
    var userWeightKg by remember { mutableStateOf(78f) }
    var deskHeightCm by remember { mutableStateOf(74f) }

    // Anthropometric Biomechanical Formulas
    val idealSeatPanHeightCm = (userHeightCm * 0.25f + 2f).roundToInt()
    val idealDeskElbowHeightCm = (idealSeatPanHeightCm + userHeightCm * 0.14f).roundToInt()
    val idealMonitorTopCm = (idealSeatPanHeightCm + userHeightCm * 0.38f).roundToInt()
    val deskDeltaCm = (deskHeightCm - idealDeskElbowHeightCm).roundToInt()
    val needsFootrest = deskDeltaCm >= 3

    val reportedCapKg = chairSpec?.maxWeightCapacityKg?.takeIf { it > 0 }
    val maxCapKg = reportedCapKg ?: 130
    val weightSafe = userWeightKg <= maxCapKg

    LabPickerCard(
        emoji = "🪑",
        label = if (isTr) "Seçili Oyuncu & Ergonomi Koltuğu" else "Selected Gaming & Ergonomic Chair",
        selectedName = selectedChair?.displayName ?: (if (isTr) "Secretlab TITAN Evo / Herman Miller Embody (Seç)" else "Secretlab TITAN Evo / Embody (Tap to Pick)"),
        selectedSub = chairSpec?.let {
            listOfNotNull(
                it.chairType.ifBlank { null },
                it.lumbarSupportType.ifBlank { null },
                reportedCapKg?.let { cap -> "Max ${cap}kg" } ?: (if (isTr) "Ref. ~130kg" else "Ref. ~130kg"),
            ).joinToString(" • ")
        }
            ?: (if (isTr) "Koltuk taşıma kapasitesi ve boy uyumluluğunu denetlemek için seçin" else "Pick chair from catalog to check capacity & height fit"),
        accentColor = NeonPurple,
        isTr = isTr,
        onClick = onPickChair,
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (isTr) "📏 Antropometrik Vücut & Masa Ölçüleriniz" else "📏 Anthropometric Body & Desk Measurements",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = NeonPurple,
            )

            Text(
                text = if (isTr) "Boyunuz: ${userHeightCm.roundToInt()} cm" else "Your Height: ${userHeightCm.roundToInt()} cm",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = userHeightCm,
                onValueChange = { userHeightCm = it },
                valueRange = 150f..205f,
                colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple),
            )

            Text(
                text = if (isTr) "Kilonuz: ${userWeightKg.roundToInt()} kg" else "Your Weight: ${userWeightKg.roundToInt()} kg",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = userWeightKg,
                onValueChange = { userWeightKg = it },
                valueRange = 45f..150f,
                colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
            )

            Text(
                text = if (isTr) "Mevcut Masa Yüksekliği: ${deskHeightCm.roundToInt()} cm" else "Current Desk Height: ${deskHeightCm.roundToInt()} cm",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = deskHeightCm,
                onValueChange = { deskHeightCm = it },
                valueRange = 65f..85f,
                colors = SliderDefaults.colors(thumbColor = NeonEmerald, activeTrackColor = NeonEmerald),
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isTr) "🩺 L4-L5 Omurga & Biyomekanik Kalibrasyon" else "🩺 L4-L5 Spine & Biomechanical Calibration",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NeonPurple,
            )

            LabMetricRow(
                label = if (isTr) "İdeal Koltuk Oturma Yüksekliği (Yerden)" else "Ideal Seat Pan Height (From Floor)",
                value = if (isTr) "$idealSeatPanHeightCm cm (Diz 90°-95° Açıda)" else "$idealSeatPanHeightCm cm (Knee 90°-95°)",
                valueColor = NeonEmerald,
            )
            LabMetricRow(
                label = if (isTr) "İdeal Dirsek / Kolçak & Masa Yüksekliği" else "Ideal Elbow / Armrest & Desk Height",
                value = "$idealDeskElbowHeightCm cm",
                valueColor = NeonCyan,
            )
            LabMetricRow(
                label = if (isTr) "Monitör Üst Çerçeve Göz Hizası" else "Monitor Top Bezel Eye Level",
                value = if (isTr) "$idealMonitorTopCm cm (Yerden)" else "$idealMonitorTopCm cm (From Floor)",
                valueColor = ElectricBlue,
            )
            LabMetricRow(
                label = if (isTr) "Masa & Ayak Desteği (Footrest) Analizi" else "Desk & Footrest Ergonomic Analysis",
                value = when {
                    needsFootrest -> if (isTr) "Ayak Desteği Önerilir (+${deskDeltaCm}cm Masa Yüksek)" else "Footrest Recommended (+${deskDeltaCm}cm High Desk)"
                    deskDeltaCm <= -4 -> if (isTr) "Masa Alçak (${deskDeltaCm}cm) • Yükseklik Ayarlı Masa Önerilir" else "Desk Too Low (${deskDeltaCm}cm) • Riser / Standing Desk Advised"
                    else -> if (isTr) "İdeal Uyum (Masa Yüksekliği Boyunuza Uygun)" else "Optimal Fit (Desk Height Matches Stature)"
                },
                valueColor = if (needsFootrest || deskDeltaCm <= -4) NeonAmber else NeonEmerald,
            )
            LabMetricRow(
                label = if (isTr) "Amortisör & Taşıma Kapasitesi Uyumu" else "Gas Lift & Weight Capacity Fit",
                value = when {
                    !weightSafe -> if (isTr) "Kapasite Aşıldı! (${userWeightKg.roundToInt()} kg > Max $maxCapKg kg • XL Seçin)" else "Exceeds Max Load! (${userWeightKg.roundToInt()} kg > $maxCapKg kg • Pick XL)"
                    userWeightKg >= maxCapKg * 0.90f -> if (isTr) "Sınıra Yakın (${userWeightKg.roundToInt()} kg / Max $maxCapKg kg)" else "Near Limit (${userWeightKg.roundToInt()} kg / Max $maxCapKg kg)"
                    else -> if (isTr) "Güvenli (${userWeightKg.roundToInt()} kg / Max $maxCapKg kg)" else "Safe (${userWeightKg.roundToInt()} kg / Max $maxCapKg kg)"
                },
                valueColor = when {
                    !weightSafe -> NeonRose
                    userWeightKg >= maxCapKg * 0.90f -> NeonAmber
                    else -> NeonEmerald
                },
            )
        }
    }
}

@Composable
private fun LabPickerCard(
    emoji: String,
    label: String,
    selectedName: String,
    selectedSub: String,
    accentColor: Color,
    isTr: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.16f),
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                    Text(
                        text = selectedName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = selectedSub,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = accentColor.copy(alpha = 0.16f),
            ) {
                Text(
                    text = if (isTr) "Değiştir" else "Change",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun LabMetricRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.End,
        )
    }
}
