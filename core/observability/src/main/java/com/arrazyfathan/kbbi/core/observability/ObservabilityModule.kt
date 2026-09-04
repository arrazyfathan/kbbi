package com.arrazyfathan.kbbi.core.observability

import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

data class AppBuildInfo(
    val flavor: String,
    val buildType: String,
    val versionName: String,
)

private val AppBuildInfo.performanceMonitoringEligible: Boolean
    get() = flavor == "production" && buildType == "release"

fun observabilityModule(buildInfo: AppBuildInfo) =
    module {
        single { buildInfo }
        single { ReportingGate() }
        single { FirebaseAnalytics.getInstance(androidContext()) }
        single { FirebaseCrashlytics.getInstance() }
        single { FirebasePerformance.getInstance() }
        single {
            FirebaseReportingPreferencesRepository(
                context = androidContext(),
                analytics = get(),
                crashlytics = get(),
                performance = get(),
                gate = get(),
                eligible = buildInfo.performanceMonitoringEligible,
            )
        }
        single<ReportingPreferencesRepository> { get<FirebaseReportingPreferencesRepository>() }
        single { FirebaseAnalyticsReporter(analytics = get(), gate = get()) }
        single<AnalyticsReporter> { get<FirebaseAnalyticsReporter>() }
        single<NetworkPerformanceReporter> {
            FirebaseNetworkPerformanceReporter(
                performance = get(),
                gate = get(),
                eligible = buildInfo.performanceMonitoringEligible,
            )
        }
        single { FirebaseCrashReporter(crashlytics = get(), gate = get()) }
        single<CrashReporter> { get<FirebaseCrashReporter>() }
        single<com.arrazyfathan.kbbi.core.logging.AppLogSink> { get<FirebaseCrashReporter>() }
        single<ReportingCoordinator> {
            FirebaseReportingCoordinator(
                preferencesRepository = get(),
                crashReporter = get(),
                logSink = get(),
                buildInfo = get(),
            )
        }
    }

private class FirebaseReportingCoordinator(
    private val preferencesRepository: FirebaseReportingPreferencesRepository,
    private val crashReporter: CrashReporter,
    private val logSink: com.arrazyfathan.kbbi.core.logging.AppLogSink,
    private val buildInfo: AppBuildInfo,
) : ReportingCoordinator {
    override suspend fun initialize() {
        preferencesRepository.synchronizeCollection()
        AppLogger.installRemoteSink(logSink)
        crashReporter.setKey("app_flavor", buildInfo.flavor)
        crashReporter.setKey("build_type", buildInfo.buildType)
        crashReporter.setKey("app_version", buildInfo.versionName)
    }
}
