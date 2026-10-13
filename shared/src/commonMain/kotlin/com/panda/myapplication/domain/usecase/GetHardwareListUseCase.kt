package com.panda.myapplication.domain.usecase

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.HardwareRepository
import kotlinx.coroutines.flow.Flow

class GetHardwareListUseCase(
    private val repository: HardwareRepository,
) {
    /** @param category `null` returns all categories. */
    operator fun invoke(category: HardwareCategory? = null): Flow<DomainResult<List<HardwareItem>>> =
        if (category == null) {
            repository.observeHardwareItems()
        } else {
            repository.observeHardwareItemsByCategory(category)
        }
}
