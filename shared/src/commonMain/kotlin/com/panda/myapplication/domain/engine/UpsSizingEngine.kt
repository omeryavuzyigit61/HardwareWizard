package com.panda.myapplication.domain.engine

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.UpsSpec
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.roundToInt

@Immutable
data class UpsTierClass(
    val vaRating: Int,
    val realWattCapacity: Int,
    val batteryConfig: String,
    val totalBatteryWh: Int,
    val isPureSine: Boolean,
    val topologyTr: String,
    val topologyEn: String,
) {
    fun localizedTopology(isTr: Boolean): String = if (isTr) topologyTr else topologyEn
}

@Immutable
data class UpsTierRuntimeRow(
    val tier: UpsTierClass,
    val isOverloaded: Boolean,
    val isRecommended: Boolean,
    val gamingMinutes: Int,
    val idleMinutes: Int,
)

@Immutable
data class UpsSizingReport(
    val dcSystemWatts: Int,
    val acWallLoadWatts: Int,
    val idleAcWatts: Int,
    val safeRecommendedVa: Int,
    val safeRecommendedWatts: Int,
    val requiresPureSineForActivePfc: Boolean,
    val recommendedTier: UpsTierClass,
    val tierRows: List<UpsTierRuntimeRow>,
    val matchingCatalogUps: List<HardwareItem>,
)

object UpsSizingEngine {

    /** Typical 80+ Gold PSU AC-to-DC efficiency under gaming load */
    const val PSU_EFFICIENCY = 0.90
    /** Inverter DC-to-AC conversion efficiency */
    const val INVERTER_EFFICIENCY = 0.85
    /** AGM VRLA Lead-Acid Peukert exponent (high-rate discharge derating) */
    const val PEUKERT_EXPONENT = 1.18
    /** Unified +20% continuous safety margin */
    const val SAFETY_MARGIN = 1.20

    val UPS_TIERS: List<UpsTierClass> = listOf(
        UpsTierClass(850, 480, "1x 12V 9Ah", 108, false, "Line-Interactive (Simüle Sinüs)", "Line-Interactive (Simulated Sine)"),
        UpsTierClass(1000, 600, "2x 12V 7Ah", 168, false, "Line-Interactive", "Line-Interactive"),
        UpsTierClass(1500, 900, "2x 12V 9Ah", 216, true, "Line-Interactive • Tam Sinüs (Active PFC Uyumlu)", "Line-Interactive • Pure Sine Wave (Active PFC)"),
        UpsTierClass(2000, 1200, "4x 12V 9Ah", 432, true, "Online Tam Sinüs (Double Conversion)", "Online Pure Sine (Double Conversion)"),
        UpsTierClass(3000, 1800, "6x 12V 9Ah", 648, true, "Online Tam Sinüs (İş İstasyonu / Sunucu)", "Online Pure Sine (Workstation / Server)"),
    )

    /**
     * Computes realistic VRLA backup runtime in minutes using Peukert's Law:
     * At 1-hour discharge rate (C1), effective capacity is ~60% of 20-hour nominal Wh,
     * and high discharge power scales non-linearly with exponent 1 / k.
     */
    fun estimatePeukertRuntimeMinutes(
        nominalBatteryWh: Int,
        acLoadWatts: Int,
        maxWattCapacity: Int,
    ): Int {
        if (acLoadWatts <= 0) return 60
        if (acLoadWatts > maxWattCapacity) return 0
        // Effective 1-hour Peukert reference energy (AGM VRLA delivers ~62% of C20 Wh at 1C rate * inverter eff)
        val oneHourEffectiveWh = nominalBatteryWh * 0.62 * INVERTER_EFFICIENCY
        val hours = (oneHourEffectiveWh / acLoadWatts.toDouble()).pow(PEUKERT_EXPONENT)
        return (hours * 60.0).roundToInt().coerceIn(2, 240)
    }

    fun calculate(
        pcDcWatts: Int,
        monitorWatts: Int,
        catalogUpsItems: List<HardwareItem> = emptyList(),
    ): UpsSizingReport {
        val cleanPcDc = pcDcWatts.coerceIn(100, 1600)
        val cleanMonitor = monitorWatts.coerceIn(15, 200)
        // Convert PC internal DC load to real AC wall draw via PSU efficiency
        val pcAcWallWatts = (cleanPcDc / PSU_EFFICIENCY).roundToInt()
        val acWallLoadWatts = pcAcWallWatts + cleanMonitor
        val idleAcWatts = ((cleanPcDc * 0.22) / 0.85).roundToInt() + (cleanMonitor * 0.75).roundToInt()

        val safeRecommendedWatts = ceil(acWallLoadWatts * SAFETY_MARGIN).roundToInt()
        val safeRecommendedVa = ceil(safeRecommendedWatts / 0.60).roundToInt()
        val requiresPureSine = cleanPcDc >= 350

        // Select tier using the EXACT same 20% safety margin + Pure Sine requirement for Active PFC rigs
        val recommendedTier = UPS_TIERS.firstOrNull { tier ->
            tier.realWattCapacity >= safeRecommendedWatts && (!requiresPureSine || tier.isPureSine)
        } ?: UPS_TIERS.firstOrNull { it.realWattCapacity >= acWallLoadWatts } ?: UPS_TIERS.last()

        val rows = UPS_TIERS.map { tier ->
            val overloaded = acWallLoadWatts > tier.realWattCapacity
            val gamingMin = estimatePeukertRuntimeMinutes(tier.totalBatteryWh, acWallLoadWatts, tier.realWattCapacity)
            val idleMin = estimatePeukertRuntimeMinutes(tier.totalBatteryWh, idleAcWatts, tier.realWattCapacity).coerceAtLeast(5)
            UpsTierRuntimeRow(
                tier = tier,
                isOverloaded = overloaded,
                isRecommended = tier.vaRating == recommendedTier.vaRating,
                gamingMinutes = gamingMin,
                idleMinutes = idleMin,
            )
        }

        // Match real UPS products from the 340-item Room catalog
        val matchedCatalog = catalogUpsItems
            .filter { item ->
                val s = item.spec as? UpsSpec ?: return@filter false
                val wattOk = s.wattCapacity >= safeRecommendedWatts && s.wattCapacity <= safeRecommendedWatts * 2.2
                val sineOk = !requiresPureSine ||
                    s.sinewaveType.contains("Tam Sinüs", ignoreCase = true) ||
                    s.sinewaveType.contains("Pure Sine", ignoreCase = true) ||
                    s.topology.contains("Online", ignoreCase = true)
                wattOk && sineOk
            }
            .sortedWith(
                compareBy<HardwareItem> { (it.spec as? UpsSpec)?.wattCapacity ?: Int.MAX_VALUE }
                    .thenByDescending {
                        val s = it.spec as? UpsSpec
                        s?.sinewaveType?.contains("Tam Sinüs", true) == true || s?.sinewaveType?.contains("Pure Sine", true) == true
                    }
            )
            .take(5)

        return UpsSizingReport(
            dcSystemWatts = cleanPcDc + cleanMonitor,
            acWallLoadWatts = acWallLoadWatts,
            idleAcWatts = idleAcWatts,
            safeRecommendedVa = safeRecommendedVa,
            safeRecommendedWatts = safeRecommendedWatts,
            requiresPureSineForActivePfc = requiresPureSine,
            recommendedTier = recommendedTier,
            tierRows = rows,
            matchingCatalogUps = matchedCatalog,
        )
    }
}
