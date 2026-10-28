package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable

enum class ReportTargetType(val titleTr: String, val titleEn: String) {
    BATTLESTATION("Battlestation Kurulumu", "Battlestation Setup"),
    TUNING_PROFILE("OC/UV Tuning Profili", "Tuning Profile"),
    PRICE_DEAL("Fiyat Fırsatı", "Price Deal"),
    COMMENT("Topluluk Yorumu", "Comment");

    fun localizedTitle(isTr: Boolean): String = if (isTr) titleTr else titleEn
}

enum class ReportReason(val titleTr: String, val titleEn: String) {
    OFFENSIVE_OR_INAPPROPRIATE("Uygunsuz / Rahatsız Edici İçerik", "Offensive or Inappropriate"),
    MISLEADING_OR_FAKE("Yanıltıcı / Sahte Donanım veya Test", "Misleading or Fake Hardware/Test"),
    DANGEROUS_VOLTAGE("Tehlikeli / Donanım Yakma Riski (Aşırı Voltaj)", "Dangerous Voltage / Hardware Damage Risk"),
    SPAM_OR_AD("Spam / Reklam / Alakasız Gönderi", "Spam / Promotion / Irrelevant"),
    STOLEN_CONTENT("Çalıntı / Başkasına Ait Fotoğraf", "Stolen / Copyrighted Photo"),
    AUTHOR_RETRACTION("Kendi Paylaşımımı Geri Çekme / Raporlama Talebi", "Author Self-Report / Retraction Request"),
    OTHER("Diğer İhlal", "Other Violation");

    fun localizedTitle(isTr: Boolean): String = if (isTr) titleTr else titleEn
}

enum class ReportStatus {
    PENDING,
    RESOLVED_REMOVED,
    RESOLVED_DISMISSED,
}

@Immutable
data class ContentReport(
    val id: String,
    val targetType: ReportTargetType,
    val targetId: String,
    val targetTitle: String,
    val reportedUserId: String,
    val reportedUserDisplayName: String,
    val reporterUserId: String,
    val reporterUserDisplayName: String,
    val isSelfReport: Boolean,
    val reason: ReportReason,
    val details: String = "",
    val timestampMillis: Long = 0L,
    val status: ReportStatus = ReportStatus.PENDING,
)
