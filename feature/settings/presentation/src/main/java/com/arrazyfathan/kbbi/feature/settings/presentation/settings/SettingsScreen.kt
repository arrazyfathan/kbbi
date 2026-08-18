package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
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
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var permissionType by remember { mutableStateOf<ReminderType?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionType?.let { viewModel.onAction(SettingsAction.OnPermissionResult(it, granted)) }
            permissionType = null
        }

    LaunchedEffect(Unit) {
        viewModel.onAction(SettingsAction.OnStarted)
        viewModel.events.collect { event ->
            when (event) {
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
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
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
private fun ReminderSection(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onTimeClick: (ReminderType) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
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
                timeLabel = "%02d:%02d".format(
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
                timeLabel = "%02d:%02d".format(
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
                timeLabel = "%02d:%02d".format(
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
