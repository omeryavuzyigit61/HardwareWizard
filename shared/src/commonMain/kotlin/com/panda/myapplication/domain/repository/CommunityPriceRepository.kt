package com.panda.myapplication.domain.repository

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.CommunityDeal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Topluluk Fiyat Bildirimi (Community Price Ledger) ve Yönetici Moderasyonu deposu.
 */
interface CommunityPriceRepository {

    /** Belirli bir donanım parçasına ait onaylanmış (ve admin için bekleyen) fırsat akışını dinler. */
    fun observeDealsForHardware(hardwareId: String): Flow<List<CommunityDeal>>

    /** Yönetici paneli için onay bekleyen tüm bildirimleri dinler. */
    fun observeAllPendingDeals(): Flow<List<CommunityDeal>>

    /** Tüm geçmiş fırsatları (onaylanan, bekleyen, reddedilen) dinler. */
    fun observeAllDeals(): Flow<List<CommunityDeal>>

    /** Yeni bir topluluk fiyat bildirimi gönderir. */
    suspend fun submitDeal(deal: CommunityDeal): DomainResult<Unit>

    /** Yönetici tarafından fiyat bildirimini onaylar. */
    suspend fun approveDeal(dealId: String, adminName: String = "Ömer Admin"): DomainResult<Unit>

    /** Yönetici tarafından fiyat bildirimini reddeder. */
    suspend fun rejectDeal(dealId: String, reason: String? = null): DomainResult<Unit>

    /** Bir bildirimi siler. */
    suspend fun deleteDeal(dealId: String): DomainResult<Unit>

    /** Yönetici giriş durumunu takip eden StateFlow. */
    val isAdminLoggedIn: StateFlow<Boolean>

    /** Yönetici PIN kodu ile oturum açma doğrulaması. */
    fun loginAdmin(pin: String): Boolean

    /** Yönetici oturumunu kapatır. */
    fun logoutAdmin()

    /** Yönetici aktif mi sorgusu. */
    fun isAdmin(): Boolean
}
