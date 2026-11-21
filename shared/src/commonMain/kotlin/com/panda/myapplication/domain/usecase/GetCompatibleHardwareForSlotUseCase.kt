package com.panda.myapplication.domain.usecase

import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.RamSpec

data class CompatibleSlotResult(
    val items: List<HardwareItem>,
    val filterConstraintDescription: String? = null,
)

class GetCompatibleHardwareForSlotUseCase {

    operator fun invoke(
        category: HardwareCategory,
        activeBuild: ActiveBuild,
        candidates: List<HardwareItem>,
    ): CompatibleSlotResult {
        val cpuSpec = activeBuild.cpu?.spec as? CpuSpec
        val moboSpec = activeBuild.motherboard?.spec as? MotherboardSpec
        val ramSpec = activeBuild.ram?.spec as? RamSpec
        val coolerSpec = activeBuild.cooler?.spec as? CoolerSpec
        val gpuSpec = activeBuild.gpu?.spec as? GpuSpec
        val caseSpec = activeBuild.case?.spec as? CaseSpec

        var constraintDesc: String? = null

        val filtered = when (category) {
            HardwareCategory.CPU -> {
                val requiredSocket = moboSpec?.socket
                val coolerRequiredSocket = if (requiredSocket == null && coolerSpec != null) {
                    coolerSpec.supportedSockets
                } else null

                when {
                    requiredSocket != null -> {
                        constraintDesc = "Anakart soketine kilitlendi: $requiredSocket"
                        candidates.filter { item ->
                            val s = item.spec as? CpuSpec ?: return@filter false
                            s.socket.equals(requiredSocket, ignoreCase = true)
                        }
                    }
                    coolerRequiredSocket != null && coolerRequiredSocket.isNotEmpty() -> {
                        constraintDesc = "Seçili soğutucu soketlerine kilitlendi (${coolerRequiredSocket.joinToString(", ")})"
                        candidates.filter { item ->
                            val s = item.spec as? CpuSpec ?: return@filter false
                            coolerRequiredSocket.any { it.equals(s.socket, ignoreCase = true) }
                        }
                    }
                    else -> candidates
                }
            }

            HardwareCategory.MOTHERBOARD -> {
                val requiredSocket = cpuSpec?.socket
                val requiredRamGen = ramSpec?.generation
                val requiredRamModules = ramSpec?.moduleCount?.takeIf { it > 0 }
                val requiredRamGb = ramSpec?.capacity?.megabytes?.div(1024)?.takeIf { it > 0 }
                val selectedCaseType = caseSpec?.type?.takeIf { it.isNotBlank() }

                val parts = mutableListOf<String>()
                if (requiredSocket != null) parts.add("$requiredSocket Soket")
                if (requiredRamGen != null) parts.add(requiredRamGen.name)
                if (requiredRamModules != null && requiredRamModules > 2) parts.add("≥${requiredRamModules} RAM Slotu")
                if (selectedCaseType != null) parts.add("$selectedCaseType Kasa")
                if (parts.isNotEmpty()) {
                    constraintDesc = "Kilitlendi: ${parts.joinToString(" + ")}"
                }

                if (parts.isEmpty()) {
                    candidates
                } else {
                    candidates.filter { item ->
                        val m = item.spec as? MotherboardSpec ?: return@filter false
                        val socketOk = requiredSocket == null || m.socket.equals(requiredSocket, ignoreCase = true)
                        val ramGenOk = requiredRamGen == null || m.memoryGeneration == requiredRamGen
                        val slotsOk = requiredRamModules == null || m.ramSlots <= 0 || m.ramSlots >= requiredRamModules
                        val capOk = requiredRamGb == null || m.maxMemoryGb == null || m.maxMemoryGb <= 0 || m.maxMemoryGb >= requiredRamGb
                        val formFactorOk = selectedCaseType == null ||
                            CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(m.formFactor, selectedCaseType)
                        socketOk && ramGenOk && slotsOk && capOk && formFactorOk
                    }
                }
            }

            HardwareCategory.RAM -> {
                val cpuInferredGen = when (cpuSpec?.socket?.uppercase()?.trim()) {
                    "AM5", "LGA1851" -> DdrGeneration.DDR5
                    "AM4", "LGA1200", "LGA1151" -> DdrGeneration.DDR4
                    else -> null
                }
                val requiredGen = moboSpec?.memoryGeneration ?: cpuInferredGen
                val maxSlots = moboSpec?.ramSlots?.takeIf { it > 0 }
                val maxGb = moboSpec?.maxMemoryGb?.takeIf { it > 0 }

                val parts = mutableListOf<String>()
                if (requiredGen != null) {
                    parts.add(
                        if (moboSpec != null) "Anakart: ${requiredGen.name}"
                        else "Soket (${cpuSpec?.socket}): ${requiredGen.name}",
                    )
                }
                if (maxSlots != null) parts.add("Max $maxSlots Modül")
                if (maxGb != null) parts.add("Max ${maxGb}GB")
                if (parts.isNotEmpty()) {
                    constraintDesc = "Kilitlendi: ${parts.joinToString(" • ")}"
                }

                if (requiredGen == null && maxSlots == null && maxGb == null) {
                    candidates
                } else {
                    candidates.filter { item ->
                        val r = item.spec as? RamSpec ?: return@filter false
                        val genOk = requiredGen == null || r.generation == requiredGen
                        val slotsOk = maxSlots == null || r.moduleCount <= 0 || r.moduleCount <= maxSlots
                        val capOk = maxGb == null || (r.capacity.megabytes / 1024) <= maxGb
                        genOk && slotsOk && capOk
                    }
                }
            }

            HardwareCategory.GPU -> {
                val maxGpu = caseSpec?.maxGpuLengthMm
                if (maxGpu != null && maxGpu > 0) {
                    constraintDesc = "Kasa GPU sınırına kilitlendi: max ${maxGpu}mm"
                    candidates.filter { item ->
                        val g = item.spec as? GpuSpec ?: return@filter false
                        val len = g.lengthMm
                        len == null || len <= 0 || len <= maxGpu
                    }
                } else {
                    candidates
                }
            }

            HardwareCategory.COOLER -> {
                val requiredSocket = cpuSpec?.socket
                val maxRad = caseSpec?.maxRadiatorSizeMm?.takeIf { it > 0 }
                val maxAirHeight = caseSpec?.maxCpuCoolerHeightMm?.takeIf { it > 0 }

                val parts = mutableListOf<String>()
                if (requiredSocket != null) parts.add("Soket: $requiredSocket")
                if (maxRad != null) parts.add("Radyatör ≤${maxRad}mm")
                if (maxAirHeight != null) parts.add("Kule ≤${maxAirHeight}mm")
                if (parts.isNotEmpty()) {
                    constraintDesc = parts.joinToString(" | ")
                }

                if (parts.isEmpty()) {
                    candidates
                } else {
                    candidates.filter { item ->
                        val c = item.spec as? CoolerSpec ?: return@filter false
                        val socketOk = requiredSocket == null ||
                            c.supportedSockets.isEmpty() ||
                            c.supportedSockets.any { it.equals(requiredSocket, ignoreCase = true) }
                        val radOk = maxRad == null || c.type != CoolerType.LIQUID || (c.radiatorSizeMm ?: 0) <= maxRad
                        val heightOk = maxAirHeight == null ||
                            c.type != CoolerType.AIR ||
                            c.heightMm == null ||
                            c.heightMm <= 0 ||
                            c.heightMm <= maxAirHeight
                        socketOk && radOk && heightOk
                    }
                }
            }

            HardwareCategory.CASE -> {
                val requiredGpuLength = gpuSpec?.lengthMm?.takeIf { it > 0 } ?: 0
                val liquidRadiator = if (coolerSpec?.type == CoolerType.LIQUID) coolerSpec.radiatorSizeMm?.takeIf { it > 0 } ?: 0 else 0
                val airCoolerHeight = if (coolerSpec?.type == CoolerType.AIR) coolerSpec.heightMm?.takeIf { it > 0 } ?: 0 else 0
                val requiredMoboFormFactor = moboSpec?.formFactor

                val parts = mutableListOf<String>()
                if (requiredMoboFormFactor != null) parts.add("${requiredMoboFormFactor.displayName} Anakart")
                if (requiredGpuLength > 0) parts.add("GPU ≥${requiredGpuLength}mm")
                if (liquidRadiator > 0) parts.add("Radyatör ≥${liquidRadiator}mm")
                if (airCoolerHeight > 0) parts.add("Soğutucu ≥${airCoolerHeight}mm")
                if (parts.isNotEmpty()) {
                    constraintDesc = "Kasa Desteği: ${parts.joinToString(" • ")}"
                }

                if (parts.isEmpty()) {
                    candidates
                } else {
                    candidates.filter { item ->
                        val cs = item.spec as? CaseSpec ?: return@filter false
                        val gpuOk = requiredGpuLength == 0 || (cs.maxGpuLengthMm ?: 0) >= requiredGpuLength
                        val radOk = liquidRadiator == 0 || (cs.maxRadiatorSizeMm ?: 0) >= liquidRadiator
                        val airOk = airCoolerHeight == 0 ||
                            cs.maxCpuCoolerHeightMm == null ||
                            cs.maxCpuCoolerHeightMm <= 0 ||
                            cs.maxCpuCoolerHeightMm >= airCoolerHeight
                        val moboOk = requiredMoboFormFactor == null ||
                            CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(requiredMoboFormFactor, cs.type)
                        gpuOk && radOk && airOk && moboOk
                    }
                }
            }

            HardwareCategory.STORAGE,
            HardwareCategory.PSU,
            HardwareCategory.LAPTOP,
            HardwareCategory.MOUSE,
            HardwareCategory.KEYBOARD,
            HardwareCategory.HEADSET,
            HardwareCategory.MONITOR,
            HardwareCategory.MICROPHONE,
            HardwareCategory.MOUSEPAD,
            HardwareCategory.WEBCAM,
            HardwareCategory.GAMING_CHAIR,
            HardwareCategory.SMARTPHONE,
            HardwareCategory.TABLET -> candidates
            else -> candidates
        }

        return CompatibleSlotResult(
            items = filtered,
            filterConstraintDescription = constraintDesc,
        )
    }
}
