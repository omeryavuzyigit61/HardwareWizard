package com.panda.myapplication.domain.usecase

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.HardwareRepository
import kotlinx.coroutines.flow.Flow

class GetHardwareDetailUseCase(
    private val repository: HardwareRepository,
) {
    operator fun invoke(id: HardwareId): Flow<DomainResult<HardwareItem>> =
        repository.observeHardwareItem(id)

    suspend fun setCustomPrice(id: HardwareId, customPriceTry: Double, note: String?) {
        repository.setCustomPrice(id, customPriceTry, note)
    }

    suspend fun clearCustomPrice(id: HardwareId) {
        repository.clearCustomPrice(id)
    }
}
