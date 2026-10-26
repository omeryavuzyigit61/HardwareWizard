package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HardwareProTip
import com.panda.myapplication.domain.model.PriceDropAlert
import com.panda.myapplication.domain.model.TuningComment
import com.panda.myapplication.domain.model.TuningProfile
import com.panda.myapplication.domain.model.UserProfile

@Immutable
data class HardwareDetailUiState(
    val isLoading: Boolean = true,
    val item: HardwareItem? = null,
    val errorMessage: String? = null,
    val errorMessageEn: String? = null,
    val isAddedToBuild: Boolean = false,
    val communityDeals: List<CommunityDeal> = emptyList(),
    val tuningProfiles: List<TuningProfile> = emptyList(),
    val tuningComments: Map<String, List<TuningComment>> = emptyMap(),
    val proTips: List<HardwareProTip> = emptyList(),
    val priceAlerts: List<PriceDropAlert> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: UserProfile? = null,
)
