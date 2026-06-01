package com.bitdesal.tabala.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitdesal.tabala.ui.components.R as ComponentsR
import com.bitdesal.tabala.domain.model.UserGroup
import com.bitdesal.tabala.domain.usecase.GetCurrentUserGroupUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val getCurrentUserGroup: GetCurrentUserGroupUseCase,
) : ViewModel() {

    val userGroup: StateFlow<UserGroup?> = flow {
        emit(getCurrentUserGroup()?.withAppLogo())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    // Maps a group's stable ID to its local drawable resource.
    // The data layer is intentionally logo-free; this app-level mapper owns the association.
    private fun UserGroup.withAppLogo(): UserGroup = copy(
        logoResId = when (id) {
            "muixeranga-elx" -> ComponentsR.drawable.logo_muixeranga_elx
            else -> null
        }
    )
}
