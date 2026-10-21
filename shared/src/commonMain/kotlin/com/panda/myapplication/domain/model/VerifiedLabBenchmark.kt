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
)

object VerifiedLabBenchmarks {

    val cpuCs2Test = VerifiedLabGameTest(
        id = "cpu_cs2_1080p_low",
        gameTitle = "Counter-Strike 2",
        resolutionSettings = "1080P / Düşük Detay (Rekabetçi)",
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
        resolutionSettings = "1080P / Ultra Ayarlar (Ray Tracing Kapalı)",
        rigSummary = "RTX 5090 32GB • 32GB DDR5 6000MTs CL28",
        rigGpuOrCpu = "NVIDIA GeForce RTX 5090 32GB",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = true,
        entries = listOf(
            VerifiedLabTestEntry("9800X3D", "AMD Ryzen 7 9800X3D", "120W", true, 242.0, 184.0, listOf("9800X3D")),
            VerifiedLabTestEntry("7800X3D", "AMD Ryzen 7 7800X3D", "65W", true, 224.0, 172.0, listOf("7800X3D")),
            VerifiedLabTestEntry("9700X", "AMD Ryzen 7 9700X", "105W", true, 215.0, 158.0, listOf("9700X")),
            VerifiedLabTestEntry("285K", "Intel Core Ultra 9 285K", "125W", false, 208.0, 152.0, listOf("285K", "Ultra 9 285K")),
            VerifiedLabTestEntry("14900K", "Intel Core i9-14900K", "253W", false, 204.0, 149.0, listOf("14900K", "14900KF")),
            VerifiedLabTestEntry("7500X3D", "AMD Ryzen 5 7500X3D", "65W", true, 198.0, 155.0, listOf("7500X3D")),
            VerifiedLabTestEntry("9500F", "AMD Ryzen 5 9500F", "65W", true, 188.0, 140.0, listOf("9500F")),
            VerifiedLabTestEntry("265F", "Intel Core Ultra 7 265F", "65W", false, 182.0, 135.0, listOf("265F", "265K")),
            VerifiedLabTestEntry("7500F", "AMD Ryzen 5 7500F", "65W", true, 172.0, 128.0, listOf("7500F")),
            VerifiedLabTestEntry("245KF", "Intel Core Ultra 5 245KF", "125W", false, 170.0, 124.0, listOf("245KF", "245K")),
            VerifiedLabTestEntry("5800X3D", "AMD Ryzen 7 5800X3D", "105W", true, 168.0, 130.0, listOf("5800X3D")),
            VerifiedLabTestEntry("5700X3D", "AMD Ryzen 7 5700X3D", "105W", true, 158.0, 122.0, listOf("5700X3D")),
            VerifiedLabTestEntry("5600", "AMD Ryzen 5 5600", "65W", true, 132.0, 95.0, listOf("Ryzen 5 5600", "5600X", "5600")),
        ),
    )

    val gpuCyberpunkTest = VerifiedLabGameTest(
        id = "gpu_cyberpunk_1440p_ultra",
        gameTitle = "Cyberpunk 2077",
        resolutionSettings = "1440P / Ultra Kalite (Saf Raster)",
        rigSummary = "Ryzen 7 9800X3D • 32GB DDR5 6000MTs • Sıfır CPU Darboğazı",
        rigGpuOrCpu = "AMD Ryzen 7 9800X3D",
        rigRam = "32GB DDR5 6000MTs CL28",
        isCpuTest = false,
        entries = listOf(
            VerifiedLabTestEntry("RTX 5090", "NVIDIA GeForce RTX 5090", "600W", false, 168.0, 134.0, listOf("5090", "RTX 5090")),
            VerifiedLabTestEntry("RTX 5080", "NVIDIA GeForce RTX 5080", "400W", false, 132.0, 106.0, listOf("5080", "RTX 5080")),
            VerifiedLabTestEntry("RTX 4090", "NVIDIA GeForce RTX 4090", "450W", false, 122.0, 98.0, listOf("4090", "RTX 4090")),
            VerifiedLabTestEntry("RX 7900 XTX", "AMD Radeon RX 7900 XTX", "355W", true, 106.0, 84.0, listOf("7900 XTX", "7900XTX")),
            VerifiedLabTestEntry("RTX 4080 Super", "NVIDIA GeForce RTX 4080 Super", "320W", false, 102.0, 82.0, listOf("4080 Super", "4080")),
            VerifiedLabTestEntry("RTX 4070 Ti Super", "NVIDIA GeForce RTX 4070 Ti Super", "285W", false, 89.0, 72.0, listOf("4070 Ti Super", "4070 Ti")),
            VerifiedLabTestEntry("RX 7900 XT", "AMD Radeon RX 7900 XT", "315W", true, 88.0, 70.0, listOf("7900 XT")),
            VerifiedLabTestEntry("RTX 4070 Super", "NVIDIA GeForce RTX 4070 Super", "220W", false, 79.0, 63.0, listOf("4070 Super")),
            VerifiedLabTestEntry("RX 7800 XT", "AMD Radeon RX 7800 XT", "263W", true, 74.0, 59.0, listOf("7800 XT")),
            VerifiedLabTestEntry("RTX 4070", "NVIDIA GeForce RTX 4070", "200W", false, 69.0, 55.0, listOf("RTX 4070")),
            VerifiedLabTestEntry("RX 7700 XT", "AMD Radeon RX 7700 XT", "245W", true, 62.0, 49.0, listOf("7700 XT")),
            VerifiedLabTestEntry("RTX 4060 Ti", "NVIDIA GeForce RTX 4060 Ti", "160W", false, 52.0, 41.0, listOf("4060 Ti")),
            VerifiedLabTestEntry("RX 7600 XT", "AMD Radeon RX 7600 XT", "190W", true, 45.0, 36.0, listOf("7600 XT")),
            VerifiedLabTestEntry("RTX 4060", "NVIDIA GeForce RTX 4060", "115W", false, 42.0, 33.0, listOf("RTX 4060")),
            VerifiedLabTestEntry("RX 6600", "AMD Radeon RX 6600", "132W", true, 34.0, 27.0, listOf("RX 6600")),
        ),
    )

    val gpuCs2Test = VerifiedLabGameTest(
        id = "gpu_cs2_1440p_very_high",
        gameTitle = "Counter-Strike 2",
        resolutionSettings = "1440P / Çok Yüksek Grafik",
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

    fun getLabTestsForItem(item: HardwareItem): List<VerifiedLabGameTest> {
        return when (item.category) {
            HardwareCategory.CPU -> listOf(cpuCs2Test, cpuCyberpunkTest)
            HardwareCategory.GPU -> listOf(gpuCyberpunkTest, gpuCs2Test)
            else -> emptyList()
        }
    }

    fun hasVerifiedLabTest(item: HardwareItem): Boolean {
        val tests = getLabTestsForItem(item)
        return tests.any { test -> test.entries.any { it.matchesItem(item) } }
    }
}
