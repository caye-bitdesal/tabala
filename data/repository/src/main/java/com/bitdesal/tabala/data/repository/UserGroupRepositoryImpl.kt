package com.bitdesal.tabala.data.repository

import com.bitdesal.tabala.domain.model.UserGroup
import com.bitdesal.tabala.domain.repository.UserGroupRepository

// TODO: Replace with real auth/API integration in a future change.
// Note: logoResId is intentionally null here. The app layer maps group IDs to
// drawable resources so the data layer stays free of Android UI dependencies.
class UserGroupRepositoryImpl : UserGroupRepository {
    override suspend fun getCurrentUserGroup(): UserGroup = UserGroup(
        id = "muixeranga-elx",
        name = "Muixeranga d'Elx",
        logoResId = null,
    )
}
