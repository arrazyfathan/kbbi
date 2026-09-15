package com.arrazyfathan.kbbi.feature.wordstudy.presentation.di

import com.arrazyfathan.kbbi.feature.wordstudy.presentation.ai.AiSettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wordStudyPresentationModule =
    module {
        viewModelOf(::AiSettingsViewModel)
    }
