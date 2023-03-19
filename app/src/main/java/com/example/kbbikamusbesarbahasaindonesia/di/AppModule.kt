package com.example.kbbikamusbesarbahasaindonesia.di

import com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase.WordInteractor
import com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase.WordUseCase
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailViewModel
import com.example.kbbikamusbesarbahasaindonesia.ui.home.HomeViewModel
import com.example.kbbikamusbesarbahasaindonesia.ui.bookmark.SavedViewModel
import com.example.kbbikamusbesarbahasaindonesia.ui.words.WordViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

val useCaseModule = module {
    factory<WordUseCase> { WordInteractor(get()) }
}

val viewModelModule = module {
    viewModel { DetailViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { SavedViewModel(get()) }
    viewModel { WordViewModel(get()) }
}
