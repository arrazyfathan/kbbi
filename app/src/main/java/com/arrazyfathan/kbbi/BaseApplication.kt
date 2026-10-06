package com.arrazyfathan.kbbi

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.arrazyfathan.kbbi.core.appupdate.di.appUpdateModule
import com.arrazyfathan.kbbi.core.di.networkModule
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.core.observability.AppBuildInfo
import com.arrazyfathan.kbbi.core.observability.ReportingCoordinator
import com.arrazyfathan.kbbi.core.observability.observabilityModule
import com.arrazyfathan.kbbi.di.appIconModule
import com.arrazyfathan.kbbi.di.appUpdateConfigModule
import com.arrazyfathan.kbbi.di.useCaseModule
import com.arrazyfathan.kbbi.di.viewModelModule
import com.arrazyfathan.kbbi.feature.figure.data.di.figureDataModule
import com.arrazyfathan.kbbi.feature.home.data.di.databaseModule
import com.arrazyfathan.kbbi.feature.home.data.di.repositoryModule
import com.arrazyfathan.kbbi.feature.proverb.data.di.proverbDataModule
import com.arrazyfathan.kbbi.feature.settings.data.di.settingsDataModule
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.arrazyfathan.kbbi.feature.settings.domain.service.NotificationPermissionGateway
import com.arrazyfathan.kbbi.feature.settings.domain.service.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.presentation.di.settingsPresentationModule
import com.arrazyfathan.kbbi.feature.wordstudy.data.di.wordStudyDataModule
import com.arrazyfathan.kbbi.feature.wordstudy.presentation.di.wordStudyPresentationModule
import com.arrazyfathan.kbbi.isProductionFlavor
import com.arrazyfathan.kbbi.notifications.AndroidNotificationPermissionGateway
import com.arrazyfathan.kbbi.notifications.AppUpdateTopicWorker
import com.arrazyfathan.kbbi.notifications.EditorialTopicWorker
import com.arrazyfathan.kbbi.notifications.WorkManagerReminderScheduler
import com.arrazyfathan.kbbi.widgets.BookmarkWidgetCoordinator
import com.arrazyfathan.kbbi.widgets.WidgetRefreshScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BaseApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        val koinApplication = initializeKoin()
        initializeReporting(koinApplication)
        observeNotificationSettings(koinApplication)
        reconcileWidgets()
        observeBookmarkChanges(koinApplication)
    }

    private fun initializeLogging() {
        if (BuildConfig.DEBUG) AppLogger.plantDebugTree()
    }

    private fun initializeKoin() =
        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(
                listOf(
                    databaseModule,
                    repositoryModule,
                    figureDataModule,
                    proverbDataModule,
                    appUpdateConfigModule,
                    appIconModule,
                    appUpdateModule,
                    viewModelModule,
                    networkModule,
                    useCaseModule,
                    settingsDataModule,
                    settingsPresentationModule,
                    wordStudyDataModule,
                    wordStudyPresentationModule,
                    observabilityModule(
                        AppBuildInfo(
                            flavor = BuildConfig.FLAVOR,
                            buildType = BuildConfig.BUILD_TYPE,
                            versionName = BuildConfig.VERSION_NAME,
                            isProductionFlavor = isProductionFlavor(),
                        ),
                    ),
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

    private fun initializeReporting(koinApplication: KoinApplication) {
        applicationScope.launch {
            koinApplication.koin.get<ReportingCoordinator>().initialize()
        }
    }

    private fun observeNotificationSettings(koinApplication: KoinApplication) {
        applicationScope.launch {
            koinApplication.koin
                .get<NotificationSettingsRepository>()
                .settings
                .map {
                    Triple(
                        it.campaignNotificationsEnabled,
                        it.updateNotificationsEnabled,
                        it.permissionGranted,
                    )
                }.distinctUntilChanged()
                .collect {
                    val work =
                        OneTimeWorkRequestBuilder<EditorialTopicWorker>()
                            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                            .build()
                    WorkManager.getInstance(this@BaseApplication).enqueueUniqueWork(
                        "editorial-topic-reconcile",
                        ExistingWorkPolicy.REPLACE,
                        work,
                    )
                    AppUpdateTopicWorker.enqueue(this@BaseApplication)
                }
        }
    }

    private fun reconcileWidgets() {
        try {
            WidgetRefreshScheduler.reconcile(this)
        } catch (_: IllegalStateException) {
            // Some host-side render tests intentionally start the app without WorkManager.
        }
    }

    private fun observeBookmarkChanges(koinApplication: KoinApplication) {
        applicationScope.launch {
            BookmarkWidgetCoordinator(
                context = this@BaseApplication,
                repository = koinApplication.koin.get(),
            ).observeBookmarkChanges()
        }
    }
}
