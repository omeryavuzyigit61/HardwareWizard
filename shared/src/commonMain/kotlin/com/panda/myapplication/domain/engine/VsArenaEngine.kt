package com.panda.myapplication.domain.engine

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GameCategory
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.ResolutionFps
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageSpec
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
    val valueChampion: VsChampionSummary,
    val rows: List<VsComparisonRow>,
    val gameRows: List<VsGameComparisonRow>,
)

/**
 * Head-to-Head Mathematical Comparison Engine ("VS Arena" - Faz 2).
 *
 * Strictly adheres to the Zero-Hallucination (Honest Data) principle:
 * - Missing specs are shown as `"Belirtilmedi"` and excluded from false winner claims.
 * - Missing prices are shown as `"Stok / Fiyat Yok"`.
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
        val valueChampion = buildValueChampion(left, right, leftScore, rightScore)

        val rows = buildList {
            // 1. Price comparison row (always first)
            add(buildPriceRow(left, right))

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
                left.spec is PrebuiltSpec && right.spec is PrebuiltSpec ->
                    addAll(comparePrebuilts(left, left.spec, right, right.spec))
                left.spec is LaptopSpec && right.spec is LaptopSpec ->
                    addAll(compareLaptops(left, left.spec, right, right.spec))
                left.spec is MouseSpec && right.spec is MouseSpec ->
                    addAll(compareMice(left.spec, right.spec))
                left.spec is KeyboardSpec && right.spec is KeyboardSpec ->
                    addAll(compareKeyboards(left.spec, right.spec))
                left.spec is HeadsetSpec && right.spec is HeadsetSpec ->
                    addAll(compareHeadsets(left.spec, right.spec))
                left.spec is MonitorSpec && right.spec is MonitorSpec ->
                    addAll(compareMonitors(left.spec, right.spec))
            }
        }

        val gameRows = buildGameComparisonRows(left, right)

        return VsComparisonReport(
            mode = mode,
            category = left.category,
            leftItem = left,
            rightItem = right,
            performanceChampion = perfChampion,
            valueChampion = valueChampion,
            rows = rows,
            gameRows = gameRows,
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
            ComparisonWinner.LEFT, ComparisonWinner.RIGHT -> "+%$pct Daha Güçlü"
            else -> "%0 Fark (Eşit)"
        }

        val unitLabel = when (left.category) {
            HardwareCategory.CPU, HardwareCategory.GPU, HardwareCategory.PREBUILT_PC, HardwareCategory.LAPTOP -> "Performans Endeksi"
            HardwareCategory.PSU -> "Güç & Tier Endeksi"
            HardwareCategory.RAM -> "Bellek Hız/Gecikme Endeksi"
            HardwareCategory.STORAGE -> "NVMe G/Ç Endeksi"
            HardwareCategory.MOTHERBOARD -> "Platform & Genişleme Endeksi"
            HardwareCategory.COOLER -> "Termal Kapasite Endeksi"
            HardwareCategory.CASE -> "Hava Akışı & Hacim Endeksi"
            HardwareCategory.MOUSE -> "Sensör & E-Spor Tepki Endeksi"
            HardwareCategory.KEYBOARD -> "Switch & Rapid Trigger Endeksi"
            HardwareCategory.HEADSET -> "Akustik Sürücü & Sahne Endeksi"
            HardwareCategory.MONITOR -> "Panel Hızı & Görüntü Endeksi"
        }

        return VsChampionSummary(
            title = "SAF PERFORMANS KAZANANI",
            winner = winner,
            winnerName = winnerName,
            badgeText = badge,
            subtitle = unitLabel,
            leftMetricText = "${leftScore.roundToInt()} Puan",
            rightMetricText = "${rightScore.roundToInt()} Puan",
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

        add(
            compareHigherBetter(
                label = "Oyun & Genel Performans Endeksi",
                leftVal = lBench.relativePerformanceIndex,
                rightVal = rBench.relativePerformanceIndex,
                leftText = "%${lBench.relativePerformanceIndex.roundToInt()} (${lBench.tier})",
                rightText = "%${rBench.relativePerformanceIndex.roundToInt()} (${rBench.tier})",
                badgeSuffix = "Daha Hızlı",
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

        add(
            compareHigherBetterNullable(
                label = "L3 Önbellek (3D V-Cache / SmartCache)",
                leftVal = l.l3CacheMb?.toDouble(),
                rightVal = r.l3CacheMb?.toDouble(),
                leftText = l.l3CacheMb?.let { "$it MB" } ?: UNSPECIFIED,
                rightText = r.l3CacheMb?.let { "$it MB" } ?: UNSPECIFIED,
                customDelta = if (l.l3CacheMb != null && r.l3CacheMb != null && l.l3CacheMb != r.l3CacheMb) {
                    "+${abs(l.l3CacheMb - r.l3CacheMb)} MB L3"
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

        add(
            compareHigherBetter(
                label = "Bağıl Raster & Oyun Gücü Endeksi",
                leftVal = lBench.relativePerformanceIndex,
                rightVal = rBench.relativePerformanceIndex,
                leftText = "%${lBench.relativePerformanceIndex.roundToInt()} (${lBench.tier})",
                rightText = "%${rBench.relativePerformanceIndex.roundToInt()} (${rBench.tier})",
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
        add(
            compareHigherBetter(
                label = "GPU Boost Saat Hızı",
                leftVal = lClock.toDouble(),
                rightVal = rClock.toDouble(),
                leftText = "$lClock MHz",
                rightText = "$rClock MHz",
                badgeSuffix = "Frekans",
            )
        )

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
        add(
            compareLowerBetter(
                label = "CAS Gecikme Değeri (CL)",
                leftVal = l.casLatency.toDouble(),
                rightVal = r.casLatency.toDouble(),
                leftText = "CL${l.casLatency}",
                rightText = "CL${r.casLatency}",
                customDelta = if (l.casLatency != r.casLatency) "-${abs(l.casLatency - r.casLatency)} CL Daha Düşük Gecikme" else null,
                isKey = true,
            )
        )
        val lNs = l.firstWordLatencyNs ?: ((l.casLatency * 2000.0) / l.speed.mhz.coerceAtLeast(1))
        val rNs = r.firstWordLatencyNs ?: ((r.casLatency * 2000.0) / r.speed.mhz.coerceAtLeast(1))
        add(
            compareLowerBetter(
                label = "İlk Kelime Tepki Süresi (ns)",
                leftVal = lNs,
                rightVal = rNs,
                leftText = "${(lNs * 10.0).roundToInt() / 10.0} ns",
                rightText = "${(rNs * 10.0).roundToInt() / 10.0} ns",
                badgeSuffix = "Daha Düşük Tepki",
            )
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

    private fun comparePrebuilts(
        leftItem: HardwareItem,
        l: PrebuiltSpec,
        rightItem: HardwareItem,
        r: PrebuiltSpec,
    ): List<VsComparisonRow> = buildList {
        add(
            compareHigherBetter(
                label = "Oyun & Sistem Performans Skoru",
                leftVal = l.gamingScore.toDouble(),
                rightVal = r.gamingScore.toDouble(),
                leftText = "${l.gamingScore}/100 Puan",
                rightText = "${r.gamingScore}/100 Puan",
                badgeSuffix = "Daha Güçlü Sistem",
                isKey = true,
            )
        )
        add(
            compareHigherBetterNullable(
                label = "Tek Tek Toplamaya Göre Fiyat Avantajı / Kâr",
                leftVal = l.priceAdvantageTry,
                rightVal = r.priceAdvantageTry,
                leftText = formatAdvantageText(l.priceAdvantageTry, l.estimatedSeparatePriceTry),
                rightText = formatAdvantageText(r.priceAdvantageTry, r.estimatedSeparatePriceTry),
                customDelta = if (l.priceAdvantageTry != null && r.priceAdvantageTry != null && abs(l.priceAdvantageTry - r.priceAdvantageTry) >= 100.0) {
                    "+${abs(l.priceAdvantageTry - r.priceAdvantageTry).formatPrice()} ₺ Daha Kârlı"
                } else null,
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "1080p Ultra Ortalama Oyun FPS",
                leftVal = l.fps1080p.toDouble(),
                rightVal = r.fps1080p.toDouble(),
                leftText = if (l.fps1080p > 0) "${l.fps1080p} FPS" else UNSPECIFIED,
                rightText = if (r.fps1080p > 0) "${r.fps1080p} FPS" else UNSPECIFIED,
                badgeSuffix = "1080p FPS",
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "1440p (2K) Ultra Ortalama Oyun FPS",
                leftVal = l.fps1440p.toDouble(),
                rightVal = r.fps1440p.toDouble(),
                leftText = if (l.fps1440p > 0) "${l.fps1440p} FPS" else UNSPECIFIED,
                rightText = if (r.fps1440p > 0) "${r.fps1440p} FPS" else UNSPECIFIED,
                badgeSuffix = "2K FPS",
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "4K UHD Ultra Ortalama Oyun FPS",
                leftVal = l.fps4k.toDouble(),
                rightVal = r.fps4k.toDouble(),
                leftText = if (l.fps4k > 0) "${l.fps4k} FPS" else UNSPECIFIED,
                rightText = if (r.fps4k > 0) "${r.fps4k} FPS" else UNSPECIFIED,
                badgeSuffix = "4K FPS",
            )
        )
        add(
            VsComparisonRow(
                label = "Ekran Kartı (GPU)",
                leftValue = l.gpuModel.ifBlank { UNSPECIFIED },
                rightValue = r.gpuModel.ifBlank { UNSPECIFIED },
                isKeyMetric = true,
            )
        )
        add(
            VsComparisonRow(
                label = "İşlemci (CPU)",
                leftValue = l.cpuModel.ifBlank { UNSPECIFIED },
                rightValue = r.cpuModel.ifBlank { UNSPECIFIED },
                isKeyMetric = true,
            )
        )
        add(
            VsComparisonRow(
                label = "Anakart & Platform",
                leftValue = l.motherboardModel.ifBlank { UNSPECIFIED },
                rightValue = r.motherboardModel.ifBlank { UNSPECIFIED },
            )
        )
        add(
            VsComparisonRow(
                label = "Bellek (RAM) & Depolama (SSD)",
                leftValue = "${l.ramSpec.ifBlank { UNSPECIFIED }} • ${l.storageSpec.ifBlank { UNSPECIFIED }}",
                rightValue = "${r.ramSpec.ifBlank { UNSPECIFIED }} • ${r.storageSpec.ifBlank { UNSPECIFIED }}",
            )
        )
        add(
            VsComparisonRow(
                label = "Kasa, PSU & Satıcı Mağaza",
                leftValue = "${l.psuCaseSpec.ifBlank { UNSPECIFIED }} (${l.storeName})",
                rightValue = "${r.psuCaseSpec.ifBlank { UNSPECIFIED }} (${r.storeName})",
            )
        )
    }

    private fun compareLaptops(
        leftItem: HardwareItem,
        l: LaptopSpec,
        rightItem: HardwareItem,
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

        add(
            compareHigherBetter(
                label = "1080p & 1440p Ortalama Oyun FPS",
                leftVal = (l.fps1080p + l.fps1440p).toDouble(),
                rightVal = (r.fps1080p + r.fps1440p).toDouble(),
                leftText = "1080p: ${l.fps1080p} FPS • 2K: ${l.fps1440p} FPS",
                rightText = "1080p: ${r.fps1080p} FPS • 2K: ${r.fps1440p} FPS",
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

        val leftHighCache = left.model.contains("X3D", ignoreCase = true) || ((left.spec as? CpuSpec)?.l3CacheMb ?: 0) >= 96
        val rightHighCache = right.model.contains("X3D", ignoreCase = true) || ((right.spec as? CpuSpec)?.l3CacheMb ?: 0) >= 96

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
            is CpuSpec, is GpuSpec -> {
                BenchmarkEngine.evaluate(item).info.relativePerformanceIndex.coerceAtLeast(10.0)
            }
            is PrebuiltSpec -> {
                if (s.gamingScore > 0) s.gamingScore.toDouble()
                else ((s.fps1080p * 0.4 + s.fps1440p * 0.6) / 1.5).coerceIn(30.0, 100.0)
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
                val latencyPenalty = s.casLatency * 0.8
                (speedScore + capGb * 0.7 - latencyPenalty).coerceIn(20.0, 120.0)
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
                val pollingBonus = (s.pollingRateHz / 1000.0) * 1.5
                val weightBonus = ((100 - s.weightG).coerceAtLeast(0)) * 0.45
                val dpiScore = (s.maxDpi / 1000.0).coerceAtMost(45.0)
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
        }
    }

    private fun compareMice(l: MouseSpec, r: MouseSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Optik Sensör", l.sensor, r.sensor, isKeyMetric = true))
        add(
            compareHigherBetter(
                label = "Polling Rate (Raporlama Hızı)",
                leftVal = l.pollingRateHz.toDouble(),
                rightVal = r.pollingRateHz.toDouble(),
                leftText = "${l.pollingRateHz} Hz (${1000.0 / l.pollingRateHz} ms)",
                rightText = "${r.pollingRateHz} Hz (${1000.0 / r.pollingRateHz} ms)",
                badgeSuffix = "Daha Hızlı Tepki",
                isKey = true,
            )
        )
        add(
            compareLowerBetter(
                label = "Ağırlık (Ultra-Light)",
                leftVal = l.weightG.toDouble(),
                rightVal = r.weightG.toDouble(),
                leftText = "${l.weightG} g",
                rightText = "${r.weightG} g",
                customDelta = if (l.weightG != r.weightG) "-${abs(l.weightG - r.weightG)}g Daha Hafif" else null,
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "Maksimum DPI Hassasiyeti",
                leftVal = l.maxDpi.toDouble(),
                rightVal = r.maxDpi.toDouble(),
                leftText = "${l.maxDpi} DPI",
                rightText = "${r.maxDpi} DPI",
                badgeSuffix = "DPI",
            )
        )
        add(VsComparisonRow("Switch Teknolojisi", l.switchType, r.switchType))
        add(VsComparisonRow("Bağlantı Modu", l.connectivity, r.connectivity))
        add(
            compareHigherBetterNullable(
                label = "Batarya Ömrü",
                leftVal = l.batteryHours?.toDouble(),
                rightVal = r.batteryHours?.toDouble(),
                leftText = l.batteryHours?.let { "$it Saat" } ?: "Kablolu",
                rightText = r.batteryHours?.let { "$it Saat" } ?: "Kablolu",
                customDelta = if (l.batteryHours != null && r.batteryHours != null && l.batteryHours != r.batteryHours) {
                    "+${abs(l.batteryHours - r.batteryHours)} Saat"
                } else null,
            )
        )
    }

    private fun compareKeyboards(l: KeyboardSpec, r: KeyboardSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Switch Mimarisi", l.switchType, r.switchType, isKeyMetric = true))
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
            compareLowerBetter(
                label = "Minimum Tetikleme Mesafesi",
                leftVal = l.minActuationMm,
                rightVal = r.minActuationMm,
                leftText = "${l.minActuationMm} mm",
                rightText = "${r.minActuationMm} mm",
                badgeSuffix = "Daha Çabuk Tetikleme",
                isKey = true,
            )
        )
        add(
            compareHigherBetter(
                label = "Polling Rate (Tepki Frekansı)",
                leftVal = l.pollingRateHz.toDouble(),
                rightVal = r.pollingRateHz.toDouble(),
                leftText = "${l.pollingRateHz} Hz",
                rightText = "${r.pollingRateHz} Hz",
                badgeSuffix = "Hz",
            )
        )
        add(VsComparisonRow("Tuş Dizilimi (Layout)", l.layout, r.layout))
        add(
            VsComparisonRow(
                label = "Hot-Swap (Lehimsiz Switch Değişimi)",
                leftValue = if (l.hotSwap) "Var" else "Yok",
                rightValue = if (r.hotSwap) "Var" else "Yok",
            )
        )
        add(VsComparisonRow("Bağlantı", l.connectivity, r.connectivity))
    }

    private fun compareHeadsets(l: HeadsetSpec, r: HeadsetSpec): List<VsComparisonRow> = buildList {
        add(VsComparisonRow("Sürücü Teknolojisi", l.driverType, r.driverType, isKeyMetric = true))
        add(
            compareHigherBetter(
                label = "Sürücü Çapı (mm)",
                leftVal = l.driverSizeMm.toDouble(),
                rightVal = r.driverSizeMm.toDouble(),
                leftText = "${l.driverSizeMm} mm",
                rightText = "${r.driverSizeMm} mm",
                customDelta = if (l.driverSizeMm != r.driverSizeMm) "+${abs(l.driverSizeMm - r.driverSizeMm)} mm Sürücü" else null,
                isKey = true,
            )
        )
        add(
            VsComparisonRow(
                label = "Empedans & DAC/Amp Gereksinimi",
                leftValue = "${l.impedanceOhm} Ω (${if (l.dacAmpRequired) "DAC/Amp Önerilir" else "Doğrudan Sürülebilir"})",
                rightValue = "${r.impedanceOhm} Ω (${if (r.dacAmpRequired) "DAC/Amp Önerilir" else "Doğrudan Sürülebilir"})",
                isKeyMetric = true,
            )
        )
        add(VsComparisonRow("Frekans Tepkisi", l.frequencyResponse, r.frequencyResponse))
        add(VsComparisonRow("Mikrofon Tipi", l.micType, r.micType))
        add(
            compareLowerBetter(
                label = "Ağırlık (Konfor)",
                leftVal = l.weightG.toDouble(),
                rightVal = r.weightG.toDouble(),
                leftText = "${l.weightG} g",
                rightText = "${r.weightG} g",
                customDelta = if (l.weightG != r.weightG) "-${abs(l.weightG - r.weightG)}g Daha Hafif" else null,
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

    private fun psuEfficiencyRank(rating: PsuEfficiencyRating): Int = when (rating) {
        PsuEfficiencyRating.TITANIUM -> 6
        PsuEfficiencyRating.PLATINUM -> 5
        PsuEfficiencyRating.GOLD -> 4
        PsuEfficiencyRating.SILVER -> 3
        PsuEfficiencyRating.BRONZE -> 2
        PsuEfficiencyRating.STANDARD -> 1
    }

    private fun formatAdvantageText(advantageTry: Double?, separatePriceTry: Double?): String {
        if (advantageTry != null && separatePriceTry != null && separatePriceTry > 0.0) {
            val sign = if (advantageTry >= 0) "+" else "-"
            val label = if (advantageTry >= 0) "Kâr" else "Fark"
            return "$sign${abs(advantageTry).formatPrice()} ₺ $label (Tek Tek: ${separatePriceTry.formatPrice()} ₺)"
        }
        if (separatePriceTry != null && separatePriceTry > 0.0) {
            return "Tek Tek: ${separatePriceTry.formatPrice()} ₺"
        }
        return UNSPECIFIED
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
