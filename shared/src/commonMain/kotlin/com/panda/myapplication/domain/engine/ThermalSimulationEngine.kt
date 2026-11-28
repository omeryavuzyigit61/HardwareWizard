package com.panda.myapplication.domain.engine

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase
import kotlin.math.roundToInt

@Immutable
data class CpuThermalProfile(
    val id: String,
    val name: String,
    val socket: String,
    val baseTdpWatts: Int,
    val gamingWatts: Int,
    val peakPptMtpWatts: Int,
    val idleWatts: Int,
    val tjMaxCelsius: Int,
    val dieContactResistanceCw: Double,
    val notesTr: String,
    val notesEn: String,
    val isFromCatalog: Boolean = false,
) {
    fun localizedNotes(isTr: Boolean): String = if (isTr) notesTr else notesEn
}

@Immutable
data class CoolerThermalProfile(
    val id: String,
    val name: String,
    val typeLabelTr: String,
    val typeLabelEn: String,
    val maxDissipationWatts: Int,
    val ratedNoiseDba: Double,
    val supportedSockets: List<String> = emptyList(),
    val isLiquid: Boolean = false,
    val isFromCatalog: Boolean = false,
) {
    fun localizedType(isTr: Boolean): String = if (isTr) typeLabelTr else typeLabelEn
}

@Immutable
data class ThermalSimulationResult(
    val idleTempCelsius: Int,
    val gamingTempCelsius: Int,
    val renderTempCelsius: Int,
    val idleWatts: Int,
    val gamingWatts: Int,
    val renderWatts: Int,
    val headroomWatts: Int,
    val saturationRatio: Float,
    val coolerThermalResistanceCw: Double,
    val estimatedGamingNoiseDba: Double,
    val estimatedStressNoiseDba: Double,
    val isThrottling: Boolean,
    val isSocketCompatible: Boolean,
)

object ThermalSimulationEngine {

    /**
     * Rated ΔT above 24°C ambient at 100% maxDissipationWatts for consumer CPU coolers (~62°C rise -> 86°C).
     */
    private const val RATED_COOLER_DELTA_T = 62.0

    val CPU_PRESETS: List<CpuThermalProfile> = listOf(
        CpuThermalProfile(
            id = "preset_7500f",
            name = "AMD Ryzen 5 7500F",
            socket = "AM5",
            baseTdpWatts = 65,
            gamingWatts = 56,
            peakPptMtpWatts = 88,
            idleWatts = 22,
            tjMaxCelsius = 95,
            dieContactResistanceCw = 0.08,
            notesTr = "6 Çekirdek • 88W PPT tavanı, tek kule hava soğutucu ile çok serin çalışır.",
            notesEn = "6 Cores • 88W PPT ceiling, runs very cool on any single-tower air cooler.",
        ),
        CpuThermalProfile(
            id = "preset_7800x3d",
            name = "AMD Ryzen 7 7800X3D",
            socket = "AM5",
            baseTdpWatts = 120,
            gamingWatts = 58,
            peakPptMtpWatts = 88,
            idleWatts = 26,
            tjMaxCelsius = 89,
            dieContactResistanceCw = 0.19, // 1st-gen stacked 3D V-Cache above CCD increases thermal impedance
            notesTr = "1. Nesil üstten 3D V-Cache • Tam yükte ~88W çeker fakat termal direnç yüksektir (TjMax 89°C).",
            notesEn = "1st-Gen top-stacked 3D V-Cache • Pulls ~88W in stress loads with higher die impedance (TjMax 89°C).",
        ),
        CpuThermalProfile(
            id = "preset_9800x3d",
            name = "AMD Ryzen 7 9800X3D",
            socket = "AM5",
            baseTdpWatts = 120,
            gamingWatts = 72,
            peakPptMtpWatts = 142,
            idleWatts = 26,
            tjMaxCelsius = 95,
            dieContactResistanceCw = 0.09, // 2nd-gen V-Cache underneath CCD restores direct IHS cooling
            notesTr = "2. Nesil alttan 3D V-Cache • Doğrudan IHS teması sayesinde yüksek saat hızında bile serindir.",
            notesEn = "2nd-Gen under-CCD 3D V-Cache • Direct CCD-to-IHS contact enables cool sustained boost.",
        ),
        CpuThermalProfile(
            id = "preset_9950x",
            name = "AMD Ryzen 9 9950X",
            socket = "AM5",
            baseTdpWatts = 170,
            gamingWatts = 115,
            peakPptMtpWatts = 230,
            idleWatts = 32,
            tjMaxCelsius = 95,
            dieContactResistanceCw = 0.06,
            notesTr = "16 Çekirdek • Ağır render yükünde 230W PPT çeker; 360mm AIO veya üst düzey çift kule önerilir.",
            notesEn = "16 Cores • Sustains 230W PPT in multi-core rendering; 360mm AIO or flagship dual-tower recommended.",
        ),
        CpuThermalProfile(
            id = "preset_14600k",
            name = "Intel Core i5-14600K",
            socket = "LGA1700",
            baseTdpWatts = 125,
            gamingWatts = 98,
            peakPptMtpWatts = 181,
            idleWatts = 18,
            tjMaxCelsius = 100,
            dieContactResistanceCw = 0.06,
            notesTr = "PL2 tam yükte 181W MTP çeker; çift kule hava veya 240mm sıvı soğutma idealdir.",
            notesEn = "Pulls 181W PL2 MTP under all-core load; dual-tower air or 240mm AIO is ideal.",
        ),
        CpuThermalProfile(
            id = "preset_14700k",
            name = "Intel Core i7-14700K",
            socket = "LGA1700",
            baseTdpWatts = 125,
            gamingWatts = 132,
            peakPptMtpWatts = 253,
            idleWatts = 20,
            tjMaxCelsius = 100,
            dieContactResistanceCw = 0.055,
            notesTr = "20 Çekirdek • Intel varsayılan 253W MTP sınırında hava soğutucuları doygunluğa yaklaştırır.",
            notesEn = "20 Cores • Intel default 253W MTP pushes air coolers near thermal saturation.",
        ),
        CpuThermalProfile(
            id = "preset_14900k",
            name = "Intel Core i9-14900K",
            socket = "LGA1700",
            baseTdpWatts = 125,
            gamingWatts = 148,
            peakPptMtpWatts = 253,
            idleWatts = 22,
            tjMaxCelsius = 100,
            dieContactResistanceCw = 0.065,
            notesTr = "24 Çekirdek (253W Intel Default MTP) • 360mm/420mm AIO olmadan tam yükte TjMax sınırına ulaşır.",
            notesEn = "24 Cores (253W Intel Default MTP) • Reaches TjMax under AVX render without 360mm/420mm AIO.",
        ),
        CpuThermalProfile(
            id = "preset_285k",
            name = "Intel Core Ultra 9 285K",
            socket = "LGA1851",
            baseTdpWatts = 125,
            gamingWatts = 105,
            peakPptMtpWatts = 250,
            idleWatts = 16,
            tjMaxCelsius = 105,
            dieContactResistanceCw = 0.05,
            notesTr = "Arrow Lake TSMC N3B • Oyunlarda ~105W ile 14900K'dan çok daha serin, renderda 250W PL2 çeker.",
            notesEn = "Arrow Lake TSMC N3B • Runs much cooler in gaming (~105W) with 250W PL2 ceiling.",
        ),
    )

    val COOLER_PRESETS: List<CoolerThermalProfile> = listOf(
        CoolerThermalProfile("preset_stock", "Stock Cooler (Wraith Stealth / Laminar)", "Stok Hava Soğutucu", "Stock Air Cooler", 95, 36.5, emptyList(), false),
        CoolerThermalProfile("preset_ak120", "Thermalright Assassin King 120 SE", "120mm Tek Kule Hava", "120mm Single Tower Air", 180, 25.6, emptyList(), false),
        CoolerThermalProfile("preset_pa120", "Thermalright Peerless Assassin 120 SE", "Çift Kule (6 Isı Borusu)", "Dual Tower (6 Heatpipes)", 235, 26.5, emptyList(), false),
        CoolerThermalProfile("preset_nhd15", "Noctua NH-D15 G2", "Amiral Gemisi Çift Kule", "Flagship Dual Tower Air", 265, 24.8, emptyList(), false),
        CoolerThermalProfile("preset_lf240", "Arctic Liquid Freezer III 240", "240mm Sıvı Soğutma (AIO)", "240mm Liquid Cooler (AIO)", 255, 27.0, emptyList(), true),
        CoolerThermalProfile("preset_lf360", "Arctic Liquid Freezer III 360", "360mm Sıvı Soğutma (38mm Rad)", "360mm Liquid (38mm Radiator)", 315, 28.5, emptyList(), true),
        CoolerThermalProfile("preset_lf420", "Arctic Liquid Freezer III 420", "420mm Ekstrem Sıvı AIO", "420mm Extreme Liquid AIO", 360, 29.0, emptyList(), true),
    )

    fun fromCatalogCpu(item: HardwareItem, spec: CpuSpec): CpuThermalProfile {
        val baseTdp = spec.tdp.watts.coerceAtLeast(35)
        val u = item.displayName.uppercase()
        val socketUpper = spec.socket.uppercase()
        val isAmd = socketUpper.contains("AM5") || socketUpper.contains("AM4") || socketUpper.contains("STR") || item.brand.contains("AMD", ignoreCase = true)

        val parsedPowerLimit = spec.powerLimits?.let { raw ->
            Regex("""(\d{2,3})\s*W""", RegexOption.IGNORE_CASE).findAll(raw)
                .mapNotNull { it.groupValues[1].toIntOrNull() }
                .maxOrNull()
        }

        val peakPpt = parsedPowerLimit ?: when {
            u.contains("7800X3D") || u.contains("5800X3D") || u.contains("5700X3D") -> 88
            u.contains("9800X3D") -> 142
            u.contains("9950X3D") || u.contains("9950X") || u.contains("7950X") -> 230
            u.contains("7950X3D") -> 162
            u.contains("14900KS") -> 320
            u.contains("14900") || u.contains("13900") || u.contains("14700") || u.contains("13700") -> 253
            u.contains("285K") || u.contains("265K") -> 250
            u.contains("245K") -> 159
            u.contains("14600") || u.contains("13600") -> 181
            isAmd -> (baseTdp * 1.35).roundToInt()
            else -> (baseTdp * 1.48).roundToInt()
        }

        val gamingWatts = when {
            u.contains("X3D") -> (peakPpt * 0.56).roundToInt().coerceIn(48, 105)
            else -> (peakPpt * 0.58).roundToInt().coerceAtLeast(40)
        }

        val tjMax = spec.tjMaxCelsius?.takeIf { it in 75..115 } ?: when {
            u.contains("7800X3D") || u.contains("7950X3D") || u.contains("7900X3D") || u.contains("7600X3D") -> 89
            u.contains("5800X3D") || u.contains("5700X3D") -> 90
            socketUpper.contains("1851") -> 105
            isAmd -> 95
            else -> 100
        }

        val dieRes = when {
            u.contains("7800X3D") || u.contains("5800X3D") || u.contains("5700X3D") || u.contains("7950X3D") -> 0.18
            u.contains("9800X3D") || u.contains("9950X3D") -> 0.09
            else -> 0.06
        }

        val idleWatts = if (isAmd && spec.coreCount >= 8) 26 else if (isAmd) 22 else 18
        val cacheNote = spec.l3CacheMb?.let { "${it}MB L3 • " } ?: ""

        return CpuThermalProfile(
            id = item.id.value,
            name = item.displayName,
            socket = spec.socket,
            baseTdpWatts = baseTdp,
            gamingWatts = gamingWatts,
            peakPptMtpWatts = peakPpt,
            idleWatts = idleWatts,
            tjMaxCelsius = tjMax,
            dieContactResistanceCw = dieRes,
            notesTr = "${spec.coreCount} Çekirdek / ${spec.threadCount} İzlek • ${cacheNote}Tam Yük PPT/MTP: ${peakPpt}W (TjMax ${tjMax}°C)",
            notesEn = "${spec.coreCount}C / ${spec.threadCount}T • ${cacheNote}Peak PPT/MTP: ${peakPpt}W (TjMax ${tjMax}°C)",
            isFromCatalog = true,
        )
    }

    fun fromCatalogCooler(item: HardwareItem, spec: CoolerSpec): CoolerThermalProfile {
        val rad = spec.radiatorSizeMm
        val rawTdp = spec.maxCoolingTdp.watts
        // Supabase default fallback is 220W when unspecified; scale radiator sizes properly if rawTdp is generic 220W
        val estimatedWatts = when {
            rad != null && rad >= 420 -> maxOf(rawTdp, 350)
            rad != null && rad >= 360 -> maxOf(rawTdp, 305)
            rad != null && rad >= 280 -> maxOf(rawTdp, 275)
            rad != null && rad >= 240 -> maxOf(rawTdp, 250)
            rawTdp > 50 -> rawTdp
            spec.type == CoolerType.LIQUID -> 240
            else -> 195
        }
        val isLiquid = spec.type == CoolerType.LIQUID
        val typeTr = if (isLiquid) "${rad ?: 240}mm Sıvı Soğutma (AIO)" else "Hava Soğutucu (${spec.heightMm ?: 155}mm)"
        val typeEn = if (isLiquid) "${rad ?: 240}mm Liquid Cooler (AIO)" else "Air Cooler (${spec.heightMm ?: 155}mm)"

        return CoolerThermalProfile(
            id = item.id.value,
            name = item.displayName,
            typeLabelTr = typeTr,
            typeLabelEn = typeEn,
            maxDissipationWatts = estimatedWatts,
            ratedNoiseDba = spec.noiseDba?.takeIf { it > 0.0 } ?: if (isLiquid) 28.0 else 26.5,
            supportedSockets = spec.supportedSockets,
            isLiquid = isLiquid,
            isFromCatalog = true,
        )
    }

    /**
     * First-principles thermal resistance simulation:
     * T_die = T_ambient + P_load * (R_cooler + R_die_IHS)
     */
    fun simulate(
        cpu: CpuThermalProfile,
        cooler: CoolerThermalProfile,
        ambientTempCelsius: Int,
    ): ThermalSimulationResult {
        val safeCoolerWatts = cooler.maxDissipationWatts.coerceAtLeast(45)
        val rCooler = RATED_COOLER_DELTA_T / safeCoolerWatts.toDouble()
        val rTotal = rCooler + cpu.dieContactResistanceCw
        val maxLimit = cpu.tjMaxCelsius.coerceAtLeast(70)

        // At idle, fans spin at ~30% PWM so effective convective resistance is ~1.45x full-RPM R_cooler
        val idleTempRaw = (ambientTempCelsius + cpu.idleWatts * (rCooler * 1.45 + cpu.dieContactResistanceCw * 0.5)).roundToInt()
        val idleTemp = idleTempRaw.coerceIn(ambientTempCelsius + 4, maxLimit - 15)

        val gamingTempRaw = (ambientTempCelsius + cpu.gamingWatts * rTotal).roundToInt()
        val gamingTemp = gamingTempRaw.coerceIn(idleTemp + 5, maxLimit)

        val renderTempRaw = (ambientTempCelsius + cpu.peakPptMtpWatts * rTotal).roundToInt()
        val saturationRatio = cpu.peakPptMtpWatts.toFloat() / safeCoolerWatts.toFloat()
        val isThrottling = renderTempRaw >= maxLimit || saturationRatio > 1.05f
        val renderTemp = renderTempRaw.coerceIn(gamingTemp + 3, maxLimit)

        val socketOk = cooler.supportedSockets.isEmpty() ||
            cooler.supportedSockets.any { CheckBuildCompatibilityUseCase.socketsMatch(it, cpu.socket) }

        val gamingDba = (cooler.ratedNoiseDba - 5.5 + (cpu.gamingWatts.toDouble() / safeCoolerWatts) * 5.0)
            .coerceIn(18.0, cooler.ratedNoiseDba + 3.0)
        val stressDba = (cooler.ratedNoiseDba - 2.0 + saturationRatio * 4.5)
            .coerceIn(21.0, cooler.ratedNoiseDba + 6.5)

        return ThermalSimulationResult(
            idleTempCelsius = idleTemp,
            gamingTempCelsius = gamingTemp,
            renderTempCelsius = renderTemp,
            idleWatts = cpu.idleWatts,
            gamingWatts = cpu.gamingWatts,
            renderWatts = cpu.peakPptMtpWatts,
            headroomWatts = safeCoolerWatts - cpu.peakPptMtpWatts,
            saturationRatio = saturationRatio,
            coolerThermalResistanceCw = (rCooler * 1000.0).roundToInt() / 1000.0,
            estimatedGamingNoiseDba = (gamingDba * 10.0).roundToInt() / 10.0,
            estimatedStressNoiseDba = (stressDba * 10.0).roundToInt() / 10.0,
            isThrottling = isThrottling,
            isSocketCompatible = socketOk,
        )
    }
}
