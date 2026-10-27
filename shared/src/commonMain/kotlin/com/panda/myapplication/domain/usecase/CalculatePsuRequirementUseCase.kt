package com.panda.myapplication.domain.usecase

import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PsuCalculationPolicy
import com.panda.myapplication.domain.model.PsuRequirement
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.Wattage

/**
 * Estimates PSU requirement for a build:
 * `load = Σ component draw + overhead`, `recommended = roundUp(max(load × (1 + headroom), GPU vendor recommendation))`.
 * Motherboard draw is covered by [PsuCalculationPolicy.baseSystemOverhead]; PSUs contribute no load.
 */
class CalculatePsuRequirementUseCase(
    private val policy: PsuCalculationPolicy = PsuCalculationPolicy(),
) {
    operator fun invoke(items: List<HardwareItem>?): PsuRequirement {
        val safeItems = items.orEmpty()
        val componentLoad = safeItems.sumOf { it.powerDrawWatts() }
        if (componentLoad == 0) {
            return PsuRequirement(
                estimatedLoad = Wattage(0),
                recommendedWattage = Wattage(0),
            )
        }
        val estimatedLoad = componentLoad + policy.baseSystemOverhead.watts
        val withHeadroom = estimatedLoad + ceilDiv(estimatedLoad * policy.headroomPercent, PERCENT)
        val vendorMinimum = safeItems.maxOfOrNull { it.vendorRecommendedPsuWatts() } ?: 0
        val step = policy.roundingStep.watts.coerceAtLeast(50)
        val recommended = roundUp(maxOf(withHeadroom, vendorMinimum), step)

        return PsuRequirement(
            estimatedLoad = Wattage(estimatedLoad),
            recommendedWattage = Wattage(recommended),
        )
    }

    private fun HardwareItem.powerDrawWatts(): Int = when (val s = spec) {
        is CpuSpec -> s.tdp.watts
        is GpuSpec -> s.tdp.watts
        is RamSpec -> s.moduleCount * policy.ramPerModule.watts
        is StorageSpec -> when (s.mediaType) {
            StorageMediaType.SSD -> policy.ssdDraw.watts
            StorageMediaType.HDD -> policy.hddDraw.watts
        }
        is CoolerSpec -> when (s.type) {
            CoolerType.AIR -> policy.airCoolerDraw.watts
            CoolerType.LIQUID -> policy.liquidCoolerDraw.watts
        }
        else -> 0
    }

    private fun HardwareItem.vendorRecommendedPsuWatts(): Int = when (val s = spec) {
        is GpuSpec -> s.recommendedPsu.watts
        else -> 0
    }

    private fun ceilDiv(value: Int, divisor: Int): Int = (value + divisor - 1) / divisor

    private fun roundUp(value: Int, step: Int): Int = ceilDiv(value, step) * step

    private companion object {
        const val PERCENT = 100
    }
}
