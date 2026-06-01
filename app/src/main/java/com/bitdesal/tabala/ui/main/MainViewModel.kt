package com.bitdesal.tabala.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bitdesal.tabala.domain.model.UserGroup
import com.bitdesal.tabala.domain.usecase.GetCurrentUserGroupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val getCurrentUserGroup: GetCurrentUserGroupUseCase,
) : ViewModel() {

    val userGroup: StateFlow<UserGroup?> = MutableStateFlow(getCurrentUserGroup())
}
