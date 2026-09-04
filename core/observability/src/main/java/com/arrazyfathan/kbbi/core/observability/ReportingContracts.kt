package com.arrazyfathan.kbbi.core.observability

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface AnalyticsReporter {
    fun log(event: AnalyticsEvent)

    fun screenViewed(screen: AnalyticsScreen)
}

interface NetworkPerformanceReporter {
    fun start(url: String, method: String): NetworkPerformanceTrace
}

interface NetworkPerformanceTrace {
    fun response(statusCode: Int, contentType: String?, payloadSizeBytes: Long?)
    fun stop()
}

object NoOpNetworkPerformanceReporter : NetworkPerformanceReporter {
    override fun start(url: String, method: String): NetworkPerformanceTrace = NoOpNetworkPerformanceTrace
}

private object NoOpNetworkPerformanceTrace : NetworkPerformanceTrace {
    override fun response(statusCode: Int, contentType: String?, payloadSizeBytes: Long?) = Unit
    override fun stop() = Unit
}

object NoOpAnalyticsReporter : AnalyticsReporter {
    override fun log(event: AnalyticsEvent) = Unit

    override fun screenViewed(screen: AnalyticsScreen) = Unit
}

enum class CrashOperation(val value: String) {
    UnexpectedFailure("unexpected_failure"),
    NetworkRequest("network_request"),
    ResponseDeserialization("response_deserialization"),
    WidgetRefresh("widget_refresh"),
    ReminderDelivery("reminder_delivery"),
    AppIconChange("app_icon_change"),
    UpdateCheck("update_check"),
    UpdateDownload("update_download"),
    UpdateInstall("update_install"),
    LocalDataRead("local_data_read"),
}

interface CrashReporter {
    fun breadcrumb(message: String)

    fun setKey(
        key: String,
        value: String,
    )

    fun recordNonFatal(
        throwable: Throwable,
        operation: CrashOperation,
        context: Map<String, String> = emptyMap(),
    )
}

object NoOpCrashReporter : CrashReporter {
    override fun breadcrumb(message: String) = Unit

    override fun setKey(
        key: String,
        value: String,
    ) = Unit

    override fun recordNonFatal(
        throwable: Throwable,
        operation: CrashOperation,
        context: Map<String, String>,
    ) = Unit
}

data class ReportingPreferences(
    val crashReportingEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val performanceMonitoringEnabled: Boolean = false,
)

interface ReportingPreferencesRepository {
    val preferences: Flow<ReportingPreferences>

    suspend fun setCrashReportingEnabled(enabled: Boolean)

    suspend fun setAnalyticsEnabled(enabled: Boolean)

    suspend fun setPerformanceMonitoringEnabled(enabled: Boolean)
}

object DefaultReportingPreferencesRepository : ReportingPreferencesRepository {
    override val preferences = flowOf(ReportingPreferences())

    override suspend fun setCrashReportingEnabled(enabled: Boolean) = Unit

    override suspend fun setAnalyticsEnabled(enabled: Boolean) = Unit

    override suspend fun setPerformanceMonitoringEnabled(enabled: Boolean) = Unit
}

interface ReportingCoordinator {
    suspend fun initialize()
}
