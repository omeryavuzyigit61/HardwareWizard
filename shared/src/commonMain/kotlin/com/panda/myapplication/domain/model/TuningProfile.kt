package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
enum class TuningType(val displayName: String, val shortLabel: String, val emoji: String) {
    @SerialName("UNDERVOLT")
    UNDERVOLT("Undervolt (Voltaj Düşürme)", "Undervolt", "❄️"),

    @SerialName("OVERCLOCK")
    OVERCLOCK("Overclock (Frekans Aşırtma)", "Overclock", "🔥"),

    @SerialName("PBO")
    PBO("AMD Curve Optimizer (PBO)", "PBO Curve", "⚡"),

    @SerialName("MEMORY")
    MEMORY("RAM Timing Sıkılaştırma", "RAM Timing", "🧠");

    companion object {
        fun fromString(str: String?): TuningType {
            val firstToken = str?.split(",")?.firstOrNull()?.trim()
            return entries.firstOrNull { it.name.equals(firstToken, ignoreCase = true) } ?: UNDERVOLT
        }

        fun fromStringList(str: String?): List<TuningType> {
            if (str.isNullOrBlank()) return listOf(UNDERVOLT)
            val parsed = str.split(",")
                .mapNotNull { token ->
                    val trimmed = token.trim()
                    entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                }
                .distinct()
            return parsed.ifEmpty { listOf(UNDERVOLT) }
        }

        fun toStorageString(types: Collection<TuningType>): String {
            val distinct = types.distinct()
            return if (distinct.isEmpty()) UNDERVOLT.name else distinct.joinToString(",") { it.name }
        }
    }
}

@Serializable
enum class SiliconLotteryTier(val badgeTr: String, val badgeEn: String) {
    GOLD("Altın Silikon (Top %10)", "Golden Sample (Top 10%)"),
    SILVER("Gümüş Silikon (Top %25)", "Silver Sample (Top 25%)"),
    VERIFIED("Doğrulanmış Profil", "Verified Profile"),
}

@Serializable
enum class TuningStatus {
    PENDING,
    APPROVED,
    REJECTED;

    companion object {
        fun fromString(str: String?): TuningStatus =
            entries.firstOrNull { it.name.equals(str, ignoreCase = true) } ?: PENDING
    }
}

@Serializable
enum class TuningReactionType(val key: String, val emoji: String, val label: String) {
    HOT("HOT", "🔥", "Canavar Performans"),
    COLD("COLD", "❄️", "Buz Gibi Silikon"),
    EFFICIENT("EFFICIENT", "⚡", "Yüksek Verimlilik"),
    STABLE("STABLE", "🧪", "Kaya Gibi Stabil"),
    UNSTABLE("UNSTABLE", "⚠️", "Kararsız / Riskli");

    companion object {
        fun fromKey(key: String?): TuningReactionType =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: HOT
    }
}

@Serializable
@Immutable
data class TuningProfile(
    val id: String,
    val hardwareId: String,
    val hardwareName: String,
    val hardwareCategory: String = "CPU",
    val tuningType: TuningType = TuningType.UNDERVOLT,
    val tuningTypes: List<TuningType> = listOf(tuningType),
    val profileTitle: String,
    val languageCode: String = "tr",
    val voltageOffsetMv: Double? = null,
    val clockMhz: Double? = null,
    val powerLimitWatt: Double? = null,
    val tempBeforeC: Double? = null,
    val tempAfterC: Double? = null,
    val scoreBefore: Double? = null,
    val scoreAfter: Double? = null,
    val benchmarkName: String? = null,
    val proofImageUrl: String? = null,
    val guideNotes: String? = null,
    val userId: String,
    val userDisplayName: String,
    val userBadge: String = "Lehim Çırağı",
    val status: TuningStatus = TuningStatus.PENDING,
    val rejectionReason: String? = null,
    val reactionHotCount: Int = 0,
    val reactionColdCount: Int = 0,
    val reactionEfficientCount: Int = 0,
    val reactionStableCount: Int = 0,
    val reactionUnstableCount: Int = 0,
    val commentsCount: Int = 0,
    val createdAtEpochMs: Long = 0L,
    val motherboardName: String? = null,
    val coolerName: String? = null,
    val ramSpec: String? = null,
    val secondaryHardware: String? = null,
) {
    val effectiveTuningTypes: List<TuningType>
        get() = tuningTypes.ifEmpty { listOf(tuningType) }

    val tempDelta: Double?
        get() = if (tempBeforeC != null && tempAfterC != null) tempAfterC - tempBeforeC else null

    val scoreDelta: Double?
        get() = if (scoreBefore != null && scoreAfter != null) scoreAfter - scoreBefore else null

    val scoreDeltaPercent: Double?
        get() = if (scoreBefore != null && scoreBefore > 0.0 && scoreAfter != null) {
            ((scoreAfter - scoreBefore) / scoreBefore) * 100.0
        } else null

    /**
     * Silikon Piyangosu (Silicon Lottery) kalite sınıfı hesaplayıcısı.
     * Voltaj ofseti, sıcaklık düşüşü, performans kazanımı ve topluluk stabilite onaylarını değerlendirir.
     */
    val siliconLotteryTier: SiliconLotteryTier
        get() {
            val absMv = abs(voltageOffsetMv ?: 0.0)
            val tempDrop = -(tempDelta ?: 0.0)
            val perfGain = scoreDeltaPercent ?: 0.0
            val positiveReactions = reactionHotCount + reactionColdCount + reactionEfficientCount + reactionStableCount

            var qualityPoints = 0
            if (absMv >= 28.0) qualityPoints += 2 else if (absMv >= 15.0) qualityPoints += 1
            if (tempDrop >= 10.0) qualityPoints += 2 else if (tempDrop >= 5.0) qualityPoints += 1
            if (perfGain >= 5.0) qualityPoints += 2 else if (perfGain >= 1.5) qualityPoints += 1
            if (!proofImageUrl.isNullOrBlank()) qualityPoints += 1
            if (positiveReactions >= 3 && reactionUnstableCount == 0) qualityPoints += 1

            return when {
                qualityPoints >= 4 -> SiliconLotteryTier.GOLD
                qualityPoints >= 2 -> SiliconLotteryTier.SILVER
                else -> SiliconLotteryTier.VERIFIED
            }
        }
}

@Serializable
@Immutable
data class TuningComment(
    val id: String,
    val tuningId: String,
    val userId: String,
    val userDisplayName: String,
    val userBadge: String = "Lehim Çırağı",
    val userReputation: Int = 0,
    val commentText: String,
    val createdAtEpochMs: Long = 0L,
)

@Serializable
@Immutable
data class HardwareProTip(
    val id: String,
    val hardwareId: String,
    val hardwareName: String,
    val userId: String,
    val userDisplayName: String,
    val userBadge: String = "Lehim Çırağı",
    val tipText: String,
    val upvotesCount: Int = 0,
    val isUpvotedByMe: Boolean = false,
    val createdAtEpochMs: Long = 0L,
)

@Serializable
@Immutable
data class PriceDropAlert(
    val id: String,
    val userId: String,
    val hardwareId: String,
    val hardwareName: String,
    val targetPriceTry: Double,
    val onlyRetailCampaign: Boolean = true,
    val isTriggered: Boolean = false,
    val createdAtEpochMs: Long = 0L,
)
