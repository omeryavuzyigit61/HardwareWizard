package com.panda.myapplication.domain.repository

import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.RigShowcase
import kotlinx.coroutines.flow.Flow

interface RigShowcaseRepository {
    fun observeAllShowcases(): Flow<List<RigShowcase>>
    suspend fun submitShowcase(showcase: RigShowcase): DomainResult<Unit>
    suspend fun toggleLike(showcaseId: String, userId: String): DomainResult<Unit>
    suspend fun uploadShowcaseImage(bytes: ByteArray, fileName: String): DomainResult<String>
    suspend fun deleteShowcase(showcaseId: String): DomainResult<Unit>
}
