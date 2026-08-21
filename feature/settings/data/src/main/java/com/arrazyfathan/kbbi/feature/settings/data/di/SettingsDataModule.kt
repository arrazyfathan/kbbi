package com.arrazyfathan.kbbi.feature.settings.data.di

import com.arrazyfathan.kbbi.feature.settings.data.DataStoreNotificationSettingsRepository
import com.arrazyfathan.kbbi.feature.settings.data.DataStoreUiPreferencesRepository
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val settingsDataModule =
    module {
        single<NotificationSettingsRepository> {
            DataStoreNotificationSettingsRepository(androidContext(), get())
        }
        singleOf(::DataStoreUiPreferencesRepository) { bind<UiPreferencesRepository>() }
    }
