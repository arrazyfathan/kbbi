package com.example.kbbikamusbesarbahasaindonesia.core.di

import androidx.room.Room
import com.example.kbbikamusbesarbahasaindonesia.core.data.WordRepository
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.LocalDataSource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.room.WordDatabase
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.RemoteDataSource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.network.ApiService
import com.example.kbbikamusbesarbahasaindonesia.core.domain.repository.IWordRepository
import com.example.kbbikamusbesarbahasaindonesia.core.utils.AppExecutors
import com.example.kbbikamusbesarbahasaindonesia.repository.KataRepository
import com.example.kbbikamusbesarbahasaindonesia.utils.Constant
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

val databaseModule = module {
    factory { get<WordDatabase>().wordDao() }
    single {
        Room.databaseBuilder(
            androidContext(),
            WordDatabase::class.java,
            "kbbi_db",
        ).fallbackToDestructiveMigration().build()
    }
}

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    single {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
        retrofit.create(ApiService::class.java)
    }
}

val repositoryModule = module {
    single { KataRepository(get()) }
    single { RemoteDataSource(get()) }
    single { LocalDataSource(get()) }
    factory { AppExecutors() }
    factory<IWordRepository> { WordRepository(get(), get()) }
}
