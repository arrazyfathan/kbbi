package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.LocaleManagerCompat
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.presentation.AppUpdatePrompt
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueSecondary
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.designsystem.components.KBBITimePickerBottomSheet
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onOpenSourceLicenses: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val context = LocalContext.current
    val shareAppText = stringResource(R.string.share_app_text)
    val state by viewModel.state.collectAsStateWithLifecycle()
    var permissionType by remember { mutableStateOf<ReminderType?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionType?.let { viewModel.onAction(SettingsAction.OnPermissionResult(it, granted)) }
            permissionType = null
        }

    LaunchedEffect(Unit) {
        viewModel.onAction(SettingsAction.OnStarted(resolveCurrentAppLanguage(context)))
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ApplyLanguage -> {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(event.language.languageTag),
                    )
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
                }

                is SettingsEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    SettingsScreen(
        state = state,
        permissionDenied = permissionDenied,
        onNavigateBack = onNavigateBack,
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
        },
        onOpenUri = { uri ->
            context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
        },
        onOpenSourceLicenses = onOpenSourceLicenses,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    permissionDenied: Boolean = false,
    onNavigateBack: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    onOpenSystemSettings: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onOpenUri: (String) -> Unit = {},
    onOpenSourceLicenses: () -> Unit = {},
) {
    var timePickerType by remember { mutableStateOf<ReminderType?>(null) }
    val selectedPreference = timePickerType?.let { state.notifications.preference(it) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = BlueBg,
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
            selectedLanguage = state.selectedLanguage,
            onLanguageSelected = { onAction(SettingsAction.OnLanguageSelected(it)) },
            onDismissRequest = { onAction(SettingsAction.OnLanguagePickerDismissed) },
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
}

private fun resolveCurrentAppLanguage(context: Context): AppLanguage =
    resolveAppLanguage(
        applicationLanguageTags = LocaleManagerCompat.getApplicationLocales(context).asLanguageTags(),
        systemLanguageTags = LocaleManagerCompat.getSystemLocales(context).asLanguageTags(),
    )

private fun LocaleListCompat.asLanguageTags(): List<String> = toLanguageTags().split(',').filter(String::isNotBlank)

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
                brush = Brush.verticalGradient(listOf(BlueSecondary, BluePrimary)),
            ),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = Color.White,
                )
            }
        },
        title = {
            Column {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = if (isCollapsed) 20.sp else 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isCollapsed) {
                    Text(
                        text = stringResource(R.string.settings_menu_subtitle),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.82f),
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
                titleContentColor = Color.White,
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
    onOpenSourceLicenses: () -> Unit,
) {
    val updateSubtitle =
        when {
            state.isCheckingUpdate -> stringResource(R.string.update_checking)
            state.availableUpdate != null ->
                stringResource(R.string.app_update_available, state.availableUpdate.latestVersion)
            else -> stringResource(R.string.app_update_up_to_date)
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
            onClick = { onAction(SettingsAction.OnPrivacyPolicyClick) },
        )
        SettingsDivider()
        SettingsMenuRow(
            title = stringResource(R.string.terms_condition_title),
            icon = R.drawable.ic_document,
            onClick = { onAction(SettingsAction.OnTermsClick) },
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
                tint = BluePrimary,
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
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier
                        .background(BluePrimary, RoundedCornerShape(50))
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
                color = BluePrimary,
                modifier = Modifier.clearAndSetSemantics { },
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = BlueBg)
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = BlueBg)
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
                    color = BluePrimary,
                    modifier = Modifier.clearAndSetSemantics { },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePickerBottomSheet(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
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
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
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
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
private fun LanguageBadge(language: AppLanguage) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(BlueSecondary, BluePrimary)),
                    CircleShape,
                ).clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = language.badgeLabel,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = BlueBg)
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = BlueBg)
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp), color = BlueBg)
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
            tint = BluePrimary.copy(alpha = if (enabled) 1f else 0.45f),
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
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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
    KBBITheme { SettingsScreen(SettingsState(), onNavigateBack = {}, onAction = {}) }
}

private const val REPORT_BUG_URL = "https://github.com/arrazyfathan/kbbi/issues"
