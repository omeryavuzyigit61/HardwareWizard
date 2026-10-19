package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Topluluk fiyat bildirimi kategorisi / işlem türü.
 */
@Serializable
enum class CommunityDealType(
    val id: String,
    val title: String,
    val emoji: String,
    val badgeLabel: String,
) {
    @SerialName("own_purchase")
    OWN_PURCHASE("own_purchase", "Kendi Aldığım Fiyat", "🛍️", "Satın Alım"),

    @SerialName("second_hand")
    SECOND_HAND("second_hand", "İkinci El", "🔄", "2. El"),

    @SerialName("campaign")
    DISCOUNT_CAMPAIGN("campaign", "Kampanya / İndirim", "🔥", "Fırsat"),

    @SerialName("foreign")
    FOREIGN_PURCHASE("foreign", "Yurtdışı Fiyatı (YD)", "✈️", "Yurtdışı");

    companion object {
        fun fromId(id: String): CommunityDealType =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: OWN_PURCHASE
    }
}

/**
 * Topluluk fiyat bildiriminin moderasyon durumu.
 */
@Serializable
enum class DealStatus(val displayName: String) {
    @SerialName("PENDING")
    PENDING("Onay Bekliyor"),

    @SerialName("APPROVED")
    APPROVED("Onaylandı"),

    @SerialName("REJECTED")
    REJECTED("Reddedildi");

    companion object {
        fun fromString(str: String): DealStatus =
            entries.firstOrNull { it.name.equals(str, ignoreCase = true) } ?: PENDING
    }
}

/**
 * Ürün detayında Binance trade tarzı canlı defterde listelenen ve Supabase'de tutulan topluluk fiyat kaydı.
 */
@Serializable
@Immutable
data class CommunityDeal(
    val id: String,
    val hardwareId: String,
    val hardwareName: String,
    val priceTry: Double,
    val dealType: CommunityDealType,
    val note: String? = null,
    val proofUrl: String? = null,
    val userDisplayName: String = "Topluluk Üyesi",
    val userEmail: String? = null,
    val createdAtEpochMs: Long,
    val status: DealStatus = DealStatus.PENDING,
    val reviewNote: String? = null,
    val reviewedBy: String? = null,
    val reviewedAtEpochMs: Long? = null,
)
