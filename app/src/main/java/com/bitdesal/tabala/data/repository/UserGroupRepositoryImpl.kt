package com.bitdesal.tabala.data.repository

import com.bitdesal.tabala.R
import com.bitdesal.tabala.domain.model.UserGroup
import com.bitdesal.tabala.domain.repository.UserGroupRepository

// TODO: Replace with real auth/API integration in a future change.
class UserGroupRepositoryImpl : UserGroupRepository {
    override fun getCurrentUserGroup(): UserGroup = UserGroup(
        id = "muixeranga-elx",
        name = "Muixeranga d'Elx",
        logoResId = R.drawable.logo_muixeranga_elx,
    )
}
