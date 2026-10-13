package com.panda.myapplication.domain.usecase

import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.BuildCompatibilityReport
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PsuRequirement
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec

class CheckBuildCompatibilityUseCase(
    private val calculatePsuRequirementUseCase: CalculatePsuRequirementUseCase,
) {
    operator fun invoke(build: ActiveBuild): Pair<PsuRequirement, BuildCompatibilityReport> {
        val psuRequirement = calculatePsuRequirementUseCase(build.items)
        val warnings = mutableListOf<String>()

        val cpuSpec = build.cpu?.spec as? CpuSpec
        val moboSpec = build.motherboard?.spec as? MotherboardSpec
        val ramSpec = build.ram?.spec as? RamSpec
        val coolerSpec = build.cooler?.spec as? CoolerSpec
        val gpuSpec = build.gpu?.spec as? GpuSpec
        val caseSpec = build.case?.spec as? CaseSpec
        val psuSpec = build.psu?.spec as? PsuSpec

        // 1. Socket Check (CPU vs Motherboard)
        var socketMismatch: String? = null
        if (cpuSpec != null && moboSpec != null) {
            if (!cpuSpec.socket.equals(moboSpec.socket, ignoreCase = true)) {
                socketMismatch = "Soket Uyuşmazlığı: İşlemci (${cpuSpec.socket}) ile Anakart (${moboSpec.socket}) fiziksel olarak uyumsuz!"
                warnings.add(socketMismatch)
            }
        }

        // 2. RAM Generation Check (Motherboard vs RAM)
        var ramMismatch: String? = null
        if (moboSpec != null && ramSpec != null) {
            if (moboSpec.memoryGeneration != ramSpec.generation) {
                ramMismatch = "Bellek Uyuşmazlığı: Anakart ${moboSpec.memoryGeneration.name} desteklerken, seçilen bellek ${ramSpec.generation.name}!"
                warnings.add(ramMismatch)
            }
        }

        // 3. Cooler Mounting Check (Cooler vs CPU Socket)
        var coolerMismatch: String? = null
        if (coolerSpec != null && cpuSpec != null) {
            val supported = coolerSpec.supportedSockets.any { it.equals(cpuSpec.socket, ignoreCase = true) }
            if (!supported) {
                coolerMismatch = "Soğutucu Uyarısı: Soğutucu montaj kiti ${cpuSpec.socket} soketini içermiyor olabilir."
                warnings.add(coolerMismatch)
            }
        }

        // 4. Case Clearance Check (GPU, Cooler vs Case)
        var clearanceMismatch: String? = null
        if (caseSpec != null) {
            if (gpuSpec != null && gpuSpec.lengthMm > 0 && gpuSpec.lengthMm > caseSpec.maxGpuLengthMm) {
                val diff = gpuSpec.lengthMm - caseSpec.maxGpuLengthMm
                clearanceMismatch = "Kasa Sığma Uyarısı: Ekran kartı (${gpuSpec.lengthMm}mm), kasanın sınırından (${caseSpec.maxGpuLengthMm}mm) ${diff}mm daha uzun!"
                warnings.add(clearanceMismatch)
            }
            if (coolerSpec != null) {
                val radSize = coolerSpec.radiatorSizeMm
                if (coolerSpec.type == CoolerType.LIQUID && radSize != null && radSize > caseSpec.maxRadiatorSizeMm) {
                    val radMsg = "Radyatör Sığma Uyarısı: Sıvı soğutucu (${radSize}mm), kasanın maksimum radyatör desteğinden (${caseSpec.maxRadiatorSizeMm}mm) büyük!"
                    clearanceMismatch = if (clearanceMismatch != null) "$clearanceMismatch\n$radMsg" else radMsg
                    warnings.add(radMsg)
                }
            }
        }

        // 5. PSU Wattage Check
        var psuWarning: String? = null
        if (psuSpec != null && psuRequirement.recommendedWattage.watts > 0) {
            if (psuSpec.wattage.watts < psuRequirement.recommendedWattage.watts) {
                psuWarning = "Yetersiz Güç Kaynağı: Seçilen PSU (${psuSpec.wattage.watts}W), sistemin önerilen ${psuRequirement.recommendedWattage.watts}W ihtiyacının altında!"
                warnings.add(psuWarning)
            }
        }

        val isCompatible = warnings.isEmpty()

        val report = BuildCompatibilityReport(
            isCompatible = isCompatible,
            socketMismatch = socketMismatch,
            ramMismatch = ramMismatch,
            coolerMismatch = coolerMismatch,
            clearanceMismatch = clearanceMismatch,
            psuWarning = psuWarning,
            warnings = warnings,
        )

        return psuRequirement to report
    }
}
