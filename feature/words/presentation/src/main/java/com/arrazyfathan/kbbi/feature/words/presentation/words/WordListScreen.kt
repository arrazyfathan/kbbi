package com.arrazyfathan.kbbi.feature.words.presentation.words

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.ui.LocalAppLoadingController
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import org.koin.androidx.compose.koinViewModel

private const val WORD_LIST_SEARCH_LOADING_SOURCE = "word_list_search"

@Composable
fun WordListScreen(
    onHaptic: (KBBIHapticType) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDetail: (ListWordModel) -> Unit,
) {
    val viewModel: WordViewModel = koinViewModel()
    val context = LocalContext.current
    val loadingController = LocalAppLoadingController.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(WordListAction.OnStarted)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WordListEvent.NavigateToDetail -> {
                    onHaptic(KBBIHapticType.Confirm)
                    onNavigateToDetail(event.word)
                }

                is WordListEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                    onHaptic(KBBIHapticType.Reject)
                }
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        loadingController.setBlocking(WORD_LIST_SEARCH_LOADING_SOURCE, state.isLoading)
    }

    DisposableEffect(Unit) {
        onDispose {
            loadingController.setBlocking(WORD_LIST_SEARCH_LOADING_SOURCE, false)
        }
    }

    WordListScreenContent(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreenContent(
    modifier: Modifier = Modifier,
    state: WordListState,
    onAction: (WordListAction) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val lazyListState = rememberLazyListState()

    val statusBarHeightPx = WindowInsets.statusBars.getTop(density).toFloat()
    val minHeaderHeightPx = statusBarHeightPx + with(density) { 0.dp.toPx() }
    val maxHeaderHeightPx = statusBarHeightPx + with(density) { 120.dp.toPx() }
    var headerHeightPx by remember { mutableFloatStateOf(maxHeaderHeightPx) }

    val nestedScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    val delta = available.y
                    return if (delta < 0) {
                        val newHeight = (headerHeightPx + delta).coerceIn(minHeaderHeightPx, maxHeaderHeightPx)
                        val consumed = newHeight - headerHeightPx
                        headerHeightPx = newHeight
                        Offset(0f, consumed)
                    } else {
                        Offset.Zero
                    }
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    val delta = available.y
                    return if (delta > 0 && lazyListState.firstVisibleItemIndex == 0 &&
                        lazyListState.firstVisibleItemScrollOffset == 0
                    ) {
                        val newHeight = (headerHeightPx + delta).coerceIn(minHeaderHeightPx, maxHeaderHeightPx)
                        val consumedHeight = newHeight - headerHeightPx
                        headerHeightPx = newHeight
                        Offset(0f, consumedHeight)
                    } else {
                        Offset.Zero
                    }
                }
            }
        }

    val headerHeight =
        remember {
            derivedStateOf {
                with(density) { headerHeightPx.toDp() }
            }
        }

    Scaffold(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection),
            ) {
                // Collapsing App Bar / Header
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(headerHeight.value)
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
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(id = R.string.word_list_title),
                        fontFamily = MetropolisFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }

                // Search Input Field
                TextField(
                    value = state.searchQuery,
                    onValueChange = { query ->
                        onAction(WordListAction.OnSearchQueryChanged(query))
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.search_word_list_hint),
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                    },
                    textStyle =
                        TextStyle(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondary,
                        ),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search,
                        ),
                    keyboardActions =
                        KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                            },
                        ),
                    singleLine = true,
                    shape = RoundedCornerShape(0.dp),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                            cursorColor = MaterialTheme.colorScheme.onSecondary,
                        ),
                )

                // Words LazyColumn
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                ) {
                    items(state.filteredWords, key = { it }) { word ->
                        Card(
                            modifier =
                                Modifier.fillMaxWidth().padding(top = 4.dp).clickable {
                                    onAction(WordListAction.OnWordClicked(word))
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(0.dp),
                        ) {
                            Text(
                                text = word,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = Color(0xFF090B1E),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun WordListScreenPreview() {
    KBBITheme {
        WordListScreenContent(
            state =
                WordListState(
                    words =
                        listOf(
                            "Abjad",
                            "Baca",
                            "Cacing",
                            "Dadu",
                            "Ember",
                            "Abjad",
                            "Baca",
                            "Cacing",
                            "Dadu",
                            "Ember",
                            "Abjad",
                            "Baca",
                            "Cacing",
                            "Dadu",
                            "Ember",
                        ),
                    filteredWords =
                        listOf(
                            "Abjad",
                            "Baca",
                            "Cacing",
                            "Dadu",
                            "Ember",
                            "Abjad",
                            "Baca",
                            "Cacing",
                            "Dadu",
                            "Ember",
                            "Abjad",
                            "Baca",
                            "Cacing",
                            "Dadu",
                            "Ember",
                        ),
                ),
            onAction = {},
        )
    }
}
