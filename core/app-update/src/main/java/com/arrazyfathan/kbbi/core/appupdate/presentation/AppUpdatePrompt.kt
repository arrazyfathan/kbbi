package com.arrazyfathan.kbbi.core.appupdate.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadState
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateInstallLauncher
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueSecondary
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextPrimary
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdatePrompt(
    modifier: Modifier = Modifier,
    update: AppUpdate,
    currentVersion: String,
    onDismiss: () -> Unit,
) {
    val installLauncher: AppUpdateInstallLauncher = koinInject()
    val viewModel: AppUpdateDownloadViewModel = koinViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val downloadState = state.downloadState.forVersion(update.latestVersion)
    val isDownloading = downloadState is AppUpdateDownloadState.Downloading

    LaunchedEffect(update) {
        viewModel.onAction(AppUpdateDownloadAction.OnPromptShown(update))
    }
    LaunchedEffect(viewModel.events, context) {
        viewModel.events.collect { event ->
            when (event) {
                is AppUpdateDownloadEvent.LaunchInstaller -> {
                    installLauncher.launch(event.downloadId)
                }
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> viewModel.onAction(AppUpdateDownloadAction.OnHostResumed)
                    Lifecycle.Event.ON_PAUSE -> viewModel.onAction(AppUpdateDownloadAction.OnHostPaused)
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            viewModel.onAction(AppUpdateDownloadAction.OnHostResumed)
        }
        onDispose {
            viewModel.onAction(AppUpdateDownloadAction.OnHostPaused)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        modifier = modifier,
    ) {
        AppUpdatePromptContent(
            update = update,
            currentVersion = currentVersion,
            downloadState = downloadState,
            onDownload = { viewModel.onAction(AppUpdateDownloadAction.OnDownloadClick) },
            onOpenRelease = {
                context.startActivity(Intent(Intent.ACTION_VIEW, update.releaseUrl.toUri()))
                onDismiss()
            },
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun AppUpdatePromptContent(
    modifier: Modifier = Modifier,
    update: AppUpdate,
    currentVersion: String,
    downloadState: AppUpdateDownloadState,
    onDownload: () -> Unit,
    onOpenRelease: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isDownloading = downloadState is AppUpdateDownloadState.Downloading

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UpdateBadge()

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.update_available_title),
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.update_available_subtitle),
                    color = TextP,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        VersionComparison(
            currentVersion = currentVersion,
            latestVersion = update.latestVersion,
        )

        update.releaseNotes?.let { notes ->
            Spacer(modifier = Modifier.height(18.dp))
            ReleaseNotes(notes = notes)
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (update.downloadUrl != null) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).height(44.dp),
                    onClick = onOpenRelease,
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.update_open_release_action),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = BluePrimary,
                    )
                }
            }

            TextButton(
                modifier = Modifier.weight(1f).height(44.dp),
                onClick = onDismiss,
                enabled = !isDownloading,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.update_later_action),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = TextP,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            onClick = if (update.downloadUrl == null) onOpenRelease else onDownload,
            enabled = !isDownloading,
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    contentColor = Color.White,
                ),
        ) {
            Text(
                text =
                    stringResource(
                        id = downloadActionLabel(update, downloadState),
                        downloadState.progressArgument(),
                    ),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }
}

private fun AppUpdateDownloadState.forVersion(version: String): AppUpdateDownloadState =
    when (this) {
        is AppUpdateDownloadState.Downloading -> if (this.version == version) this else AppUpdateDownloadState.Idle
        is AppUpdateDownloadState.Ready -> if (this.version == version) this else AppUpdateDownloadState.Idle
        is AppUpdateDownloadState.Failed -> if (this.version == version) this else AppUpdateDownloadState.Idle
        AppUpdateDownloadState.Idle -> this
    }

private fun downloadActionLabel(
    update: AppUpdate,
    state: AppUpdateDownloadState,
): Int =
    when {
        update.downloadUrl == null -> {
            R.string.update_open_release_action
        }

        state is AppUpdateDownloadState.Downloading && state.progressPercent != null -> {
            R.string.update_downloading_progress_action
        }

        state is AppUpdateDownloadState.Downloading -> {
            R.string.update_downloading_action
        }

        state is AppUpdateDownloadState.Ready -> {
            R.string.update_install_action
        }

        state is AppUpdateDownloadState.Failed -> {
            R.string.update_retry_action
        }

        else -> {
            R.string.update_download_action
        }
    }

private fun AppUpdateDownloadState.progressArgument(): Any =
    (this as? AppUpdateDownloadState.Downloading)?.progressPercent ?: ""

@Composable
private fun UpdateBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = BlueBg,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(id = R.string.update_badge_text),
                color = BluePrimary,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
private fun VersionComparison(
    modifier: Modifier = Modifier,
    currentVersion: String,
    latestVersion: String,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = BlueBg,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VersionPill(
                label = stringResource(id = R.string.update_current_version_label),
                version = currentVersion,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(id = R.string.update_version_arrow),
                color = BlueSecondary,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            VersionPill(
                label = stringResource(id = R.string.update_latest_version_label),
                version = latestVersion,
                emphasized = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun VersionPill(
    modifier: Modifier = Modifier,
    label: String,
    version: String,
    emphasized: Boolean = false,
) {
    val containerColor = if (emphasized) BluePrimary else Color.White

    Column(
        modifier =
            modifier
                .background(color = containerColor, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            color = if (emphasized) Color.White.copy(alpha = 0.78f) else TextP,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = version,
            color = if (emphasized) Color.White else TextPrimary,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReleaseNotes(
    notes: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.update_release_notes_title),
            color = TextH1,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF7F9FA),
        ) {
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                text = notes,
                color = TextPrimary,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VersionComparisonPreview() {
    KBBITheme {
        VersionComparison(
            currentVersion = "1.0.0",
            latestVersion = "1.1.0",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReleaseNotesPreview() {
    KBBITheme {
        ReleaseNotes(
            notes = "• Added new features\n• Fixed bugs\n• Improved performance",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppUpdatePromptContentPreview() {
    KBBITheme {
        AppUpdatePromptContent(
            update =
                AppUpdate(
                    latestVersion = "1.1.0",
                    releaseUrl = "https://github.com/arrazyfathan/kbbi-android/releases/tag/v1.1.0",
                    downloadUrl = "https://github.com/arrazyfathan/kbbi-android/releases/download/v1.1.0/kbbi.apk",
                    releaseNotes = "• Added new features\n• Fixed bugs\n• Improved performance",
                ),
            currentVersion = "1.0.0",
            downloadState = AppUpdateDownloadState.Idle,
            onDownload = {},
            onOpenRelease = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppUpdateDownloadingPromptContentPreview() {
    KBBITheme {
        AppUpdatePromptContent(
            update =
                AppUpdate(
                    latestVersion = "1.1.0",
                    releaseUrl = "https://github.com/arrazyfathan/kbbi-android/releases/tag/v1.1.0",
                    downloadUrl = "https://github.com/arrazyfathan/kbbi-android/releases/download/v1.1.0/kbbi.apk",
                    releaseNotes = "• Added new features\n• Fixed bugs\n• Improved performance",
                ),
            currentVersion = "1.0.0",
            downloadState = AppUpdateDownloadState.Downloading(42L, "1.1.0", 48),
            onDownload = {},
            onOpenRelease = {},
            onDismiss = {},
        )
    }
}
