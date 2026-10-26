package com.panda.myapplication.domain.repository

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUser: StateFlow<UserProfile?>
    val isLoggedIn: StateFlow<Boolean>

    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        birthDateIso: String,
        gender: Gender,
        spiritHardware: SpiritHardware,
    ): DomainResult<UserProfile>

    suspend fun login(
        emailOrUsername: String,
        password: String,
    ): DomainResult<UserProfile>

    suspend fun logout()

    suspend fun updateProfile(profile: UserProfile): DomainResult<Unit>

    suspend fun refreshCurrentUserStats()
}
