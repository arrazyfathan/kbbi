package com.arrazyfathan.kbbi.presentation.bookmark

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.MeaningModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.presentation.theme.BluePrimary
import com.arrazyfathan.kbbi.presentation.theme.Grey
import com.arrazyfathan.kbbi.presentation.theme.InterFontFamily
import com.arrazyfathan.kbbi.presentation.theme.KBBITheme
import com.arrazyfathan.kbbi.presentation.theme.MetropolisFontFamily
import com.arrazyfathan.kbbi.presentation.theme.Red
import com.arrazyfathan.kbbi.presentation.theme.SpaceGroteskFontFamily
import com.arrazyfathan.kbbi.presentation.theme.TextH1
import com.arrazyfathan.kbbi.presentation.theme.TextP
import org.koin.androidx.compose.koinViewModel

@Composable
fun BookmarksScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (ListWordModel) -> Unit,
    viewModel: BookmarksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BookmarksScreenContent(
        state = state,
        onNavigateToDetail = onNavigateToDetail,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
private fun BookmarksScreenContent(
    state: BookmarksState,
    onNavigateToDetail: (ListWordModel) -> Unit,
    onAction: (BookmarksAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var wordToDelete by remember { mutableStateOf<ListWordModel?>(null) }

    Box(
        modifier = modifier.fillMaxSize().background(BluePrimary).statusBarsPadding(),
    ) {
        // Background Hero Image if not empty
        if (state.bookmarks.isNotEmpty()) {
            Image(
                painter = painterResource(id = R.drawable.hero_saved),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight(0.35f),
                contentScale = ContentScale.FillHeight,
            )
        }

        // Empty layout if empty
        if (state.bookmarks.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val emptyComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty))
                LottieAnimation(
                    composition = emptyComposition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(80.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.empty_bookmarks_message),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                )
            }
        }

        // Bookmarks List / Grid
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.bookmarks_title),
                color = Color.White,
                fontSize = 24.sp,
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.bookmarks_screen_subtitle),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                items(state.bookmarks, key = { it.word }) { item ->
                    BookmarkItem(
                        model = item,
                        onClick = {
                            onNavigateToDetail(item)
                        },
                        onDeleteInitiated = {
                            wordToDelete = item
                        },
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        wordToDelete?.let { item ->
            DeleteConfirmationDialog(
                title = stringResource(id = R.string.delete_word_title),
                message = stringResource(id = R.string.delete_word_message),
                okTitle = stringResource(id = R.string.delete),
                cancelTitle = stringResource(id = R.string.cancel),
                onConfirm = {
                    onAction(BookmarksAction.OnDeleteConfirmed(item.word))
                    wordToDelete = null
                },
                onDismiss = {
                    wordToDelete = null
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkItem(
    model: ListWordModel,
    onClick: () -> Unit,
    onDeleteInitiated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isDeleteOverlayVisible by remember { mutableStateOf(false) }

    val triggerVibration = {
        val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(VibratorManager::class.java)?.defaultVibrator
            } else {
                context.getSystemService(Vibrator::class.java)
            }
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(50L)
            }
        }
    }

    Box(
        modifier = modifier.padding(8.dp).fillMaxWidth().height(110.dp),
    ) {
        // Standard Content Card
        Card(
            modifier =
                Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).combinedClickable(
                    onClick = {
                        if (!isDeleteOverlayVisible) {
                            onClick()
                        }
                    },
                    onLongClick = {
                        triggerVibration()
                        isDeleteOverlayVisible = true
                    },
                ),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            ) {
                Text(
                    text = model.word.replaceFirstChar { it.uppercase() },
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = model.listWords.firstOrNull()?.entry ?: "",
                    color = TextP,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // Slide-in Red Delete Overlay
        AnimatedVisibility(
            visible = isDeleteOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Card(
                modifier =
                    Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).clickable {
                        onDeleteInitiated()
                        isDeleteOverlayVisible = false
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Red),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Close/Cancel overlay button on top right
                    IconButton(
                        onClick = { isDeleteOverlayVisible = false },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "Cancel",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }

                    // Large Trash Can in the Center
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center).size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    message: String,
    okTitle: String,
    cancelTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            ) {
                Text(
                    text = title,
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = message,
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Cancel button
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(64.dp))
                                .background(Grey)
                                .clickable { onDismiss() }
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = cancelTitle,
                            color = TextH1,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // OK/Confirm button
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(64.dp))
                                .background(BluePrimary)
                                .clickable { onConfirm() }
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = okTitle,
                            color = Color.White,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarksScreenPreview() {
    val sampleBookmarks =
        listOf(
            ListWordModel(
                word = "belajar",
                listWords =
                    listOf(
                        WordModel(
                            entry = "belajar",
                            meanings =
                                listOf(
                                    MeaningModel(wordClass = "v", description = "berusaha memperoleh kepandaian atau ilmu"),
                                ),
                        ),
                    ),
            ),
            ListWordModel(
                word = "makan",
                listWords =
                    listOf(
                        WordModel(
                            entry = "makan",
                            meanings =
                                listOf(
                                    MeaningModel(
                                        wordClass = "v",
                                        description = "memasukkan bahan makanan ke dalam mulut serta mengunyah dan menelannya",
                                    ),
                                ),
                        ),
                    ),
            ),
        )

    KBBITheme {
        BookmarksScreenContent(
            state = BookmarksState(bookmarks = sampleBookmarks),
            onNavigateToDetail = {},
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarksScreenEmptyPreview() {
    KBBITheme {
        BookmarksScreenContent(
            state = BookmarksState(bookmarks = emptyList()),
            onNavigateToDetail = {},
            onAction = {},
        )
    }
}
