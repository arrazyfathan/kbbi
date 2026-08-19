package com.arrazyfathan.kbbi.feature.settings.presentation.settings

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
        val viewModel = SettingsViewModel(repository, scheduler)

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
        val viewModel = SettingsViewModel(repository, scheduler)
        val newTime = ReminderTime(8, 30)

        viewModel.onAction(SettingsAction.OnReminderTimeChanged(ReminderType.DAILY_WORD, newTime))

        assertEquals(newTime, scheduler.scheduledTime)
    }

    @Test
    fun `starting settings resolves current language`() = runTest(dispatcher) {
        val viewModel = SettingsViewModel(FakeSettingsRepository(), FakeScheduler())

        viewModel.onAction(SettingsAction.OnStarted(AppLanguage.INDONESIAN))

        assertEquals(AppLanguage.INDONESIAN, viewModel.state.value.selectedLanguage)
    }

    @Test
    fun `language picker actions update visibility`() = runTest(dispatcher) {
        val viewModel = SettingsViewModel(FakeSettingsRepository(), FakeScheduler())

        viewModel.onAction(SettingsAction.OnLanguageClick)
        assertTrue(viewModel.state.value.isLanguagePickerVisible)

        viewModel.onAction(SettingsAction.OnLanguagePickerDismissed)
        assertFalse(viewModel.state.value.isLanguagePickerVisible)
    }

    @Test
    fun `selecting another language updates state and emits apply event`() = runTest(dispatcher) {
        val viewModel = SettingsViewModel(FakeSettingsRepository(), FakeScheduler())
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
