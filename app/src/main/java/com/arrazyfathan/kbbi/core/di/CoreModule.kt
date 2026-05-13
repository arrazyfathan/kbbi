package com.arrazyfathan.kbbi.core.di

import androidx.room.Room
import com.arrazyfathan.kbbi.core.data.WordRepository
import com.arrazyfathan.kbbi.core.data.source.local.LocalDataSource
import com.arrazyfathan.kbbi.core.data.source.local.room.WordDatabase
import com.arrazyfathan.kbbi.core.data.source.remote.RemoteDataSource
import com.arrazyfathan.kbbi.core.data.source.remote.network.ApiService
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository
import com.arrazyfathan.kbbi.utils.Constant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

private const val NETWORK_TIMEOUT_SECONDS = 120L

val databaseModule =
    module {
        factory { get<WordDatabase>().wordDao() }
        single {
            Room
                .databaseBuilder(
                    androidContext(),
                    WordDatabase::class.java,
                    "kbbi_db",
                ).fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }

val networkModule =
    module {
        single {
            OkHttpClient
                .Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()
        }

        single {
            val retrofit =
                Retrofit
                    .Builder()
                    .baseUrl(Constant.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(get())
                    .build()
            retrofit.create(ApiService::class.java)
        }
    }

val repositoryModule =
    module {
        single { RemoteDataSource(get()) }
        single { LocalDataSource(get()) }
        factory<IWordRepository> { WordRepository(get(), get()) }
    }
