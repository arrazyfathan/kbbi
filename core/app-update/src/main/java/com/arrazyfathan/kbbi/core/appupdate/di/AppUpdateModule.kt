package com.arrazyfathan.kbbi.core.appupdate.di

import com.arrazyfathan.kbbi.core.appupdate.data.AppUpdatePreferences
import com.arrazyfathan.kbbi.core.appupdate.data.GitHubAppUpdateRepository
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.core.appupdate.presentation.AppUpdateViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appUpdateModule =
    module {
        single { AppUpdatePreferences(androidContext()) }
        singleOf(::GitHubAppUpdateRepository)
        single<AppUpdateRepository> { get<GitHubAppUpdateRepository>() }
        viewModelOf(::AppUpdateViewModel)
    }
