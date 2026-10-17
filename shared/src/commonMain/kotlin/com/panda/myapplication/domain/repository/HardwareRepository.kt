package com.panda.myapplication.domain.repository

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first contract: `observe*` streams emit from the local cache (SSOT),
 * while [refreshHardwareItems] syncs remote data into that cache.
 */
interface HardwareRepository {

    fun observeHardwareItems(): Flow<DomainResult<List<HardwareItem>>>

    fun observeHardwareItemsByCategory(category: HardwareCategory): Flow<DomainResult<List<HardwareItem>>>

    /** Emits [com.panda.myapplication.domain.common.DomainError.NotFound] if no item matches [id]. */
    fun observeHardwareItem(id: HardwareId): Flow<DomainResult<HardwareItem>>

    fun observeBrands(category: HardwareCategory? = null): Flow<DomainResult<List<String>>>

    suspend fun refreshHardwareItems(): DomainResult<Unit>

    suspend fun setCustomPrice(id: HardwareId, customPriceTry: Double, note: String? = null): DomainResult<Unit>

    suspend fun clearCustomPrice(id: HardwareId): DomainResult<Unit>
}
