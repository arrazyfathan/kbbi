package com.example.kbbikamusbesarbahasaindonesia

import android.app.Application
import com.example.kbbikamusbesarbahasaindonesia.core.di.databaseModule
import com.example.kbbikamusbesarbahasaindonesia.core.di.networkModule
import com.example.kbbikamusbesarbahasaindonesia.core.di.repositoryModule
import com.example.kbbikamusbesarbahasaindonesia.di.useCaseModule
import com.example.kbbikamusbesarbahasaindonesia.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(
                listOf(
                    databaseModule,
                    repositoryModule,
                    viewModelModule,
                    networkModule,
                    useCaseModule,
                ),
            )
        }
    }
}
