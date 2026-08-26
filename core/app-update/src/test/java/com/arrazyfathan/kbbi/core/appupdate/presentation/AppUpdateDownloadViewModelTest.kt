package com.arrazyfathan.kbbi.core.appupdate.presentation

import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadManager
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateDownloadViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `download click starts the shown update`() {
        val manager = FakeAppUpdateDownloadManager()
        val viewModel = AppUpdateDownloadViewModel(manager)
        val update = update()

        viewModel.onAction(AppUpdateDownloadAction.OnPromptShown(update))
        viewModel.onAction(AppUpdateDownloadAction.OnDownloadClick)

        assertEquals(update, manager.startedUpdate)
    }

    @Test
    fun `ready update launches installer when host resumes`() =
        runTest {
            val manager = FakeAppUpdateDownloadManager()
            val viewModel = AppUpdateDownloadViewModel(manager)
            viewModel.onAction(AppUpdateDownloadAction.OnPromptShown(update()))
            manager.emit(AppUpdateDownloadState.Ready(42L, "2.0.0"))

            val event = async { viewModel.events.first() }
            viewModel.onAction(AppUpdateDownloadAction.OnHostResumed)

            assertEquals(AppUpdateDownloadEvent.LaunchInstaller(42L), event.await())
        }

    @Test
    fun `ready download for another version is not installed`() =
        runTest {
            val manager = FakeAppUpdateDownloadManager()
            val viewModel = AppUpdateDownloadViewModel(manager)
            viewModel.onAction(AppUpdateDownloadAction.OnPromptShown(update()))
            viewModel.onAction(AppUpdateDownloadAction.OnHostResumed)
            manager.emit(AppUpdateDownloadState.Ready(42L, "3.0.0"))

            assertEquals(AppUpdateDownloadState.Ready(42L, "3.0.0"), viewModel.state.value.downloadState)
        }

    private fun update() =
        AppUpdate(
            latestVersion = "2.0.0",
            releaseUrl = "https://example.com/release",
            downloadUrl = "https://example.com/kbbi.apk",
            releaseNotes = null,
        )
}

private class FakeAppUpdateDownloadManager : AppUpdateDownloadManager {
    private val _state = MutableStateFlow<AppUpdateDownloadState>(AppUpdateDownloadState.Idle)
    override val state = _state.asStateFlow()
    var startedUpdate: AppUpdate? = null

    override fun startDownload(update: AppUpdate) {
        startedUpdate = update
    }

    override fun refresh() = Unit

    fun emit(state: AppUpdateDownloadState) {
        _state.value = state
    }
}
