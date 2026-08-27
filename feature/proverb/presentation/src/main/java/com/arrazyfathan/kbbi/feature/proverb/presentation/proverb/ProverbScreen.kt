package com.arrazyfathan.kbbi.feature.proverb.presentation.proverb

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbPagingException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

private const val SEARCH_BAR_SCROLL_VISIBILITY_THRESHOLD = 4f
private const val PROVERB_SHIMMER_ITEM_COUNT = 8
private const val PROVERB_APPEND_SHIMMER_ITEM_COUNT = 3
private val SEARCH_BAR_IDLE_SHOW_DELAY_MILLIS = 1_000L.milliseconds

@Composable
fun ProverbRoot(
    onNavigateBack: () -> Unit,
    onHaptic: (KBBIHapticType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProverbViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val proverbs = viewModel.proverbs.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProverbEvent.MeaningLoaded -> {
                    onHaptic(KBBIHapticType.Confirm)
                }

                is ProverbEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                    onHaptic(KBBIHapticType.Reject)
                }
            }
        }
    }

    ProverbScreen(
        state = state,
        proverbs = proverbs,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProverbScreen(
    state: ProverbState,
    proverbs: LazyPagingItems<ProverbModel>,
    onAction: (ProverbAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    var isSearchVisible by remember { mutableStateOf(true) }
    val searchBarScrollConnection =
        remember {
            object : NestedScrollConnection {
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    when {
                        available.y < -SEARCH_BAR_SCROLL_VISIBILITY_THRESHOLD -> isSearchVisible = false
                        available.y > SEARCH_BAR_SCROLL_VISIBILITY_THRESHOLD -> isSearchVisible = true
                    }
                    return Offset.Zero
                }
            }
        }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.distinctUntilChanged().collectLatest { isScrolling ->
            if (!isScrolling) {
                delay(SEARCH_BAR_IDLE_SHOW_DELAY_MILLIS)
                isSearchVisible = true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ProverbTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        ) {
            ProverbList(
                proverbs = proverbs,
                onProverbClick = { onAction(ProverbAction.OnProverbClicked(it)) },
                listState = listState,
                modifier = Modifier.fillMaxSize().nestedScroll(searchBarScrollConnection),
            )
            FloatingProverbSearchField(
                visible = isSearchVisible,
                value = state.searchQuery,
                onValueChange = { onAction(ProverbAction.OnSearchQueryChanged(it)) },
                onSearch = { focusManager.clearFocus() },
            )
        }
    }

    if (state.selectedProverb != null) {
        ModalBottomSheet(
            onDismissRequest = { onAction(ProverbAction.OnMeaningDismissed) },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        ) {
            ProverbMeaningSheet(
                proverb = state.selectedProverb,
                isLoading = state.isMeaningLoading,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProverbTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
) {
    val isCollapsed = scrollBehavior.state.collapsedFraction > 0.5f

    MediumTopAppBar(
        modifier =
            Modifier.background(
                brush =
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary,
                            ),
                    ),
            ),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(id = R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        title = {
            Column {
                Text(
                    text = stringResource(id = R.string.proverb_screen_title),
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = if (isCollapsed) 20.sp else 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isCollapsed) {
                    Text(
                        text = stringResource(id = R.string.proverb_screen_subtitle),
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
private fun BoxScope.FloatingProverbSearchField(
    visible: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier =
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
        enter =
            slideInVertically(
                animationSpec = tween(durationMillis = 180),
                initialOffsetY = { height -> height / 2 },
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
        exit =
            slideOutVertically(
                animationSpec = tween(durationMillis = 140),
                targetOffsetY = { height -> height + 32 },
            ) + fadeOut(animationSpec = tween(durationMillis = 120)),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary,
        ) {
            ProverbSearchField(
                value = value,
                onValueChange = onValueChange,
                onSearch = onSearch,
                modifier = Modifier.fillMaxWidth().height(58.dp),
            )
        }
    }
}

@Composable
private fun ProverbSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gradientAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0f else 1f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "proverb-search-gradient-alpha",
    )

    TextField(
        value = value,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        modifier =
            modifier
                .clip(CircleShape)
                .background(secondaryColor)
                .drawWithCache {
                    val gradient =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    secondaryColor,
                                    primaryColor,
                                ),
                        )

                    onDrawBehind {
                        drawRect(brush = gradient, alpha = gradientAlpha)
                    }
                },
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_proverb_hint),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.72f),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = stringResource(id = R.string.button_search),
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(start = 10.dp).size(20.dp),
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
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        singleLine = true,
        shape = CircleShape,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                cursorColor = MaterialTheme.colorScheme.onSecondary,
            ),
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProverbList(
    proverbs: LazyPagingItems<ProverbModel>,
    onProverbClick: (ProverbModel) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val proverbKey = proverbs.itemKey { it.slug }
    val refresh = proverbs.loadState.refresh
    val isPullRefreshing = refresh is LoadState.Loading && proverbs.itemCount > 0

    PullToRefreshBox(
        isRefreshing = isPullRefreshing,
        onRefresh = proverbs::refresh,
        modifier = modifier.fillMaxWidth(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (index in 0 until proverbs.itemCount) {
                val item = proverbs.peek(index)
                val letter = item?.letter.orEmpty()
                val previousLetter = if (index > 0) proverbs.peek(index - 1)?.letter.orEmpty() else ""

                if (letter.isNotEmpty() && letter != previousLetter) {
                    stickyHeader(key = "letter-$letter-$index") {
                        LetterHeader(letter = letter)
                    }
                }

                item(key = proverbKey(index)) {
                    val proverb = proverbs[index]
                    if (proverb != null) {
                        ProverbCard(
                            proverb = proverb,
                            onClick = { onProverbClick(proverb) },
                        )
                    }
                }
            }

            when (refresh) {
                is LoadState.Loading -> {
                    if (proverbs.itemCount == 0) {
                        item(key = "refresh-loading") {
                            ProverbListShimmer()
                        }
                    }
                }

                is LoadState.Error -> {
                    item(key = "refresh-error") {
                        ErrorState(
                            loadState = refresh,
                            onRetry = proverbs::retry,
                            modifier = Modifier.padding(top = 48.dp),
                        )
                    }
                }

                is LoadState.NotLoading -> {
                    if (proverbs.itemCount == 0) {
                        item(key = "empty-state") {
                            EmptyState(modifier = Modifier.padding(top = 56.dp))
                        }
                    }
                }
            }

            val append = proverbs.loadState.append
            if (append is LoadState.Loading) {
                item(key = "append-loading") {
                    ProverbListShimmer(
                        itemCount = PROVERB_APPEND_SHIMMER_ITEM_COUNT,
                        showHeader = false,
                        modifier = Modifier.padding(vertical = 6.dp),
                    )
                }
            } else if (append is LoadState.Error) {
                item(key = "append-error") {
                    ErrorState(
                        loadState = append,
                        onRetry = proverbs::retry,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProverbListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = PROVERB_SHIMMER_ITEM_COUNT,
    showHeader: Boolean = true,
) {
    val shimmerBrush = rememberProverbShimmerBrush()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(itemCount) { index ->
            if (showHeader && index == 0) {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 2.dp, bottom = 2.dp)
                            .size(width = 38.dp, height = 24.dp)
                            .clip(CircleShape)
                            .background(shimmerBrush),
                )
            }
            ProverbShimmerCard(shimmerBrush = shimmerBrush)
        }
    }
}

@Composable
private fun ProverbShimmerCard(shimmerBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerBrush),
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(0.64f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmerBrush),
            )
        }
    }
}

@Composable
private fun rememberProverbShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "proverb-shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1_000f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1_100, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "proverb-shimmer-translation",
    )

    return Brush.linearGradient(
        colors =
            listOf(
                MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
                Color.White,
                MaterialTheme.colorScheme.background.copy(alpha = 0.72f),
            ),
        start = Offset(x = translateAnimation - 1_000f, y = 0f),
        end = Offset(x = translateAnimation, y = 0f),
    )
}

@Composable
private fun LetterHeader(letter: String) {
    Surface(
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 0.dp,
    ) {
        Text(
            text = letter,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun ProverbCard(
    proverb: ProverbModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = proverb.text,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextH1,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f),
            )
//            Text(
//                text = proverb.letter,
//                fontFamily = MetropolisFontFamily,
//                fontWeight = FontWeight.ExtraBold,
//                fontSize = 18.sp,
//                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
//                modifier = Modifier.padding(start = 12.dp),
//            )
        }
    }
}

@Composable
private fun ProverbMeaningSheet(
    proverb: ProverbDetailModel,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = proverb.text,
            fontFamily = MetropolisFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = TextH1,
            lineHeight = 29.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.proverb_meaning_title),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(id = R.string.proverb_meaning_loading),
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    color = TextP,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        } else {
            ProverbMeaningContent(
                meaning = proverb.meaning,
                emptyText = stringResource(id = R.string.proverb_meaning_empty),
            )
        }
        Spacer(modifier = Modifier.height(34.dp))
    }
}

@Composable
private fun ProverbMeaningContent(
    meaning: String?,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    val meanings =
        meaning
            ?.split(";")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    if (meanings.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            meanings.forEachIndexed { index, item ->
                NumberedMeaningItem(
                    number = index + 1,
                    meaning = item.uppercaseFirstLetter(),
                )
            }
        }
    } else {
        Text(
            text = emptyText,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = TextH1,
            lineHeight = 24.sp,
            modifier = modifier,
        )
    }
}

@Composable
private fun NumberedMeaningItem(
    number: Int,
    meaning: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(26.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                lineHeight = 13.sp,
            )
        }
        Text(
            text = meaning,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextH1,
            lineHeight = 24.sp,
            modifier = Modifier.padding(start = 12.dp).weight(1f),
        )
    }
}

private fun String.uppercaseFirstLetter(): String {
    val firstLetterIndex = indexOfFirst { it.isLetter() }
    if (firstLetterIndex == -1) return this

    return replaceRange(
        startIndex = firstLetterIndex,
        endIndex = firstLetterIndex + 1,
        replacement = this[firstLetterIndex].uppercase(),
    )
}

@Composable
private fun ErrorState(
    loadState: LoadState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val message =
        (loadState.error as? ProverbPagingException)?.dataError?.asUiText()?.asString(context)
            ?: stringResource(id = R.string.error_unknown)

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = TextP,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(
                text = stringResource(id = R.string.retry),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.proverb_empty_message),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = TextP,
        )
    }
}

@Preview
@Composable
private fun ProverbCardPreview() {
    KBBITheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(8.dp)) {
            ProverbCard(
                proverb =
                    ProverbModel(
                        text = "Air beriak tanda tak dalam",
                        letter = "A",
                        slug = "Air_beriak_tanda_tak_dalam",
                        sourceUrl = null,
                    ),
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProverbScreenPreview() {
    val sampleProverbs =
        listOf(
            ProverbModel("Air beriak tanda tak dalam", "A", "Air_beriak_tanda_tak_dalam", null),
            ProverbModel("Bagai air di daun talas", "B", "Bagai_air_di_daun_talas", null),
            ProverbModel("Cepat kaki ringan tangan", "C", "Cepat_kaki_ringan_tangan", null),
            ProverbModel("Darah daging sendiri", "D", "Darah_daging_sendiri", null),
            ProverbModel("Emas bersepuh perak", "E", "Emas_bersepuh_perak", null),
        )
    val pagingData = PagingData.from(sampleProverbs)
    val proverbs = MutableStateFlow(pagingData).collectAsLazyPagingItems()

    KBBITheme {
        ProverbScreen(
            state = ProverbState(),
            proverbs = proverbs,
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProverbScreenWithMeaningPreview() {
    val sampleProverbs =
        listOf(
            ProverbModel("Air beriak tanda tak dalam", "A", "Air_beriak_tanda_tak_dalam", null),
        )
    val pagingData = PagingData.from(sampleProverbs)
    val proverbs = MutableStateFlow(pagingData).collectAsLazyPagingItems()

    KBBITheme {
        ProverbScreen(
            state =
                ProverbState(
                    selectedProverb =
                        ProverbDetailModel(
                            text = "Air beriak tanda tak dalam",
                            letter = "A",
                            slug = "Air_beriak_tanda_tak_dalam",
                            sourceUrl = null,
                            meaning = "Orang yang sombong biasanya bodoh.",
                        ),
                ),
            proverbs = proverbs,
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProverbMeaningSheetPreview() {
    KBBITheme {
        Surface(color = Color.White) {
            ProverbMeaningSheet(
                proverb =
                    ProverbDetailModel(
                        text = "Air beriak tanda tak dalam",
                        letter = "A",
                        slug = "Air_beriak_tanda_tak_dalam",
                        sourceUrl = null,
                        meaning = "Orang yang sombong biasanya bodoh.; Siapa yang banyak bicara ilmunya.",
                    ),
                isLoading = false,
                modifier = Modifier.padding(20.dp),
            )
        }
    }
}
