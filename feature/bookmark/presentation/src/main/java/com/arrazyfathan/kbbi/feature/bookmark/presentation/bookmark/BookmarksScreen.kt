package com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.Grey
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.Red
import com.arrazyfathan.kbbi.core.presentation.designsystem.SpaceGroteskFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.settings.domain.model.BookmarkLayout
import org.koin.androidx.compose.koinViewModel
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items as listItems

internal const val BOOKMARK_GRID_TEST_TAG = "bookmark_grid"
internal const val BOOKMARK_LIST_TEST_TAG = "bookmark_list"
internal const val BOOKMARK_GRID_ITEM_TEST_TAG_PREFIX = "bookmark_grid_item_"
internal const val BOOKMARK_LIST_ITEM_TEST_TAG_PREFIX = "bookmark_list_item_"
private const val BOOKMARK_LAYOUT_TRANSITION_DURATION_MILLIS = 180
private val bookmarkLayoutTransitionEasing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

@Composable
fun BookmarksScreen(
    onHaptic: (KBBIHapticType) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDetail: (ListWordModel) -> Unit,
) {
    val viewModel: BookmarksViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                BookmarksEvent.BookmarkDeleted -> onHaptic(KBBIHapticType.Confirm)
            }
        }
    }
    BookmarksScreenContent(
        modifier = modifier,
        state = state,
        onNavigateToDetail = onNavigateToDetail,
        onAction = viewModel::onAction,
        onHaptic = onHaptic,
    )
}

@Composable
fun BookmarksScreenContent(
    modifier: Modifier = Modifier,
    state: BookmarksState,
    onNavigateToDetail: (ListWordModel) -> Unit,
    onAction: (BookmarksAction) -> Unit,
    onHaptic: (KBBIHapticType) -> Unit = {},
) {
    var wordToDelete by remember { mutableStateOf<ListWordModel?>(null) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                ),
                        ),
                ).statusBarsPadding(),
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.bookmarks_title),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 24.sp,
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                )

                BookmarkLayoutToggle(
                    selectedLayout = state.bookmarkLayout,
                    onLayoutSelected = { layout ->
                        onAction(BookmarksAction.OnLayoutSelected(layout))
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.bookmarks_screen_subtitle),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 16.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.bookmarkLayout,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val animationSpec =
                        tween<Float>(
                            durationMillis = BOOKMARK_LAYOUT_TRANSITION_DURATION_MILLIS,
                            easing = bookmarkLayoutTransitionEasing,
                        )
                    (fadeIn(animationSpec) + scaleIn(animationSpec, initialScale = 0.98f)) togetherWith
                        (fadeOut(animationSpec) + scaleOut(animationSpec, targetScale = 0.98f))
                },
                label = "bookmarkLayoutContent",
            ) { bookmarkLayout ->
                when (bookmarkLayout) {
                    BookmarkLayout.GRID -> {
                        BookmarkGrid(
                            bookmarks = state.bookmarks,
                            onNavigateToDetail = onNavigateToDetail,
                            onDeleteInitiated = { wordToDelete = it },
                            onHaptic = onHaptic,
                        )
                    }

                    BookmarkLayout.LIST -> {
                        BookmarkList(
                            bookmarks = state.bookmarks,
                            onNavigateToDetail = onNavigateToDetail,
                            onDeleteInitiated = { wordToDelete = it },
                        )
                    }
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

@Composable
private fun BookmarkLayoutToggle(
    selectedLayout: BookmarkLayout,
    onLayoutSelected: (BookmarkLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layouts = BookmarkLayout.entries
    val density = LocalDensity.current
    val indicatorOffset =
        animateIntOffsetAsState(
            targetValue =
                with(density) {
                    IntOffset(
                        x = if (selectedLayout == BookmarkLayout.GRID) 3.dp.roundToPx() else 43.dp.roundToPx(),
                        y = 3.dp.roundToPx(),
                    )
                },
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            label = "bookmarkLayoutIndicator",
        )

    Box(
        modifier =
            modifier
                .width(86.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White),
    ) {
        Box(
            modifier =
                Modifier
                    .offset { indicatorOffset.value }
                    .width(40.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(MaterialTheme.colorScheme.primary),
        )

        Row(
            modifier = Modifier.padding(3.dp).selectableGroup(),
        ) {
            layouts.forEach { layout ->
                val selected = layout == selectedLayout
                val description =
                    when (layout) {
                        BookmarkLayout.GRID -> stringResource(R.string.bookmark_grid_view)
                        BookmarkLayout.LIST -> stringResource(R.string.bookmark_list_view)
                    }
                val icon =
                    when (layout) {
                        BookmarkLayout.GRID -> R.drawable.ic_grid_view
                        BookmarkLayout.LIST -> R.drawable.ic_list_view
                    }

                Box(
                    modifier =
                        Modifier
                            .width(40.dp)
                            .height(30.dp)
                            .selectable(
                                selected = selected,
                                onClick = { onLayoutSelected(layout) },
                                role = Role.RadioButton,
                            ).semantics { contentDescription = description },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = if (selected) Color.White else TextP,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkGrid(
    bookmarks: List<ListWordModel>,
    onNavigateToDetail: (ListWordModel) -> Unit,
    onDeleteInitiated: (ListWordModel) -> Unit,
    onHaptic: (KBBIHapticType) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().testTag(BOOKMARK_GRID_TEST_TAG),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        gridItems(bookmarks, key = { it.word }) { item ->
            BookmarkItem(
                model = item,
                onClick = { onNavigateToDetail(item) },
                onDeleteInitiated = { onDeleteInitiated(item) },
                onHaptic = onHaptic,
                modifier = Modifier.testTag("$BOOKMARK_GRID_ITEM_TEST_TAG_PREFIX${item.word}"),
            )
        }
    }
}

@Composable
private fun BookmarkList(
    bookmarks: List<ListWordModel>,
    onNavigateToDetail: (ListWordModel) -> Unit,
    onDeleteInitiated: (ListWordModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag(BOOKMARK_LIST_TEST_TAG),
        contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 80.dp),
    ) {
        listItems(bookmarks, key = { it.word }) { item ->
            SwipeableBookmarkListItem(
                model = item,
                onClick = { onNavigateToDetail(item) },
                onDeleteInitiated = { onDeleteInitiated(item) },
                modifier = Modifier.padding(bottom = 8.dp).testTag("$BOOKMARK_LIST_ITEM_TEST_TAG_PREFIX${item.word}"),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableBookmarkListItem(
    model: ListWordModel,
    onClick: () -> Unit,
    onDeleteInitiated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val latestOnDeleteInitiated by rememberUpdatedState(onDeleteInitiated)

    LaunchedEffect(dismissState.settledValue) {
        if (dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
            latestOnDeleteInitiated()
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Red)
                        .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.delete),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(88.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            BookmarkCardContent(
                model = model,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkItem(
    modifier: Modifier = Modifier,
    model: ListWordModel,
    onClick: () -> Unit,
    onDeleteInitiated: () -> Unit,
    onHaptic: (KBBIHapticType) -> Unit = {},
) {
    var isDeleteOverlayVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.padding(4.dp).fillMaxWidth().height(110.dp),
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
                        onHaptic(KBBIHapticType.LongPress)
                        isDeleteOverlayVisible = true
                    },
                ),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            BookmarkCardContent(
                model = model,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.Top,
            )
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
                            contentDescription = stringResource(R.string.cancel),
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }

                    // Large Trash Can in the Center
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete),
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center).size(28.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkCardContent(
    model: ListWordModel,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = verticalArrangement,
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
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onConfirm() }
                                .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = okTitle,
                            color = MaterialTheme.colorScheme.onPrimary,
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
                                    MeaningModel(
                                        wordClass = "v",
                                        description = "berusaha memperoleh kepandaian atau ilmu",
                                    ),
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
                                        description = "memasukkan bahan makanan ke dalam mulut",
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
