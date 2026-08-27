package com.arrazyfathan.kbbi.ui

import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.feature.settings.domain.model.UiPreferences
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppUiViewModelTest {
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
    fun `state exposes haptics and active theme`() =
        runTest(dispatcher) {
            val repository = FakeUiPreferencesRepository()
            val viewModel = AppUiViewModel(repository)

            repository.value =
                UiPreferences(
                    hapticsEnabled = false,
                    theme = AppTheme.GOLDEN_CORAL_ENERGY,
                )

            val state = viewModel.state.first { it.theme == AppTheme.GOLDEN_CORAL_ENERGY }
            assertEquals(false, state.hapticsEnabled)
            assertEquals(AppTheme.GOLDEN_CORAL_ENERGY, state.theme)
        }
}

private class FakeUiPreferencesRepository : UiPreferencesRepository {
    private val preferencesState = MutableStateFlow(UiPreferences())
    override val preferences: Flow<UiPreferences> = preferencesState

    var value: UiPreferences
        get() = preferencesState.value
        set(value) {
            preferencesState.value = value
        }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        value = value.copy(hapticsEnabled = enabled)
    }

    override suspend fun setTheme(theme: AppTheme) {
        value = value.copy(theme = theme)
    }
}
