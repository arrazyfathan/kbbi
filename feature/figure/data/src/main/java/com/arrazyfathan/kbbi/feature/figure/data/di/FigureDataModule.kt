package com.arrazyfathan.kbbi.feature.figure.data.di

import com.arrazyfathan.kbbi.feature.figure.data.NetworkFigureRepository
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.FigureRemoteDataSource
import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val figureDataModule =
    module {
        singleOf(::FigureRemoteDataSource)
        singleOf(::NetworkFigureRepository) {
            bind<FigureRepository>()
        }
    }
