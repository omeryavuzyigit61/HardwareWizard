package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class VerifiedLabTestEntry(
    val modelName: String,
    val fullModelName: String,
    val wattLabel: String,
    val isAmd: Boolean,
    val avgFps: Double,
    val minFps: Double,
    val modelKeywords: List<String> = emptyList(),
    val maxFps: Double? = null,
    val low1PctFps: Double? = null,
) {
    fun matchesItem(item: HardwareItem): Boolean {
        val target = "${item.brand} ${item.model}".lowercase()
        return modelKeywords.any { kw ->
            val lk = kw.lowercase()
            target.contains(lk)
        } || target.contains(modelName.lowercase())
    }
}

@Immutable
@Serializable
data class VerifiedLabGameTest(
    val id: String,
    val gameTitle: String,
    val resolutionSettings: String,
    val rigSummary: String,
    val rigGpuOrCpu: String,
    val rigRam: String,
    val isCpuTest: Boolean,
    val entries: List<VerifiedLabTestEntry>,
) {
    val cleanSettings: String
        get() = resolutionSettings
            .replace("(", "")
            .replace(")", "")
            .replace("/", "•")
            .trim()
}

object VerifiedLabBenchmarks {

    private var remoteTests: List<VerifiedLabGameTest>? = null

    fun updateRemoteTests(tests: List<VerifiedLabGameTest>) {
        if (tests.isNotEmpty()) {
            remoteTests = tests
        }
    }

    // ------------------ CPU LABORATORY TESTS (RTX 5090 Reference Rig) ------------------

    val cpuCs2Test = VerifiedLabGameTest(
        id = "cpu_cs2_1080p_low",
        gameTitle = "Counter-Strike 2",
        resolutionSettings = "1080P • Düşük Detay (Rekabetçi)",
        rigSummary = "RTX 5090 32GB • DDR5 Hızlı Bellek • Sıfır GPU Darboğazı",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "Intel: DDR5 6400MTs CL30 / AMD: DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 926.0, 318.0, listOf("9800X3D")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 897.0, 260.0, listOf("9700X")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 871.0, 303.0, listOf("7800X3D")),
            VerifiedLabTestEntry("9500F", "AMD Ryzen 5 9500F", "65W", true, 787.0, 246.0, listOf("9500F")),
            VerifiedLabTestEntry("7500X3D", "AMD Ryzen 5 7500X3D", "65W", true, 778.0, 274.0, listOf("7500X3D")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 735.0, 230.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 726.0, 225.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 696.0, 232.0, listOf("7500F")),
            VerifiedLabTestEntry("5800X3D", "AMD Ryzen 7 5800X3D", "105W", true, 680.0, 240.0, listOf("5800X3D")),
            VerifiedLabTestEntry("13600K", "Intel Core i5-13600K", "125W", false, 645.0, 215.0, listOf("13600K", "13600KF")),
            VerifiedLabTestEntry("5700X3D", "AMD Ryzen 7 5700X3D", "105W", true, 640.0, 228.0, listOf("5700X3D")),
            VerifiedLabTestEntry("265F", "Intel Core Ultra 7 265F", "65W", false, 620.0, 212.0, listOf("265F", "265K", "Ultra 7 265")),
            VerifiedLabTestEntry("245KF", "Intel Core Ultra 5 245KF", "125W", false, 592.0, 202.0, listOf("245KF", "245K", "Ultra 5 245")),
            VerifiedLabTestEntry("225F", "Intel Core Ultra 5 225F", "65W", false, 525.0, 189.0, listOf("225F", "225", "Ultra 3 225")),
            VerifiedLabTestEntry("5600", "AMD Ryzen 5 5600", "65W", true, 510.0, 175.0, listOf("Ryzen 5 5600", "5600X", "5600")),
        ),
    )

    val cpuCyberpunkTest = VerifiedLabGameTest(
        id = "cpu_cyberpunk_1080p_ultra",
        gameTitle = "Cyberpunk 2077",
        resolutionSettings = "1080P • Ultra Ayarlar (RT Kapalı)",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 242.0, 184.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 224.0, 172.0, listOf("7800X3D")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 215.0, 158.0, listOf("9700X")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 218.0, 155.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 208.0, 152.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500X3D", "AMD Ryzen 5 7500X3D", "65W", true, 198.0, 150.0, listOf("7500X3D")),
            VerifiedLabTestEntry("9500F", "AMD Ryzen 5 9500F", "65W", true, 188.0, 142.0, listOf("9500F")),
            VerifiedLabTestEntry("265F", "Intel Core Ultra 7 265F", "65W", false, 182.0, 135.0, listOf("265F", "265K")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 172.0, 130.0, listOf("7500F")),
            VerifiedLabTestEntry("245KF", "Intel Core Ultra 5 245KF", "125W", false, 170.0, 124.0, listOf("245KF", "245K")),
            VerifiedLabTestEntry("5800X3D", "AMD Ryzen 7 5800X3D", "105W", true, 168.0, 128.0, listOf("5800X3D")),
            VerifiedLabTestEntry("13600K", "Intel Core i5-13600K", "125W", false, 165.0, 122.0, listOf("13600K", "13600KF")),
            VerifiedLabTestEntry("5700X3D", "AMD Ryzen 7 5700X3D", "105W", true, 158.0, 122.0, listOf("5700X3D")),
        ),
    )

    val cpuWukongTest = VerifiedLabGameTest(
        id = "cpu_wukong_1080p_high",
        gameTitle = "Black Myth: Wukong",
        resolutionSettings = "1080P • Çok Yüksek Grafik",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 198.0, 156.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 185.0, 148.0, listOf("7800X3D")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 182.0, 140.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 178.0, 136.0, listOf("9700X")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 174.0, 134.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500X3D", "AMD Ryzen 5 7500X3D", "65W", true, 168.0, 132.0, listOf("7500X3D")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 152.0, 118.0, listOf("7500F")),
            VerifiedLabTestEntry("13600K", "Intel Core i5-13600K", "125W", false, 148.0, 114.0, listOf("13600K", "13600KF")),
        ),
    )

    val cpuBaldursGateTest = VerifiedLabGameTest(
        id = "cpu_baldurs_gate_3_1080p_ultra",
        gameTitle = "Baldur's Gate 3",
        resolutionSettings = "1080P • Ultra (Act 3 Şehir İçi)",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 178.0, 142.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 164.0, 130.0, listOf("7800X3D")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 152.0, 116.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 148.0, 114.0, listOf("9700X")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 144.0, 110.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("5800X3D", "AMD Ryzen 7 5800X3D", "105W", true, 136.0, 108.0, listOf("5800X3D")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 126.0, 94.0, listOf("7500F")),
            VerifiedLabTestEntry("13600K", "Intel Core i5-13600K", "125W", false, 122.0, 90.0, listOf("13600K", "13600KF")),
        ),
    )

    val cpuValorantTest = VerifiedLabGameTest(
        id = "cpu_valorant_1080p_low",
        gameTitle = "Valorant",
        resolutionSettings = "1080P • Düşük (Rekabetçi)",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 1050.0, 460.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 980.0, 430.0, listOf("7800X3D")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 940.0, 390.0, listOf("9700X")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 890.0, 370.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 860.0, 355.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500X3D", "AMD Ryzen 5 7500X3D", "65W", true, 870.0, 380.0, listOf("7500X3D")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 790.0, 320.0, listOf("7500F")),
            VerifiedLabTestEntry("13600K", "Intel Core i5-13600K", "125W", false, 740.0, 305.0, listOf("13600K", "13600KF")),
        ),
    )

    val cpuRdr2Test = VerifiedLabGameTest(
        id = "cpu_rdr2_1080p_ultra",
        gameTitle = "Red Dead Redemption 2",
        resolutionSettings = "1080P • Ultra Ayarlar",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 238.0, 176.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 222.0, 164.0, listOf("7800X3D")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 214.0, 150.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 210.0, 152.0, listOf("9700X")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 202.0, 146.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 180.0, 130.0, listOf("7500F")),
        ),
    )

    val cpuForzaTest = VerifiedLabGameTest(
        id = "cpu_forza5_1080p_extreme",
        gameTitle = "Forza Horizon 5",
        resolutionSettings = "1080P • Ekstrem Ayarlar",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 288.0, 230.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 270.0, 218.0, listOf("7800X3D")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 255.0, 198.0, listOf("9700X")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 252.0, 194.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 244.0, 190.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 215.0, 168.0, listOf("7500F")),
        ),
    )

    val cpuAlanWakeTest = VerifiedLabGameTest(
        id = "cpu_alan_wake_2_1080p_high",
        gameTitle = "Alan Wake 2",
        resolutionSettings = "1080P • Yüksek Ayarlar",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 155.0, 118.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 146.0, 110.0, listOf("7800X3D")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 142.0, 102.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 138.0, 100.0, listOf("9700X")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 135.0, 98.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 120.0, 86.0, listOf("7500F")),
        ),
    )

    val cpuWarzoneTest = VerifiedLabGameTest(
        id = "cpu_warzone_1080p_comp",
        gameTitle = "Call of Duty: Warzone",
        resolutionSettings = "1080P • Rekabetçi Dengeli",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 345.0, 225.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 320.0, 208.0, listOf("7800X3D")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 295.0, 188.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 285.0, 180.0, listOf("9700X")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 275.0, 172.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 245.0, 155.0, listOf("7500F")),
        ),
    )

    val cpuApexTest = VerifiedLabGameTest(
        id = "cpu_apex_1080p_high",
        gameTitle = "Apex Legends",
        resolutionSettings = "1080P • Yüksek Ayarlar",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 298.0, 210.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 282.0, 198.0, listOf("7800X3D")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 265.0, 182.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 260.0, 178.0, listOf("9700X")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 250.0, 170.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 225.0, 150.0, listOf("7500F")),
        ),
    )

    val cpuTests = listOf(
        cpuCs2Test,
        cpuCyberpunkTest,
        cpuWukongTest,
        cpuBaldursGateTest,
        cpuValorantTest,
        cpuRdr2Test,
        cpuForzaTest,
        cpuAlanWakeTest,
        cpuWarzoneTest,
        cpuApexTest,
    )

    // ------------------ GPU LABORATORY TESTS (Ryzen 7 9800X3D Reference Rig) ------------------

    val gpuCyberpunkTest = VerifiedLabGameTest(
        id = "gpu_cyberpunk_1440p_ultra",
        gameTitle = "Cyberpunk 2077",
        resolutionSettings = "1440P • Ultra Ayarlar (Ray Tracing Kapalı)",
        rigSummary = "Ryzen 7 9800X3D • 32GB DDR5 6000MTs • Sıfır CPU Darboğazı",
        rigGpuOrCpu = "AMD Ryzen 7 9800X3D",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = false,
        entries = listOf(
            VerifiedLabTestEntry("RTX 5090", "NVIDIA GeForce RTX 5090", "600W", false, 210.0, 165.0, listOf("5090", "RTX 5090")),
            VerifiedLabTestEntry("RTX 5080", "NVIDIA GeForce RTX 5080", "400W", false, 172.0, 138.0, listOf("5080", "RTX 5080")),
            VerifiedLabTestEntry("RTX 4090", "NVIDIA GeForce RTX 4090", "450W", false, 156.0, 125.0, listOf("4090", "RTX 4090")),
            VerifiedLabTestEntry("RX 7900 XTX", "AMD Radeon RX 7900 XTX", "355W", true, 142.0, 114.0, listOf("7900 XTX", "7900XTX")),
            VerifiedLabTestEntry("RTX 4080 Super", "NVIDIA GeForce RTX 4080 Super", "320W", false, 136.0, 108.0, listOf("4080 Super", "4080")),
            VerifiedLabTestEntry("RX 7900 XT", "AMD Radeon RX 7900 XT", "315W", true, 122.0, 96.0, listOf("7900 XT", "7900XT")),
            VerifiedLabTestEntry("RTX 4070 Ti Super", "NVIDIA GeForce RTX 4070 Ti Super", "285W", false, 118.0, 94.0, listOf("4070 Ti Super", "4070 Ti")),
            VerifiedLabTestEntry("RX 7800 XT", "AMD Radeon RX 7800 XT", "263W", true, 104.0, 82.0, listOf("7800 XT")),
            VerifiedLabTestEntry("RTX 4070 Super", "NVIDIA GeForce RTX 4070 Super", "220W", false, 102.0, 81.0, listOf("4070 Super")),
            VerifiedLabTestEntry("RTX 4070", "NVIDIA GeForce RTX 4070", "200W", false, 88.0, 70.0, listOf("RTX 4070")),
            VerifiedLabTestEntry("RX 7700 XT", "AMD Radeon RX 7700 XT", "245W", true, 86.0, 68.0, listOf("7700 XT")),
            VerifiedLabTestEntry("RTX 4060 Ti", "NVIDIA GeForce RTX 4060 Ti", "160W", false, 68.0, 54.0, listOf("4060 Ti")),
            VerifiedLabTestEntry("RX 7600 XT", "AMD Radeon RX 7600 XT", "190W", true, 58.0, 46.0, listOf("7600 XT")),
            VerifiedLabTestEntry("RTX 4060", "NVIDIA GeForce RTX 4060", "115W", false, 54.0, 42.0, listOf("RTX 4060")),
            VerifiedLabTestEntry("RX 6600", "AMD Radeon RX 6600", "132W", true, 42.0, 32.0, listOf("RX 6600")),
        ),
    )

    val gpuCs2Test = VerifiedLabGameTest(
        id = "gpu_cs2_1440p_very_high",
        gameTitle = "Counter-Strike 2",
        resolutionSettings = "1440P • Çok Yüksek Grafik",
        rigSummary = "Ryzen 7 9800X3D • 32GB DDR5 6000MTs • Sıfır CPU Darboğazı",
        rigGpuOrCpu = "AMD Ryzen 7 9800X3D",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = false,
        entries = listOf(
            VerifiedLabTestEntry("RTX 5090", "NVIDIA GeForce RTX 5090", "600W", false, 640.0, 310.0, listOf("5090", "RTX 5090")),
            VerifiedLabTestEntry("RTX 5080", "NVIDIA GeForce RTX 5080", "400W", false, 560.0, 285.0, listOf("5080", "RTX 5080")),
            VerifiedLabTestEntry("RTX 4090", "NVIDIA GeForce RTX 4090", "450W", false, 520.0, 270.0, listOf("4090", "RTX 4090")),
            VerifiedLabTestEntry("RX 7900 XTX", "AMD Radeon RX 7900 XTX", "355W", true, 480.0, 255.0, listOf("7900 XTX", "7900XTX")),
            VerifiedLabTestEntry("RTX 4080 Super", "NVIDIA GeForce RTX 4080 Super", "320W", false, 470.0, 250.0, listOf("4080 Super", "4080")),
            VerifiedLabTestEntry("RTX 4070 Ti Super", "NVIDIA GeForce RTX 4070 Ti Super", "285W", false, 420.0, 230.0, listOf("4070 Ti Super", "4070 Ti")),
            VerifiedLabTestEntry("RX 7800 XT", "AMD Radeon RX 7800 XT", "263W", true, 380.0, 215.0, listOf("7800 XT")),
            VerifiedLabTestEntry("RTX 4070 Super", "NVIDIA GeForce RTX 4070 Super", "220W", false, 375.0, 210.0, listOf("4070 Super")),
            VerifiedLabTestEntry("RTX 4060 Ti", "NVIDIA GeForce RTX 4060 Ti", "160W", false, 290.0, 180.0, listOf("4060 Ti")),
            VerifiedLabTestEntry("RTX 4060", "NVIDIA GeForce RTX 4060", "115W", false, 240.0, 155.0, listOf("RTX 4060")),
        ),
    )

    val gpuWukongTest = VerifiedLabGameTest(
        id = "gpu_wukong_1440p_cinematic",
        gameTitle = "Black Myth: Wukong",
        resolutionSettings = "1440P • Sinematik Ayarlar",
        rigSummary = "Ryzen 7 9800X3D • 32GB DDR5 6000MTs • Sıfır CPU Darboğazı",
        rigGpuOrCpu = "AMD Ryzen 7 9800X3D",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = false,
        entries = listOf(
            VerifiedLabTestEntry("RTX 5090", "NVIDIA GeForce RTX 5090", "600W", false, 148.0, 118.0, listOf("5090", "RTX 5090")),
            VerifiedLabTestEntry("RTX 5080", "NVIDIA GeForce RTX 5080", "400W", false, 118.0, 94.0, listOf("5080", "RTX 5080")),
            VerifiedLabTestEntry("RTX 4090", "NVIDIA GeForce RTX 4090", "450W", false, 105.0, 84.0, listOf("4090", "RTX 4090")),
            VerifiedLabTestEntry("RTX 4080 Super", "NVIDIA GeForce RTX 4080 Super", "320W", false, 88.0, 70.0, listOf("4080 Super", "4080")),
            VerifiedLabTestEntry("RX 7900 XTX", "AMD Radeon RX 7900 XTX", "355W", true, 84.0, 66.0, listOf("7900 XTX", "7900XTX")),
            VerifiedLabTestEntry("RTX 4070 Ti Super", "NVIDIA GeForce RTX 4070 Ti Super", "285W", false, 76.0, 60.0, listOf("4070 Ti Super", "4070 Ti")),
            VerifiedLabTestEntry("RTX 4070 Super", "NVIDIA GeForce RTX 4070 Super", "220W", false, 66.0, 52.0, listOf("4070 Super")),
            VerifiedLabTestEntry("RX 7800 XT", "AMD Radeon RX 7800 XT", "263W", true, 62.0, 48.0, listOf("7800 XT")),
            VerifiedLabTestEntry("RTX 4060", "NVIDIA GeForce RTX 4060", "115W", false, 36.0, 28.0, listOf("RTX 4060")),
        ),
    )

    val gpuForzaTest = VerifiedLabGameTest(
        id = "gpu_forza5_1440p_extreme",
        gameTitle = "Forza Horizon 5",
        resolutionSettings = "1440P • Ekstrem Ayarlar",
        rigSummary = "Ryzen 7 9800X3D • 32GB DDR5 6000MTs • Sıfır CPU Darboğazı",
        rigGpuOrCpu = "AMD Ryzen 7 9800X3D",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = false,
        entries = listOf(
            VerifiedLabTestEntry("RTX 5090", "NVIDIA GeForce RTX 5090", "600W", false, 275.0, 235.0, listOf("5090", "RTX 5090")),
            VerifiedLabTestEntry("RTX 5080", "NVIDIA GeForce RTX 5080", "400W", false, 228.0, 195.0, listOf("5080", "RTX 5080")),
            VerifiedLabTestEntry("RTX 4090", "NVIDIA GeForce RTX 4090", "450W", false, 212.0, 180.0, listOf("4090", "RTX 4090")),
            VerifiedLabTestEntry("RX 7900 XTX", "AMD Radeon RX 7900 XTX", "355W", true, 202.0, 172.0, listOf("7900 XTX", "7900XTX")),
            VerifiedLabTestEntry("RTX 4080 Super", "NVIDIA GeForce RTX 4080 Super", "320W", false, 184.0, 156.0, listOf("4080 Super", "4080")),
            VerifiedLabTestEntry("RTX 4070 Super", "NVIDIA GeForce RTX 4070 Super", "220W", false, 148.0, 125.0, listOf("4070 Super")),
            VerifiedLabTestEntry("RX 7800 XT", "AMD Radeon RX 7800 XT", "263W", true, 142.0, 120.0, listOf("7800 XT")),
            VerifiedLabTestEntry("RTX 4060", "NVIDIA GeForce RTX 4060", "115W", false, 85.0, 72.0, listOf("RTX 4060")),
        ),
    )

    val gpuRdr2Test = VerifiedLabGameTest(
        id = "gpu_rdr2_1440p_ultra",
        gameTitle = "Red Dead Redemption 2",
        resolutionSettings = "1440P • Ultra Ayarlar",
        rigSummary = "Ryzen 7 9800X3D • 32GB DDR5 6000MTs • Sıfır CPU Darboğazı",
        rigGpuOrCpu = "AMD Ryzen 7 9800X3D",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = false,
        entries = listOf(
            VerifiedLabTestEntry("RTX 5090", "NVIDIA GeForce RTX 5090", "600W", false, 215.0, 175.0, listOf("5090", "RTX 5090")),
            VerifiedLabTestEntry("RTX 5080", "NVIDIA GeForce RTX 5080", "400W", false, 178.0, 145.0, listOf("5080", "RTX 5080")),
            VerifiedLabTestEntry("RTX 4090", "NVIDIA GeForce RTX 4090", "450W", false, 162.0, 132.0, listOf("4090", "RTX 4090")),
            VerifiedLabTestEntry("RX 7900 XTX", "AMD Radeon RX 7900 XTX", "355W", true, 150.0, 122.0, listOf("7900 XTX", "7900XTX")),
            VerifiedLabTestEntry("RTX 4080 Super", "NVIDIA GeForce RTX 4080 Super", "320W", false, 140.0, 114.0, listOf("4080 Super", "4080")),
            VerifiedLabTestEntry("RTX 4070 Super", "NVIDIA GeForce RTX 4070 Super", "220W", false, 110.0, 90.0, listOf("4070 Super")),
            VerifiedLabTestEntry("RX 7800 XT", "AMD Radeon RX 7800 XT", "263W", true, 105.0, 85.0, listOf("7800 XT")),
            VerifiedLabTestEntry("RTX 4060", "NVIDIA GeForce RTX 4060", "115W", false, 62.0, 50.0, listOf("RTX 4060")),
        ),
    )

    val gpuTests = listOf(
        gpuCyberpunkTest,
        gpuCs2Test,
        gpuWukongTest,
        gpuForzaTest,
        gpuRdr2Test,
    )

    val allEmbeddedTests: List<VerifiedLabGameTest> = cpuTests + gpuTests

    fun getAllTests(): List<VerifiedLabGameTest> {
        return remoteTests ?: allEmbeddedTests
    }

    fun getLabTestsForItem(item: HardwareItem): List<VerifiedLabGameTest> {
        val all = getAllTests()
        val isCpu = item.category == HardwareCategory.CPU
        val relevant = all.filter { it.isCpuTest == isCpu }
        return if (relevant.isNotEmpty()) relevant else (if (isCpu) cpuTests else gpuTests)
    }

    fun hasVerifiedLabTest(item: HardwareItem): Boolean {
        val tests = getLabTestsForItem(item)
        return tests.any { test -> test.entries.any { it.matchesItem(item) } }
    }
}
