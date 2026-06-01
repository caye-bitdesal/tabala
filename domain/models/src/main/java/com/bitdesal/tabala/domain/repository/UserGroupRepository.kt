package com.bitdesal.tabala.domain.repository

import com.bitdesal.tabala.domain.model.UserGroup

interface UserGroupRepository {
    suspend fun getCurrentUserGroup(): UserGroup?
}
