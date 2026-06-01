package com.bitdesal.tabala.di

import com.bitdesal.tabala.data.repository.UserGroupRepositoryImpl
import com.bitdesal.tabala.domain.repository.UserGroupRepository
import com.bitdesal.tabala.domain.usecase.GetCurrentUserGroupUseCase
import com.bitdesal.tabala.ui.main.MainViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { UserGroupRepositoryImpl() } bind UserGroupRepository::class
    factoryOf(::GetCurrentUserGroupUseCase)
    viewModelOf(::MainViewModel)
}
