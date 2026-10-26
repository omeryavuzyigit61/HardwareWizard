package com.panda.myapplication.domain.repository

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.HardwareProTip
import com.panda.myapplication.domain.model.PriceDropAlert
import com.panda.myapplication.domain.model.TuningComment
import com.panda.myapplication.domain.model.TuningProfile
import com.panda.myapplication.domain.model.TuningReactionType
import kotlinx.coroutines.flow.Flow

interface TuningVaultRepository {
    /** Belirli bir donanıma ait onaylanmış OC/UV ayarlarını dinler. */
    fun observeTuningsForHardware(hardwareId: String): Flow<List<TuningProfile>>

    /** Topluluktaki tüm onaylanmış OC/UV ayarlarını dinler. */
    fun observeAllApprovedTunings(): Flow<List<TuningProfile>>

    /** Yönetici moderasyonu için onay bekleyen tüm ayarları dinler. */
    fun observePendingTunings(): Flow<List<TuningProfile>>

    /** Yeni bir OC/UV ayarı paylaşımı gönderir. */
    suspend fun submitTuningProfile(profile: TuningProfile): DomainResult<Unit>

    /** Yönetici tarafından ayarı onaylar (Yazara +50 itibar puanı kazandırır). */
    suspend fun approveTuningProfile(id: String): DomainResult<Unit>

    /** Yönetici tarafından ayarı reddeder (gerekçe ile). */
    suspend fun rejectTuningProfile(id: String, reason: String): DomainResult<Unit>

    /** Bir ayara emojisel tepki (🔥, ❄️, ⚡, 🧪, ⚠️) verir. */
    suspend fun toggleReaction(tuningId: String, reactionType: TuningReactionType, userId: String): DomainResult<Unit>

    /** Bir ayara ait yorumları getirir. */
    fun observeComments(tuningId: String): Flow<List<TuningComment>>

    /** Bir ayara yorum ekler (+2 itibar puanı kazandırır). */
    suspend fun addComment(comment: TuningComment): DomainResult<Unit>

    /** Kanıt görselini Supabase Storage (tuning-proofs) alanına yükler ve URL'sini döner. */
    suspend fun uploadProofImage(bytes: ByteArray, fileName: String): DomainResult<String>

    /** Donanım detay sayfası mikro ipuçlarını dinler. */
    fun observeTipsForHardware(hardwareId: String): Flow<List<HardwareProTip>>

    /** Yeni mikro ipucu paylaşır. */
    suspend fun submitTip(tip: HardwareProTip): DomainResult<Unit>

    /** Mikro ipucuna faydalı (upvote) oyu verir. */
    suspend fun upvoteTip(tipId: String, userId: String): DomainResult<Unit>

    /** Hedef fiyat alarmı oluşturur (Yalnızca perakende/kampanya indirimleri için). */
    suspend fun createPriceAlert(alert: PriceDropAlert): DomainResult<Unit>

    /** Kullanıcının kayıtlı fiyat alarmlarını getirir. */
    fun observeUserPriceAlerts(userId: String): Flow<List<PriceDropAlert>>

    /** Fiyat alarmını siler. */
    suspend fun deletePriceAlert(alertId: String): DomainResult<Unit>
}
