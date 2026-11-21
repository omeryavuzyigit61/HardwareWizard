package com.panda.myapplication.data.repository

import com.panda.myapplication.data.local.FastSettingsStore
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class ActiveBuildRepositoryImpl(
    private val hardwareRepository: HardwareRepository? = null,
    private val fastSettingsStore: FastSettingsStore? = null,
    private val json: Json = Json { ignoreUnknownKeys = true },
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ActiveBuildRepository {

    private val _activeBuild = MutableStateFlow(ActiveBuild())
    override val activeBuild: StateFlow<ActiveBuild> = _activeBuild.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val locallyModifiedCategories = mutableSetOf<HardwareCategory>()
    private var clearedByUser = false

    init {
        hydrateFromStore()
    }

    override fun setComponent(item: HardwareItem) {
        locallyModifiedCategories.add(item.category)
        _activeBuild.update { it.withComponent(item) }
        persistCurrentBuild()
    }

    override fun removeComponent(category: HardwareCategory) {
        locallyModifiedCategories.add(category)
        _activeBuild.update { it.withoutCategory(category) }
        persistCurrentBuild()
    }

    override fun toggleComponent(item: HardwareItem) {
        locallyModifiedCategories.add(item.category)
        _activeBuild.update { current ->
            val existing = current.getSlot(item.category)
            if (existing?.id == item.id) {
                current.withoutCategory(item.category)
            } else {
                current.withComponent(item)
            }
        }
        persistCurrentBuild()
    }

    override fun clearBuild() {
        clearedByUser = true
        locallyModifiedCategories.clear()
        _activeBuild.value = ActiveBuild()
        persistCurrentBuild()
    }

    override fun isItemInBuild(id: HardwareId): Boolean {
        return _activeBuild.value.items.any { it.id == id }
    }

    private fun persistCurrentBuild() {
        val store = fastSettingsStore ?: return
        runCatching {
            val slotMap = _activeBuild.value.items.associate { it.category.name to it.id.value }
            store.setString(KEY_ACTIVE_BUILD_SLOTS, json.encodeToString(slotMap))
        }
    }

    private fun hydrateFromStore() {
        val store = fastSettingsStore ?: return
        val repo = hardwareRepository ?: return
        val raw = runCatching { store.getString(KEY_ACTIVE_BUILD_SLOTS) }.getOrNull() ?: return
        if (raw.isBlank() || raw == "{}") return

        val savedSlots = runCatching {
            json.decodeFromString<Map<String, String>>(raw)
        }.getOrNull().orEmpty()
        if (savedSlots.isEmpty()) return

        scope.launch {
            savedSlots.forEach { (categoryName, itemId) ->
                if (clearedByUser) return@launch
                val category = runCatching { HardwareCategory.valueOf(categoryName) }.getOrNull()
                    ?: return@forEach
                if (category in locallyModifiedCategories) return@forEach

                val result = runCatching {
                    repo.observeHardwareItem(HardwareId(itemId)).firstOrNull()
                }.getOrNull()
                if (result is DomainResult.Success && !clearedByUser && category !in locallyModifiedCategories) {
                    _activeBuild.update { current ->
                        if (current.getSlot(result.data.category) == null) {
                            current.withComponent(result.data)
                        } else {
                            current
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val KEY_ACTIVE_BUILD_SLOTS = "active_build_slots_v1"
    }
}
