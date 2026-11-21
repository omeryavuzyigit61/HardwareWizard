package com.panda.myapplication.domain.usecase

import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.BuildCompatibilityReport
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.MotherboardFormFactor
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
            if (!socketsMatch(cpuSpec.socket, moboSpec.socket)) {
                socketMismatch = "Soket Uyuşmazlığı: İşlemci (${normalizeSocketName(cpuSpec.socket)}) ile Anakart (${normalizeSocketName(moboSpec.socket)}) fiziksel olarak uyumsuz!"
                warnings.add(socketMismatch)
            }
        }

        // 2. RAM Generation, Slot Count & Max Capacity Check (Motherboard vs RAM)
        var ramMismatch: String? = null
        if (moboSpec != null && ramSpec != null) {
            val ramIssues = mutableListOf<String>()
            if (moboSpec.memoryGeneration != ramSpec.generation) {
                ramIssues.add("Bellek Uyuşmazlığı: Anakart ${moboSpec.memoryGeneration.name} desteklerken, seçilen bellek ${ramSpec.generation.name}!")
            }
            if (moboSpec.ramSlots > 0 && ramSpec.moduleCount > moboSpec.ramSlots) {
                ramIssues.add("RAM Yuvası Yetersiz: Seçilen kit ${ramSpec.moduleCount} modül, anakartta yalnızca ${moboSpec.ramSlots} RAM slotu var!")
            }
            val ramGb = ramSpec.capacity.megabytes / 1024
            val maxMoboGb = moboSpec.maxMemoryGb
            if (maxMoboGb != null && maxMoboGb > 0 && ramGb > maxMoboGb) {
                ramIssues.add("RAM Kapasite Aşımı: ${ramGb}GB bellek, anakartın maksimum ${maxMoboGb}GB desteğini aşıyor!")
            }
            if (ramIssues.isNotEmpty()) {
                ramMismatch = ramIssues.joinToString("\n")
                warnings.addAll(ramIssues)
            }
        }

        // 3. Cooler Mounting Check (Cooler vs CPU Socket)
        var coolerMismatch: String? = null
        if (coolerSpec != null && cpuSpec != null && coolerSpec.supportedSockets.isNotEmpty()) {
            val supported = coolerSpec.supportedSockets.any { socketsMatch(it, cpuSpec.socket) }
            if (!supported) {
                coolerMismatch = "Soğutucu Uyarısı: Soğutucu montaj kiti ${normalizeSocketName(cpuSpec.socket)} soketini içermiyor olabilir."
                warnings.add(coolerMismatch)
            }
        }

        // 4. Case Clearance & Form Factor Check (GPU, Air Cooler Height, Radiator, Motherboard Form Factor vs Case)
        val clearanceIssues = mutableListOf<String>()
        if (caseSpec != null) {
            val maxGpu = caseSpec.maxGpuLengthMm
            val gpuLen = gpuSpec?.lengthMm
            if (gpuLen != null && maxGpu != null && gpuLen > 0 && gpuLen > maxGpu) {
                val diff = gpuLen - maxGpu
                clearanceIssues.add("Kasa Sığma Uyarısı: Ekran kartı (${gpuLen}mm), kasanın sınırından (${maxGpu}mm) ${diff}mm daha uzun!")
            }
            if (coolerSpec != null) {
                val radSize = coolerSpec.radiatorSizeMm
                val maxRad = caseSpec.maxRadiatorSizeMm
                if (coolerSpec.type == CoolerType.LIQUID && radSize != null && maxRad != null && radSize > maxRad) {
                    clearanceIssues.add("Radyatör Sığma Uyarısı: Sıvı soğutucu (${radSize}mm), kasanın maksimum radyatör desteğinden (${maxRad}mm) büyük!")
                }
                val coolerHeight = coolerSpec.heightMm
                val maxCoolerH = caseSpec.maxCpuCoolerHeightMm
                if (coolerSpec.type == CoolerType.AIR && coolerHeight != null && maxCoolerH != null && coolerHeight > 0 && coolerHeight > maxCoolerH) {
                    val diff = coolerHeight - maxCoolerH
                    clearanceIssues.add("Hava Soğutucu Yükseklik Uyarısı: Soğutucu (${coolerHeight}mm), kasanın sınırını (${maxCoolerH}mm) ${diff}mm aşıyor!")
                }
            }
            if (moboSpec != null && !isMotherboardSupportedByCase(moboSpec.formFactor, caseSpec.type)) {
                clearanceIssues.add("Anakart Kasa Uyumsuzluğu: ${moboSpec.formFactor.displayName} anakart, ${caseSpec.type ?: "seçili"} kasaya fiziksel olarak sığmaz!")
            }
        }
        val clearanceMismatch = clearanceIssues.takeIf { it.isNotEmpty() }?.joinToString("\n")
        warnings.addAll(clearanceIssues)

        // 5. PSU Wattage & ATX 3.0 / 12V-2x6 Advisory Check
        val psuIssues = mutableListOf<String>()
        if (psuSpec != null) {
            if (psuRequirement.recommendedWattage.watts > 0 && psuSpec.wattage.watts < psuRequirement.recommendedWattage.watts) {
                psuIssues.add("Yetersiz Güç Kaynağı: Seçilen PSU (${psuSpec.wattage.watts}W), sistemin önerilen ${psuRequirement.recommendedWattage.watts}W ihtiyacının altında!")
            }
            val needsPcie5Power = gpuSpec != null && (
                gpuSpec.tdp.watts >= 320 ||
                    gpuSpec.chipset.contains("RTX 50", ignoreCase = true) ||
                    gpuSpec.chipset.contains("RTX 4090", ignoreCase = true) ||
                    gpuSpec.chipset.contains("RTX 4080", ignoreCase = true)
                )
            if (needsPcie5Power && !psuSpec.isAtx3Compliant && !psuSpec.supportsPcie5Power) {
                psuIssues.add("Güç Konnektörü Uyarısı: ${gpuSpec.chipset} için yerel 12V-2x6 / ATX 3.0+ destekli PSU önerilir (adaptör kablosu gerekir).")
            }
        }
        val psuWarning = psuIssues.takeIf { it.isNotEmpty() }?.joinToString("\n")
        warnings.addAll(psuIssues)

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

    companion object {
        private val INTEL_NUMERIC_SOCKETS = setOf(
            "1851", "1700", "1200", "1151", "1150", "1155", "1156", "2066", "2011", "2011V3", "4677",
        )

        /**
         * Canonicalizes CPU/Motherboard/Cooler socket labels so Intel numeric labels
         * ("1700", "1851", "1200", "FCLGA1700", "LGA 1700") match "LGA1700"/"LGA1851"/"LGA1200".
         */
        fun normalizeSocketName(raw: String?): String {
            val trimmed = raw?.trim().orEmpty()
            if (trimmed.isEmpty()) return ""
            val compact = trimmed
                .uppercase()
                .replace("SOCKET", "")
                .replace("FCLGA", "LGA")
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "")
                .trim()
            return when {
                compact in INTEL_NUMERIC_SOCKETS -> "LGA$compact"
                compact == "TR4" || compact == "STR4" -> "STR4"
                compact == "TRX4" || compact == "STRX4" -> "STRX4"
                compact == "WRX8" || compact == "SWRX8" -> "SWRX8"
                compact == "TR5" || compact == "STR5" -> "STR5"
                else -> compact
            }
        }

        fun socketsMatch(socketA: String?, socketB: String?): Boolean {
            val normA = normalizeSocketName(socketA)
            val normB = normalizeSocketName(socketB)
            if (normA.isEmpty() || normB.isEmpty()) return false
            return normA == normB
        }

        /**
         * Determines whether a case's form-factor descriptor physically supports the given motherboard form factor.
         * Defaults to true when case type is unspecified to avoid false rejections.
         */
        fun isMotherboardSupportedByCase(moboFormFactor: MotherboardFormFactor, caseType: String?): Boolean {
            val raw = caseType?.trim()?.uppercase()?.takeIf { it.isNotBlank() } ?: return true
            val caseMaxTier = when {
                raw.contains("FULL TOWER") || raw.contains("SUPER TOWER") || raw.contains("E-ATX") || raw.contains("EATX") -> 4
                raw.contains("MID TOWER") || (raw.contains("ATX") && !raw.contains("MICRO") && !raw.contains("MATX") && !raw.contains("MINI")) -> 3
                raw.contains("MICRO") || raw.contains("MATX") || raw.contains("M-ATX") -> 2
                raw.contains("MINI") || raw.contains("ITX") || raw.contains("SFF") -> 1
                else -> 4 // Unknown/custom chassis type -> do not false-reject
            }
            val moboTier = when (moboFormFactor) {
                MotherboardFormFactor.E_ATX -> 4
                MotherboardFormFactor.ATX -> 3
                MotherboardFormFactor.MICRO_ATX -> 2
                MotherboardFormFactor.MINI_ITX -> 1
            }
            return caseMaxTier >= moboTier
        }
    }
}
