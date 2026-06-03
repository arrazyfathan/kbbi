package com.arrazyfathan.kbbi.di

import com.arrazyfathan.kbbi.core.domain.usecase.AddSearchHistoryUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.ObserveBookmarksUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.SaveBookmarkUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.SearchWordUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.SearchWordWithHistoryUseCase
import com.arrazyfathan.kbbi.presentation.bookmark.BookmarksViewModel
import com.arrazyfathan.kbbi.presentation.detail.DetailViewModel
import com.arrazyfathan.kbbi.presentation.home.HomeViewModel
import com.arrazyfathan.kbbi.presentation.words.WordViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

val useCaseModule =
    module {
        factoryOf(::SearchWordUseCase)
        factoryOf(::SearchWordWithHistoryUseCase)
        factoryOf(::AddSearchHistoryUseCase)
        factoryOf(::ObserveSearchHistoryUseCase)
        factoryOf(::SaveBookmarkUseCase)
        factoryOf(::DeleteBookmarkUseCase)
        factoryOf(::CheckWordSavedUseCase)
        factoryOf(::ObserveBookmarksUseCase)
    }

val viewModelModule =
    module {
        viewModelOf(::DetailViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::BookmarksViewModel)
        viewModelOf(::WordViewModel)
    }
