package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class RigShowcase(
    val id: String,
    val userId: String,
    val userDisplayName: String,
    val userBadge: String = "Lehim Çırağı",
    val title: String,
    val description: String? = null,
    val photoRigUrl: String,     // Fotoğraf 1: Kasa İçi Yakın Çekim
    val photoSetupUrl: String,   // Fotoğraf 2: Masa Düzeni & Setup
    val hardwareSpecsJson: String? = null,
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAtEpochMs: Long = 0L,
)
