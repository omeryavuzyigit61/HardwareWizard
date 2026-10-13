package com.panda.myapplication.presentation.wizard

import com.panda.myapplication.domain.engine.BottleneckAnalysis
import com.panda.myapplication.domain.engine.BottleneckType
import com.panda.myapplication.domain.engine.ClearanceRadarResult
import com.panda.myapplication.domain.engine.GamingSimulationResult
import com.panda.myapplication.domain.engine.PlatformStatusBadge
import com.panda.myapplication.domain.engine.ResolutionOption
import com.panda.myapplication.domain.engine.WattageCockpitInfo
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.BuildCompatibilityReport
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem

data class WizardUiState(
    val activeBuild: ActiveBuild = ActiveBuild(),
    val cockpitInfo: WattageCockpitInfo = WattageCockpitInfo(0, 0, 0, null, true),
    val platformStatus: PlatformStatusBadge = PlatformStatusBadge(
        platformName = "Platform Belirlenmedi",
        isCompatible = true,
        statusText = "CPU veya Anakart ekleyerek başlayın",
    ),
    val bottleneckAnalysis: BottleneckAnalysis = BottleneckAnalysis(
        percentage = 0,
        type = BottleneckType.BALANCED,
        message = "CPU ve GPU seçildiğinde hesaplanacak",
    ),
    val clearanceRadar: ClearanceRadarResult = ClearanceRadarResult(
        gpuClearanceMm = null,
        maxGpuLengthMm = null,
        gpuLengthMm = null,
        coolerClearanceMm = null,
        maxCoolerHeightMm = null,
        coolerHeightMm = null,
        radiatorClearanceMm = null,
        maxRadiatorMm = null,
        coolerRadiatorMm = null,
        hasClearanceIssue = false,
        issueMessage = null,
    ),
    val selectedResolution: ResolutionOption = ResolutionOption.P1440,
    val gamingSimulation: GamingSimulationResult = GamingSimulationResult(
        resolution = ResolutionOption.P1440,
        isReady = false,
        games = emptyList(),
        synthetics = emptyList(),
    ),
    val compatibilityReport: BuildCompatibilityReport = BuildCompatibilityReport(isCompatible = true),

    // ModalBottomSheet Slot Picker
    val isPickerOpen: Boolean = false,
    val pickerCategory: HardwareCategory? = null,
    val pickerSearchQuery: String = "",
    val pickerConstraintDescription: String? = null,
    val pickerItems: List<HardwareItem> = emptyList(),
    val isPickerLoading: Boolean = false,

    // Share System Card Dialog
    val isShareDialogOpen: Boolean = false,
    val shareCardText: String = "",
)
