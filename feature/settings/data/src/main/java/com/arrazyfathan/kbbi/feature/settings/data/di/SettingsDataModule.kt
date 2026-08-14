package com.arrazyfathan.kbbi.feature.settings.data.di

import com.arrazyfathan.kbbi.feature.settings.data.DataStoreNotificationSettingsRepository
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsDataModule =
    module {
        single<NotificationSettingsRepository> {
            DataStoreNotificationSettingsRepository(androidContext(), get())
        }
    }
