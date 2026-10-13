package com.panda.myapplication.domain.repository

import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import kotlinx.coroutines.flow.StateFlow

interface ActiveBuildRepository {
    val activeBuild: StateFlow<ActiveBuild>

    fun setComponent(item: HardwareItem)
    fun removeComponent(category: HardwareCategory)
    fun toggleComponent(item: HardwareItem)
    fun clearBuild()
    fun isItemInBuild(id: HardwareId): Boolean
}
