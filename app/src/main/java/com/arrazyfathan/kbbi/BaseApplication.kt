package com.arrazyfathan.kbbi

import android.app.Application
import com.arrazyfathan.kbbi.core.di.databaseModule
import com.arrazyfathan.kbbi.core.di.networkModule
import com.arrazyfathan.kbbi.core.di.repositoryModule
import com.arrazyfathan.kbbi.di.useCaseModule
import com.arrazyfathan.kbbi.di.viewModelModule
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
