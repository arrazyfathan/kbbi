package com.arrazyfathan.kbbi.core.appupdate.presentation

import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateConfig
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRequirement
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateViewModelTest {
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
    fun `app start exposes required update`() =
        runTest {
            val update = update(AppUpdateRequirement.REQUIRED)
            val repository = FakeAppUpdateRepository(AppResult.Success(update))
            val viewModel = AppUpdateViewModel(repository, AppUpdateConfig("5.24", true))

            viewModel.onAction(AppUpdateAction.OnAppStarted)

            assertEquals(update, viewModel.state.value.availableUpdate)
            assertEquals(1, repository.checkCount)
        }

    @Test
    fun `duplicate app start checks only once per view model`() =
        runTest {
            val repository = FakeAppUpdateRepository(AppResult.Success(null))
            val viewModel = AppUpdateViewModel(repository, AppUpdateConfig("5.24", true))

            viewModel.onAction(AppUpdateAction.OnAppStarted)
            viewModel.onAction(AppUpdateAction.OnAppStarted)

            assertEquals(1, repository.checkCount)
        }

    @Test
    fun `disabled update checks do not call repository`() =
        runTest {
            val repository = FakeAppUpdateRepository(AppResult.Success(null))
            val viewModel = AppUpdateViewModel(repository, AppUpdateConfig("5.24", false))

            viewModel.onAction(AppUpdateAction.OnAppStarted)

            assertEquals(0, repository.checkCount)
        }

    private fun update(requirement: AppUpdateRequirement) =
        AppUpdate(
            latestVersion = "6.0",
            releaseUrl = "https://example.com/release",
            downloadUrl = "https://example.com/kbbi.apk",
            releaseNotes = null,
            requirement = requirement,
        )
}

private class FakeAppUpdateRepository(
    private val result: AppResult<AppUpdate?, DataError>,
) : AppUpdateRepository {
    var checkCount = 0

    override suspend fun checkForUpdate(
        currentVersion: String,
        force: Boolean,
    ): AppResult<AppUpdate?, DataError> {
        checkCount += 1
        return result
    }
}
