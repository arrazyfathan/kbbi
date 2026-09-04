package com.arrazyfathan.kbbi.core.observability

import android.os.Bundle
import com.arrazyfathan.kbbi.core.logging.AppLogSink
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.HttpMetric
import java.util.concurrent.atomic.AtomicBoolean

internal class ReportingGate {
    val analyticsEnabled = AtomicBoolean(false)
    val crashReportingEnabled = AtomicBoolean(true)
    val performanceMonitoringEnabled = AtomicBoolean(false)
}

internal class FirebaseNetworkPerformanceReporter(
    private val performance: FirebasePerformance,
    private val gate: ReportingGate,
    private val eligible: Boolean,
) : NetworkPerformanceReporter {
    override fun start(url: String, method: String): NetworkPerformanceTrace {
        if (!eligible || !gate.performanceMonitoringEnabled.get()) {
            return NoOpNetworkPerformanceReporter.start(url, method)
        }
        return runCatching {
            FirebaseNetworkPerformanceTrace(
                performance.newHttpMetric(sanitizePerformanceUrl(url), method),
            ).also { it.start() }
        }.getOrElse { NoOpNetworkPerformanceReporter.start(url, method) }
    }
}

private class FirebaseNetworkPerformanceTrace(
    private val metric: HttpMetric,
) : NetworkPerformanceTrace {
    private val stopped = AtomicBoolean(false)

    fun start() = metric.start()

    override fun response(statusCode: Int, contentType: String?, payloadSizeBytes: Long?) {
        if (stopped.get()) return
        runCatching {
            metric.setHttpResponseCode(statusCode)
            contentType?.let(metric::setResponseContentType)
            payloadSizeBytes?.let(metric::setResponsePayloadSize)
        }
    }

    override fun stop() {
        if (stopped.compareAndSet(false, true)) runCatching { metric.stop() }
    }
}

internal fun sanitizePerformanceUrl(url: String): String {
    val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return "https://invalid.invalid/unknown"
    val origin = "${uri.scheme ?: "https"}://${uri.rawAuthority ?: "invalid.invalid"}"
    val path = uri.path.orEmpty()
    val safePath = when {
        path == "/proverb" || path == "/proverb/search" -> path
        path.startsWith("/search/") -> "/search/_redacted_"
        path.startsWith("/translate/") -> "/translate/_redacted_"
        path.startsWith("/proverb/") -> "/proverb/_redacted_"
        else -> "/unknown"
    }
    return origin + safePath
}

internal class FirebaseAnalyticsReporter(
    private val analytics: FirebaseAnalytics,
    private val gate: ReportingGate,
) : AnalyticsReporter {
    override fun log(event: AnalyticsEvent) {
        if (!gate.analyticsEnabled.get()) return
        val parameters = Bundle().apply { event.parameters.forEach { (key, value) -> putString(key, value) } }
        analytics.logEvent(event.name, parameters)
    }

    override fun screenViewed(screen: AnalyticsScreen) {
        if (!gate.analyticsEnabled.get()) return
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screen.value)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screen.value)
            },
        )
    }
}

internal class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics,
    private val gate: ReportingGate,
) : CrashReporter,
    AppLogSink {
    override fun breadcrumb(message: String) {
        if (gate.crashReportingEnabled.get()) crashlytics.log(message.take(MAX_LOG_LENGTH))
    }

    override fun setKey(
        key: String,
        value: String,
    ) {
        if (gate.crashReportingEnabled.get() && key in ALLOWED_CRASH_KEYS) {
            crashlytics.setCustomKey(key, value.take(MAX_KEY_LENGTH))
        }
    }

    override fun recordNonFatal(
        throwable: Throwable,
        operation: CrashOperation,
        context: Map<String, String>,
    ) {
        if (!gate.crashReportingEnabled.get()) return
        crashlytics.setCustomKey("operation", operation.value)
        context.filterKeys(ALLOWED_CRASH_KEYS::contains).forEach { (key, value) ->
            crashlytics.setCustomKey(key, value.take(MAX_KEY_LENGTH))
        }
        crashlytics.recordException(throwable.toSanitizedNonFatal(operation))
    }

    override fun write(
        level: AppLogger.LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (!gate.crashReportingEnabled.get() || level < AppLogger.LogLevel.Warning) return
        breadcrumb("${tag.take(MAX_TAG_LENGTH)}: ${message.take(MAX_LOG_LENGTH)}")
        if (throwable != null && level >= AppLogger.LogLevel.Error) {
            recordNonFatal(throwable, operationFor(tag, message))
        }
    }

    private fun operationFor(
        tag: String,
        message: String,
    ): CrashOperation =
        when {
            tag == "KBBI-API" && message.contains("serialization", ignoreCase = true) -> {
                CrashOperation.ResponseDeserialization
            }
            tag == "KBBI-API" -> CrashOperation.NetworkRequest
            tag == "WidgetRefresh" -> CrashOperation.WidgetRefresh
            tag == "DailyReminder" -> CrashOperation.ReminderDelivery
            tag == "AppIcon" -> CrashOperation.AppIconChange
            tag == "WordCatalog" -> CrashOperation.LocalDataRead
            else -> CrashOperation.UnexpectedFailure
        }

    private fun Throwable.toSanitizedNonFatal(operation: CrashOperation): Throwable =
        SanitizedNonFatalException(
            operation = operation.value,
            originalType = this::class.java.simpleName,
        ).also { it.stackTrace = stackTrace }

    private class SanitizedNonFatalException(
        operation: String,
        originalType: String,
    ) : RuntimeException("$operation failed ($originalType)")

    private companion object {
        const val MAX_LOG_LENGTH = 1_024
        const val MAX_KEY_LENGTH = 1_024
        const val MAX_TAG_LENGTH = 64
        val ALLOWED_CRASH_KEYS =
            setOf(
                "app_flavor",
                "build_type",
                "app_version",
                "current_screen",
                "entry_point",
                "operation",
                "content_type",
                "cache_hit",
                "widget_type",
                "reminder_type",
                "download_state",
            )
    }
}
