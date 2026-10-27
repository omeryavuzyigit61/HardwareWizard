package com.panda.myapplication.domain.usecase

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.UpsSpec

@Immutable
data class UpsCalculationResult(
    val totalSystemWatts: Int,
    val recommendedWattCapacity: Int,
    val recommendedVaRating: Int,
    val pureSineWaveRequired: Boolean,
    val activePfcWarning: String,
    val matchingUpsRecommendations: List<UpsRecommendationItem>,
)

@Immutable
data class UpsRecommendationItem(
    val item: HardwareItem,
    val estimatedRuntimeMinutes: Int,
    val isPureSineWave: Boolean,
    val topology: String,
)

class UpsCalculatorUseCase {

    /**
     * Calculates required UPS capacity and runtime for a PC battlestation setup.
     */
    fun calculate(
        cpuTdpWatts: Int,
        gpuTgpWatts: Int,
        monitorCount: Int = 1,
        monitorWattEach: Int = 35,
        baseSystemWatts: Int = 65,
        upsCatalog: List<HardwareItem>,
    ): UpsCalculationResult {
        val totalWatts = cpuTdpWatts + gpuTgpWatts + baseSystemWatts + (monitorCount * monitorWattEach)
        val recommendedWatt = (totalWatts * 1.25).toInt().coerceAtLeast(400)
        val recommendedVa = (recommendedWatt / 0.60).toInt()

        val warning = "Sisteminizdeki 80 Plus sertifikalı modern güç kaynakları Aktif PFC (Güç Faktörü Düzeltme) devresi içerir. Aktif PFC devreleri basamaklı/simüle sinüs dalgasında rezonansa girerek kapanır; bu nedenle 'Tam Sinüs (Pure Sine Wave)' UPS seçilmelidir."

        val recommendations = upsCatalog
            .filter { it.category == HardwareCategory.UPS }
            .mapNotNull { item ->
                val spec = item.spec as? UpsSpec
                val wattCap = spec?.wattCapacity ?: (spec?.vaRating?.let { (it * 0.6).toInt() } ?: 600)
                if (wattCap < totalWatts * 0.9) return@mapNotNull null

                val halfRuntime = spec?.runtimeMinHalfLoad ?: 12
                val loadRatio = totalWatts.toDouble() / wattCap.toDouble()
                val estimatedMinutes = if (loadRatio >= 0.9) {
                    spec?.runtimeMinFullLoad ?: 4
                } else {
                    (halfRuntime * (0.5 / loadRatio.coerceIn(0.2, 1.0))).toInt().coerceIn(3, 45)
                }

                val isPure = spec?.sinewaveType?.contains("Tam Sinüs", ignoreCase = true) == true ||
                        spec?.sinewaveType?.contains("Pure", ignoreCase = true) == true

                UpsRecommendationItem(
                    item = item,
                    estimatedRuntimeMinutes = estimatedMinutes,
                    isPureSineWave = isPure,
                    topology = spec?.topology ?: "Line-Interactive",
                )
            }
            .sortedByDescending { it.isPureSineWave }

        return UpsCalculationResult(
            totalSystemWatts = totalWatts,
            recommendedWattCapacity = recommendedWatt,
            recommendedVaRating = recommendedVa,
            pureSineWaveRequired = true,
            activePfcWarning = warning,
            matchingUpsRecommendations = recommendations,
        )
    }
}
