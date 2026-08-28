package com.arrazyfathan.kbbi

import android.app.Application
import com.arrazyfathan.kbbi.core.appupdate.di.appUpdateModule
import com.arrazyfathan.kbbi.core.di.networkModule
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.di.appIconModule
import com.arrazyfathan.kbbi.di.appUpdateConfigModule
import com.arrazyfathan.kbbi.di.useCaseModule
import com.arrazyfathan.kbbi.di.viewModelModule
import com.arrazyfathan.kbbi.feature.home.data.di.databaseModule
import com.arrazyfathan.kbbi.feature.home.data.di.repositoryModule
import com.arrazyfathan.kbbi.feature.proverb.data.di.proverbDataModule
import com.arrazyfathan.kbbi.feature.settings.data.di.settingsDataModule
import com.arrazyfathan.kbbi.feature.settings.domain.service.NotificationPermissionGateway
import com.arrazyfathan.kbbi.feature.settings.domain.service.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.presentation.di.settingsPresentationModule
import com.arrazyfathan.kbbi.notifications.AndroidNotificationPermissionGateway
import com.arrazyfathan.kbbi.notifications.WorkManagerReminderScheduler
import com.arrazyfathan.kbbi.widgets.BookmarkWidgetCoordinator
import com.arrazyfathan.kbbi.widgets.WidgetRefreshScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BaseApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            AppLogger.plantDebugTree()
        }

        val koinApplication =
            startKoin {
                androidLogger()
                androidContext(this@BaseApplication)
                modules(
                    listOf(
                        databaseModule,
                        repositoryModule,
                        proverbDataModule,
                        appUpdateConfigModule,
                        appIconModule,
                        appUpdateModule,
                        viewModelModule,
                        networkModule,
                        useCaseModule,
                        settingsDataModule,
                        settingsPresentationModule,
                        module {
                            single<NotificationPermissionGateway> {
                                AndroidNotificationPermissionGateway(
                                    androidContext(),
                                )
                            }
                            single<ReminderScheduler> {
                                WorkManagerReminderScheduler(androidContext())
                            }
                        },
                    ),
                )
            }

        try {
            WidgetRefreshScheduler.reconcile(this)
        } catch (_: IllegalStateException) {
            // Some host-side render tests intentionally start the app without WorkManager.
        }
        applicationScope.launch {
            BookmarkWidgetCoordinator(
                context = this@BaseApplication,
                repository = koinApplication.koin.get(),
            ).observeBookmarkChanges()
        }
    }
}
