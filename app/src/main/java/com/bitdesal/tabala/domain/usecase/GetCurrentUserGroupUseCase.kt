package com.bitdesal.tabala.domain.usecase

import com.bitdesal.tabala.domain.model.UserGroup
import com.bitdesal.tabala.domain.repository.UserGroupRepository

class GetCurrentUserGroupUseCase(private val repository: UserGroupRepository) {
    operator fun invoke(): UserGroup? = repository.getCurrentUserGroup()
}
