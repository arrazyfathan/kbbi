package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateConfig
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ClearSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.settings.domain.model.NotificationSettings
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderPreference
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.arrazyfathan.kbbi.feature.settings.domain.service.ReminderScheduler
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
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
    fun `enabling reminder schedules it and disabling cancels it`() = runTest(dispatcher) {
        val repository = FakeSettingsRepository()
        val scheduler = FakeScheduler()
        val viewModel = createViewModel(repository = repository, scheduler = scheduler)

        viewModel.onAction(SettingsAction.OnReminderToggled(ReminderType.DAILY_WORD, true))
        assertEquals(ReminderType.DAILY_WORD, scheduler.scheduledType)
        assertEquals(true, repository.settings.first().dailyWord.enabled)

        viewModel.onAction(SettingsAction.OnReminderToggled(ReminderType.DAILY_WORD, false))
        assertEquals(ReminderType.DAILY_WORD, scheduler.cancelledType)
        assertEquals(false, repository.settings.first().dailyWord.enabled)
    }

    @Test
    fun `changing enabled reminder time reschedules it`() = runTest(dispatcher) {
        val repository = FakeSettingsRepository()
        repository.value = NotificationSettings(
            dailyWord = ReminderPreference(true, ReminderTime.DailyWord),
        )
        val scheduler = FakeScheduler()
        val viewModel = createViewModel(repository = repository, scheduler = scheduler)
        val newTime = ReminderTime(8, 30)

        viewModel.onAction(SettingsAction.OnReminderTimeChanged(ReminderType.DAILY_WORD, newTime))

        assertEquals(newTime, scheduler.scheduledTime)
    }

    @Test
    fun `starting settings resolves current language`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onAction(SettingsAction.OnStarted(AppLanguage.INDONESIAN))

        assertEquals(AppLanguage.INDONESIAN, viewModel.state.value.selectedLanguage)
    }

    @Test
    fun `language picker actions update visibility`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onAction(SettingsAction.OnLanguageClick)
        assertTrue(viewModel.state.value.isLanguagePickerVisible)

        viewModel.onAction(SettingsAction.OnLanguagePickerDismissed)
        assertFalse(viewModel.state.value.isLanguagePickerVisible)
    }

    @Test
    fun `selecting another language updates state and emits apply event`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.onAction(SettingsAction.OnStarted(AppLanguage.ENGLISH))
        viewModel.onAction(SettingsAction.OnLanguageClick)

        viewModel.onAction(SettingsAction.OnLanguageSelected(AppLanguage.INDONESIAN))

        assertEquals(AppLanguage.INDONESIAN, viewModel.state.value.selectedLanguage)
        assertFalse(viewModel.state.value.isLanguagePickerVisible)
        assertEquals(
            SettingsEvent.ApplyLanguage(AppLanguage.INDONESIAN),
            viewModel.events.first(),
        )
    }

    @Test
    fun `locale resolution prefers app locale then supported device locale`() {
        assertEquals(
            AppLanguage.INDONESIAN,
            resolveAppLanguage(listOf("id-ID"), listOf("en-US")),
        )
        assertEquals(
            AppLanguage.INDONESIAN,
            resolveAppLanguage(listOf("in"), listOf("en-US")),
        )
        assertEquals(
            AppLanguage.INDONESIAN,
            resolveAppLanguage(emptyList(), listOf("fr-FR", "id-ID")),
        )
        assertEquals(
            AppLanguage.ENGLISH,
            resolveAppLanguage(emptyList(), listOf("fr-FR")),
        )
    }

    @Test
    fun `checking update with available update sets state and shows prompt`() = runTest(dispatcher) {
        val update = AppUpdate("2.0.0", "https://example.com/release", null, "Notes")
        val updateRepository = FakeAppUpdateRepository(AppResult.Success(update))
        val viewModel = createViewModel(updateRepository = updateRepository)

        viewModel.onAction(SettingsAction.OnCheckForUpdate)

        assertEquals(update, viewModel.state.value.availableUpdate)
        assertTrue(viewModel.state.value.isUpdatePromptVisible)
        assertFalse(viewModel.state.value.isCheckingUpdate)
        assertEquals(true, updateRepository.lastForce)
    }

    @Test
    fun `automatic update check only shows available badge`() = runTest(dispatcher) {
        val update = AppUpdate("2.0.0", "https://example.com/release", null, null)
        val updateRepository = FakeAppUpdateRepository(AppResult.Success(update))
        val viewModel =
            createViewModel(
                updateRepository = updateRepository,
                isUpdateCheckEnabled = true,
            )

        viewModel.onAction(SettingsAction.OnStarted(AppLanguage.ENGLISH))

        assertEquals(update, viewModel.state.value.availableUpdate)
        assertFalse(viewModel.state.value.isUpdatePromptVisible)
        assertEquals(false, updateRepository.lastForce)
    }

    @Test
    fun `checking update with no update emits up-to-date message`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onAction(SettingsAction.OnCheckForUpdate)

        assertMessage(R.string.app_update_up_to_date, viewModel.events.first())
    }

    @Test
    fun `checking update on error emits failure message`() = runTest(dispatcher) {
        val viewModel =
            createViewModel(
                updateRepository = FakeAppUpdateRepository(AppResult.Error(DataError.NoInternet)),
            )

        viewModel.onAction(SettingsAction.OnCheckForUpdate)

        assertMessage(R.string.update_check_failed, viewModel.events.first())
    }

    @Test
    fun `dismissing update prompt hides it`() = runTest(dispatcher) {
        val update = AppUpdate("2.0.0", "https://example.com/release", null, null)
        val viewModel =
            createViewModel(updateRepository = FakeAppUpdateRepository(AppResult.Success(update)))
        viewModel.onAction(SettingsAction.OnCheckForUpdate)

        viewModel.onAction(SettingsAction.OnUpdatePromptDismissed)

        assertFalse(viewModel.state.value.isUpdatePromptVisible)
        assertEquals(update, viewModel.state.value.availableUpdate)
    }

    @Test
    fun `appVersion comes from config`() {
        val viewModel = createViewModel(appVersion = "1.2.3")

        assertEquals("1.2.3", viewModel.state.value.appVersion)
    }

    @Test
    fun `clearing history calls usecase and emits message`() = runTest(dispatcher) {
        val historyRepository = FakeSearchHistoryRepository()
        val viewModel = createViewModel(historyRepository = historyRepository)
        viewModel.onAction(SettingsAction.OnClearHistoryClick)

        viewModel.onAction(SettingsAction.OnClearHistoryConfirmed)

        assertEquals(1, historyRepository.clearInvocations)
        assertFalse(viewModel.state.value.isClearHistoryDialogVisible)
        assertMessage(R.string.clear_history_confirmed, viewModel.events.first())
    }

    @Test
    fun `privacy and terms clicks emit coming-soon message`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onAction(SettingsAction.OnPrivacyPolicyClick)
        assertMessage(R.string.coming_soon, viewModel.events.first())

        viewModel.onAction(SettingsAction.OnTermsClick)
        assertMessage(R.string.coming_soon, viewModel.events.first())
    }

    private fun createViewModel(
        repository: FakeSettingsRepository = FakeSettingsRepository(),
        scheduler: FakeScheduler = FakeScheduler(),
        updateRepository: FakeAppUpdateRepository = FakeAppUpdateRepository(),
        historyRepository: FakeSearchHistoryRepository = FakeSearchHistoryRepository(),
        appVersion: String = "1.0.0",
        isUpdateCheckEnabled: Boolean = false,
    ) =
        SettingsViewModel(
            repository = repository,
            scheduler = scheduler,
            appUpdateRepository = updateRepository,
            appUpdateConfig = AppUpdateConfig(appVersion, isUpdateCheckEnabled),
            clearSearchHistoryUseCase = ClearSearchHistoryUseCase(historyRepository),
        )

    private fun assertMessage(
        expectedResId: Int,
        event: SettingsEvent,
    ) {
        val message = (event as SettingsEvent.ShowMessage).message as UiText.StringResource
        assertEquals(expectedResId, message.id)
    }
}

private class FakeSettingsRepository : NotificationSettingsRepository {
    private val state = MutableStateFlow(NotificationSettings())
    var value: NotificationSettings
        get() = state.value
        set(newValue) { state.value = newValue }
    override val settings: Flow<NotificationSettings> = state

    override suspend fun setEnabled(type: ReminderType, enabled: Boolean) {
        value = value.with(type, value.preference(type).copy(enabled = enabled))
        state.value = value
    }

    override suspend fun setTime(type: ReminderType, time: ReminderTime) {
        value = value.with(type, value.preference(type).copy(time = time))
        state.value = value
    }

    private fun NotificationSettings.with(type: ReminderType, preference: ReminderPreference): NotificationSettings =
        when (type) {
            ReminderType.DAILY_WORD -> copy(dailyWord = preference)
            ReminderType.DAILY_PROVERB -> copy(dailyProverb = preference)
            ReminderType.BOOKMARK_REVIEW -> copy(bookmarkReview = preference)
        }
}

private class FakeScheduler : ReminderScheduler {
    var scheduledType: ReminderType? = null
    var scheduledTime: ReminderTime? = null
    var cancelledType: ReminderType? = null

    override fun schedule(type: ReminderType, time: ReminderTime) {
        scheduledType = type
        scheduledTime = time
    }

    override fun cancel(type: ReminderType) {
        cancelledType = type
    }
}

private class FakeAppUpdateRepository(
    var result: AppResult<AppUpdate?, DataError> = AppResult.Success(null),
) : AppUpdateRepository {
    var lastForce: Boolean? = null

    override suspend fun checkForUpdate(
        currentVersion: String,
        force: Boolean,
    ): AppResult<AppUpdate?, DataError> {
        lastForce = force
        return result
    }
}

private class FakeSearchHistoryRepository : SearchHistoryRepository {
    var clearInvocations = 0

    override suspend fun addToHistory(history: HistoryModel) = Unit

    override fun getAllHistories(): Flow<List<HistoryModel>> = MutableStateFlow(emptyList())

    override suspend fun clearHistory() {
        clearInvocations++
    }
}
