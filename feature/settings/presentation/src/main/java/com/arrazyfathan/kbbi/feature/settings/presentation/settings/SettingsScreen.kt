package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.presentation.AppUpdatePrompt
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.designsystem.components.KBBITimePickerBottomSheet
import com.arrazyfathan.kbbi.core.presentation.designsystem.palette
import com.arrazyfathan.kbbi.core.presentation.designsystem.perform
import com.arrazyfathan.kbbi.feature.settings.domain.model.AppIcon
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

private const val LANGUAGE_FADE_OUT_DURATION_MILLIS = 100
private const val LANGUAGE_FADE_IN_DURATION_MILLIS = 180
private const val LANGUAGE_CONFIGURATION_TIMEOUT_MILLIS = 1_000L
private const val APP_ICON_COLUMN_COUNT = 6
internal const val LANGUAGE_TRANSITION_OVERLAY_TEST_TAG = "language_transition_overlay"
internal const val APP_ICON_OPTION_TEST_TAG_PREFIX = "app_icon_option_"
internal const val APP_ICON_SELECTED_INDICATOR_TEST_TAG_PREFIX = "app_icon_selected_indicator_"
private val languageTransitionEasing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

@Composable
fun SettingsRoute(
    onHaptic: (KBBIHapticType) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsConditions: () -> Unit,
    onOpenSourceLicenses: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val analyticsReporter: AnalyticsReporter = koinInject()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val platformHapticFeedback = LocalHapticFeedback.current
    val shareAppText = stringResource(R.string.share_app_text)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configurationLanguage =
        resolveAppLanguage(
            applicationLanguageTags = ConfigurationCompat.getLocales(configuration).asLanguageTags(),
            systemLanguageTags = emptyList(),
        )
    val currentConfigurationLanguage by rememberUpdatedState(configurationLanguage)
    val currentOnHaptic by rememberUpdatedState(onHaptic)
    val languageOverlayAlpha = remember { Animatable(0f) }
    var isLanguageTransitionActive by remember { mutableStateOf(false) }
    var permissionType by remember { mutableStateOf<ReminderType?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionType?.let { viewModel.onAction(SettingsAction.OnPermissionResult(it, granted)) }
            permissionType = null
        }

    LaunchedEffect(viewModel) {
        viewModel.onAction(SettingsAction.OnStarted(configurationLanguage))
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ApplyLanguage -> {
                    currentOnHaptic(KBBIHapticType.Selection)
                    isLanguageTransitionActive = true
                    try {
                        languageOverlayAlpha.animateTo(
                            targetValue = 1f,
                            animationSpec =
                                tween(
                                    durationMillis = LANGUAGE_FADE_OUT_DURATION_MILLIS,
                                    easing = languageTransitionEasing,
                                ),
                        )
                        withContext(Dispatchers.Main.immediate) {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(event.language.languageTag),
                            )
                        }
                        withTimeoutOrNull(LANGUAGE_CONFIGURATION_TIMEOUT_MILLIS.milliseconds) {
                            snapshotFlow { currentConfigurationLanguage }.first { it == event.language }
                        }
                        languageOverlayAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec =
                                tween(
                                    durationMillis = LANGUAGE_FADE_IN_DURATION_MILLIS,
                                    easing = languageTransitionEasing,
                                ),
                        )
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Exception) {
                        // The finally block restores the screen if locale application fails.
                    } finally {
                        withContext(NonCancellable) { languageOverlayAlpha.snapTo(0f) }
                        isLanguageTransitionActive = false
                    }
                }

                is SettingsEvent.RequestNotificationPermission -> {
                    permissionType = event.type
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.onAction(SettingsAction.OnPermissionResult(event.type, true))
                    }
                }

                SettingsEvent.PermissionDenied -> {
                    permissionDenied = true
                    onHaptic(KBBIHapticType.Reject)
                }

                is SettingsEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                    onHaptic(
                        if (event.isError) KBBIHapticType.Reject else KBBIHapticType.Confirm,
                    )
                }

                is SettingsEvent.ReminderChanged -> {
                    onHaptic(
                        if (event.enabled) KBBIHapticType.ToggleOn else KBBIHapticType.ToggleOff,
                    )
                }

                is SettingsEvent.HapticsChanged -> {
                    platformHapticFeedback.perform(
                        if (event.enabled) KBBIHapticType.ToggleOn else KBBIHapticType.ToggleOff,
                    )
                }

                SettingsEvent.SelectionChanged -> {
                    onHaptic(KBBIHapticType.Selection)
                }

                SettingsEvent.AppIconChanged -> {
                    onHaptic(KBBIHapticType.Selection)
                }
            }
        }
    }

    LaunchedEffect(configurationLanguage) {
        viewModel.onAction(SettingsAction.OnLanguageConfigurationChanged(configurationLanguage))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SettingsScreen(
            state = state,
            permissionDenied = permissionDenied,
            onNavigateBack = onNavigateBack,
            onOpenPrivacyPolicy = onOpenPrivacyPolicy,
            onOpenTermsConditions = onOpenTermsConditions,
            onAction = viewModel::onAction,
            onOpenSystemSettings = {
                val intent =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                    } else {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    }
                context.startActivity(intent)
            },
            onShareApp = {
                val shareIntent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "$shareAppText\nhttps://github.com/arrazyfathan/kbbi",
                        )
                    }
                context.startActivity(Intent.createChooser(shareIntent, null))
                analyticsReporter.log(AnalyticsEvent.AppShared)
                onHaptic(KBBIHapticType.ContextClick)
            },
            onOpenUri = { uri ->
                context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
            },
            onOpenSourceLicenses = onOpenSourceLicenses,
        )

        if (isLanguageTransitionActive) {
            LanguageTransitionOverlay(
                alpha = { languageOverlayAlpha.value },
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    permissionDenied: Boolean = false,
    onNavigateBack: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenTermsConditions: () -> Unit = {},
    onOpenSystemSettings: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onOpenUri: (String) -> Unit = {},
    onOpenSourceLicenses: () -> Unit = {},
) {
    var timePickerType by remember { mutableStateOf<ReminderType?>(null) }
    var isLanguageSelectionInProgress by remember { mutableStateOf(false) }
    val selectedPreference = timePickerType?.let { state.notifications.preference(it) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val languagePickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SettingsTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AnimatedVisibility(
                visible =
                    permissionDenied ||
                        (state.notifications.permissionRequired && !state.notifications.permissionGranted),
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
            ) {
                PermissionBanner(onOpenSystemSettings = onOpenSystemSettings)
            }
            ReminderSection(
                state = state,
                onAction = onAction,
                onTimeClick = { timePickerType = it },
            )
            AppearanceSection(
                selectedTheme = state.selectedTheme,
                selectedAppIcon = state.selectedAppIcon,
                onThemeSelected = { onAction(SettingsAction.OnThemeSelected(it)) },
                onAppIconSelected = { onAction(SettingsAction.OnAppIconSelected(it)) },
            )
            InteractionSection(
                hapticsEnabled = state.hapticsEnabled,
                onHapticsChanged = { onAction(SettingsAction.OnHapticsToggled(it)) },
            )
            PrivacyDiagnosticsSection(
                crashReportingEnabled = state.crashReportingEnabled,
                analyticsEnabled = state.analyticsEnabled,
                performanceMonitoringEnabled = state.performanceMonitoringEnabled,
                onCrashReportingChanged = { onAction(SettingsAction.OnCrashReportingToggled(it)) },
                onAnalyticsChanged = { onAction(SettingsAction.OnAnalyticsToggled(it)) },
                onPerformanceMonitoringChanged = { onAction(SettingsAction.OnPerformanceMonitoringToggled(it)) },
            )
            LanguageSection(
                selectedLanguage = state.selectedLanguage,
                onClick = { onAction(SettingsAction.OnLanguageClick) },
            )
            DataSection(onAction = onAction)
            AboutSection(
                state = state,
                onAction = onAction,
                onShareApp = onShareApp,
                onOpenUri = onOpenUri,
                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                onOpenTermsConditions = onOpenTermsConditions,
                onOpenSourceLicenses = onOpenSourceLicenses,
            )
        }
    }

    if (timePickerType != null && selectedPreference != null) {
        KBBITimePickerBottomSheet(
            initialHour = selectedPreference.time.hour,
            initialMinute = selectedPreference.time.minute,
            onDismissRequest = { timePickerType = null },
            onConfirm = { hour, minute ->
                onAction(
                    SettingsAction.OnReminderTimeChanged(
                        timePickerType!!,
                        ReminderTime(hour, minute),
                    ),
                )
                timePickerType = null
            },
        )
    }

    if (state.isLanguagePickerVisible) {
        LanguagePickerBottomSheet(
            sheetState = languagePickerSheetState,
            selectedLanguage = state.selectedLanguage,
            isSelectionInProgress = isLanguageSelectionInProgress,
            onLanguageSelected = { language ->
                if (!isLanguageSelectionInProgress) {
                    isLanguageSelectionInProgress = true
                    coroutineScope.launch {
                        try {
                            languagePickerSheetState.hide()
                            if (!languagePickerSheetState.isVisible) {
                                onAction(SettingsAction.OnLanguageSelected(language))
                            }
                        } finally {
                            isLanguageSelectionInProgress = false
                        }
                    }
                }
            },
            onDismissRequest = {
                if (!isLanguageSelectionInProgress) {
                    onAction(SettingsAction.OnLanguagePickerDismissed)
                }
            },
        )
    }

    if (state.isUpdatePromptVisible && state.availableUpdate != null) {
        AppUpdatePrompt(
            update = state.availableUpdate,
            currentVersion = state.appVersion,
            onDismiss = { onAction(SettingsAction.OnUpdatePromptDismissed) },
        )
    }

    if (state.isClearHistoryDialogVisible) {
        AlertDialog(
            onDismissRequest = { onAction(SettingsAction.OnClearHistoryDismissed) },
            title = { Text(stringResource(R.string.clear_history_confirm_title)) },
            text = { Text(stringResource(R.string.clear_history_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { onAction(SettingsAction.OnClearHistoryConfirmed) }) {
                    Text(stringResource(R.string.clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(SettingsAction.OnClearHistoryDismissed) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (state.pendingAppIcon != null) {
        AppIconChangeBottomSheet(
            onDismissRequest = { onAction(SettingsAction.OnAppIconChangeDismissed) },
            onConfirm = { onAction(SettingsAction.OnAppIconChangeConfirmed) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppIconChangeBottomSheet(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.app_icon_change_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.app_icon_change_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.app_icon_change_dialog_confirm))
                }
            }
        }
    }
}

@Composable
private fun AppearanceSection(
    selectedTheme: AppTheme,
    selectedAppIcon: AppIcon,
    onThemeSelected: (AppTheme) -> Unit,
    onAppIconSelected: (AppIcon) -> Unit,
) {
    SettingsSectionCard(title = stringResource(R.string.appearance_section_title)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.theme_picker_description),
                style = MaterialTheme.typography.bodySmall,
                color = TextP,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppTheme.entries.chunked(2).forEach { themes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        themes.forEach { theme ->
                            ThemeOption(
                                theme = theme,
                                selected = theme == selectedTheme,
                                onClick = { onThemeSelected(theme) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (themes.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = stringResource(R.string.app_icon_picker_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Text(
                text = stringResource(R.string.app_icon_picker_description),
                style = MaterialTheme.typography.bodySmall,
                color = TextP,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppIcon.entries.chunked(APP_ICON_COLUMN_COUNT).forEach { icons ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        icons.forEach { icon ->
                            AppIconOption(
                                icon = icon,
                                selected = icon == selectedAppIcon,
                                onClick = { onAppIconSelected(icon) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(APP_ICON_COLUMN_COUNT - icons.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconOption(
    icon: AppIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(icon.labelResId)
    val shape = RoundedCornerShape(100.dp)
    val accessibilityDescription = stringResource(R.string.app_icon_preview_accessibility, label)
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .testTag("$APP_ICON_OPTION_TEST_TAG_PREFIX${icon.identifier}")
                .clip(shape)
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    shape = shape,
                ).selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).semantics { contentDescription = accessibilityDescription },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(icon.previewResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
        )
        if (selected) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(24.dp)
                            .testTag("$APP_ICON_SELECTED_INDICATOR_TEST_TAG_PREFIX${icon.identifier}")
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

private val AppIcon.labelResId: Int
    get() =
        when (this) {
            AppIcon.DEFAULT -> R.string.app_icon_default
            AppIcon.ROYAL_OCEAN -> R.string.app_icon_royal_ocean
            AppIcon.GOLDEN_SUNSET -> R.string.app_icon_golden_sunset
            AppIcon.GOLDEN_CORAL_ENERGY -> R.string.app_icon_golden_coral_energy
            AppIcon.DEEP_FOREST_ENERGY -> R.string.app_icon_deep_forest_energy
            AppIcon.NEON_VIOLET -> R.string.app_icon_neon_violet
            AppIcon.BLAZE_ORANGE -> R.string.app_icon_blaze_orange
        }

private val AppIcon.previewResId: Int
    get() =
        when (this) {
            AppIcon.DEFAULT -> R.drawable.app_icon_preview_default
            AppIcon.ROYAL_OCEAN -> R.drawable.app_icon_preview_royal_ocean
            AppIcon.GOLDEN_SUNSET -> R.drawable.app_icon_preview_golden_sunset
            AppIcon.GOLDEN_CORAL_ENERGY -> R.drawable.app_icon_preview_golden_coral_energy
            AppIcon.DEEP_FOREST_ENERGY -> R.drawable.app_icon_preview_deep_forest_energy
            AppIcon.NEON_VIOLET -> R.drawable.app_icon_preview_neon_violet
            AppIcon.BLAZE_ORANGE -> R.drawable.app_icon_preview_blaze_orange
        }

@Composable
private fun ThemeOption(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = theme.palette
    Card(
        modifier =
            modifier.selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        shape = RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        border =
            BorderStroke(
                width = if (selected) 2.dp else 1.dp,
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(2f)) {
                    Box(
                        modifier = Modifier.size(28.dp).background(palette.primary, CircleShape),
                    )
                    Box(
                        modifier =
                            Modifier
                                .offset(x = 18.dp)
                                .size(28.dp)
                                .background(palette.secondary, CircleShape)
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                text = stringResource(theme.labelResId),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
            )
        }
    }
}

private val AppTheme.labelResId: Int
    get() =
        when (this) {
            AppTheme.ROYAL_OCEAN -> R.string.theme_royal_ocean
            AppTheme.GOLDEN_SUNSET -> R.string.theme_golden_sunset
            AppTheme.GOLDEN_CORAL_ENERGY -> R.string.theme_golden_coral_energy
            AppTheme.DEEP_FOREST_ENERGY -> R.string.theme_deep_forest_energy
            AppTheme.NEON_VIOLET -> R.string.theme_neon_violet
            AppTheme.BLAZE_ORANGE -> R.string.theme_blaze_orange
        }

@Composable
private fun InteractionSection(
    hapticsEnabled: Boolean,
    onHapticsChanged: (Boolean) -> Unit,
) {
    SettingsSectionCard(title = stringResource(R.string.interaction_section_title)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = hapticsEnabled,
                        role = Role.Switch,
                        onValueChange = onHapticsChanged,
                    ).padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_vibration),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.haptic_feedback_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = TextH1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.haptic_feedback_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextP,
                )
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                modifier = Modifier.scale(0.7f),
                checked = hapticsEnabled,
                onCheckedChange = null,
            )
        }
    }
}

@Composable
private fun PrivacyDiagnosticsSection(
    crashReportingEnabled: Boolean,
    analyticsEnabled: Boolean,
    performanceMonitoringEnabled: Boolean,
    onCrashReportingChanged: (Boolean) -> Unit,
    onAnalyticsChanged: (Boolean) -> Unit,
    onPerformanceMonitoringChanged: (Boolean) -> Unit,
) {
    SettingsSectionCard(title = stringResource(R.string.privacy_diagnostics_section_title)) {
        ReportingToggleRow(
            iconRes = R.drawable.ic_bug,
            title = stringResource(R.string.crash_reporting_title),
            description = stringResource(R.string.crash_reporting_description),
            enabled = crashReportingEnabled,
            onToggle = onCrashReportingChanged,
        )
        SettingsDivider()
        ReportingToggleRow(
            iconRes = R.drawable.ic_analytics,
            title = stringResource(R.string.usage_analytics_title),
            description = stringResource(R.string.usage_analytics_description),
            enabled = analyticsEnabled,
            onToggle = onAnalyticsChanged,
        )
        SettingsDivider()
        ReportingToggleRow(
            iconRes = R.drawable.ic_speed,
            title = stringResource(R.string.performance_monitoring_title),
            description = stringResource(R.string.performance_monitoring_description),
            enabled = performanceMonitoringEnabled,
            onToggle = onPerformanceMonitoringChanged,
        )
    }
}

@Composable
private fun ReportingToggleRow(
    iconRes: Int,
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(value = enabled, role = Role.Switch, onValueChange = onToggle)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = TextH1)
            Spacer(Modifier.height(2.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextP)
        }
        Spacer(Modifier.width(8.dp))
        Switch(modifier = Modifier.scale(0.7f), checked = enabled, onCheckedChange = null)
    }
}

private fun LocaleListCompat.asLanguageTags(): List<String> = toLanguageTags().split(',').filter(String::isNotBlank)

@Composable
internal fun LanguageTransitionOverlay(
    alpha: () -> Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .graphicsLayer { this.alpha = alpha() }
                .background(MaterialTheme.colorScheme.background)
                .testTag(LANGUAGE_TRANSITION_OVERLAY_TEST_TAG)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                        }
                    }
                },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
) {
    val isCollapsed = scrollBehavior.state.collapsedFraction > 0.5f

    MediumTopAppBar(
        modifier =
            Modifier.background(
                brush =
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                    ),
            ),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        title = {
            Column {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = if (isCollapsed) 20.sp else 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isCollapsed) {
                    Text(
                        text = stringResource(R.string.settings_menu_subtitle),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun AboutSection(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onShareApp: () -> Unit,
    onOpenUri: (String) -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTermsConditions: () -> Unit,
    onOpenSourceLicenses: () -> Unit,
) {
    val updateSubtitle =
        when {
            state.isCheckingUpdate -> {
                stringResource(R.string.update_checking)
            }

            state.availableUpdate != null -> {
                stringResource(R.string.app_update_available, state.availableUpdate.latestVersion)
            }

            else -> {
                stringResource(R.string.app_update_up_to_date)
            }
        }

    SettingsSectionCard(title = stringResource(R.string.about_section_title)) {
        SettingsMenuRow(
            title = stringResource(R.string.app_update_title),
            subtitle = updateSubtitle,
            icon = R.drawable.ic_update,
            showNewBadge = state.availableUpdate != null,
            onClick = { onAction(SettingsAction.OnCheckForUpdate) },
        )
        SettingsDivider()
        SettingsMenuRow(
            title = stringResource(R.string.app_version_title),
            icon = R.drawable.ic_info,
            trailingText = state.appVersion,
        )
        SettingsDivider()
        SettingsMenuRow(
            title = stringResource(R.string.share_app_title),
            icon = R.drawable.share,
            onClick = onShareApp,
        )
        SettingsDivider()
        SettingsMenuRow(
            title = stringResource(R.string.report_bug_title),
            icon = R.drawable.ic_bug,
            onClick = { onOpenUri(REPORT_BUG_URL) },
        )
        SettingsDivider()
        SettingsMenuRow(
            title = stringResource(R.string.privacy_policy_title),
            icon = R.drawable.ic_privacy,
            onClick = onOpenPrivacyPolicy,
        )
        SettingsDivider()
        SettingsMenuRow(
            title = stringResource(R.string.terms_condition_title),
            icon = R.drawable.ic_document,
            onClick = onOpenTermsConditions,
        )
        SettingsDivider()
        SettingsMenuRow(
            title = stringResource(R.string.open_source_licenses_title),
            icon = R.drawable.ic_code,
            onClick = onOpenSourceLicenses,
        )
    }
}

@Composable
private fun DataSection(onAction: (SettingsAction) -> Unit) {
    SettingsSectionCard(title = stringResource(R.string.data_section_title)) {
        SettingsMenuRow(
            title = stringResource(R.string.clear_search_history_title),
            icon = R.drawable.ic_history,
            onClick = { onAction(SettingsAction.OnClearHistoryClick) },
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextH1,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
            SettingsDivider()
            content()
        }
    }
}

@Composable
private fun SettingsMenuRow(
    title: String,
    subtitle: String? = null,
    icon: Int? = null,
    trailingText: String? = null,
    showNewBadge: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextH1,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextP,
                )
            }
        }
        if (showNewBadge) {
            Text(
                text = stringResource(R.string.new_badge),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextP,
            )
        } else if (onClick != null) {
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.background)
}

@Composable
private fun LanguageSection(
    selectedLanguage: AppLanguage,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.language_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = TextH1,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.background,
            )
            Row(
                modifier =
                    Modifier
                        .clickable(onClick = onClick)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LanguageBadge(language = selectedLanguage)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_language_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextH1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(selectedLanguage.labelResId),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextP,
                    )
                }
                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clearAndSetSemantics { },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePickerBottomSheet(
    sheetState: SheetState,
    selectedLanguage: AppLanguage,
    isSelectionInProgress: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().selectableGroup().padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.choose_language_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextH1,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            AppLanguage.entries.forEach { language ->
                LanguageOptionRow(
                    language = language,
                    selected = language == selectedLanguage,
                    enabled = !isSelectionInProgress,
                    onClick = { onLanguageSelected(language) },
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionRow(
    language: AppLanguage,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    enabled = enabled,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LanguageBadge(language = language)
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(language.labelResId),
            style = MaterialTheme.typography.bodyLarge,
            color = TextH1,
            modifier = Modifier.weight(1f),
        )
        RadioButton(
            selected = selected,
            enabled = enabled,
            onClick = null,
        )
    }
}

@Composable
private fun LanguageBadge(language: AppLanguage) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .background(
                    brush =
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                        ),
                    CircleShape,
                ).clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = language.badgeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

private val AppLanguage.labelResId: Int
    get() =
        when (this) {
            AppLanguage.INDONESIAN -> R.string.language_indonesian
            AppLanguage.ENGLISH -> R.string.language_english
        }

private val AppLanguage.badgeLabel: String
    get() =
        when (this) {
            AppLanguage.INDONESIAN -> "ID"
            AppLanguage.ENGLISH -> "EN"
        }

@Composable
private fun ReminderSection(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onTimeClick: (ReminderType) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.notification_reminders_section),
                style = MaterialTheme.typography.titleMedium,
                color = TextH1,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.background,
            )
            ReminderRow(
                icon = R.drawable.word,
                title = stringResource(R.string.notification_daily_word),
                description = stringResource(R.string.notification_daily_word_description),
                enabled = state.notifications.dailyWord.enabled,
                timeLabel =
                    "%02d:%02d".format(
                        state.notifications.dailyWord.time.hour,
                        state.notifications.dailyWord.time.minute,
                    ),
                onToggle = { onAction(SettingsAction.OnReminderToggled(ReminderType.DAILY_WORD, it)) },
                onTimeClick = { onTimeClick(ReminderType.DAILY_WORD) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.background,
            )
            ReminderRow(
                icon = R.drawable.ic_proverb,
                title = stringResource(R.string.notification_daily_proverb),
                description = stringResource(R.string.notification_daily_proverb_description),
                enabled = state.notifications.dailyProverb.enabled,
                timeLabel =
                    "%02d:%02d".format(
                        state.notifications.dailyProverb.time.hour,
                        state.notifications.dailyProverb.time.minute,
                    ),
                onToggle = { onAction(SettingsAction.OnReminderToggled(ReminderType.DAILY_PROVERB, it)) },
                onTimeClick = { onTimeClick(ReminderType.DAILY_PROVERB) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 18.dp),
                color = MaterialTheme.colorScheme.background,
            )
            ReminderRow(
                icon = R.drawable.saved,
                title = stringResource(R.string.notification_bookmark_review),
                description = stringResource(R.string.notification_bookmark_review_description),
                enabled = state.notifications.bookmarkReview.enabled,
                timeLabel =
                    "%02d:%02d".format(
                        state.notifications.bookmarkReview.time.hour,
                        state.notifications.bookmarkReview.time.minute,
                    ),
                onToggle = { onAction(SettingsAction.OnReminderToggled(ReminderType.BOOKMARK_REVIEW, it)) },
                onTimeClick = { onTimeClick(ReminderType.BOOKMARK_REVIEW) },
            )
        }
    }
}

@Composable
private fun ReminderRow(
    icon: Int,
    title: String,
    description: String,
    enabled: Boolean,
    timeLabel: String,
    onToggle: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
) {
    val titleColor = if (enabled) TextH1 else TextH1.copy(alpha = 0.58f)
    val descriptionColor = if (enabled) TextP else TextP.copy(alpha = 0.58f)

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.45f),
            modifier = Modifier.size(18.dp),
        )

        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = descriptionColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            AssistChip(
                onClick = onTimeClick,
                label = {
                    Text(
                        stringResource(
                            R.string.notification_time_label,
                            timeLabel,
                        ),
                    )
                },
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(modifier = Modifier.scale(0.7f), checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun PermissionBanner(onOpenSystemSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.notification_permission_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.notification_permission_denied),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(onClick = onOpenSystemSettings) {
                Text(stringResource(R.string.notification_open_system_settings))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsPreview() {
    KBBITheme(theme = AppTheme.GOLDEN_SUNSET) {
        SettingsScreen(
            state = SettingsState(selectedTheme = AppTheme.GOLDEN_SUNSET),
            onNavigateBack = {},
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppIconOptionPreview() {
    KBBITheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppIconOption(
                icon = AppIcon.DEFAULT,
                selected = true,
                onClick = {},
                modifier = Modifier.size(80.dp),
            )
            AppIconOption(
                icon = AppIcon.ROYAL_OCEAN,
                selected = false,
                onClick = {},
                modifier = Modifier.size(80.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppearanceSectionPreview() {
    KBBITheme {
        AppearanceSection(
            selectedTheme = AppTheme.ROYAL_OCEAN,
            selectedAppIcon = AppIcon.DEFAULT,
            onThemeSelected = {},
            onAppIconSelected = {},
        )
    }
}

private const val REPORT_BUG_URL = "https://github.com/arrazyfathan/kbbi/issues"
