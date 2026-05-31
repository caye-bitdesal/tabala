package com.bitdesal.tabala.domain.repository

import com.bitdesal.tabala.domain.model.UserGroup

interface UserGroupRepository {
    fun getCurrentUserGroup(): UserGroup?
}
