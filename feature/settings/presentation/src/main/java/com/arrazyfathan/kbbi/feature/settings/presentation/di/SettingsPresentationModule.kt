package com.arrazyfathan.kbbi.feature.settings.presentation.di

import com.arrazyfathan.kbbi.feature.settings.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsPresentationModule = module { viewModelOf(::SettingsViewModel) }
