package com.panda.myapplication.domain.engine

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.DdrGeneration
import kotlin.math.roundToInt

enum class MemoryChannelMode(val channels: Int, val labelTr: String, val labelEn: String) {
    SINGLE(1, "Tek Kanal (1x)", "Single-Channel (1x)"),
    DUAL(2, "Çift Kanal (2x)", "Dual-Channel (2x)"),
    QUAD(4, "Dört Kanal (4x HEDT)", "Quad-Channel (4x HEDT)");

    fun localized(isTr: Boolean): String = if (isTr) labelTr else labelEn
}

@Immutable
data class RamKitAnalysis(
    val generation: DdrGeneration,
    val freqMts: Int,
    val casLatency: Int,
    val channelMode: MemoryChannelMode,
    val trueLatencyNs: Double,
    val peakBandwidthGbs: Double,
)

@Immutable
data class RamComparisonVerdict(
    val kitA: RamKitAnalysis,
    val kitB: RamKitAnalysis,
    val platformTitleTr: String,
    val platformTitleEn: String,
    val platformAdviceTr: String,
    val platformAdviceEn: String,
)

object RamLatencyEngine {

    fun freqRangeFor(gen: DdrGeneration): IntRange = when (gen) {
        DdrGeneration.DDR4 -> 2133..4400
        DdrGeneration.DDR5 -> 4800..8800
        else -> 1600..2400
    }

    fun clRangeFor(gen: DdrGeneration): IntRange = when (gen) {
        DdrGeneration.DDR4 -> 12..24
        DdrGeneration.DDR5 -> 26..46
        else -> 9..16
    }

    fun coerceSpec(gen: DdrGeneration, freqMts: Int, casLatency: Int): Pair<Int, Int> {
        val fRange = freqRangeFor(gen)
        val cRange = clRangeFor(gen)
        return freqMts.coerceIn(fRange.first, fRange.last) to casLatency.coerceIn(cRange.first, cRange.last)
    }

    fun analyzeKit(
        generation: DdrGeneration,
        freqMts: Int,
        casLatency: Int,
        channelMode: MemoryChannelMode = MemoryChannelMode.DUAL,
    ): RamKitAnalysis {
        val (safeFreq, safeCl) = coerceSpec(generation, freqMts, casLatency)
        val trueLatencyNs = ((safeCl * 2000.0) / safeFreq.toDouble() * 100.0).roundToInt() / 100.0
        // 8 bytes (64-bit) per memory channel per transfer: Dual-Channel = 16 B/transfer
        val bytesPerTransfer = 8.0 * channelMode.channels
        val peakBwGbs = ((safeFreq * bytesPerTransfer) / 1000.0 * 10.0).roundToInt() / 10.0
        return RamKitAnalysis(
            generation = generation,
            freqMts = safeFreq,
            casLatency = safeCl,
            channelMode = channelMode,
            trueLatencyNs = trueLatencyNs,
            peakBandwidthGbs = peakBwGbs,
        )
    }

    fun compare(kitA: RamKitAnalysis, kitB: RamKitAnalysis): RamComparisonVerdict {
        val bothDdr4 = kitA.generation == DdrGeneration.DDR4 && kitB.generation == DdrGeneration.DDR4
        val hasHighSpeedDdr5 = maxOf(kitA.freqMts, kitB.freqMts) >= 6600
        val hasUltraSpeedDdr5 = maxOf(kitA.freqMts, kitB.freqMts) >= 7800
        val hasSingleChannel = kitA.channelMode == MemoryChannelMode.SINGLE || kitB.channelMode == MemoryChannelMode.SINGLE

        val (titleTr, titleEn, adviceTr, adviceEn) = when {
            hasSingleChannel -> QuadString(
                "Tek Kanal (1x) Bant Genişliği Darboğazı Uyarısı",
                "Single-Channel (1x) Bandwidth Bottleneck Alert",
                "Tek modül (Single-Channel) bellek kullanımı veri yolunu yarıya indirir ve %1 Low FPS değerlerinde %15-25 düşüşe yol açar. Her zaman 2x16GB veya 2x24GB Çift Kanal kit tercih edin.",
                "Running a single DIMM halves memory bus width and can cut 1% Low gaming FPS by 15-25%. Always pair 2x16GB or 2x24GB in Dual-Channel.",
            )
            bothDdr4 -> QuadString(
                "DDR4 Gear 1 / FCLK 1:1 Tatlı Nokta Analizi",
                "DDR4 Gear 1 / FCLK 1:1 Sweet-Spot Analysis",
                "AMD Ryzen 5000 (AM4) ve Intel LGA1700 DDR4 sistemlerde 3600 MT/s CL16 (10.0 ns), bellek kontrolcüsünün Gear 1 / 1800MHz FCLK 1:1 modunda en kararlı çalıştığı altın noktadır. 4000+ MT/s genellikle Gear 2 cezasına takılır.",
                "On AMD Ryzen 5000 (AM4) and Intel DDR4 platforms, 3600 MT/s CL16 (10.0 ns) is the Gear 1 / 1800MHz FCLK 1:1 sweet spot. Pushing 4000+ MT/s often triggers Gear 2 latency penalties.",
            )
            hasUltraSpeedDdr5 -> QuadString(
                "DDR5 8000+ MT/s CUDIMM & 1:2 Mod Analizi",
                "DDR5 8000+ MT/s CUDIMM & 1:2 Divider Analysis",
                "8000 MT/s hızları Intel Core Ultra 200S (CUDIMM) sistemlerde yüksek bant genişliği sağlar. AMD Ryzen 9000'de ise 6400 MT/s üstü UCLK 1:2 moduna geçer; 8000 CL38 bu cezayı telafi etse de 2-DIMM anakart gerektirir.",
                "8000 MT/s shines on Intel Core Ultra 200S (CUDIMM) rigs. On AMD Ryzen 9000, >6400 MT/s drops UCLK to 1:2 mode; 8000 CL38 offsets the penalty but requires a 2-DIMM board.",
            )
            hasHighSpeedDdr5 -> QuadString(
                "AMD UCLK 1:2 Tuzağı vs Intel XMP Avantajı (6600-7600 MT/s)",
                "AMD UCLK 1:2 Trap vs Intel XMP Advantage (6600-7600 MT/s)",
                "6600-7600 MT/s aralığı Intel LGA1700/1851 sistemlerde ekstra FPS kazandırırken, AMD Ryzen 7000/9000 (AM5) işlemcilerde UCLK:MCLK oranını 1:2'ye düşürerek oyunlarda DDR5-6000 CL30'dan daha yavaş kalabilir!",
                "6600-7600 MT/s boosts Intel LGA1700/1851 gaming performance, but forces AMD Ryzen 7000/9000 (AM5) into 1:2 UCLK divider mode — often losing to DDR5-6000 CL30 in games!",
            )
            else -> QuadString(
                "AMD AM5 & Modern DDR5 6000 CL30 Senkron Altın Kuralı",
                "AMD AM5 & Modern DDR5 6000 CL30 1:1 Sync Sweet Spot",
                "DDR5-6000 CL30 (10.0 ns) ve DDR5-6400 CL32, Ryzen 7000/9000 serisinde UCLK:MCLK 1:1 senkron kalarak en yüksek %1 Low oyun akıcılığını ve tak-çalıştır EXPO kararlılığını sunar.",
                "DDR5-6000 CL30 (10.0 ns) and DDR5-6400 CL32 keep Ryzen 7000/9000 in 1:1 UCLK:MCLK sync, delivering peak 1% Low gaming smoothness and plug-and-play EXPO stability.",
            )
        }

        return RamComparisonVerdict(
            kitA = kitA,
            kitB = kitB,
            platformTitleTr = titleTr,
            platformTitleEn = titleEn,
            platformAdviceTr = adviceTr,
            platformAdviceEn = adviceEn,
        )
    }

    private data class QuadString(val a: String, val b: String, val c: String, val d: String)
}
