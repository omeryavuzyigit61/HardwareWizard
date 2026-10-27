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

                when {
                    requiredSocket != null && requiredRamGen != null -> {
                        constraintDesc = "Kilitlendi: $requiredSocket Soket + ${requiredRamGen.name}"
                        candidates.filter { item ->
                            val m = item.spec as? MotherboardSpec ?: return@filter false
                            m.socket.equals(requiredSocket, ignoreCase = true) && m.memoryGeneration == requiredRamGen
                        }
                    }
                    requiredSocket != null -> {
                        constraintDesc = "İşlemci soketine kilitlendi: $requiredSocket"
                        candidates.filter { item ->
                            val m = item.spec as? MotherboardSpec ?: return@filter false
                            m.socket.equals(requiredSocket, ignoreCase = true)
                        }
                    }
                    requiredRamGen != null -> {
                        constraintDesc = "RAM nesline kilitlendi: ${requiredRamGen.name}"
                        candidates.filter { item ->
                            val m = item.spec as? MotherboardSpec ?: return@filter false
                            m.memoryGeneration == requiredRamGen
                        }
                    }
                    else -> candidates
                }
            }

            HardwareCategory.RAM -> {
                val cpuInferredGen = when (cpuSpec?.socket?.uppercase()?.trim()) {
                    "AM5", "LGA1851" -> DdrGeneration.DDR5
                    "AM4", "LGA1200", "LGA1151" -> DdrGeneration.DDR4
                    else -> null
                }
                val requiredGen = moboSpec?.memoryGeneration ?: cpuInferredGen
                if (requiredGen != null) {
                    constraintDesc = if (moboSpec != null) {
                        "Anakart nesline kilitlendi: ${requiredGen.name}"
                    } else {
                        "İşlemci soketine (${cpuSpec?.socket}) göre kilitlendi: ${requiredGen.name}"
                    }
                    candidates.filter { item ->
                        val r = item.spec as? RamSpec ?: return@filter false
                        r.generation == requiredGen
                    }
                } else {
                    candidates
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
                val maxRad = caseSpec?.maxRadiatorSizeMm

                when {
                    requiredSocket != null && maxRad != null -> {
                        constraintDesc = "Soket: $requiredSocket | Kasa Radyatör Sınırı: ${maxRad}mm"
                        candidates.filter { item ->
                            val c = item.spec as? CoolerSpec ?: return@filter false
                            val socketOk = c.supportedSockets.isEmpty() || c.supportedSockets.any { it.equals(requiredSocket, ignoreCase = true) }
                            val radOk = c.type != CoolerType.LIQUID || (c.radiatorSizeMm ?: 0) <= maxRad
                            socketOk && radOk
                        }
                    }
                    requiredSocket != null -> {
                        constraintDesc = "İşlemci soketine kilitlendi: $requiredSocket"
                        candidates.filter { item ->
                            val c = item.spec as? CoolerSpec ?: return@filter false
                            c.supportedSockets.isEmpty() || c.supportedSockets.any { it.equals(requiredSocket, ignoreCase = true) }
                        }
                    }
                    maxRad != null -> {
                        constraintDesc = "Kasa radyatör sınırına kilitlendi: max ${maxRad}mm"
                        candidates.filter { item ->
                            val c = item.spec as? CoolerSpec ?: return@filter false
                            c.type != CoolerType.LIQUID || (c.radiatorSizeMm ?: 0) <= maxRad
                        }
                    }
                    else -> candidates
                }
            }

            HardwareCategory.CASE -> {
                val requiredGpuLength = gpuSpec?.lengthMm ?: 0
                val liquidRadiator = if (coolerSpec?.type == CoolerType.LIQUID) coolerSpec.radiatorSizeMm ?: 0 else 0

                when {
                    requiredGpuLength > 0 && liquidRadiator > 0 -> {
                        constraintDesc = "GPU (≥${requiredGpuLength}mm) & Radyatör (≥${liquidRadiator}mm) desteği"
                        candidates.filter { item ->
                            val cs = item.spec as? CaseSpec ?: return@filter false
                            (cs.maxGpuLengthMm ?: 0) >= requiredGpuLength && (cs.maxRadiatorSizeMm ?: 0) >= liquidRadiator
                        }
                    }
                    requiredGpuLength > 0 -> {
                        constraintDesc = "GPU boyu desteğine kilitlendi: ≥${requiredGpuLength}mm"
                        candidates.filter { item ->
                            val cs = item.spec as? CaseSpec ?: return@filter false
                            (cs.maxGpuLengthMm ?: 0) >= requiredGpuLength
                        }
                    }
                    liquidRadiator > 0 -> {
                        constraintDesc = "Sıvı soğutucu radyatörüne kilitlendi: ≥${liquidRadiator}mm"
                        candidates.filter { item ->
                            val cs = item.spec as? CaseSpec ?: return@filter false
                            (cs.maxRadiatorSizeMm ?: 0) >= liquidRadiator
                        }
                    }
                    else -> candidates
                }
            }

            HardwareCategory.STORAGE,
            HardwareCategory.PSU,
            HardwareCategory.PREBUILT_PC,
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
