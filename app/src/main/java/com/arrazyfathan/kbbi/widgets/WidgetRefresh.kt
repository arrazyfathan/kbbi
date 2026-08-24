package com.arrazyfathan.kbbi.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

internal object WidgetRefreshScheduler {
    private const val UNIQUE_WORK_NAME = "kbbi_daily_data_widget_refresh"
    private const val PERIOD_HOURS = 24L

    fun reconcile(context: Context) {
        val applicationContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(applicationContext)
        val hasDataWidgets =
            manager
                .getAppWidgetIds(ComponentName(applicationContext, WordOfDayWidgetReceiver::class.java))
                .isNotEmpty() ||
                manager
                    .getAppWidgetIds(
                        ComponentName(
                            applicationContext,
                            SavedWordWidgetReceiver::class.java,
                        ),
                    ).isNotEmpty()
        val workManager = WorkManager.getInstance(applicationContext)
        if (hasDataWidgets) {
            val request =
                PeriodicWorkRequestBuilder<WidgetRefreshWorker>(PERIOD_HOURS, TimeUnit.HOURS)
                    .setInitialDelay(
                        delayToNextLocalDay(),
                        TimeUnit.MILLISECONDS,
                    ).build()
            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        } else {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }

    private fun delayToNextLocalDay(): Long {
        val now = Calendar.getInstance()
        val nextDay =
            (now.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        return nextDay.timeInMillis - now.timeInMillis
    }
}

internal class WidgetRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result =
        try {
            WordOfDayWidget().updateAll(applicationContext)
            SavedWordWidget().updateAll(applicationContext)
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            Result.retry()
        }
}

internal class BookmarkWidgetCoordinator(
    private val context: Context,
    private val repository: BookmarkRepository,
) {
    suspend fun observeBookmarkChanges() {
        repository
            .getBookmarks()
            .map { bookmarks -> bookmarks.map { it.word }.sorted() }
            .distinctUntilChanged()
            .collect {
                try {
                    SavedWordWidget().updateAll(context)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    // A later bookmark change will retry while the daily worker remains a fallback.
                }
            }
    }
}
