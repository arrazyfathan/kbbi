package com.arrazyfathan.kbbi.di

import com.arrazyfathan.kbbi.BuildConfig
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateConfig
import com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark.BookmarksViewModel
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailViewModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.AddSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ClearSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordSuggestionsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordTranslationUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveBookmarksUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordWithHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeViewModel
import com.arrazyfathan.kbbi.feature.proverb.domain.usecase.GetListProverbsUseCase
import com.arrazyfathan.kbbi.feature.proverb.domain.usecase.GetProverbMeaningUseCase
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbViewModel
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordViewModel
import com.arrazyfathan.kbbi.ui.AppUiViewModel
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
        factoryOf(::ClearSearchHistoryUseCase)
        factoryOf(::SaveBookmarkUseCase)
        factoryOf(::DeleteBookmarkUseCase)
        factoryOf(::CheckWordSavedUseCase)
        factoryOf(::ObserveBookmarksUseCase)
        factoryOf(::GetWordEntriesUseCase)
        factoryOf(::GetWordSuggestionsUseCase)
        factoryOf(::GetWordTranslationUseCase)
        factoryOf(::GetListProverbsUseCase)
        factoryOf(::GetProverbMeaningUseCase)
    }

val viewModelModule =
    module {
        viewModelOf(::AppUiViewModel)
        viewModelOf(::DetailViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::BookmarksViewModel)
        viewModelOf(::WordViewModel)
        viewModelOf(::ProverbViewModel)
    }

val appUpdateConfigModule =
    module {
        single {
            AppUpdateConfig(
                currentVersion = BuildConfig.VERSION_NAME,
                isUpdateCheckEnabled = BuildConfig.FLAVOR == "production",
            )
        }
    }
