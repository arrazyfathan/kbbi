package com.arrazyfathan.kbbi.di

import com.arrazyfathan.kbbi.core.domain.usecase.WordInteractor
import com.arrazyfathan.kbbi.core.domain.usecase.WordUseCase
import com.arrazyfathan.kbbi.presentation.bookmark.SavedViewModel
import com.arrazyfathan.kbbi.presentation.detail.DetailViewModel
import com.arrazyfathan.kbbi.presentation.home.HomeViewModel
import com.arrazyfathan.kbbi.presentation.words.WordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

val useCaseModule =
    module {
        factory<WordUseCase> { WordInteractor(get()) }
    }

val viewModelModule =
    module {
        viewModelOf(::DetailViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::SavedViewModel)
        viewModelOf(::WordViewModel)
    }
