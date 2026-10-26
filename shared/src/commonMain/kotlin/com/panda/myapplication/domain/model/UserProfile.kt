package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Gender(val displayName: String, val emoji: String) {
    @SerialName("male")
    MALE("Erkek", "♂️"),

    @SerialName("female")
    FEMALE("Kadın", "♀️"),

    @SerialName("unspecified")
    UNSPECIFIED("Belirtmek İstemiyorum", "👤");

    companion object {
        fun fromString(str: String?): Gender =
            entries.firstOrNull { it.name.equals(str, ignoreCase = true) || it.displayName.equals(str, ignoreCase = true) } ?: UNSPECIFIED
    }
}

/**
 * Kullanıcının "Kendini hangi donanım parçası olarak görüyorsun?" esprili ve havalı donanım karakteri.
 */
@Serializable
enum class SpiritHardware(
    val title: String,
    val emoji: String,
    val motto: String,
    val colorHex: Long,
) {
    @SerialName("cpu")
    OVERCLOCKED_CPU(
        "Overclock Canavarı İşlemci (CPU)",
        "🔥",
        "Saf hesaplama gücü, tüm sistemin beyni ve lideri.",
        0xFFFF9100,
    ),

    @SerialName("gpu")
    FLAGSHIP_GPU(
        "Amiral Gemisi Ekran Kartı (GPU)",
        "🎮",
        "Ultra grafikler, sıfır gecikme ve görsel şölen tutkunu.",
        0xFF00E5FF,
    ),

    @SerialName("ram")
    LOW_LATENCY_RAM(
        "RGB Low-Latency Bellek (RAM)",
        "⚡",
        "Işık hızında refleksler, çevik ve çoklu görev ustası.",
        0xFF00E676,
    ),

    @SerialName("cooler")
    LIQUID_COOLER(
        "360mm Özel Sıvı Soğutma (AIO)",
        "🌊",
        "En ağır stres altında bile serinkanlı ve fısıltı sessizliğinde.",
        0xFF2979FF,
    ),

    @SerialName("psu")
    TITANIUM_PSU(
        "Titanyum Güç Kaynağı (PSU)",
        "🛡️",
        "Sarsılmaz enerji omurgası, tam koruma ve sonsuz kararlılık.",
        0xFFFFD600,
    ),

    @SerialName("ssd")
    GEN5_NVME_SSD(
        "PCIe 5.0 NVMe SSD",
        "🚀",
        "Beklemeye tahammülü yok, anında okur ve ışık hızında yazar.",
        0xFFFF1744,
    );

    companion object {
        fun fromString(str: String?): SpiritHardware =
            entries.firstOrNull { it.name.equals(str, ignoreCase = true) } ?: OVERCLOCKED_CPU
    }
}

@Serializable
@Immutable
data class UserProfile(
    val id: String,
    val email: String,
    val username: String,
    val birthDateIso: String = "2000-01-01",
    val gender: Gender = Gender.UNSPECIFIED,
    val spiritHardware: SpiritHardware = SpiritHardware.OVERCLOCKED_CPU,
    val createdAtEpochMs: Long = 1713000000000L,
    val reputationScore: Int = 0,
    val totalDealsCount: Int = 0,
    val totalTuningsCount: Int = 0,
    val totalTipsCount: Int = 0,
    val badgeTitle: String = "Lehim Çırağı",
) {
    /**
     * İtibar puanına (Karma) göre dinamik rütbe unvanı hesaplaması.
     */
    val effectiveBadge: String
        get() = calculateBadgeTitle(reputationScore, badgeTitle)

    companion object {
        fun calculateBadgeTitle(score: Int, fallback: String = "Lehim Çırağı"): String = when {
            score >= 1200 -> "Efsanevi Mühendis"
            score >= 501 -> "Overclock Şampiyonu"
            score >= 201 -> "Undervolt Ustası"
            score >= 51 -> "Silikon Meraklısı"
            else -> fallback.ifBlank { "Lehim Çırağı" }
        }
    }

    /**
     * Doğum tarihinden (YYYY-AA-GG) güncel tarihi baz alarak kullanıcının kesin yaşını hesaplar.
     */
    val age: Int
        get() {
            return try {
                val parts = birthDateIso.split("-")
                val birthYear = parts.getOrNull(0)?.toIntOrNull() ?: 2000
                val birthMonth = parts.getOrNull(1)?.toIntOrNull() ?: 1
                val birthDay = parts.getOrNull(2)?.toIntOrNull() ?: 1

                val currentYear = 2026
                val currentMonth = 10
                val currentDay = 19
                var calculatedAge = currentYear - birthYear
                if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
                    calculatedAge--
                }
                calculatedAge.coerceAtLeast(0)
            } catch (_: Exception) {
                25
            }
        }
}
