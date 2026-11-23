package com.panda.myapplication.domain.engine

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.config.FeatureFlags
import com.panda.myapplication.domain.model.AccessPointSpec
import com.panda.myapplication.domain.model.CaseFanSpec
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GameCategory
import com.panda.myapplication.domain.model.GamingDeskSpec
import com.panda.myapplication.domain.model.GamingLightingSpec
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.GamingChairSpec
import com.panda.myapplication.domain.model.MicrophoneSpec
import com.panda.myapplication.domain.model.ModemSpec
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MousepadSpec
import com.panda.myapplication.domain.model.RangeExtenderSpec
import com.panda.myapplication.domain.model.RouterSpec
import com.panda.myapplication.domain.model.SmartwatchSpec
import com.panda.myapplication.domain.model.UpsSpec
import com.panda.myapplication.domain.model.WebcamSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.ResolutionFps
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.TabletSpec
import com.panda.myapplication.domain.repository.VsArenaMode
import com.panda.myapplication.domain.util.SecurityUtils
import com.panda.myapplication.presentation.util.formatPrice
import com.panda.myapplication.presentation.util.formatPriceWithCurrency
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class ComparisonWinner {
    LEFT,
    RIGHT,
    TIE,
    NEUTRAL,
}

@Immutable
data class VsComparisonRow(
    val label: String,
    val leftValue: String,
    val rightValue: String,
    val winner: ComparisonWinner = ComparisonWinner.NEUTRAL,
    val deltaBadge: String? = null,
    val isKeyMetric: Boolean = false,
)

@Immutable
data class VsGameComparisonRow(
    val gameId: String,
    val title: String,
    val shortTitle: String,
    val category: GameCategory,
    val engineTag: String,
    val leftFps: ResolutionFps,
    val rightFps: ResolutionFps,
    val leftIsHighCache: Boolean = false,
    val rightIsHighCache: Boolean = false,
) {
    fun winnerFor(resolution: GamingResolution): ComparisonWinner {
        val l = leftFps.avgFps(resolution)
        val r = rightFps.avgFps(resolution)
        return when {
            l > r -> ComparisonWinner.LEFT
            r > l -> ComparisonWinner.RIGHT
            else -> ComparisonWinner.TIE
        }
    }

    fun deltaBadgeFor(resolution: GamingResolution): String? {
        val l = leftFps.avgFps(resolution)
        val r = rightFps.avgFps(resolution)
        if (l == r || l <= 0 || r <= 0) return null
        val hi = max(l, r)
        val lo = min(l, r)
        val pct = (((hi - lo).toDouble() / lo.toDouble()) * 100.0).roundToInt()
        val diffFps = hi - lo
        return if (pct >= 1) "+%$pct (+$diffFps FPS)" else "+$diffFps FPS"
    }
}

@Immutable
data class VsChampionSummary(
    val title: String,
    val winner: ComparisonWinner,
    val winnerName: String,
    val badgeText: String,
    val subtitle: String,
    val leftMetricText: String,
    val rightMetricText: String,
)

@Immutable
data class VsComparisonReport(
    val mode: VsArenaMode,
    val category: HardwareCategory,
    val leftItem: HardwareItem,
    val rightItem: HardwareItem,
    val performanceChampion: VsChampionSummary,
    val productivityChampion: VsChampionSummary? = null,
    val valueChampion: VsChampionSummary,
    val rows: List<VsComparisonRow>,
    val gameRows: List<VsGameComparisonRow>,
)

/**
 * Head-to-Head Mathematical Comparison Engine ("VS Arena" - v2).
 *
 * Strictly adheres to the Zero-Hallucination (Honest Data) principle:
 * - Missing specs are shown as `"Belirtilmedi"` and excluded from false winner claims.
 * - Missing prices are shown as `"Stok / Fiyat Yok"`.
 * - CPUs report BOTH Gaming Champion (3D V-Cache / CCD-aware) AND Productivity/Render Champion.
 * - GPUs use uncapped TechPowerUp relative performance (RTX 4090 = 100, RTX 5090 = 133).
 * - Cross-architecture clock MHz and cross-generation raw CL cycles never award misleading badges.
 */
object VsArenaEngine {

    private const val UNSPECIFIED = "Belirtilmedi"

    fun compare(
        left: HardwareItem,
        right: HardwareItem,
        mode: VsArenaMode,
    ): VsComparisonReport {
        val leftScore = computeCompositePerformanceScore(left)
        val rightScore = computeCompositePerformanceScore(right)

        val perfChampion = buildPerformanceChampion(left, right, leftScore, rightScore)
        val prodChampion = if (left.spec is CpuSpec && right.spec is CpuSpec) {
            buildCpuProductivityChampion(left, right)
        } else null
        val valueChampion = buildValueChampion(left, right, leftScore, rightScore)

        val rows = buildList {
            // 1. Price comparison row (always first when pricing is enabled)
            if (FeatureFlags.IS_PRICING_ENABLED) {
                add(buildPriceRow(left, right))
            }

            // 2. Category-specific deep engineering & performance rows
            when {
                left.spec is CpuSpec && right.spec is CpuSpec ->
                    addAll(compareCpus(left, left.spec, right, right.spec))
                left.spec is GpuSpec && right.spec is GpuSpec ->
                    addAll(compareGpus(left, left.spec, right, right.spec))
                left.spec is MotherboardSpec && right.spec is MotherboardSpec ->
                    addAll(compareMotherboards(left.spec, right.spec))
                left.spec is RamSpec && right.spec is RamSpec ->
                    addAll(compareRams(left.spec, right.spec))
                left.spec is StorageSpec && right.spec is StorageSpec ->
                    addAll(compareStorages(left.spec, right.spec))
                left.spec is PsuSpec && right.spec is PsuSpec ->
                    addAll(comparePsus(left.spec, right.spec))
                left.spec is CoolerSpec && right.spec is CoolerSpec ->
                    addAll(compareCoolers(left.spec, right.spec))
                left.spec is CaseSpec && right.spec is CaseSpec ->
                    addAll(compareCases(left.spec, right.spec))
                left.spec is LaptopSpec && right.spec is LaptopSpec ->
                    addAll(compareLaptops(left.spec, right.spec))
                left.spec is MouseSpec && right.spec is MouseSpec ->
                    addAll(compareMice(left.spec, right.spec))
                left.spec is KeyboardSpec && right.spec is KeyboardSpec ->
                    addAll(compareKeyboards(left.spec, right.spec))
                left.spec is HeadsetSpec && right.spec is HeadsetSpec ->
                    addAll(compareHeadsets(left.spec, right.spec))
                left.spec is MonitorSpec && right.spec is MonitorSpec ->
                    addAll(compareMonitors(left.spec, right.spec))
                left.spec is MicrophoneSpec && right.spec is MicrophoneSpec ->
                    addAll(compareMicrophones(left.spec, right.spec))
                left.spec is MousepadSpec && right.spec is MousepadSpec ->
                    addAll(compareMousepads(left.spec, right.spec))
                left.spec is WebcamSpec && right.spec is WebcamSpec ->
                    addAll(compareWebcams(left.spec, right.spec))
                left.spec is GamingChairSpec && right.spec is GamingChairSpec ->
                    addAll(compareGamingChairs(left.spec, right.spec))
                left.spec is PhoneSpec && right.spec is PhoneSpec ->
                    addAll(compareSmartphones(left.spec, right.spec))
                left.spec is TabletSpec && right.spec is TabletSpec ->
                    addAll(compareTablets(left.spec, right.spec))
                left.spec is CaseFanSpec && right.spec is CaseFanSpec ->
                    addAll(compareCaseFans(left.spec, right.spec))
                left.spec is UpsSpec && right.spec is UpsSpec ->
                    addAll(compareUps(left.spec, right.spec))
                left.spec is GamingDeskSpec && right.spec is GamingDeskSpec ->
                    addAll(compareGamingDesks(left.spec, right.spec))
                left.spec is GamingLightingSpec && right.spec is GamingLightingSpec ->
                    addAll(compareGamingLighting(left.spec, right.spec))
                left.spec is SmartwatchSpec && right.spec is SmartwatchSpec ->
                    addAll(compareSmartwatches(left.spec, right.spec))
                left.spec is RouterSpec && right.spec is RouterSpec ->
                    addAll(compareRouters(left.spec, right.spec))
                left.spec is ModemSpec && right.spec is ModemSpec ->
                    addAll(compareModems(left.spec, right.spec))
                left.spec is AccessPointSpec && right.spec is AccessPointSpec ->
                    addAll(compareAccessPoints(left.spec, right.spec))
                left.spec is RangeExtenderSpec && right.spec is RangeExtenderSpec ->
                    addAll(compareRangeExtenders(left.spec, right.spec))
            }
        }

        val gameRows = buildGameComparisonRows(left, right)

        return VsComparisonReport(
            mode = mode,
            category = left.category,
            leftItem = left,
            rightItem = right,
            performanceChampion = perfChampion,
            productivityChampion = prodChampion,
            valueChampion = valueChampion,
            rows = rows,
            gameRows = gameRows,
        )
    }

    private fun buildCpuProductivityChampion(
        left: HardwareItem,
        right: HardwareItem,
    ): VsChampionSummary {
        val lProd = UnifiedFpsSimulator.resolveCpuProfile(left).productivityIndex
        val rProd = UnifiedFpsSimulator.resolveCpuProfile(right).productivityIndex
        val diff = abs(lProd - rProd)
        val base = min(lProd, rProd).coerceAtLeast(1.0)
        val pct = ((diff / base) * 100.0).roundToInt()

        val winner = when {
            pct < 2 -> ComparisonWinner.TIE
            lProd > rProd -> ComparisonWinner.LEFT
            else -> ComparisonWinner.RIGHT
        }
        val winnerName = when (winner) {
            ComparisonWinner.LEFT -> left.displayName
            ComparisonWinner.RIGHT -> right.displayName
            else -> "Baş Başa Çoklu Çekirdek (Eşit)"
        }
        val badge = when (winner) {
            ComparisonWinner.LEFT, ComparisonWinner.RIGHT -> "+%$pct Render & Çoklu Çekirdek"
            else -> "%0 Fark (Eşit)"
        }
        return VsChampionSummary(
            title = "İŞ / RENDER & ÇOKLU ÇEKİRDEK ŞAMPİYONU",
            winner = winner,
            winnerName = winnerName,
            badgeText = badge,
            subtitle = "Cinebench R23 Multi & Üretkenlik Endeksi (9950X = 100)",
            leftMetricText = "${lProd.roundToInt()} Puan",
            rightMetricText = "${rProd.roundToInt()} Puan",
        )
    }

    private fun buildPerformanceChampion(
        left: HardwareItem,
        right: HardwareItem,
        leftScore: Double,
        rightScore: Double,
    ): VsChampionSummary {
        val diff = abs(leftScore - rightScore)
        val base = min(leftScore, rightScore).coerceAtLeast(1.0)
        val pct = ((diff / base) * 100.0).roundToInt()

        val winner = when {
            pct < 1 -> ComparisonWinner.TIE
            leftScore > rightScore -> ComparisonWinner.LEFT
            else -> ComparisonWinner.RIGHT
        }

        val winnerName = when (winner) {
            ComparisonWinner.LEFT -> left.displayName
            ComparisonWinner.RIGHT -> right.displayName
            else -> "Baş Başa Performans (Eşit)"
        }

        val badge = when (winner) {
            ComparisonWinner.LEFT, ComparisonWinner.RIGHT -> when (left.category) {
                HardwareCategory.CPU -> "+%$pct Daha Yüksek Oyun Gücü"
                HardwareCategory.GPU -> "+%$pct Daha Yüksek Grafik Gücü"
                else -> "+%$pct Daha Güçlü"
            }
            else -> "%0 Fark (Eşit)"
        }

        val unitLabel = when (left.category) {
            HardwareCategory.CPU -> "1080p Oyun Endeksi (Ref: RTX 5090 • 9800X3D = 100)"
            HardwareCategory.GPU -> "4K/Raster Bağıl Endeksi (Ref: 9800X3D • RTX 4090 = 100)"
            HardwareCategory.LAPTOP -> "Mobil Oyun Performans Endeksi"
            HardwareCategory.PSU -> "Güç & Tier Endeksi"
            HardwareCategory.RAM -> "Bellek Hız & Gerçek Gecikme Endeksi"
            HardwareCategory.STORAGE -> "NVMe G/Ç Endeksi"
            HardwareCategory.MOTHERBOARD -> "Platform & Genişleme Endeksi"
            HardwareCategory.COOLER -> "Termal Kapasite Endeksi"
            HardwareCategory.CASE -> "Hava Akışı & Hacim Endeksi"
            HardwareCategory.MOUSE -> "Sensör & E-Spor Tepki Endeksi"
            HardwareCategory.KEYBOARD -> "Switch & Rapid Trigger Endeksi"
            HardwareCategory.HEADSET -> "Akustik Sürücü & Sahne Endeksi"
            HardwareCategory.MONITOR -> "Panel Hızı & Görüntü Endeksi"
            HardwareCategory.MICROPHONE -> "Stüdyo Akustik & Kayıt Endeksi"
            HardwareCategory.MOUSEPAD -> "Yüzey Kayma & Kontrol Endeksi"
            HardwareCategory.WEBCAM -> "Sensör & Yayıncı Görüntü Endeksi"
            HardwareCategory.GAMING_CHAIR -> "Ergonomi & Ortopedik Destek Endeksi"
            HardwareCategory.SMARTPHONE -> "Mobil Sentetik & Donanım Endeksi"
            HardwareCategory.TABLET -> "Tablet Üretkenlik & Multimedya Endeksi"
            HardwareCategory.CASE_FAN -> "Termal Hava Akışı & Akustik Endeksi"
            HardwareCategory.UPS -> "Güç Kapasitesi & Kesinti Koruma Endeksi"
            HardwareCategory.GAMING_DESK -> "Ergonomi & Yük Taşıma Endeksi"
            HardwareCategory.GAMING_LIGHTING -> "Işık Akısı & Ekosistem Endeksi"
            HardwareCategory.SMARTWATCH -> "Giyilebilir Teknoloji & Pil Endeksi"
            HardwareCategory.ROUTER, HardwareCategory.MODEM,
            HardwareCategory.ACCESS_POINT, HardwareCategory.RANGE_EXTENDER -> "Ağ Bant Genişliği & Kapsama Endeksi"

        }

        val championTitle = when (left.category) {
            HardwareCategory.CPU -> "OYUN PERFORMANSI ŞAMPİYONU"
            HardwareCategory.GPU -> "SAF GRAFİK & OYUN ŞAMPİYONU"
            else -> "SAF PERFORMANS KAZANANI"
        }

        val metricSuffix = if (left.category == HardwareCategory.CPU || left.category == HardwareCategory.GPU) "%" else ""
        val leftMetric = if (metricSuffix.isNotEmpty()) "%${leftScore.roundToInt()}" else "${leftScore.roundToInt()} Puan"
        val rightMetric = if (metricSuffix.isNotEmpty()) "%${rightScore.roundToInt()}" else "${rightScore.roundToInt()} Puan"

        return VsChampionSummary(
            title = championTitle,
            winner = winner,
            winnerName = winnerName,
            badgeText = badge,
            subtitle = unitLabel,
            leftMetricText = leftMetric,
            rightMetricText = rightMetric,
        )
    }

    private fun buildValueChampion(
        left: HardwareItem,
        right: HardwareItem,
        leftScore: Double,
        rightScore: Double,
    ): VsChampionSummary {
        val lPrice = SecurityUtils.sanitizeTryPrice(left.currentPriceTry)
        val rPrice = SecurityUtils.sanitizeTryPrice(right.currentPriceTry)

        if (lPrice == null || rPrice == null) {
            return VsChampionSummary(
                title = "FİYAT / PERFORMANS (F/P) ŞAMPİYONU",
                winner = ComparisonWinner.NEUTRAL,
                winnerName = "Aktif Mağaza Fiyatı Bekleniyor",
                badgeText = "Stok / Fiyat Yok",
                subtitle = "Doğrulanmış Türkiye mağaza fiyatı olmayan ürünler için sahte F/P hesabı yapılmaz.",
                leftMetricText = lPrice.formatPriceWithCurrency(),
                rightMetricText = rPrice.formatPriceWithCurrency(),
            )
        }

        // Performance points per 10.000 TL
        val leftFp = (leftScore / lPrice) * 10_000.0
        val rightFp = (rightScore / rPrice) * 10_000.0
        val minFp = min(leftFp, rightFp).coerceAtLeast(0.01)
        val pct = ((( abs(leftFp - rightFp) ) / minFp) * 100.0).roundToInt()

        val winner = when {
            pct < 1 -> ComparisonWinner.TIE
            leftFp > rightFp -> ComparisonWinner.LEFT
            else -> ComparisonWinner.RIGHT
        }

        val winnerName = when (winner) {
            ComparisonWinner.LEFT -> left.displayName
            ComparisonWinner.RIGHT -> right.displayName
            else -> "Eşit Fiyat / Performans Oranı"
        }

        val badge = when (winner) {
            ComparisonWinner.LEFT, ComparisonWinner.RIGHT -> "+%$pct Daha Yüksek F/P"
            else -> "Baş Başa F/P"
        }

        fun formatFp(v: Double): String {
            val rounded1 = (v * 10.0).roundToInt() / 10.0
            return "$rounded1 Puan / 10B ₺"
        }

        return VsChampionSummary(
            title = "FİYAT / PERFORMANS (F/P) ŞAMPİYONU",
            winner = winner,
            winnerName = winnerName,
            badgeText = badge,
            subtitle = "Harcanan her 10.000 ₺ başına alınan net performans skoru",
            leftMetricText = formatFp(leftFp),
            rightMetricText = formatFp(rightFp),
        )
    }

    private fun buildPriceRow(left: HardwareItem, right: HardwareItem): VsComparisonRow {
        val lPrice = SecurityUtils.sanitizeTryPrice(left.currentPriceTry)
        val rPrice = SecurityUtils.sanitizeTryPrice(right.currentPriceTry)

        val lText = lPrice.formatPriceWithCurrency()
        val rText = rPrice.formatPriceWithCurrency()

        if (lPrice == null || rPrice == null) {
            return VsComparisonRow(
                label = "Türkiye En Ucuz Mağaza Fiyatı",
                leftValue = lText,
                rightValue = rText,
                winner = ComparisonWinner.NEUTRAL,
                deltaBadge = null,
                isKeyMetric = true,
            )
        }

        val diff = abs(lPrice - rPrice)
        if (diff < 50.0) {
            return VsComparisonRow(
                label = "Türkiye En Ucuz Mağaza Fiyatı",
                leftValue = lText,
                rightValue = rText,
                winner = ComparisonWinner.TIE,
                deltaBadge = "Aynı Fiyat",
                isKeyMetric = true,
            )
        }

        val winner = if (lPrice < rPrice) ComparisonWinner.LEFT else ComparisonWinner.RIGHT
        val pct = ((diff / max(lPrice, rPrice)) * 100.0).roundToInt()
        return VsComparisonRow(
            label = "Türkiye En Ucuz Mağaza Fiyatı",
            leftValue = lText,
            rightValue = rText,
            winner = winner,
            deltaBadge = "-${diff.formatPrice()} ₺ Daha Ucuz (%$pct)",
            isKeyMetric = true,
        )
    }

    // region Category Comparisons

    private fun compareCpus(
        leftItem: HardwareItem,
        l: CpuSpec,
        rightItem: HardwareItem,
        r: CpuSpec,
    ): List<VsComparisonRow> = buildList {
        val lBench = BenchmarkEngine.evaluate(leftItem).info
        val rBench = BenchmarkEngine.evaluate(rightItem).info
        val lProf = UnifiedFpsSimulator.resolveCpuProfile(leftItem)
        val rProf = UnifiedFpsSimulator.resolveCpuProfile(rightItem)

        add(
            compareHigherBetter(
                label = "Oyun Performans Endeksi (1080p • Ref: RTX 5090)",
                leftVal = lProf.gamingIndex,
                rightVal = rProf.gamingIndex,
                leftText = "%${lProf.gamingIndex.roundToInt()} (${lBench.tier})",
                rightText = "%${rProf.gamingIndex.roundToInt()} (${rBench.tier})",
                badgeSuffix = "Daha Yüksek Oyun",
                isKey = true,
            )
        )

        add(
            compareHigherBetter(
                label = "Üretkenlik & Render Endeksi (Çoklu Çekirdek)",
                leftVal = lProf.productivityIndex,
                rightVal = rProf.productivityIndex,
                leftText = "%${lProf.productivityIndex.roundToInt()}",
                rightText = "%${rProf.productivityIndex.roundToInt()}",
                badgeSuffix = "Render Gücü",
                isKey = true,
            )
        )

        // Architectural CCD & 3D V-Cache row: explains why Single-CCD 96 MB 3D V-Cache beats Dual-CCD split L3!
        val lCacheScore = lProf.effectiveL3PerCcdMb.toDouble() +
            (if (lProf.isSingleCcdX3d) 40.0 else if (lProf.isX3d) 25.0 else 0.0) -
            (if (lProf.isDualCcdNonX3d) 12.0 else 0.0)
        val rCacheScore = rProf.effectiveL3PerCcdMb.toDouble() +
            (if (rProf.isSingleCcdX3d) 40.0 else if (rProf.isX3d) 25.0 else 0.0) -
            (if (rProf.isDualCcdNonX3d) 12.0 else 0.0)
        val cacheDeltaText = when {
            lProf.isX3d != rProf.isX3d -> "3D V-Cache Oyun Avantajı"
            lProf.ccdCount != rProf.ccdCount -> "Düşük Çapraz-CCD Gecikmesi"
            lProf.effectiveL3PerCcdMb != rProf.effectiveL3PerCcdMb ->
                "+${abs(lProf.effectiveL3PerCcdMb - rProf.effectiveL3PerCcdMb)} MB Yerel L3"
            else -> null
        }
        add(
            compareHigherBetter(
                label = "Önbellek & CCD Mimarisi (Oyun Gecikmesi)",
                leftVal = lCacheScore,
                rightVal = rCacheScore,
                leftText = lProf.topologySummaryTr,
                rightText = rProf.topologySummaryTr,
                customDelta = cacheDeltaText,
                isKey = true,
            )
        )

        val lSingle = lBench.effectiveSingleCore
        val rSingle = rBench.effectiveSingleCore
        if (lSingle != null || rSingle != null) {
            add(
                compareHigherBetterNullable(
                    label = "Tek Çekirdek (Cinebench R23)",
                    leftVal = lSingle?.toDouble(),
                    rightVal = rSingle?.toDouble(),
                    leftText = lSingle?.let { "$it pts" } ?: UNSPECIFIED,
                    rightText = rSingle?.let { "$it pts" } ?: UNSPECIFIED,
                    badgeSuffix = "Tek Çekirdek",
                    isKey = true,
                )
            )
        }

        val lMulti = lBench.effectiveMultiCore
        val rMulti = rBench.effectiveMultiCore
        if (lMulti != null || rMulti != null) {
            add(
                compareHigherBetterNullable(
                    label = "Çoklu Çekirdek (Cinebench R23)",
                    leftVal = lMulti?.toDouble(),
                    rightVal = rMulti?.toDouble(),
                    leftText = lMulti?.let { "$it pts" } ?: UNSPECIFIED,
                    rightText = rMulti?.let { "$it pts" } ?: UNSPECIFIED,
                    badgeSuffix = "Çoklu Güç",
                    isKey = true,
                )
            )
        }

        add(
            compareHigherBetter(
                label = "Çekirdek / İzlek Sayısı",
                leftVal = (l.coreCount * 100 + l.threadCount).toDouble(),
                rightVal = (r.coreCount * 100 + r.threadCount).toDouble(),
                leftText = "${l.coreCount} Çekirdek / ${l.threadCount} İzlek",
                rightText = "${r.coreCount} Çekirdek / ${r.threadCount} İzlek",
                customDelta = if (l.coreCount != r.coreCount) "+${abs(l.coreCount - r.coreCount)} Çekirdek" else "+${abs(l.threadCount - r.threadCount)} İzlek",
            )
        )

        val lBoost = (l.boostClock?.mhz ?: l.baseClock.mhz) / 1000.0
        val rBoost = (r.boostClock?.mhz ?: r.baseClock.mhz) / 1000.0
        val sameCpuArch = !l.architecture.isNullOrBlank() &&
            l.architecture.equals(r.architecture, ignoreCase = true) &&
            lProf.isX3d == rProf.isX3d
        if (sameCpuArch) {
            add(
                compareHigherBetter(
                    label = "Maksimum Boost Frekansı",
                    leftVal = lBoost,
                    rightVal = rBoost,
                    leftText = "$lBoost GHz (Taban: ${l.baseClock.mhz / 1000.0} GHz)",
                    rightText = "$rBoost GHz (Taban: ${r.baseClock.mhz / 1000.0} GHz)",
                    badgeSuffix = "Saat Hızı",
                )
            )
        } else {
            // Different IPC/cache architectures: clock MHz alone is NOT directly comparable
            add(
                VsComparisonRow(
                    label = "Maksimum Boost Frekansı (Farklı Mimari)",
                    leftValue = "$lBoost GHz (Taban: ${l.baseClock.mhz / 1000.0} GHz)",
                    rightValue = "$rBoost GHz (Taban: ${r.baseClock.mhz / 1000.0} GHz)",
                    winner = ComparisonWinner.NEUTRAL,
                    deltaBadge = "Farklı IPC / Önbellek",
                )
            )
        }

        add(
            compareHigherBetter(
                label = "Toplam L3 Önbellek (3D V-Cache / SmartCache)",
                leftVal = lProf.l3CacheMb.toDouble(),
                rightVal = rProf.l3CacheMb.toDouble(),
                leftText = "${lProf.l3CacheMb} MB" + if (lProf.ccdCount >= 2 && !lProf.isX3d) " (CCD başı ${lProf.effectiveL3PerCcdMb} MB)" else "",
                rightText = "${rProf.l3CacheMb} MB" + if (rProf.ccdCount >= 2 && !rProf.isX3d) " (CCD başı ${rProf.effectiveL3PerCcdMb} MB)" else "",
                customDelta = if (lProf.l3CacheMb != rProf.l3CacheMb) {
                    "+${abs(lProf.l3CacheMb - rProf.l3CacheMb)} MB L3"
                } else null,
                isKey = true,
            )
        )

        add(
            compareLowerBetter(
                label = "TDP Güç Tüketimi (Verimlilik)",
                leftVal = l.tdp.watts.toDouble(),
                rightVal = r.tdp.watts.toDouble(),
                leftText = "${l.tdp.watts}W",
                rightText = "${r.tdp.watts}W",
                customDelta = if (l.tdp.watts != r.tdp.watts) "-${abs(l.tdp.watts - r.tdp.watts)}W Daha Verimli" else null,
            )
        )

        add(
            VsComparisonRow(
                label = "Soket & Üretim Mimarisi",
                leftValue = "${l.socket} • ${l.architecture ?: UNSPECIFIED}",
                rightValue = "${r.socket} • ${r.architecture ?: UNSPECIFIED}",
            )
        )

        add(
            VsComparisonRow(
                label = "Litografi & PCIe Versiyonu",
                leftValue = "${l.processNode ?: UNSPECIFIED} • ${l.pcieVersion ?: UNSPECIFIED}",
                rightValue = "${r.processNode ?: UNSPECIFIED} • ${r.pcieVersion ?: UNSPECIFIED}",
            )
        )
    }

    private fun compareGpus(
        leftItem: HardwareItem,
        l: GpuSpec,
        rightItem: HardwareItem,
        r: GpuSpec,
    ): List<VsComparisonRow> = buildList {
        val lBench = BenchmarkEngine.evaluate(leftItem).info
        val rBench = BenchmarkEngine.evaluate(rightItem).info
        val lProf = UnifiedFpsSimulator.resolveGpuProfile(leftItem)
        val rProf = UnifiedFpsSimulator.resolveGpuProfile(rightItem)

        add(
            compareHigherBetter(
                label = "Bağıl Raster & Oyun Gücü (RTX 4090 = %100)",
                leftVal = lProf.relativeIndex,
                rightVal = rProf.relativeIndex,
                leftText = "%${lProf.relativeIndex.roundToInt()} (${lBench.tier})",
                rightText = "%${rProf.relativeIndex.roundToInt()} (${rBench.tier})",
                badgeSuffix = "Daha Hızlı",
                isKey = true,
            )
        )

        val lTs = lBench.effectiveTimeSpy
        val rTs = rBench.effectiveTimeSpy
        if (lTs != null || rTs != null) {
            add(
                compareHigherBetterNullable(
                    label = "3DMark Time Spy Grafik Skoru",
                    leftVal = lTs?.toDouble(),
                    rightVal = rTs?.toDouble(),
                    leftText = lTs?.let { "$it pts" } ?: UNSPECIFIED,
                    rightText = rTs?.let { "$it pts" } ?: UNSPECIFIED,
                    badgeSuffix = "Sentetik",
                    isKey = true,
                )
            )
        }

        val lVramGb = l.vram.megabytes / 1024
        val rVramGb = r.vram.megabytes / 1024
        add(
            compareHigherBetter(
                label = "VRAM Kapasitesi & Bellek Tipi",
                leftVal = lVramGb.toDouble(),
                rightVal = rVramGb.toDouble(),
                leftText = "$lVramGb GB ${l.memoryType}",
                rightText = "$rVramGb GB ${r.memoryType}",
                customDelta = if (lVramGb != rVramGb) "+${abs(lVramGb - rVramGb)} GB VRAM" else null,
                isKey = true,
            )
        )

        add(
            compareHigherBetterNullable(
                label = "Bellek Veri Yolu Genişliği",
                leftVal = l.busWidthBits?.toDouble(),
                rightVal = r.busWidthBits?.toDouble(),
                leftText = l.busWidthBits?.let { "$it-bit" } ?: UNSPECIFIED,
                rightText = r.busWidthBits?.let { "$it-bit" } ?: UNSPECIFIED,
                customDelta = if (l.busWidthBits != null && r.busWidthBits != null && l.busWidthBits != r.busWidthBits) {
                    "+${abs(l.busWidthBits - r.busWidthBits)}-bit"
                } else null,
            )
        )

        val lClock = l.boostClock?.mhz ?: l.coreClock.mhz
        val rClock = r.boostClock?.mhz ?: r.coreClock.mhz
        val sameChipset = l.chipset.isNotBlank() && l.chipset.equals(r.chipset, ignoreCase = true)
        if (sameChipset && lClock > 0 && rClock > 0) {
            add(
                compareHigherBetter(
                    label = "GPU Boost Saat Hızı (Aynı Çipset OC)",
                    leftVal = lClock.toDouble(),
                    rightVal = rClock.toDouble(),
                    leftText = "$lClock MHz",
                    rightText = "$rClock MHz",
                    badgeSuffix = "Fabrika OC",
                )
            )
        } else {
            // Cross-architecture MHz is NOT comparable (e.g. RX 9070 XT 3060 MHz vs RTX 5090 2600 MHz)
            add(
                VsComparisonRow(
                    label = "GPU Boost Saat Hızı (Farklı Mimari)",
                    leftValue = if (lClock > 0) "$lClock MHz" else UNSPECIFIED,
                    rightValue = if (rClock > 0) "$rClock MHz" else UNSPECIFIED,
                    winner = ComparisonWinner.NEUTRAL,
                    deltaBadge = "Farklı Mimari",
                )
            )
        }

        add(
            compareLowerBetter(
                label = "TGP / TDP Güç Tüketimi",
                leftVal = l.tdp.watts.toDouble(),
                rightVal = r.tdp.watts.toDouble(),
                leftText = "${l.tdp.watts}W (Önerilen PSU: ${l.recommendedPsu.watts}W)",
                rightText = "${r.tdp.watts}W (Önerilen PSU: ${r.recommendedPsu.watts}W)",
                customDelta = if (l.tdp.watts != r.tdp.watts) "-${abs(l.tdp.watts - r.tdp.watts)}W Daha Verimli" else null,
            )
        )

        add(
            VsComparisonRow(
                label = "Güç Konnektörü & Kart Uzunluğu",
                leftValue = "${l.powerConnectors ?: UNSPECIFIED} • ${l.lengthMm?.let { "$it mm" } ?: UNSPECIFIED}",
                rightValue = "${r.powerConnectors ?: UNSPECIFIED} • ${r.lengthMm?.let { "$it mm" } ?: UNSPECIFIED}",
            )
        )
    }

    private fun compareMotherboards(l: MotherboardSpec, r: MotherboardSpec): List<VsComparisonRow> = buildList {
        add(
            VsComparisonRow(
                label = "Soket & Yonga Seti (Chipset)",
                leftValue = "${l.socket} • ${l.chipset}",
                rightValue = "${r.socket} • ${r.chipset}",
                isKeyMetric = true,
            )
        )
        add(
            VsComparisonRow(
                label = "VRM Güç Fazı Mimarisi",
                leftValue = l.vrmPhases ?: UNSPECIFIED,
                rightValue = r.vrmPhases ?: UNSPECIFIED,
                isKeyMetric = true,
            )
        )
        add(
            compareHigherBetter(
                label = "PCIe Veri Yolu Standardı",
                leftVal = l.pcieGeneration.ordinal.toDouble(),
                rightVal = r.pcieGeneration.ordinal.toDouble(),
                leftText = l.pcieGeneration.displayName,
                rightText = r.pcieGeneration.displayName,
                customDelta = if (l.pcieGeneration != r.pcieGeneration) "Yeni Nesil PCIe" else null,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "M.2 NVMe Yuva Sayısı",
                leftVal = l.m2Slots?.toDouble(),
                rightVal = r.m2Slots?.toDouble(),
                leftText = l.m2Slots?.let { "${it}x M.2 (${l.m2SlotsDetail ?: "NVMe"})" } ?: UNSPECIFIED,
                rightText = r.m2Slots?.let { "${it}x M.2 (${r.m2SlotsDetail ?: "NVMe"})" } ?: UNSPECIFIED,
                customDelta = if (l.m2Slots != null && r.m2Slots != null && l.m2Slots != r.m2Slots) "+${abs(l.m2Slots - r.m2Slots)} M.2 Yuvası" else null,
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "RAM Yuvası & Bellek Standardı",
                leftVal = (l.ramSlots + if (l.memoryGeneration.name == "DDR5") 10 else 0).toDouble(),
                rightVal = (r.ramSlots + if (r.memoryGeneration.name == "DDR5") 10 else 0).toDouble(),
                leftText = "${l.ramSlots}x DIMM ${l.memoryGeneration.displayName}",
                rightText = "${r.ramSlots}x DIMM ${r.memoryGeneration.displayName}",
                customDelta = if (l.memoryGeneration != r.memoryGeneration) l.memoryGeneration.displayName.takeIf { l.memoryGeneration.ordinal > r.memoryGeneration.ordinal } ?: r.memoryGeneration.displayName else null,
            )
        )
        add(
            VsComparisonRow(
                label = "Ağ & Kablosuz Bağlantı",
                leftValue = l.network ?: (if (l.hasWifi == true) "Wi-Fi Mevcut" else UNSPECIFIED),
                rightValue = r.network ?: (if (r.hasWifi == true) "Wi-Fi Mevcut" else UNSPECIFIED),
            )
        )
    }

    private fun compareRams(l: RamSpec, r: RamSpec): List<VsComparisonRow> = buildList {
        val lCapGb = l.capacity.megabytes / 1024
        val rCapGb = r.capacity.megabytes / 1024
        add(
            compareHigherBetter(
                label = "Toplam Bellek Kapasitesi",
                leftVal = lCapGb.toDouble(),
                rightVal = rCapGb.toDouble(),
                leftText = "$lCapGb GB (${l.moduleCount}x${lCapGb / l.moduleCount.coerceAtLeast(1)} GB)",
                rightText = "$rCapGb GB (${r.moduleCount}x${rCapGb / r.moduleCount.coerceAtLeast(1)} GB)",
                customDelta = if (lCapGb != rCapGb) "+${abs(lCapGb - rCapGb)} GB RAM" else null,
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "Bellek Frekansı (MT/s)",
                leftVal = l.speed.mhz.toDouble(),
                rightVal = r.speed.mhz.toDouble(),
                leftText = "${l.generation.displayName} ${l.speed.mhz} MT/s",
                rightText = "${r.generation.displayName} ${r.speed.mhz} MT/s",
                badgeSuffix = "Daha Hızlı",
                isKey = true,
            )
        )
        val lNs = l.firstWordLatencyNs?.takeIf { it > 0.0 } ?: ((l.casLatency * 2000.0) / l.speed.mhz.coerceAtLeast(1))
        val rNs = r.firstWordLatencyNs?.takeIf { it > 0.0 } ?: ((r.casLatency * 2000.0) / r.speed.mhz.coerceAtLeast(1))
        val lNsRound = (lNs * 10.0).roundToInt() / 10.0
        val rNsRound = (rNs * 10.0).roundToInt() / 10.0
        val sameDdrGen = l.generation == r.generation
        if (sameDdrGen) {
            add(
                compareLowerBetter(
                    label = "CAS Gecikme Değeri (CL)",
                    leftVal = l.casLatency.toDouble(),
                    rightVal = r.casLatency.toDouble(),
                    leftText = "CL${l.casLatency} ($lNsRound ns)",
                    rightText = "CL${r.casLatency} ($rNsRound ns)",
                    customDelta = if (l.casLatency != r.casLatency) "-${abs(l.casLatency - r.casLatency)} CL Daha Düşük Gecikme" else null,
                    isKey = true,
                )
            )
        } else {
            // Cross-generation (DDR4 vs DDR5): raw CL cycles are NOT comparable — compare true nanoseconds!
            add(
                VsComparisonRow(
                    label = "CAS Gecikme Döngüsü (Farklı DDR Nesli)",
                    leftValue = "CL${l.casLatency} (${l.generation.displayName})",
                    rightValue = "CL${r.casLatency} (${r.generation.displayName})",
                    winner = ComparisonWinner.NEUTRAL,
                    deltaBadge = "Gerçek Gecikme (ns) Esas Alınır",
                )
            )
        }
        val nsDiff = abs(lNsRound - rNsRound)
        add(
            if (nsDiff < 0.15) {
                VsComparisonRow(
                    label = "Gerçek Gecikme — İlk Kelime (ns)",
                    leftValue = "$lNsRound ns",
                    rightValue = "$rNsRound ns",
                    winner = ComparisonWinner.TIE,
                    deltaBadge = "Eşit ($lNsRound ns)",
                    isKeyMetric = true,
                )
            } else {
                compareLowerBetter(
                    label = "Gerçek Gecikme — İlk Kelime (ns)",
                    leftVal = lNs,
                    rightVal = rNs,
                    leftText = "$lNsRound ns",
                    rightText = "$rNsRound ns",
                    badgeSuffix = "Daha Düşük Tepki",
                    isKey = true,
                )
            }
        )
        add(
            VsComparisonRow(
                label = "Zar (Die) Tipi & Profil",
                leftValue = "${l.dieType ?: UNSPECIFIED} • ${l.profile ?: UNSPECIFIED}",
                rightValue = "${r.dieType ?: UNSPECIFIED} • ${r.profile ?: UNSPECIFIED}",
            )
        )
    }

    private fun compareStorages(l: StorageSpec, r: StorageSpec): List<VsComparisonRow> = buildList {
        val lCapGb = l.capacity.megabytes / 1024
        val rCapGb = r.capacity.megabytes / 1024
        add(
            compareHigherBetter(
                label = "Depolama Kapasitesi",
                leftVal = lCapGb.toDouble(),
                rightVal = rCapGb.toDouble(),
                leftText = if (lCapGb >= 1000) "${lCapGb / 1000} TB ($lCapGb GB)" else "$lCapGb GB",
                rightText = if (rCapGb >= 1000) "${rCapGb / 1000} TB ($rCapGb GB)" else "$rCapGb GB",
                badgeSuffix = "Kapasite",
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "Sıralı Okuma Hızı",
                leftVal = l.sequentialReadMbps.toDouble(),
                rightVal = r.sequentialReadMbps.toDouble(),
                leftText = "${l.sequentialReadMbps} MB/s",
                rightText = "${r.sequentialReadMbps} MB/s",
                badgeSuffix = "Daha Hızlı Okuma",
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "Sıralı Yazma Hızı",
                leftVal = l.sequentialWriteMbps.toDouble(),
                rightVal = r.sequentialWriteMbps.toDouble(),
                leftText = "${l.sequentialWriteMbps} MB/s",
                rightText = "${r.sequentialWriteMbps} MB/s",
                badgeSuffix = "Daha Hızlı Yazma",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Yazma Ömrü Dayanıklılığı (TBW)",
                leftVal = l.enduranceTbw?.toDouble(),
                rightVal = r.enduranceTbw?.toDouble(),
                leftText = l.enduranceTbw?.let { "$it TBW" } ?: UNSPECIFIED,
                rightText = r.enduranceTbw?.let { "$it TBW" } ?: UNSPECIFIED,
                badgeSuffix = "Dayanıklılık",
            )
        )
        add(
            VsComparisonRow(
                label = "Kontrolcü & DRAM Önbellek",
                leftValue = "${l.controller ?: UNSPECIFIED} • ${l.dramCache ?: (l.cacheMb?.let { "$it MB" } ?: UNSPECIFIED)}",
                rightValue = "${r.controller ?: UNSPECIFIED} • ${r.dramCache ?: (r.cacheMb?.let { "$it MB" } ?: UNSPECIFIED)}",
            )
        )
    }

    private fun comparePsus(l: PsuSpec, r: PsuSpec): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetter(
                label = "Sürekli Güç Kapasitesi (Watt)",
                leftVal = l.wattage.watts.toDouble(),
                rightVal = r.wattage.watts.toDouble(),
                leftText = "${l.wattage.watts}W",
                rightText = "${r.wattage.watts}W",
                customDelta = if (l.wattage.watts != r.wattage.watts) "+${abs(l.wattage.watts - r.wattage.watts)}W Kapasite" else null,
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "80+ Verimlilik Sertifikası",
                leftVal = psuEfficiencyRank(l.efficiency).toDouble(),
                rightVal = psuEfficiencyRank(r.efficiency).toDouble(),
                leftText = l.efficiency.displayName,
                rightText = r.efficiency.displayName,
                customDelta = if (l.efficiency != r.efficiency) "Daha Yüksek Verim" else null,
                isKey = true,
            )
        )
        add(
            VsComparisonRow(
                label = "ATX 3.0 / 3.1 & 12V-2x6 Standardı",
                leftValue = l.atxStandard ?: (if (l.isAtx3Compliant) "ATX 3.0 Uyumlu" else UNSPECIFIED),
                rightValue = r.atxStandard ?: (if (r.isAtx3Compliant) "ATX 3.0 Uyumlu" else UNSPECIFIED),
                winner = when {
                    l.isAtx3Compliant && !r.isAtx3Compliant -> ComparisonWinner.LEFT
                    r.isAtx3Compliant && !l.isAtx3Compliant -> ComparisonWinner.RIGHT
                    l.isAtx3Compliant && r.isAtx3Compliant -> ComparisonWinner.TIE
                    else -> ComparisonWinner.NEUTRAL
                },
                deltaBadge = if (l.isAtx3Compliant != r.isAtx3Compliant) "ATX 3.0 / PCIe 5.0" else null,
                isKeyMetric = true,
            )
        )
        add(
            VsComparisonRow(
                label = "Cultists PSU Tier & Topoloji",
                leftValue = "${l.tierRating ?: UNSPECIFIED} • ${l.topology ?: UNSPECIFIED}",
                rightValue = "${r.tierRating ?: UNSPECIFIED} • ${r.topology ?: UNSPECIFIED}",
            )
        )
        add(
            VsComparisonRow(
                label = "Kablo Modülerliği",
                leftValue = l.modularity ?: UNSPECIFIED,
                rightValue = r.modularity ?: UNSPECIFIED,
            )
        )
    }

    private fun compareCoolers(l: CoolerSpec, r: CoolerSpec): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetter(
                label = "Maksimum Termal Soğutma Gücü (TDP)",
                leftVal = l.maxCoolingTdp.watts.toDouble(),
                rightVal = r.maxCoolingTdp.watts.toDouble(),
                leftText = "${l.maxCoolingTdp.watts}W TDP",
                rightText = "${r.maxCoolingTdp.watts}W TDP",
                customDelta = if (l.maxCoolingTdp.watts != r.maxCoolingTdp.watts) "+${abs(l.maxCoolingTdp.watts - r.maxCoolingTdp.watts)}W Termal Güç" else null,
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Radyatör / Soğutucu Boyutu",
                leftVal = (l.radiatorSizeMm ?: l.heightMm)?.toDouble(),
                rightVal = (r.radiatorSizeMm ?: r.heightMm)?.toDouble(),
                leftText = if (l.type == CoolerType.LIQUID) "Sıvı (${l.radiatorSizeMm?.let { "$it mm" } ?: UNSPECIFIED})" else "Hava (${l.heightMm?.let { "$it mm" } ?: UNSPECIFIED})",
                rightText = if (r.type == CoolerType.LIQUID) "Sıvı (${r.radiatorSizeMm?.let { "$it mm" } ?: UNSPECIFIED})" else "Hava (${r.heightMm?.let { "$it mm" } ?: UNSPECIFIED})",
                isKey = true,
            )
        )
        add(
            compareLowerBetterNullable(
                label = "Gürültü Seviyesi (Akustik dBA)",
                leftVal = l.noiseDba,
                rightVal = r.noiseDba,
                leftText = l.noiseDba?.let { "$it dBA" } ?: UNSPECIFIED,
                rightText = r.noiseDba?.let { "$it dBA" } ?: UNSPECIFIED,
                badgeSuffix = "Daha Sessiz",
            )
        )
    }

    private fun compareCases(l: CaseSpec, r: CaseSpec): List<VsComparisonRow> = buildList {
        add(
            VsComparisonRow(
                label = "Kasa Form Faktörü & Yan Panel",
                leftValue = "${l.type ?: UNSPECIFIED} • ${l.sidePanel ?: UNSPECIFIED}",
                rightValue = "${r.type ?: UNSPECIFIED} • ${r.sidePanel ?: UNSPECIFIED}",
                isKeyMetric = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Maksimum Ekran Kartı (GPU) Uzunluğu",
                leftVal = l.maxGpuLengthMm?.toDouble(),
                rightVal = r.maxGpuLengthMm?.toDouble(),
                leftText = l.maxGpuLengthMm?.let { "$it mm" } ?: UNSPECIFIED,
                rightText = r.maxGpuLengthMm?.let { "$it mm" } ?: UNSPECIFIED,
                customDelta = if (l.maxGpuLengthMm != null && r.maxGpuLengthMm != null && l.maxGpuLengthMm != r.maxGpuLengthMm) "+${abs(l.maxGpuLengthMm - r.maxGpuLengthMm)} mm Boşluk" else null,
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Maksimum Sıvı Soğutma Radyatör Desteği",
                leftVal = l.maxRadiatorSizeMm?.toDouble(),
                rightVal = r.maxRadiatorSizeMm?.toDouble(),
                leftText = l.maxRadiatorSizeMm?.let { "$it mm" } ?: UNSPECIFIED,
                rightText = r.maxRadiatorSizeMm?.let { "$it mm" } ?: UNSPECIFIED,
                customDelta = if (l.maxRadiatorSizeMm != null && r.maxRadiatorSizeMm != null && l.maxRadiatorSizeMm != r.maxRadiatorSizeMm) "+${abs(l.maxRadiatorSizeMm - r.maxRadiatorSizeMm)} mm Radyatör" else null,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "CPU Hava Soğutucu Yükseklik Limiti",
                leftVal = l.maxCpuCoolerHeightMm?.toDouble(),
                rightVal = r.maxCpuCoolerHeightMm?.toDouble(),
                leftText = l.maxCpuCoolerHeightMm?.let { "$it mm" } ?: UNSPECIFIED,
                rightText = r.maxCpuCoolerHeightMm?.let { "$it mm" } ?: UNSPECIFIED,
            )
        )
        add(
            VsComparisonRow(
                label = "Dahili Fanlar & Ön Panel Portları",
                leftValue = "${l.includedFans ?: UNSPECIFIED} • ${l.frontPanelUsb ?: UNSPECIFIED}",
                rightValue = "${r.includedFans ?: UNSPECIFIED} • ${r.frontPanelUsb ?: UNSPECIFIED}",
            )
        )
    }

    private fun compareLaptops(
        l: LaptopSpec,
        r: LaptopSpec,
    ): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetter(
                label = "Oyun & Mobil Performans Skoru",
                leftVal = l.gamingScore.toDouble(),
                rightVal = r.gamingScore.toDouble(),
                leftText = "${l.gamingScore}/100 Puan",
                rightText = "${r.gamingScore}/100 Puan",
                badgeSuffix = "Daha Güçlü",
                isKey = true,
            )
        )

        val lTgp = extractFirstInt(l.gpuWattTgp)
        val rTgp = extractFirstInt(r.gpuWattTgp)
        add(
            compareHigherBetterNullable(
                label = "Ekran Kartı TGP Güç Limiti (Watt)",
                leftVal = lTgp?.toDouble(),
                rightVal = rTgp?.toDouble(),
                leftText = l.gpuWattTgp.ifBlank { UNSPECIFIED },
                rightText = r.gpuWattTgp.ifBlank { UNSPECIFIED },
                customDelta = if (lTgp != null && rTgp != null && lTgp != rTgp) "+${abs(lTgp - rTgp)}W Daha Yüksek TGP" else null,
                isKey = true,
            )
        )

        val lHasMux = l.muxSwitch.contains("Var", ignoreCase = true) || l.muxSwitch.contains("Advanced Optimus", ignoreCase = true)
        val rHasMux = r.muxSwitch.contains("Var", ignoreCase = true) || r.muxSwitch.contains("Advanced Optimus", ignoreCase = true)
        add(
            VsComparisonRow(
                label = "MUX Switch & Advanced Optimus",
                leftValue = l.muxSwitch.ifBlank { UNSPECIFIED },
                rightValue = r.muxSwitch.ifBlank { UNSPECIFIED },
                winner = when {
                    lHasMux && !rHasMux -> ComparisonWinner.LEFT
                    rHasMux && !lHasMux -> ComparisonWinner.RIGHT
                    lHasMux && rHasMux -> ComparisonWinner.TIE
                    else -> ComparisonWinner.NEUTRAL
                },
                deltaBadge = if (lHasMux != rHasMux) "MUX Avantajı" else null,
                isKeyMetric = true,
            )
        )

        val lHz = extractRefreshRateHz(l.displaySpec)
        val rHz = extractRefreshRateHz(r.displaySpec)
        add(
            compareHigherBetterNullable(
                label = "Ekran Paneli & Yenileme Hızı (Hz)",
                leftVal = lHz?.toDouble(),
                rightVal = rHz?.toDouble(),
                leftText = l.displaySpec.ifBlank { UNSPECIFIED },
                rightText = r.displaySpec.ifBlank { UNSPECIFIED },
                customDelta = if (lHz != null && rHz != null && lHz != rHz) "+${abs(lHz - rHz)} Hz Panel" else null,
                isKey = true,
            )
        )

        val lFpsSum = (l.fps1080p.takeIf { it > 0 }?.let { it + l.fps1440p.coerceAtLeast(0) })?.toDouble()
        val rFpsSum = (r.fps1080p.takeIf { it > 0 }?.let { it + r.fps1440p.coerceAtLeast(0) })?.toDouble()
        add(
            compareHigherBetterNullable(
                label = "1080p & 1440p Ortalama Oyun FPS",
                leftVal = lFpsSum,
                rightVal = rFpsSum,
                leftText = if (l.fps1080p > 0) "1080p: ${l.fps1080p} FPS • 2K: ${l.fps1440p} FPS" else UNSPECIFIED,
                rightText = if (r.fps1080p > 0) "1080p: ${r.fps1080p} FPS • 2K: ${r.fps1440p} FPS" else UNSPECIFIED,
                badgeSuffix = "Daha Yüksek FPS",
                isKey = true,
            )
        )

        add(
            VsComparisonRow(
                label = "Mobil GPU & CPU Donanımı",
                leftValue = "${l.gpuModel.ifBlank { UNSPECIFIED }} • ${l.cpuModel.ifBlank { UNSPECIFIED }}",
                rightValue = "${r.gpuModel.ifBlank { UNSPECIFIED }} • ${r.cpuModel.ifBlank { UNSPECIFIED }}",
            )
        )

        add(
            VsComparisonRow(
                label = "Bellek (RAM) & SSD Depolama",
                leftValue = "${l.ramSpec.ifBlank { UNSPECIFIED }} • ${l.storageSpec.ifBlank { UNSPECIFIED }}",
                rightValue = "${r.ramSpec.ifBlank { UNSPECIFIED }} • ${r.storageSpec.ifBlank { UNSPECIFIED }}",
            )
        )

        add(
            compareHigherBetterNullable(
                label = "Batarya Kapasitesi (Wh)",
                leftVal = l.batteryWh.takeIf { it > 0 }?.toDouble(),
                rightVal = r.batteryWh.takeIf { it > 0 }?.toDouble(),
                leftText = if (l.batteryWh > 0) "${l.batteryWh} Wh" else UNSPECIFIED,
                rightText = if (r.batteryWh > 0) "${r.batteryWh} Wh" else UNSPECIFIED,
                customDelta = if (l.batteryWh > 0 && r.batteryWh > 0 && l.batteryWh != r.batteryWh) "+${abs(l.batteryWh - r.batteryWh)} Wh Batarya" else null,
            )
        )

        add(
            compareLowerBetterNullable(
                label = "Taşınabilirlik & Ağırlık (kg)",
                leftVal = l.weightKg.takeIf { it > 0.0 },
                rightVal = r.weightKg.takeIf { it > 0.0 },
                leftText = if (l.weightKg > 0.0) "${l.weightKg} kg" else UNSPECIFIED,
                rightText = if (r.weightKg > 0.0) "${r.weightKg} kg" else UNSPECIFIED,
                customDelta = if (l.weightKg > 0.0 && r.weightKg > 0.0 && abs(l.weightKg - r.weightKg) >= 0.05) {
                    val diffKg = ((abs(l.weightKg - r.weightKg) * 100.0).roundToInt() / 100.0)
                    "-$diffKg kg Daha Hafif"
                } else null,
            )
        )
    }

    // endregion

    // region Multi-Game Comparison Builder

    private fun buildGameComparisonRows(
        left: HardwareItem,
        right: HardwareItem,
    ): List<VsGameComparisonRow> {
        val leftGames = BenchmarkEngine.resolveGameFpsForItem(left)?.toSimulationEntries().orEmpty()
        val rightGames = BenchmarkEngine.resolveGameFpsForItem(right)?.toSimulationEntries().orEmpty().associateBy { it.id }
        if (leftGames.isEmpty() || rightGames.isEmpty()) return emptyList()

        val leftHighCache = (left.spec as? CpuSpec)?.let {
            UnifiedFpsSimulator.resolveCpuProfile(left).isX3d
        } ?: left.model.contains("X3D", ignoreCase = true)
        val rightHighCache = (right.spec as? CpuSpec)?.let {
            UnifiedFpsSimulator.resolveCpuProfile(right).isX3d
        } ?: right.model.contains("X3D", ignoreCase = true)

        return leftGames.mapNotNull { lEntry ->
            val rEntry = rightGames[lEntry.id] ?: return@mapNotNull null
            VsGameComparisonRow(
                gameId = lEntry.id,
                title = lEntry.title,
                shortTitle = lEntry.shortTitle,
                category = lEntry.category,
                engineTag = lEntry.engineTag,
                leftFps = lEntry.fps,
                rightFps = rEntry.fps,
                leftIsHighCache = leftHighCache,
                rightIsHighCache = rightHighCache,
            )
        }
    }

    // endregion

    // region Composite Performance Scoring

    fun computeCompositePerformanceScore(item: HardwareItem): Double {
        return when (val s = item.spec) {
            is CpuSpec -> {
                UnifiedFpsSimulator.resolveCpuProfile(item).gamingIndex.coerceAtLeast(10.0)
            }
            is GpuSpec -> {
                UnifiedFpsSimulator.resolveGpuProfile(item).relativeIndex.coerceAtLeast(8.0)
            }
            is LaptopSpec -> {
                val tgpBonus = (extractFirstInt(s.gpuWattTgp) ?: 100) * 0.08
                val muxBonus = if (s.muxSwitch.contains("Var", ignoreCase = true) || s.muxSwitch.contains("Optimus", ignoreCase = true)) 4.0 else 0.0
                if (s.gamingScore > 0) (s.gamingScore + tgpBonus * 0.3 + muxBonus).coerceIn(25.0, 110.0)
                else ((s.fps1080p * 0.5) + tgpBonus + muxBonus).coerceIn(25.0, 100.0)
            }
            is RamSpec -> {
                val capGb = s.capacity.megabytes / 1024.0
                val speedScore = s.speed.mhz / 100.0
                val trueNs = s.firstWordLatencyNs?.takeIf { it > 0.0 }
                    ?: ((s.casLatency * 2000.0) / s.speed.mhz.coerceAtLeast(1))
                val latencyPenalty = trueNs * 1.4
                (speedScore + capGb * 0.7 - latencyPenalty).coerceIn(20.0, 140.0)
            }
            is StorageSpec -> {
                val readScore = s.sequentialReadMbps / 100.0
                val writeScore = s.sequentialWriteMbps / 120.0
                val capScore = (s.capacity.megabytes / 1024.0) * 0.01
                val nvmeBonus = if (s.storageInterface == StorageInterface.NVME) 15.0 else 0.0
                (readScore * 0.55 + writeScore * 0.35 + capScore + nvmeBonus).coerceIn(20.0, 130.0)
            }
            is PsuSpec -> {
                val wattScore = s.wattage.watts / 15.0
                val effBonus = psuEfficiencyRank(s.efficiency) * 6.0
                val atxBonus = if (s.isAtx3Compliant) 12.0 else 0.0
                (wattScore + effBonus + atxBonus).coerceIn(25.0, 130.0)
            }
            is MotherboardSpec -> {
                val pcieScore = (s.pcieGeneration.ordinal + 1) * 14.0
                val m2Score = (s.m2Slots ?: 2) * 8.0
                val ddrBonus = if (s.memoryGeneration.name == "DDR5") 18.0 else 8.0
                val wifiBonus = if (s.hasWifi == true) 6.0 else 0.0
                (pcieScore + m2Score + ddrBonus + wifiBonus).coerceIn(30.0, 120.0)
            }
            is CoolerSpec -> {
                val tdpScore = s.maxCoolingTdp.watts / 3.5
                val radBonus = (s.radiatorSizeMm ?: 0) / 12.0
                (tdpScore + radBonus).coerceIn(25.0, 125.0)
            }
            is CaseSpec -> {
                val gpuScore = (s.maxGpuLengthMm ?: 340) / 6.0
                val radScore = (s.maxRadiatorSizeMm ?: 240) / 10.0
                (gpuScore + radScore).coerceIn(30.0, 110.0)
            }
            is MouseSpec -> {
                val itemBench = item.benchmarkScore?.toDouble()
                val pollingBonus = if (s.pollingRateHz > 0) (s.pollingRateHz / 1000.0) * 1.5 else 1.5
                val weightBonus = if (s.weightG > 0) ((100 - s.weightG).coerceAtLeast(0)) * 0.45 else 12.0
                val dpiScore = if (s.maxDpi > 0) (s.maxDpi / 1000.0).coerceAtMost(45.0) else 16.0
                itemBench ?: (40.0 + pollingBonus + weightBonus + dpiScore * 0.5).coerceIn(40.0, 100.0)
            }
            is KeyboardSpec -> {
                val itemBench = item.benchmarkScore?.toDouble()
                val rtBonus = if (s.rapidTriggerSupported) 25.0 else 0.0
                val actBonus = ((2.5 - s.minActuationMm).coerceAtLeast(0.1)) * 12.0
                val pollBonus = (s.pollingRateHz / 1000.0) * 1.5
                itemBench ?: (45.0 + rtBonus + actBonus + pollBonus).coerceIn(40.0, 100.0)
            }
            is HeadsetSpec -> {
                val itemBench = item.benchmarkScore?.toDouble()
                val driverBonus = (s.driverSizeMm - 35).coerceAtLeast(0) * 0.6
                val planarBonus = if (s.driverType.contains("Planar", ignoreCase = true)) 15.0 else 0.0
                itemBench ?: (65.0 + driverBonus + planarBonus).coerceIn(45.0, 100.0)
            }
            is MonitorSpec -> {
                val itemBench = item.benchmarkScore?.toDouble()
                val hzScore = (s.refreshRateHz / 10.0).coerceAtMost(54.0)
                val oledBonus = if (s.panelType.contains("OLED", ignoreCase = true)) 22.0 else 0.0
                val ppiBonus = (s.ppi - 80).coerceAtLeast(0) * 0.35
                itemBench ?: (35.0 + hzScore + oledBonus + ppiBonus).coerceIn(45.0, 105.0)
            }
            is MicrophoneSpec -> {
                val itemBench = item.benchmarkScore?.toDouble()
                val xlrBonus = if (s.connectivity.contains("XLR", ignoreCase = true)) 12.0 else 0.0
                val dspBonus = if (s.hasBuiltInDsp) 8.0 else 0.0
                val splBonus = (s.maxSplDb - 110).coerceAtLeast(0) * 0.5
                itemBench ?: (68.0 + xlrBonus + dspBonus + splBonus).coerceIn(50.0, 100.0)
            }
            is MousepadSpec -> {
                val itemBench = item.benchmarkScore?.toDouble()
                val poronBonus = if (s.baseMaterial.contains("Poron", ignoreCase = true) || s.surfaceType.contains("Cam", ignoreCase = true)) 15.0 else 5.0
                val stitchBonus = if (s.stitchedEdges) 5.0 else 0.0
                itemBench ?: (70.0 + poronBonus + stitchBonus + s.stoppingPowerScore).coerceIn(50.0, 100.0)
            }
            is WebcamSpec -> {
                val itemBench = item.benchmarkScore?.toDouble()
                val resBonus = if (s.maxResolution.contains("4K", ignoreCase = true)) 20.0 else 8.0
                val fpsBonus = if (s.maxFrameRateFps >= 60) 12.0 else 0.0
                val aiBonus = if (s.hasAiTracking) 8.0 else 0.0
                itemBench ?: (60.0 + resBonus + fpsBonus + aiBonus).coerceIn(50.0, 100.0)
            }
            is GamingChairSpec -> {
                val itemBench = item.benchmarkScore?.toDouble()
                val capBonus = (s.maxWeightCapacityKg - 110).coerceAtLeast(0) * 0.25
                val lumbarBonus = if (s.lumbarSupportType.contains("4 Yönlü", ignoreCase = true) || s.lumbarSupportType.contains("4D", ignoreCase = true) || s.lumbarSupportType.contains("Mekanik", ignoreCase = true)) 14.0 else 6.0
                itemBench ?: (68.0 + capBonus + lumbarBonus).coerceIn(50.0, 100.0)
            }
            is PhoneSpec -> {
                val antutu = s.antutuScore?.toDouble() ?: 1500000.0
                (antutu / 25000.0).coerceIn(30.0, 115.0)
            }
            is TabletSpec -> {
                val antutu = s.antutuScore?.toDouble() ?: 1800000.0
                (antutu / 25000.0).coerceIn(30.0, 120.0)
            }
            is CaseFanSpec -> {
                val airflowScore = (s.maxAirflowCfm * 0.55).coerceAtMost(55.0)
                val pressureScore = (s.staticPressureMmH2o * 6.0).coerceAtMost(24.0)
                val quietBonus = if (s.noiseLevelDba > 0.0) ((40.0 - s.noiseLevelDba).coerceAtLeast(0.0) * 0.7) else 8.0
                val daisyBonus = if (s.isDaisyChain) 5.0 else 0.0
                item.benchmarkScore?.toDouble() ?: (25.0 + airflowScore + pressureScore + quietBonus + daisyBonus).coerceIn(35.0, 105.0)
            }
            is UpsSpec -> {
                val wattScore = (s.wattCapacity / 22.0).coerceAtMost(55.0)
                val vaScore = (s.vaRating / 50.0).coerceAtMost(35.0)
                val sineBonus = if (s.sinewaveType.contains("Sinüs", ignoreCase = true) || s.sinewaveType.contains("Pure", ignoreCase = true)) 14.0 else 4.0
                val onlineBonus = if (s.topology.contains("Online", ignoreCase = true)) 12.0 else 4.0
                item.benchmarkScore?.toDouble() ?: (20.0 + wattScore + vaScore * 0.35 + sineBonus + onlineBonus).coerceIn(35.0, 115.0)
            }
            is GamingDeskSpec -> {
                val widthScore = (s.deskWidthCm / 4.0).coerceAtMost(45.0)
                val loadScore = (s.maxLoadKg * 0.25).coerceAtMost(30.0)
                val motorBonus = if (s.isMotorizedHeight) 18.0 else 4.0
                val cableBonus = if (s.hasCableManagement) 6.0 else 0.0
                item.benchmarkScore?.toDouble() ?: (20.0 + widthScore + loadScore + motorBonus + cableBonus).coerceIn(35.0, 105.0)
            }
            is GamingLightingSpec -> {
                val lumenScore = (s.maxLumens / 25.0).coerceAtMost(45.0)
                val criScore = if (s.criScore > 0) ((s.criScore - 70).coerceAtLeast(0) * 1.1) else 15.0
                val mirrorBonus = if (s.hasScreenMirroring) 12.0 else 0.0
                item.benchmarkScore?.toDouble() ?: (30.0 + lumenScore + criScore + mirrorBonus).coerceIn(35.0, 105.0)
            }
            is SmartwatchSpec -> {
                val battScore = (s.batteryLifeDays * 2.4).coerceAtMost(35.0)
                val brightScore = (s.peakBrightnessNits / 80.0).coerceAtMost(30.0)
                val waterScore = (s.waterResistanceAtm * 1.5).coerceAtMost(15.0)
                val lteBonus = if (s.hasCellularLte) 8.0 else 0.0
                item.benchmarkScore?.toDouble() ?: (35.0 + battScore + brightScore + waterScore + lteBonus).coerceIn(40.0, 105.0)
            }
            is RouterSpec -> {
                val wifiScore = wifiStandardRank(s.wifiStandard) * 14.0
                val speedScore = (extractSpeedMbps(s.speedClass) / 300.0).coerceAtMost(35.0)
                val qosBonus = (if (s.hasGamingQos) 6.0 else 0.0) + (if (s.hasSqmBufferbloat) 6.0 else 0.0)
                item.benchmarkScore?.toDouble() ?: (25.0 + wifiScore + speedScore + qosBonus).coerceIn(35.0, 115.0)
            }
            is ModemSpec -> {
                val wifiScore = wifiStandardRank(s.wifiStandard) * 14.0
                val fiberBonus = if (s.fiberWanSupport) 14.0 else 0.0
                val vdslBonus = if (s.vdslProfile.contains("35b", ignoreCase = true)) 12.0 else 6.0
                item.benchmarkScore?.toDouble() ?: (35.0 + wifiScore + fiberBonus + vdslBonus).coerceIn(35.0, 105.0)
            }
            is AccessPointSpec -> {
                val wifiScore = wifiStandardRank(s.wifiStandard) * 14.0
                val clientScore = (s.maxClients / 15.0).coerceAtMost(30.0)
                val poeBonus = if (s.poeSupport.isNotBlank()) 8.0 else 0.0
                item.benchmarkScore?.toDouble() ?: (28.0 + wifiScore + clientScore + poeBonus).coerceIn(35.0, 110.0)
            }
            is RangeExtenderSpec -> {
                val wifiScore = wifiStandardRank(s.wifiStandard) * 14.0
                val covScore = (s.coverageAreaM2 / 8.0).coerceAtMost(32.0)
                val meshBonus = if (s.meshProtocol.isNotBlank()) 8.0 else 0.0
                item.benchmarkScore?.toDouble() ?: (28.0 + wifiScore + covScore + meshBonus).coerceIn(35.0, 105.0)
            }
        }
    }

    private fun formatPollingRate(hz: Int): String {
        if (hz <= 0) return UNSPECIFIED
        val msRounded = ((1000.0 / hz) * 1000.0).roundToInt() / 1000.0
        return "$hz Hz ($msRounded ms)"
    }

    private fun wifiStandardRank(std: String): Int = when {
        std.contains("Wi-Fi 7", ignoreCase = true) || std.contains("802.11be", ignoreCase = true) -> 5
        std.contains("Wi-Fi 6E", ignoreCase = true) -> 4
        std.contains("Wi-Fi 6", ignoreCase = true) || std.contains("802.11ax", ignoreCase = true) -> 3
        std.contains("Wi-Fi 5", ignoreCase = true) || std.contains("802.11ac", ignoreCase = true) -> 2
        std.isNotBlank() -> 1
        else -> 0
    }

    private fun extractSpeedMbps(speedClass: String): Int =
        Regex("""(\d{3,5})""").find(speedClass)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0

    private fun compareMice(l: MouseSpec, r: MouseSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Optik Sensör", l.sensor.ifBlank { UNSPECIFIED }, r.sensor.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Polling Rate (Raporlama Hızı)",
                leftVal = l.pollingRateHz.takeIf { it > 0 }?.toDouble(),
                rightVal = r.pollingRateHz.takeIf { it > 0 }?.toDouble(),
                leftText = formatPollingRate(l.pollingRateHz),
                rightText = formatPollingRate(r.pollingRateHz),
                badgeSuffix = "Daha Hızlı Tepki",
                isKey = true,
            )
        )
        val lWeight = l.weightG.takeIf { it > 0 }
        val rWeight = r.weightG.takeIf { it > 0 }
        add(
            compareLowerBetterNullable(
                label = "Ağırlık (Ultra-Light)",
                leftVal = lWeight?.toDouble(),
                rightVal = rWeight?.toDouble(),
                leftText = lWeight?.let { "$it g" } ?: UNSPECIFIED,
                rightText = rWeight?.let { "$it g" } ?: UNSPECIFIED,
                customDelta = if (lWeight != null && rWeight != null && lWeight != rWeight) "-${abs(lWeight - rWeight)}g Daha Hafif" else null,
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Maksimum DPI Hassasiyeti",
                leftVal = l.maxDpi.takeIf { it > 0 }?.toDouble(),
                rightVal = r.maxDpi.takeIf { it > 0 }?.toDouble(),
                leftText = l.maxDpi.takeIf { it > 0 }?.let { "$it DPI" } ?: UNSPECIFIED,
                rightText = r.maxDpi.takeIf { it > 0 }?.let { "$it DPI" } ?: UNSPECIFIED,
                badgeSuffix = "DPI",
            )
        )
        add(VsComparisonRow("Switch Teknolojisi", l.switchType.ifBlank { UNSPECIFIED }, r.switchType.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("Bağlantı Modu", l.connectivity.ifBlank { UNSPECIFIED }, r.connectivity.ifBlank { UNSPECIFIED }))
        add(
            compareHigherBetterNullable(
                label = "Batarya Ömrü",
                leftVal = l.batteryHours?.takeIf { it > 0 }?.toDouble(),
                rightVal = r.batteryHours?.takeIf { it > 0 }?.toDouble(),
                leftText = l.batteryHours?.takeIf { it > 0 }?.let { "$it Saat" } ?: "Kablolu",
                rightText = r.batteryHours?.takeIf { it > 0 }?.let { "$it Saat" } ?: "Kablolu",
                customDelta = if (l.batteryHours != null && r.batteryHours != null && l.batteryHours > 0 && r.batteryHours > 0 && l.batteryHours != r.batteryHours) {
                    "+${abs(l.batteryHours - r.batteryHours)} Saat"
                } else null,
            )
        )
    }

    private fun compareKeyboards(l: KeyboardSpec, r: KeyboardSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Switch Mimarisi", l.switchType.ifBlank { UNSPECIFIED }, r.switchType.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetter(
                label = "Rapid Trigger (Dinamik Sıfırlama)",
                leftVal = if (l.rapidTriggerSupported) 1.0 else 0.0,
                rightVal = if (r.rapidTriggerSupported) 1.0 else 0.0,
                leftText = if (l.rapidTriggerSupported) "Destekliyor (Hall Effect)" else "Yok (Sabit Eşik)",
                rightText = if (r.rapidTriggerSupported) "Destekliyor (Hall Effect)" else "Yok (Sabit Eşik)",
                customDelta = "Rapid Trigger Avantajı",
                isKey = true,
            )
        )
        add(
            compareLowerBetterNullable(
                label = "Minimum Tetikleme Mesafesi",
                leftVal = l.minActuationMm.takeIf { it > 0.0 },
                rightVal = r.minActuationMm.takeIf { it > 0.0 },
                leftText = l.minActuationMm.takeIf { it > 0.0 }?.let { "$it mm" } ?: UNSPECIFIED,
                rightText = r.minActuationMm.takeIf { it > 0.0 }?.let { "$it mm" } ?: UNSPECIFIED,
                badgeSuffix = "Daha Çabuk Tetikleme",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Polling Rate (Tepki Frekansı)",
                leftVal = l.pollingRateHz.takeIf { it > 0 }?.toDouble(),
                rightVal = r.pollingRateHz.takeIf { it > 0 }?.toDouble(),
                leftText = formatPollingRate(l.pollingRateHz),
                rightText = formatPollingRate(r.pollingRateHz),
                badgeSuffix = "Hz",
            )
        )
        add(VsComparisonRow("Tuş Dizilimi (Layout)", l.layout.ifBlank { UNSPECIFIED }, r.layout.ifBlank { UNSPECIFIED }))
        add(
            VsComparisonRow(
                label = "Hot-Swap (Lehimsiz Switch Değişimi)",
                leftValue = if (l.hotSwap) "Var" else "Yok",
                rightValue = if (r.hotSwap) "Var" else "Yok",
            )
        )
        add(VsComparisonRow("Bağlantı", l.connectivity.ifBlank { UNSPECIFIED }, r.connectivity.ifBlank { UNSPECIFIED }))
    }

    private fun compareHeadsets(l: HeadsetSpec, r: HeadsetSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Sürücü Teknolojisi", l.driverType.ifBlank { UNSPECIFIED }, r.driverType.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        val lDrv = l.driverSizeMm.takeIf { it > 0 }
        val rDrv = r.driverSizeMm.takeIf { it > 0 }
        add(
            compareHigherBetterNullable(
                label = "Sürücü Çapı (mm)",
                leftVal = lDrv?.toDouble(),
                rightVal = rDrv?.toDouble(),
                leftText = lDrv?.let { "$it mm" } ?: UNSPECIFIED,
                rightText = rDrv?.let { "$it mm" } ?: UNSPECIFIED,
                customDelta = if (lDrv != null && rDrv != null && lDrv != rDrv) "+${abs(lDrv - rDrv)} mm Sürücü" else null,
                isKey = true,
            )
        )
        add(
            VsComparisonRow(
                label = "Empedans & DAC/Amp Gereksinimi",
                leftValue = if (l.impedanceOhm > 0) "${l.impedanceOhm} Ω (${if (l.dacAmpRequired) "DAC/Amp Önerilir" else "Doğrudan Sürülebilir"})" else UNSPECIFIED,
                rightValue = if (r.impedanceOhm > 0) "${r.impedanceOhm} Ω (${if (r.dacAmpRequired) "DAC/Amp Önerilir" else "Doğrudan Sürülebilir"})" else UNSPECIFIED,
                isKeyMetric = true,
            )
        )
        add(VsComparisonRow("Frekans Tepkisi", l.frequencyResponse.ifBlank { UNSPECIFIED }, r.frequencyResponse.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("Mikrofon Tipi", l.micType.ifBlank { UNSPECIFIED }, r.micType.ifBlank { UNSPECIFIED }))
        val lW = l.weightG.takeIf { it > 0 }
        val rW = r.weightG.takeIf { it > 0 }
        add(
            compareLowerBetterNullable(
                label = "Ağırlık (Konfor)",
                leftVal = lW?.toDouble(),
                rightVal = rW?.toDouble(),
                leftText = lW?.let { "$it g" } ?: UNSPECIFIED,
                rightText = rW?.let { "$it g" } ?: UNSPECIFIED,
                customDelta = if (lW != null && rW != null && lW != rW) "-${abs(lW - rW)}g Daha Hafif" else null,
            )
        )
    }

    private fun compareMonitors(l: MonitorSpec, r: MonitorSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Panel Teknolojisi", l.panelType, r.panelType, isKeyMetric = true))
        add(
            compareHigherBetter(
                label = "Yenileme Hızı (Refresh Rate)",
                leftVal = l.refreshRateHz.toDouble(),
                rightVal = r.refreshRateHz.toDouble(),
                leftText = "${l.refreshRateHz} Hz",
                rightText = "${r.refreshRateHz} Hz",
                customDelta = if (l.refreshRateHz != r.refreshRateHz) "+${abs(l.refreshRateHz - r.refreshRateHz)} Hz Daha Akıcı" else null,
                isKey = true,
            )
        )
        add(
            compareLowerBetter(
                label = "Griden Griye Tepki Süresi (GtG)",
                leftVal = l.responseSpeedGtGMs,
                rightVal = r.responseSpeedGtGMs,
                leftText = "${l.responseSpeedGtGMs} ms GtG",
                rightText = "${r.responseSpeedGtGMs} ms GtG",
                badgeSuffix = "Daha Net Hareket",
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "Piksel Yoğunluğu (PPI) & Çözünürlük",
                leftVal = l.ppi.toDouble(),
                rightVal = r.ppi.toDouble(),
                leftText = "${l.resolution} • ${l.ppi} PPI (${l.screenSizeInch}\")",
                rightText = "${r.resolution} • ${r.ppi} PPI (${r.screenSizeInch}\")",
                customDelta = if (l.ppi != r.ppi) "+${abs(l.ppi - r.ppi)} PPI Daha Keskin" else null,
                isKey = true,
            )
        )
        add(VsComparisonRow("HDR & Parlaklık Sertifikası", l.hdrCertification, r.hdrCertification))
    }

    private fun compareMicrophones(l: MicrophoneSpec, r: MicrophoneSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Kapsül Mimarisi", l.capsuleType.ifBlank { UNSPECIFIED }, r.capsuleType.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(VsComparisonRow("Kutup Deseni (Polar Pattern)", l.polarPattern.ifBlank { UNSPECIFIED }, r.polarPattern.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(VsComparisonRow("Örnekleme & Bit Derinliği", l.sampleRateAndBit.ifBlank { UNSPECIFIED }, r.sampleRateAndBit.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(VsComparisonRow("Bağlantı Arayüzü", l.connectivity.ifBlank { UNSPECIFIED }, r.connectivity.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Maksimum Ses Basıncı (Max SPL)",
                leftVal = l.maxSplDb.takeIf { it > 0 }?.toDouble(),
                rightVal = r.maxSplDb.takeIf { it > 0 }?.toDouble(),
                leftText = if (l.maxSplDb > 0) "${l.maxSplDb} dB SPL" else UNSPECIFIED,
                rightText = if (r.maxSplDb > 0) "${r.maxSplDb} dB SPL" else UNSPECIFIED,
                badgeSuffix = "Daha Yüksek Ses Direnci",
            )
        )
        add(VsComparisonRow("Frekans Tepkisi", l.frequencyResponse.ifBlank { UNSPECIFIED }, r.frequencyResponse.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("Dahili DSP İşleme", if (l.hasBuiltInDsp) "Var" else "Yok", if (r.hasBuiltInDsp) "Var" else "Yok"))
    }

    private fun compareMousepads(l: MousepadSpec, r: MousepadSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Yüzey Teknolojisi", l.surfaceType.ifBlank { UNSPECIFIED }, r.surfaceType.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(VsComparisonRow("Kayma Hızı & Karakter", l.speedGlideTier.ifBlank { UNSPECIFIED }, r.speedGlideTier.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Durdurma Gücü (Stopping Power)",
                leftVal = l.stoppingPowerScore.takeIf { it > 0 }?.toDouble(),
                rightVal = r.stoppingPowerScore.takeIf { it > 0 }?.toDouble(),
                leftText = if (l.stoppingPowerScore > 0) "${l.stoppingPowerScore}/10 Puan" else UNSPECIFIED,
                rightText = if (r.stoppingPowerScore > 0) "${r.stoppingPowerScore}/10 Puan" else UNSPECIFIED,
                badgeSuffix = "Daha Keskin Flick Kontrolü",
                isKey = true,
            )
        )
        add(VsComparisonRow("Taban Materyali", l.baseMaterial.ifBlank { UNSPECIFIED }, r.baseMaterial.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(VsComparisonRow("Boyutlar", l.dimensionsMm.ifBlank { UNSPECIFIED }, r.dimensionsMm.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("Kalınlık", if (l.thicknessMm > 0.0) "${l.thicknessMm} mm" else UNSPECIFIED, if (r.thicknessMm > 0.0) "${r.thicknessMm} mm" else UNSPECIFIED))
        add(VsComparisonRow("Nem & Ter Direnci", l.humidityResistance.ifBlank { UNSPECIFIED }, r.humidityResistance.ifBlank { UNSPECIFIED }))
    }

    private fun compareWebcams(l: WebcamSpec, r: WebcamSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Maksimum Çözünürlük", l.maxResolution.ifBlank { UNSPECIFIED }, r.maxResolution.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Kare Hızı (FPS)",
                leftVal = l.maxFrameRateFps.takeIf { it > 0 }?.toDouble(),
                rightVal = r.maxFrameRateFps.takeIf { it > 0 }?.toDouble(),
                leftText = if (l.maxFrameRateFps > 0) "${l.maxFrameRateFps} FPS" else UNSPECIFIED,
                rightText = if (r.maxFrameRateFps > 0) "${r.maxFrameRateFps} FPS" else UNSPECIFIED,
                badgeSuffix = "FPS Daha Akıcı",
                isKey = true,
            )
        )
        add(VsComparisonRow("Sensör & Boyut", "${l.sensorModel.ifBlank { UNSPECIFIED }} (${l.sensorSizeInch.ifBlank { "-" }})", "${r.sensorModel.ifBlank { UNSPECIFIED }} (${r.sensorSizeInch.ifBlank { "-" }})", isKeyMetric = true))
        add(VsComparisonRow("Diyafram Açıklığı", l.aperture.ifBlank { UNSPECIFIED }, r.aperture.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Görüş Açısı (FOV)",
                leftVal = l.fieldOfViewDeg.takeIf { it > 0 }?.toDouble(),
                rightVal = r.fieldOfViewDeg.takeIf { it > 0 }?.toDouble(),
                leftText = if (l.fieldOfViewDeg > 0) "${l.fieldOfViewDeg}°" else UNSPECIFIED,
                rightText = if (r.fieldOfViewDeg > 0) "${r.fieldOfViewDeg}°" else UNSPECIFIED,
                badgeSuffix = "Daha Geniş Kadraj",
            )
        )
        add(VsComparisonRow("Otomatik Odak & AI Takip", if (l.hasAiTracking) "${l.focusType} + AI Takip" else l.focusType.ifBlank { UNSPECIFIED }, if (r.hasAiTracking) "${r.focusType} + AI Takip" else r.focusType.ifBlank { UNSPECIFIED }))
    }

    private fun compareGamingChairs(l: GamingChairSpec, r: GamingChairSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Koltuk Mimarisi", l.chairType.ifBlank { UNSPECIFIED }, r.chairType.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(VsComparisonRow("Döşeme Kumaşı / Deri", l.upholsteryMaterial.ifBlank { UNSPECIFIED }, r.upholsteryMaterial.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(VsComparisonRow("L4/L5 Bel Desteği", l.lumbarSupportType.ifBlank { UNSPECIFIED }, r.lumbarSupportType.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Maksimum Taşıma Kapasitesi",
                leftVal = l.maxWeightCapacityKg.takeIf { it > 0 }?.toDouble(),
                rightVal = r.maxWeightCapacityKg.takeIf { it > 0 }?.toDouble(),
                leftText = if (l.maxWeightCapacityKg > 0) "${l.maxWeightCapacityKg} kg" else UNSPECIFIED,
                rightText = if (r.maxWeightCapacityKg > 0) "${r.maxWeightCapacityKg} kg" else UNSPECIFIED,
                badgeSuffix = "kg Daha Dayanıklı",
                isKey = true,
            )
        )
        add(VsComparisonRow("Kolçak Tipi", l.armrestType.ifBlank { UNSPECIFIED }, r.armrestType.ifBlank { UNSPECIFIED }))
        val lReclineText = if (l.reclineAngleDeg > 0) "${l.tiltMechanism.ifBlank { "-" }} (${l.reclineAngleDeg}°)" else l.tiltMechanism.ifBlank { UNSPECIFIED }
        val rReclineText = if (r.reclineAngleDeg > 0) "${r.tiltMechanism.ifBlank { "-" }} (${r.reclineAngleDeg}°)" else r.tiltMechanism.ifBlank { UNSPECIFIED }
        add(VsComparisonRow("Mekanizma & Yatış Açısı", lReclineText, rReclineText))
        add(VsComparisonRow("Önerilen Kullanıcı Boyu", l.recommendedHeightRange.ifBlank { UNSPECIFIED }, r.recommendedHeightRange.ifBlank { UNSPECIFIED }))
    }

    private fun compareSmartphones(l: PhoneSpec, r: PhoneSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Yonga Seti (SoC) & Mimari", if (l.processNodeNm > 0) "${l.chipset} (${l.processNodeNm}nm)" else l.chipset, if (r.processNodeNm > 0) "${r.chipset} (${r.processNodeNm}nm)" else r.chipset, isKeyMetric = true))
        
        if (l.antutuScore != null || r.antutuScore != null) {
            add(
                compareHigherBetterNullable(
                    label = "AnTuTu v10 Benchmark Skoru",
                    leftVal = l.antutuScore?.toDouble(),
                    rightVal = r.antutuScore?.toDouble(),
                    leftText = l.antutuScore?.let { "${it} Puan" } ?: UNSPECIFIED,
                    rightText = r.antutuScore?.let { "${it} Puan" } ?: UNSPECIFIED,
                    badgeSuffix = "Daha Yüksek AnTuTu",
                    isKey = true,
                )
            )
        }

        if (l.geekbenchSingle != null || r.geekbenchSingle != null) {
            add(
                compareHigherBetterNullable(
                    label = "Geekbench 6 (Tek Çekirdek)",
                    leftVal = l.geekbenchSingle?.toDouble(),
                    rightVal = r.geekbenchSingle?.toDouble(),
                    leftText = l.geekbenchSingle?.let { "$it pts" } ?: UNSPECIFIED,
                    rightText = r.geekbenchSingle?.let { "$it pts" } ?: UNSPECIFIED,
                    badgeSuffix = "Tek Çekirdek",
                )
            )
        }

        if (l.geekbenchMulti != null || r.geekbenchMulti != null) {
            add(
                compareHigherBetterNullable(
                    label = "Geekbench 6 (Çoklu Çekirdek)",
                    leftVal = l.geekbenchMulti?.toDouble(),
                    rightVal = r.geekbenchMulti?.toDouble(),
                    leftText = l.geekbenchMulti?.let { "$it pts" } ?: UNSPECIFIED,
                    rightText = r.geekbenchMulti?.let { "$it pts" } ?: UNSPECIFIED,
                    badgeSuffix = "Çoklu Çekirdek",
                    isKey = true,
                )
            )
        }

        if (l.stabilityPercent != null || r.stabilityPercent != null) {
            add(
                compareHigherBetterNullable(
                    label = "3DMark Termal Kararlılık",
                    leftVal = l.stabilityPercent?.toDouble(),
                    rightVal = r.stabilityPercent?.toDouble(),
                    leftText = l.stabilityPercent?.let { "%$it Kararlı" } ?: UNSPECIFIED,
                    rightText = r.stabilityPercent?.let { "%$it Kararlı" } ?: UNSPECIFIED,
                    badgeSuffix = "Daha Kararlı Termal",
                    isKey = true,
                )
            )
        }

        add(
            compareHigherBetter(
                label = "Bellek (RAM) Kapasitesi",
                leftVal = l.ramGb.toDouble(),
                rightVal = r.ramGb.toDouble(),
                leftText = "${l.ramGb} GB (${l.ramType})",
                rightText = "${r.ramGb} GB (${r.ramType})",
                badgeSuffix = "Daha Fazla Bellek",
            )
        )

        add(
            compareHigherBetter(
                label = "Dahili Depolama Alanı",
                leftVal = l.storageGb.toDouble(),
                rightVal = r.storageGb.toDouble(),
                leftText = "${l.storageGb} GB (${l.storageType})",
                rightText = "${r.storageGb} GB (${r.storageType})",
                badgeSuffix = "Daha Geniş Hafıza",
            )
        )

        add(
            compareHigherBetter(
                label = "Ekran Yenileme Hızı (Akıcılık)",
                leftVal = l.refreshRateHz.toDouble(),
                rightVal = r.refreshRateHz.toDouble(),
                leftText = "${l.refreshRateHz} Hz",
                rightText = "${r.refreshRateHz} Hz",
                badgeSuffix = "Daha Akıcı Panel",
            )
        )

        add(
            compareHigherBetter(
                label = "Tepe Parlaklık (Güneş Altı Netlik)",
                leftVal = l.peakBrightnessNit.toDouble(),
                rightVal = r.peakBrightnessNit.toDouble(),
                leftText = "${l.peakBrightnessNit} Nit",
                rightText = "${r.peakBrightnessNit} Nit",
                badgeSuffix = "Daha Parlak Ekran",
                isKey = true,
            )
        )

        add(
            compareHigherBetter(
                label = "Piksel Yoğunluğu (Keskinlik)",
                leftVal = l.ppi.toDouble(),
                rightVal = r.ppi.toDouble(),
                leftText = "${l.ppi} PPI (${l.resolution})",
                rightText = "${r.ppi} PPI (${r.resolution})",
                badgeSuffix = "Daha Keskin Görüntü",
            )
        )

        add(VsComparisonRow("Ekran Boyutu & Koruma", "${l.displaySizeInch}\" ${l.displayTech} (${l.glassProtection})", "${r.displaySizeInch}\" ${r.displayTech} (${r.glassProtection})"))
        
        val lOis = if (l.hasOis && l.oisType.isNotBlank()) " (${l.oisType})" else if (l.hasOis) " (OIS)" else ""
        val rOis = if (r.hasOis && r.oisType.isNotBlank()) " (${r.oisType})" else if (r.hasOis) " (OIS)" else ""
        add(VsComparisonRow("Ana Kamera Sensörü", "${l.mainCameraMp} MP • ${l.mainSensorSize}$lOis", "${r.mainCameraMp} MP • ${r.mainSensorSize}$rOis", isKeyMetric = true))

        if (l.opticalZoomX != null || r.opticalZoomX != null) {
            add(
                compareHigherBetterNullable(
                    label = "Optik Yakınlaştırma (Telefoto)",
                    leftVal = l.opticalZoomX,
                    rightVal = r.opticalZoomX,
                    leftText = l.opticalZoomX?.let { "${it}x Optik Zoom" } ?: "Optik Zoom Yok",
                    rightText = r.opticalZoomX?.let { "${it}x Optik Zoom" } ?: "Optik Zoom Yok",
                    badgeSuffix = "Daha Fazla Yakınlaştırma",
                    isKey = true,
                )
            )
        }

        add(
            compareHigherBetter(
                label = "Batarya Kapasitesi",
                leftVal = l.batteryMah.toDouble(),
                rightVal = r.batteryMah.toDouble(),
                leftText = "${l.batteryMah} mAh",
                rightText = "${r.batteryMah} mAh",
                badgeSuffix = "Daha Büyük Pil",
                isKey = true,
            )
        )

        add(
            compareHigherBetter(
                label = "Kablolu Hızlı Şarj Gücü",
                leftVal = l.wiredChargingWatt.toDouble(),
                rightVal = r.wiredChargingWatt.toDouble(),
                leftText = "${l.wiredChargingWatt} W",
                rightText = "${r.wiredChargingWatt} W",
                badgeSuffix = "Daha Hızlı Dolum",
            )
        )

        add(
            compareLowerBetter(
                label = "Cihaz Gövde Ağırlığı",
                leftVal = l.weightGrams.toDouble(),
                rightVal = r.weightGrams.toDouble(),
                leftText = "${l.weightGrams} g (${l.thicknessMm} mm)",
                rightText = "${r.weightGrams} g (${r.thicknessMm} mm)",
                badgeSuffix = "Daha Hafif",
            )
        )

        add(VsComparisonRow("Kasa Malzemesi & Suya Dayanıklılık", "${l.frameMaterial} • ${l.waterResistance}", "${r.frameMaterial} • ${r.waterResistance}"))
        
        add(
            compareHigherBetter(
                label = "Yazılım Güncelleme Garantisi",
                leftVal = l.updateYears.toDouble(),
                rightVal = r.updateYears.toDouble(),
                leftText = "${l.osName} (${l.updateYears} Yıl)",
                rightText = "${r.osName} (${r.updateYears} Yıl)",
                badgeSuffix = "Daha Uzun Destek",
            )
        )

        if (l.pubgFpsSupport.isNotBlank() || r.pubgFpsSupport.isNotBlank()) {
            add(VsComparisonRow("Oyun Tavan Desteği", l.pubgFpsSupport.ifBlank { UNSPECIFIED }, r.pubgFpsSupport.ifBlank { UNSPECIFIED }))
        }
    }

    private fun compareTablets(l: TabletSpec, r: TabletSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Yonga Seti (SoC) & Mimari", if (l.processNodeNm > 0) "${l.chipset} (${l.processNodeNm}nm)" else l.chipset, if (r.processNodeNm > 0) "${r.chipset} (${r.processNodeNm}nm)" else r.chipset, isKeyMetric = true))

        if (l.antutuScore != null || r.antutuScore != null) {
            add(
                compareHigherBetterNullable(
                    label = "AnTuTu v10 Skoru",
                    leftVal = l.antutuScore?.toDouble(),
                    rightVal = r.antutuScore?.toDouble(),
                    leftText = l.antutuScore?.let { "${it} Puan" } ?: UNSPECIFIED,
                    rightText = r.antutuScore?.let { "${it} Puan" } ?: UNSPECIFIED,
                    badgeSuffix = "Daha Yüksek AnTuTu",
                    isKey = true,
                )
            )
        }

        if (l.geekbenchSingle != null || r.geekbenchSingle != null) {
            add(
                compareHigherBetterNullable(
                    label = "Geekbench 6 (Tek Çekirdek)",
                    leftVal = l.geekbenchSingle?.toDouble(),
                    rightVal = r.geekbenchSingle?.toDouble(),
                    leftText = l.geekbenchSingle?.let { "$it pts" } ?: UNSPECIFIED,
                    rightText = r.geekbenchSingle?.let { "$it pts" } ?: UNSPECIFIED,
                    badgeSuffix = "Tek Çekirdek",
                )
            )
        }

        if (l.geekbenchMulti != null || r.geekbenchMulti != null) {
            add(
                compareHigherBetterNullable(
                    label = "Geekbench 6 (Çoklu Çekirdek)",
                    leftVal = l.geekbenchMulti?.toDouble(),
                    rightVal = r.geekbenchMulti?.toDouble(),
                    leftText = l.geekbenchMulti?.let { "$it pts" } ?: UNSPECIFIED,
                    rightText = r.geekbenchMulti?.let { "$it pts" } ?: UNSPECIFIED,
                    badgeSuffix = "Çoklu Çekirdek",
                    isKey = true,
                )
            )
        }

        add(
            compareHigherBetter(
                label = "Ekran Boyutu & Çalışma Alanı",
                leftVal = l.displaySizeInch,
                rightVal = r.displaySizeInch,
                leftText = "${l.displaySizeInch}\" ${l.displayTech}",
                rightText = "${r.displaySizeInch}\" ${r.displayTech}",
                badgeSuffix = "Daha Geniş Ekran",
                isKey = true,
            )
        )

        add(
            compareHigherBetter(
                label = "Ekran Yenileme Hızı",
                leftVal = l.refreshRateHz.toDouble(),
                rightVal = r.refreshRateHz.toDouble(),
                leftText = "${l.refreshRateHz} Hz",
                rightText = "${r.refreshRateHz} Hz",
                badgeSuffix = "Daha Akıcı",
            )
        )

        add(
            compareHigherBetter(
                label = "Tepe Parlaklık (Nit)",
                leftVal = l.peakBrightnessNit.toDouble(),
                rightVal = r.peakBrightnessNit.toDouble(),
                leftText = "${l.peakBrightnessNit} Nit",
                rightText = "${r.peakBrightnessNit} Nit",
                badgeSuffix = "Daha Parlak",
            )
        )

        add(VsComparisonRow("Stylus Kalem Standardı", l.stylusSupport, r.stylusSupport, isKeyMetric = true))

        add(
            compareHigherBetter(
                label = "Hoparlör & Akustik Ses",
                leftVal = l.speakerCount.toDouble(),
                rightVal = r.speakerCount.toDouble(),
                leftText = "${l.speakerCount} Hoparlör${if (l.hasDolbyAtmos) " Dolby Atmos" else ""}",
                rightText = "${r.speakerCount} Hoparlör${if (r.hasDolbyAtmos) " Dolby Atmos" else ""}",
                badgeSuffix = "Daha Güçlü Ses",
            )
        )

        add(
            compareHigherBetter(
                label = "Batarya Kapasitesi",
                leftVal = l.batteryMah.toDouble(),
                rightVal = r.batteryMah.toDouble(),
                leftText = "${l.batteryMah} mAh",
                rightText = "${r.batteryMah} mAh",
                badgeSuffix = "Daha Büyük Batarya",
                isKey = true,
            )
        )

        add(
            compareLowerBetter(
                label = "Kasa İnceliği",
                leftVal = l.thicknessMm,
                rightVal = r.thicknessMm,
                leftText = "${l.thicknessMm} mm (${l.weightGrams}g)",
                rightText = "${r.thicknessMm} mm (${r.weightGrams}g)",
                badgeSuffix = "Daha İnce Gövde",
            )
        )

        add(VsComparisonRow("İşletim Sistemi & Verimlilik", "${l.osName} (${l.updateYears} Yıl)", "${r.osName} (${r.updateYears} Yıl)"))
        add(VsComparisonRow("Bağlantı & Hücresel", l.cellularOption, r.cellularOption))
    }

    private fun compareCaseFans(l: CaseFanSpec, r: CaseFanSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Fan Çapı", l.fanSizeMm.takeIf { it > 0 }?.let { "$it mm" } ?: UNSPECIFIED, r.fanSizeMm.takeIf { it > 0 }?.let { "$it mm" } ?: UNSPECIFIED, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Hava Akışı (CFM)",
                leftVal = l.maxAirflowCfm.takeIf { it > 0.0 },
                rightVal = r.maxAirflowCfm.takeIf { it > 0.0 },
                leftText = l.maxAirflowCfm.takeIf { it > 0.0 }?.let { "$it CFM" } ?: UNSPECIFIED,
                rightText = r.maxAirflowCfm.takeIf { it > 0.0 }?.let { "$it CFM" } ?: UNSPECIFIED,
                badgeSuffix = "Hava Akışı",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Statik Basınç (mmH₂O)",
                leftVal = l.staticPressureMmH2o.takeIf { it > 0.0 },
                rightVal = r.staticPressureMmH2o.takeIf { it > 0.0 },
                leftText = l.staticPressureMmH2o.takeIf { it > 0.0 }?.let { "$it mmH₂O" } ?: UNSPECIFIED,
                rightText = r.staticPressureMmH2o.takeIf { it > 0.0 }?.let { "$it mmH₂O" } ?: UNSPECIFIED,
                badgeSuffix = "Basınç",
            )
        )
        add(
            compareLowerBetterNullable(
                label = "Gürültü Seviyesi (dBA)",
                leftVal = l.noiseLevelDba.takeIf { it > 0.0 },
                rightVal = r.noiseLevelDba.takeIf { it > 0.0 },
                leftText = l.noiseLevelDba.takeIf { it > 0.0 }?.let { "$it dBA" } ?: UNSPECIFIED,
                rightText = r.noiseLevelDba.takeIf { it > 0.0 }?.let { "$it dBA" } ?: UNSPECIFIED,
                badgeSuffix = "Daha Sessiz",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Maksimum Devir (RPM)",
                leftVal = l.maxRpm.takeIf { it > 0 }?.toDouble(),
                rightVal = r.maxRpm.takeIf { it > 0 }?.toDouble(),
                leftText = l.maxRpm.takeIf { it > 0 }?.let { "$it RPM" } ?: UNSPECIFIED,
                rightText = r.maxRpm.takeIf { it > 0 }?.let { "$it RPM" } ?: UNSPECIFIED,
                badgeSuffix = "RPM",
            )
        )
        add(VsComparisonRow("Rulman & Aydınlatma", listOfNotNull(l.bearingType.ifBlank { null }, l.argbType.ifBlank { null }).joinToString(" • ").ifBlank { UNSPECIFIED }, listOfNotNull(r.bearingType.ifBlank { null }, r.argbType.ifBlank { null }).joinToString(" • ").ifBlank { UNSPECIFIED }))
    }

    private fun compareUps(l: UpsSpec, r: UpsSpec): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetterNullable(
                label = "Aktif Güç Kapasitesi (Watt)",
                leftVal = l.wattCapacity.takeIf { it > 0 }?.toDouble(),
                rightVal = r.wattCapacity.takeIf { it > 0 }?.toDouble(),
                leftText = l.wattCapacity.takeIf { it > 0 }?.let { "$it W" } ?: UNSPECIFIED,
                rightText = r.wattCapacity.takeIf { it > 0 }?.let { "$it W" } ?: UNSPECIFIED,
                badgeSuffix = "Daha Yüksek Güç",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Görünür Güç (VA)",
                leftVal = l.vaRating.takeIf { it > 0 }?.toDouble(),
                rightVal = r.vaRating.takeIf { it > 0 }?.toDouble(),
                leftText = l.vaRating.takeIf { it > 0 }?.let { "$it VA" } ?: UNSPECIFIED,
                rightText = r.vaRating.takeIf { it > 0 }?.let { "$it VA" } ?: UNSPECIFIED,
                badgeSuffix = "VA",
                isKey = true,
            )
        )
        add(VsComparisonRow("Topoloji & Dalga Tipi", listOfNotNull(l.topology.ifBlank { null }, l.sinewaveType.ifBlank { null }).joinToString(" • ").ifBlank { UNSPECIFIED }, listOfNotNull(r.topology.ifBlank { null }, r.sinewaveType.ifBlank { null }).joinToString(" • ").ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Priz Sayısı",
                leftVal = l.outletCount.takeIf { it > 0 }?.toDouble(),
                rightVal = r.outletCount.takeIf { it > 0 }?.toDouble(),
                leftText = l.outletCount.takeIf { it > 0 }?.let { "$it Priz" } ?: UNSPECIFIED,
                rightText = r.outletCount.takeIf { it > 0 }?.let { "$it Priz" } ?: UNSPECIFIED,
                badgeSuffix = "Çıkış",
            )
        )
        add(VsComparisonRow("Batarya Yapısı", l.batterySpecs.ifBlank { UNSPECIFIED }, r.batterySpecs.ifBlank { UNSPECIFIED }))
    }

    private fun compareGamingDesks(l: GamingDeskSpec, r: GamingDeskSpec): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetterNullable(
                label = "Masa Genişliği (cm)",
                leftVal = l.deskWidthCm.takeIf { it > 0 }?.toDouble(),
                rightVal = r.deskWidthCm.takeIf { it > 0 }?.toDouble(),
                leftText = if (l.deskWidthCm > 0) "${l.deskWidthCm} x ${l.deskDepthCm} cm" else UNSPECIFIED,
                rightText = if (r.deskWidthCm > 0) "${r.deskWidthCm} x ${r.deskDepthCm} cm" else UNSPECIFIED,
                badgeSuffix = "Daha Geniş",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Taşıma Kapasitesi (kg)",
                leftVal = l.maxLoadKg.takeIf { it > 0 }?.toDouble(),
                rightVal = r.maxLoadKg.takeIf { it > 0 }?.toDouble(),
                leftText = l.maxLoadKg.takeIf { it > 0 }?.let { "$it kg" } ?: UNSPECIFIED,
                rightText = r.maxLoadKg.takeIf { it > 0 }?.let { "$it kg" } ?: UNSPECIFIED,
                badgeSuffix = "Kapasite",
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "Motorlu Yükseklik Ayarı",
                leftVal = if (l.isMotorizedHeight) 1.0 else 0.0,
                rightVal = if (r.isMotorizedHeight) 1.0 else 0.0,
                leftText = if (l.isMotorizedHeight) "Motorlu (${l.minHeightCm}-${l.maxHeightCm} cm)" else "Sabit",
                rightText = if (r.isMotorizedHeight) "Motorlu (${r.minHeightCm}-${r.maxHeightCm} cm)" else "Sabit",
                customDelta = "Ergonomi Avantajı",
            )
        )
        add(VsComparisonRow("Yüzey Kaplaması", l.surfaceFinish.ifBlank { UNSPECIFIED }, r.surfaceFinish.ifBlank { UNSPECIFIED }))
    }

    private fun compareGamingLighting(l: GamingLightingSpec, r: GamingLightingSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Form Faktörü", l.formFactor.ifBlank { UNSPECIFIED }, r.formFactor.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Parlaklık (Lümen)",
                leftVal = l.maxLumens.takeIf { it > 0 }?.toDouble(),
                rightVal = r.maxLumens.takeIf { it > 0 }?.toDouble(),
                leftText = l.maxLumens.takeIf { it > 0 }?.let { "$it lm" } ?: UNSPECIFIED,
                rightText = r.maxLumens.takeIf { it > 0 }?.let { "$it lm" } ?: UNSPECIFIED,
                badgeSuffix = "Daha Parlak",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Renk Doğruluğu (CRI)",
                leftVal = l.criScore.takeIf { it > 0 }?.toDouble(),
                rightVal = r.criScore.takeIf { it > 0 }?.toDouble(),
                leftText = l.criScore.takeIf { it > 0 }?.let { "Ra $it" } ?: UNSPECIFIED,
                rightText = r.criScore.takeIf { it > 0 }?.let { "Ra $it" } ?: UNSPECIFIED,
                badgeSuffix = "CRI",
            )
        )
        add(VsComparisonRow("Renk Sıcaklığı & Ekosistem", listOfNotNull(l.colorTempKelvin.ifBlank { null }, l.supportedEcosystems.ifBlank { null }).joinToString(" • ").ifBlank { UNSPECIFIED }, listOfNotNull(r.colorTempKelvin.ifBlank { null }, r.supportedEcosystems.ifBlank { null }).joinToString(" • ").ifBlank { UNSPECIFIED }))
    }

    private fun compareSmartwatches(l: SmartwatchSpec, r: SmartwatchSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Ekran Paneli", if (l.screenSizeInch > 0.0) "${l.screenSizeInch}\" ${l.screenPanel}".trim() else l.screenPanel.ifBlank { UNSPECIFIED }, if (r.screenSizeInch > 0.0) "${r.screenSizeInch}\" ${r.screenPanel}".trim() else r.screenPanel.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Pil Ömrü (Gün)",
                leftVal = l.batteryLifeDays.takeIf { it > 0 }?.toDouble(),
                rightVal = r.batteryLifeDays.takeIf { it > 0 }?.toDouble(),
                leftText = l.batteryLifeDays.takeIf { it > 0 }?.let { "$it Gün" } ?: UNSPECIFIED,
                rightText = r.batteryLifeDays.takeIf { it > 0 }?.let { "$it Gün" } ?: UNSPECIFIED,
                badgeSuffix = "Daha Uzun Pil",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Tepe Parlaklık (Nit)",
                leftVal = l.peakBrightnessNits.takeIf { it > 0 }?.toDouble(),
                rightVal = r.peakBrightnessNits.takeIf { it > 0 }?.toDouble(),
                leftText = l.peakBrightnessNits.takeIf { it > 0 }?.let { "$it Nit" } ?: UNSPECIFIED,
                rightText = r.peakBrightnessNits.takeIf { it > 0 }?.let { "$it Nit" } ?: UNSPECIFIED,
                badgeSuffix = "Daha Parlak",
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Su Geçirmezlik (ATM)",
                leftVal = l.waterResistanceAtm.takeIf { it > 0 }?.toDouble(),
                rightVal = r.waterResistanceAtm.takeIf { it > 0 }?.toDouble(),
                leftText = l.waterResistanceAtm.takeIf { it > 0 }?.let { "$it ATM" } ?: UNSPECIFIED,
                rightText = r.waterResistanceAtm.takeIf { it > 0 }?.let { "$it ATM" } ?: UNSPECIFIED,
                badgeSuffix = "ATM",
            )
        )
        add(VsComparisonRow("İşletim Sistemi & Kasa", listOfNotNull(l.osName.ifBlank { null }, l.caseMaterial.ifBlank { null }).joinToString(" • ").ifBlank { UNSPECIFIED }, listOfNotNull(r.osName.ifBlank { null }, r.caseMaterial.ifBlank { null }).joinToString(" • ").ifBlank { UNSPECIFIED }))
    }

    private fun compareRouters(l: RouterSpec, r: RouterSpec): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetterNullable(
                label = "Wi-Fi Standardı",
                leftVal = wifiStandardRank(l.wifiStandard).takeIf { it > 0 }?.toDouble(),
                rightVal = wifiStandardRank(r.wifiStandard).takeIf { it > 0 }?.toDouble(),
                leftText = l.wifiStandard.ifBlank { UNSPECIFIED },
                rightText = r.wifiStandard.ifBlank { UNSPECIFIED },
                customDelta = "Daha Yeni Nesil Wi-Fi",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Hız Sınıfı",
                leftVal = extractSpeedMbps(l.speedClass).takeIf { it > 0 }?.toDouble(),
                rightVal = extractSpeedMbps(r.speedClass).takeIf { it > 0 }?.toDouble(),
                leftText = l.speedClass.ifBlank { UNSPECIFIED },
                rightText = r.speedClass.ifBlank { UNSPECIFIED },
                badgeSuffix = "Bant Genişliği",
                isKey = true,
            )
        )
        add(VsComparisonRow("Ethernet Portları", l.ethernetPorts.ifBlank { UNSPECIFIED }, r.ethernetPorts.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(VsComparisonRow("Frekans Bantları", l.bands.ifBlank { UNSPECIFIED }, r.bands.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("İşlemci & Bellek", l.processorAndRam.ifBlank { UNSPECIFIED }, r.processorAndRam.ifBlank { UNSPECIFIED }))
    }

    private fun compareModems(l: ModemSpec, r: ModemSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("DSL / Hat Profili", l.vdslProfile.ifBlank { UNSPECIFIED }, r.vdslProfile.ifBlank { UNSPECIFIED }, isKeyMetric = true))
        add(
            compareHigherBetterNullable(
                label = "Wi-Fi Standardı",
                leftVal = wifiStandardRank(l.wifiStandard).takeIf { it > 0 }?.toDouble(),
                rightVal = wifiStandardRank(r.wifiStandard).takeIf { it > 0 }?.toDouble(),
                leftText = l.wifiStandard.ifBlank { UNSPECIFIED },
                rightText = r.wifiStandard.ifBlank { UNSPECIFIED },
                customDelta = "Daha Yeni Wi-Fi",
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "Fiber WAN Desteği",
                leftVal = if (l.fiberWanSupport) 1.0 else 0.0,
                rightVal = if (r.fiberWanSupport) 1.0 else 0.0,
                leftText = if (l.fiberWanSupport) "Destekliyor" else "Yok",
                rightText = if (r.fiberWanSupport) "Destekliyor" else "Yok",
                customDelta = "Fiber Hazır",
            )
        )
        add(VsComparisonRow("LAN Portları", l.lanPorts.ifBlank { UNSPECIFIED }, r.lanPorts.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("Yonga Seti (SoC)", l.chipset.ifBlank { UNSPECIFIED }, r.chipset.ifBlank { UNSPECIFIED }))
    }

    private fun compareAccessPoints(l: AccessPointSpec, r: AccessPointSpec): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetterNullable(
                label = "Wi-Fi Standardı",
                leftVal = wifiStandardRank(l.wifiStandard).takeIf { it > 0 }?.toDouble(),
                rightVal = wifiStandardRank(r.wifiStandard).takeIf { it > 0 }?.toDouble(),
                leftText = l.wifiStandard.ifBlank { UNSPECIFIED },
                rightText = r.wifiStandard.ifBlank { UNSPECIFIED },
                customDelta = "Daha Yeni Wi-Fi",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Maksimum İstemci Kapasitesi",
                leftVal = l.maxClients.takeIf { it > 0 }?.toDouble(),
                rightVal = r.maxClients.takeIf { it > 0 }?.toDouble(),
                leftText = l.maxClients.takeIf { it > 0 }?.let { "$it+ İstemci" } ?: UNSPECIFIED,
                rightText = r.maxClients.takeIf { it > 0 }?.let { "$it+ İstemci" } ?: UNSPECIFIED,
                badgeSuffix = "Kapasite",
                isKey = true,
            )
        )
        add(VsComparisonRow("PoE Güç Desteği", l.poeSupport.ifBlank { UNSPECIFIED }, r.poeSupport.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("Uplink Portu", l.uplinkPort.ifBlank { UNSPECIFIED }, r.uplinkPort.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("MU-MIMO Akışı", l.muMimoStreams.ifBlank { UNSPECIFIED }, r.muMimoStreams.ifBlank { UNSPECIFIED }))
    }

    private fun compareRangeExtenders(l: RangeExtenderSpec, r: RangeExtenderSpec): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetterNullable(
                label = "Wi-Fi Standardı",
                leftVal = wifiStandardRank(l.wifiStandard).takeIf { it > 0 }?.toDouble(),
                rightVal = wifiStandardRank(r.wifiStandard).takeIf { it > 0 }?.toDouble(),
                leftText = l.wifiStandard.ifBlank { UNSPECIFIED },
                rightText = r.wifiStandard.ifBlank { UNSPECIFIED },
                customDelta = "Daha Yeni Wi-Fi",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Kapsama Alanı (m²)",
                leftVal = l.coverageAreaM2.takeIf { it > 0 }?.toDouble(),
                rightVal = r.coverageAreaM2.takeIf { it > 0 }?.toDouble(),
                leftText = l.coverageAreaM2.takeIf { it > 0 }?.let { "$it m²" } ?: UNSPECIFIED,
                rightText = r.coverageAreaM2.takeIf { it > 0 }?.let { "$it m²" } ?: UNSPECIFIED,
                badgeSuffix = "Daha Geniş Kapsama",
                isKey = true,
            )
        )
        add(VsComparisonRow("Mesh Protokolü", l.meshProtocol.ifBlank { UNSPECIFIED }, r.meshProtocol.ifBlank { UNSPECIFIED }))
        add(VsComparisonRow("Ethernet Portu", l.ethernetPorts.ifBlank { UNSPECIFIED }, r.ethernetPorts.ifBlank { UNSPECIFIED }))
    }

    private fun psuEfficiencyRank(rating: PsuEfficiencyRating): Int = when (rating) {
        PsuEfficiencyRating.TITANIUM -> 6
        PsuEfficiencyRating.PLATINUM -> 5
        PsuEfficiencyRating.GOLD -> 4
        PsuEfficiencyRating.SILVER -> 3
        PsuEfficiencyRating.BRONZE -> 2
        PsuEfficiencyRating.STANDARD -> 1
    }

    private fun extractFirstInt(text: String): Int? =
        Regex("""(\d{2,3})\s*W""", RegexOption.IGNORE_CASE).find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: Regex("""(\d{2,3})""").find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun extractRefreshRateHz(text: String): Int? =
        Regex("""(\d{2,3})\s*Hz""", RegexOption.IGNORE_CASE).find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun compareHigherBetter(
        label: String,
        leftVal: Double,
        rightVal: Double,
        leftText: String,
        rightText: String,
        badgeSuffix: String = "Üstün",
        customDelta: String? = null,
        isKey: Boolean = false,
    ): VsComparisonRow {
        val diff = abs(leftVal - rightVal)
        if (diff < 0.01) {
            return VsComparisonRow(label, leftText, rightText, ComparisonWinner.TIE, "Eşit", isKey)
        }
        val winner = if (leftVal > rightVal) ComparisonWinner.LEFT else ComparisonWinner.RIGHT
        val minVal = min(abs(leftVal), abs(rightVal)).coerceAtLeast(1.0)
        val pct = ((diff / minVal) * 100.0).roundToInt().coerceAtLeast(1)
        val badge = customDelta ?: "+%$pct $badgeSuffix"
        return VsComparisonRow(label, leftText, rightText, winner, badge, isKey)
    }

    private fun compareHigherBetterNullable(
        label: String,
        leftVal: Double?,
        rightVal: Double?,
        leftText: String,
        rightText: String,
        badgeSuffix: String = "Üstün",
        customDelta: String? = null,
        isKey: Boolean = false,
    ): VsComparisonRow {
        if (leftVal == null || rightVal == null) {
            return VsComparisonRow(label, leftText, rightText, ComparisonWinner.NEUTRAL, null, isKey)
        }
        return compareHigherBetter(label, leftVal, rightVal, leftText, rightText, badgeSuffix, customDelta, isKey)
    }

    private fun compareLowerBetter(
        label: String,
        leftVal: Double,
        rightVal: Double,
        leftText: String,
        rightText: String,
        badgeSuffix: String = "Daha Verimli",
        customDelta: String? = null,
        isKey: Boolean = false,
    ): VsComparisonRow {
        val diff = abs(leftVal - rightVal)
        if (diff < 0.01) {
            return VsComparisonRow(label, leftText, rightText, ComparisonWinner.TIE, "Eşit", isKey)
        }
        val winner = if (leftVal < rightVal) ComparisonWinner.LEFT else ComparisonWinner.RIGHT
        val maxVal = max(abs(leftVal), abs(rightVal)).coerceAtLeast(1.0)
        val pct = ((diff / maxVal) * 100.0).roundToInt().coerceAtLeast(1)
        val badge = customDelta ?: "-%$pct $badgeSuffix"
        return VsComparisonRow(label, leftText, rightText, winner, badge, isKey)
    }

    private fun compareLowerBetterNullable(
        label: String,
        leftVal: Double?,
        rightVal: Double?,
        leftText: String,
        rightText: String,
        badgeSuffix: String = "Daha İyi",
        customDelta: String? = null,
        isKey: Boolean = false,
    ): VsComparisonRow {
        if (leftVal == null || rightVal == null) {
            return VsComparisonRow(label, leftText, rightText, ComparisonWinner.NEUTRAL, null, isKey)
        }
        return compareLowerBetter(label, leftVal, rightVal, leftText, rightText, badgeSuffix, customDelta, isKey)
    }

    // endregion
}
