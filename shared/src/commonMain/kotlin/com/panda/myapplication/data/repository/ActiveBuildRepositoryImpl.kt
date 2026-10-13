package com.panda.myapplication.data.repository

import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ActiveBuildRepositoryImpl : ActiveBuildRepository {

    private val _activeBuild = MutableStateFlow(ActiveBuild())
    override val activeBuild: StateFlow<ActiveBuild> = _activeBuild.asStateFlow()

    override fun setComponent(item: HardwareItem) {
        _activeBuild.update { it.withComponent(item) }
    }

    override fun removeComponent(category: HardwareCategory) {
        _activeBuild.update { it.withoutCategory(category) }
    }

    override fun toggleComponent(item: HardwareItem) {
        _activeBuild.update { current ->
            val existing = current.getSlot(item.category)
            if (existing?.id == item.id) {
                current.withoutCategory(item.category)
            } else {
                current.withComponent(item)
            }
        }
    }

    override fun clearBuild() {
        _activeBuild.value = ActiveBuild()
    }

    override fun isItemInBuild(id: HardwareId): Boolean {
        return _activeBuild.value.items.any { it.id == id }
    }
}
