package com.arrazyfathan.kbbi.core.observability

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.reportingPreferencesDataStore by preferencesDataStore(name = "reporting_preferences")

internal class FirebaseReportingPreferencesRepository(
    private val context: Context,
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
    private val performance: FirebasePerformance,
    private val gate: ReportingGate,
    private val eligible: Boolean,
) : ReportingPreferencesRepository {
    override val preferences: Flow<ReportingPreferences> =
        context.reportingPreferencesDataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(
                        androidx.datastore.preferences.core
                            .emptyPreferences(),
                    )
                } else {
                    throw error
                }
            }.map(Preferences::toReportingPreferences)

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        context.reportingPreferencesDataStore.edit { it[CRASH_REPORTING_ENABLED] = enabled }
        gate.crashReportingEnabled.set(enabled)
        crashlytics.isCrashlyticsCollectionEnabled = enabled
        if (!enabled) crashlytics.deleteUnsentReports()
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.reportingPreferencesDataStore.edit { it[ANALYTICS_ENABLED] = enabled }
        gate.analyticsEnabled.set(enabled)
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    override suspend fun setPerformanceMonitoringEnabled(enabled: Boolean) {
        context.reportingPreferencesDataStore.edit { it[PERFORMANCE_MONITORING_ENABLED] = enabled }
        val active = enabled && eligible
        gate.performanceMonitoringEnabled.set(active)
        performance.isPerformanceCollectionEnabled = active
    }

    suspend fun synchronizeCollection() {
        val current = preferences.first()
        gate.crashReportingEnabled.set(current.crashReportingEnabled)
        gate.analyticsEnabled.set(current.analyticsEnabled)
        gate.performanceMonitoringEnabled.set(current.performanceMonitoringEnabled && eligible)
        crashlytics.isCrashlyticsCollectionEnabled = current.crashReportingEnabled
        analytics.setAnalyticsCollectionEnabled(current.analyticsEnabled)
        performance.isPerformanceCollectionEnabled = current.performanceMonitoringEnabled && eligible
        if (!current.crashReportingEnabled) crashlytics.deleteUnsentReports()
    }
}

private val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
private val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
private val PERFORMANCE_MONITORING_ENABLED = booleanPreferencesKey("performance_monitoring_enabled")

internal fun Preferences.toReportingPreferences(): ReportingPreferences =
    ReportingPreferences(
        crashReportingEnabled = this[CRASH_REPORTING_ENABLED] ?: true,
        analyticsEnabled = this[ANALYTICS_ENABLED] ?: false,
        performanceMonitoringEnabled = this[PERFORMANCE_MONITORING_ENABLED] ?: false,
    )
