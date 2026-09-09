package com.arrazyfathan.kbbi.feature.figure.data.di

import com.arrazyfathan.kbbi.feature.figure.data.NetworkFigureRepository
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.FigureRemoteDataSource
import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class FigureDataModuleTest {
    @Test
    fun `resolves remote data source and repository binding`() {
        val client = HttpClient(MockEngine { error("No request expected") })
        val application =
            koinApplication {
                modules(
                    module { single { client } },
                    figureDataModule,
                )
            }

        try {
            val dataSource = application.koin.get<FigureRemoteDataSource>()
            val repository = application.koin.get<FigureRepository>()

            assertEquals(FigureRemoteDataSource::class, dataSource::class)
            assertEquals(NetworkFigureRepository::class, repository::class)
        } finally {
            application.close()
            client.close()
        }
    }
}
