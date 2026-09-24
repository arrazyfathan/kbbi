package com.arrazyfathan.kbbi.feature.figure.presentation.figure

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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.layout.ContentScale
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
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigurePagingException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

private const val FIGURE_INITIAL_SHIMMER_COUNT = 6
private const val FIGURE_SHIMMER_DURATION_MILLIS = 1_100
private const val FIGURE_SEARCH_BAR_SCROLL_VISIBILITY_THRESHOLD = 4f
private val FIGURE_SEARCH_BAR_IDLE_SHOW_DELAY_MILLIS = 1_000L.milliseconds

@Composable
fun FigureRoot(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: FigureViewModel = koinViewModel()
    val figures = viewModel.figures.collectAsLazyPagingItems()
    val state by viewModel.state.collectAsStateWithLifecycle()
    FigureScreen(
        state = state,
        figures = figures,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FigureScreen(
    state: FigureState,
    figures: LazyPagingItems<FigureModel>,
    onAction: (FigureAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
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
                        available.y < -FIGURE_SEARCH_BAR_SCROLL_VISIBILITY_THRESHOLD -> isSearchVisible = false
                        available.y > FIGURE_SEARCH_BAR_SCROLL_VISIBILITY_THRESHOLD -> isSearchVisible = true
                    }
                    return Offset.Zero
                }
            }
        }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.distinctUntilChanged().collectLatest { isScrolling ->
            if (!isScrolling) {
                delay(FIGURE_SEARCH_BAR_IDLE_SHOW_DELAY_MILLIS)
                isSearchVisible = true
            }
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.White,
        topBar = {
            FigureTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().nestedScroll(searchBarScrollConnection),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 100.dp),
            ) {
                item(key = "intro") {
                    Column {
                        Text(
                            text = stringResource(R.string.figure_collection_kicker),
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.8.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.figure_collection_title),
                            fontFamily = MetropolisFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 34.sp,
                            lineHeight = 40.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.figure_collection_description),
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(22.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (figures.loadState.refresh is LoadState.Loading && figures.itemCount == 0) {
                    repeat(FIGURE_INITIAL_SHIMMER_COUNT) { index ->
                        item(key = "loading-$index") {
                            FigureListShimmerRow(index = index + 1)
                        }
                    }
                }

                if (figures.loadState.refresh is LoadState.Error && figures.itemCount == 0) {
                    item(key = "error") {
                        FigureError(
                            loadState = figures.loadState.refresh as LoadState.Error,
                            onRetry = figures::retry,
                        )
                    }
                }

                if (figures.loadState.refresh is LoadState.NotLoading && figures.itemCount == 0) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.figure_collection_empty),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                            fontFamily = InterFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                items(count = figures.itemCount, key = figures.itemKey { it.slug }) { index ->
                    val figure = figures[index] ?: return@items
                    FigureListRow(figure = figure, index = index + 1)
                }

                when (val append = figures.loadState.append) {
                    LoadState.Loading -> {
                        item(key = "append-loading") {
                            FigureListShimmerRow(index = figures.itemCount + 1)
                        }
                    }

                    is LoadState.Error -> {
                        item(key = "append-error") { FigureError(append, figures::retry) }
                    }

                    is LoadState.NotLoading -> {}
                }
            }
            FloatingFigureSearchField(
                visible = isSearchVisible,
                value = state.searchQuery,
                onValueChange = { onAction(FigureAction.OnSearchQueryChanged(it)) },
                onSearch = { focusManager.clearFocus() },
            )
        }
    }
}

@Composable
private fun BoxScope.FloatingFigureSearchField(
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
            slideInVertically(animationSpec = tween(durationMillis = 180), initialOffsetY = { height -> height / 2 }) +
                fadeIn(animationSpec = tween(durationMillis = 180)),
        exit =
            slideOutVertically(animationSpec = tween(durationMillis = 140), targetOffsetY = { height -> height + 32 }) +
                fadeOut(animationSpec = tween(durationMillis = 120)),
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary) {
            FigureSearchField(
                value = value,
                onValueChange = onValueChange,
                onSearch = onSearch,
                modifier = Modifier.fillMaxWidth().height(58.dp),
            )
        }
    }
}

@Composable
private fun FigureSearchField(
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
        label = "figure-search-gradient-alpha",
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
                    val gradient = Brush.verticalGradient(colors = listOf(secondaryColor, primaryColor))
                    onDrawBehind { drawRect(brush = gradient, alpha = gradientAlpha) }
                },
        placeholder = {
            Text(
                text = stringResource(R.string.search_figure_hint),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.72f),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = stringResource(R.string.button_search),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FigureTopAppBar(
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
                    text = stringResource(R.string.figure_menu_title),
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = if (isCollapsed) 20.sp else 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!isCollapsed) {
                    Text(
                        text = stringResource(R.string.figure_menu_subtitle),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(16.dp))
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
private fun FigureListShimmerRow(index: Int) {
    val shimmerBrush = rememberFigureShimmerBrush()
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(width = 82.dp, height = 92.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(shimmerBrush),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                ShimmerBar(shimmerBrush, widthFraction = 0.08f, height = 10.dp)
                ShimmerBar(shimmerBrush, widthFraction = if (index % 2 == 0) 0.62f else 0.78f, height = 18.dp)
                ShimmerBar(shimmerBrush, widthFraction = 0.94f, height = 12.dp)
                ShimmerBar(shimmerBrush, widthFraction = 0.66f, height = 12.dp)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
    }
}

@Composable
private fun ShimmerBar(
    shimmerBrush: Brush,
    widthFraction: Float,
    height: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth(widthFraction)
                .height(height)
                .clip(RoundedCornerShape(6.dp))
                .background(shimmerBrush),
    )
}

@Composable
private fun rememberFigureShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "figure-shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1_000f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(FIGURE_SHIMMER_DURATION_MILLIS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "figure-shimmer-translation",
    )
    val baseColor = Color.Gray.copy(alpha = 0.2f)
    return Brush.linearGradient(
        colors = listOf(baseColor, MaterialTheme.colorScheme.surface, baseColor),
        start = Offset(x = translateAnimation - 1_000f, y = 0f),
        end = Offset(x = translateAnimation, y = 0f),
    )
}

@Composable
private fun FigureListRow(
    figure: FigureModel,
    index: Int,
) {
    val context = LocalContext.current
    var imageFailed by remember(figure.photo) { mutableStateOf(false) }
    var imageLoading by remember(figure.photo) { mutableStateOf(!figure.photo.isNullOrBlank()) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(width = 100.dp, height = 120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                if (figure.photo.isNullOrBlank() || imageFailed) {
                    Text(
                        text = figure.initials(),
                        fontFamily = InterFontFamily,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                } else {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(context)
                                .data(figure.photo)
                                .httpHeaders(
                                    NetworkHeaders
                                        .Builder()
                                        .set("User-Agent", FIGURE_IMAGE_USER_AGENT)
                                        .build(),
                                ).build(),
                        contentDescription = figure.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            imageFailed = false
                            imageLoading = true
                        },
                        onSuccess = { imageLoading = false },
                        onError = { imageFailed = true },
                    )
                    if (imageLoading && !imageFailed) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(rememberFigureShimmerBrush()),
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = index.toString().padStart(2, '0'),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = figure.name ?: figure.slug.replace('_', ' '),
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 23.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                figure.description
                    ?.takeIf(String::isNotBlank)
                    ?.let { description ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = description,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
    }
}

private fun FigureModel.initials(): String {
    val words =
        name
            .orEmpty()
            .trim()
            .split(Regex("\\s+"))
            .filter(String::isNotBlank)
    val initials =
        if (words.size > 1) {
            words.mapNotNull { word -> word.firstOrNull(Char::isLetter)?.uppercaseChar() }.joinToString("").take(2)
        } else {
            words
                .singleOrNull()
                ?.filter(Char::isLetter)
                ?.take(2)
                ?.uppercase()
                .orEmpty()
        }
    return initials.ifBlank { "?" }
}

@Composable
private fun FigureError(
    loadState: LoadState.Error,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    val message =
        (loadState.error as? FigurePagingException)?.dataError?.asUiText()?.asString(context)
            ?: stringResource(R.string.error_unknown)
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = InterFontFamily)
        androidx.compose.material3.TextButton(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FigureScreenPreview() {
    val figures =
        MutableStateFlow(
            PagingData.from(
                listOf(
                    FigureModel("Soekarno", "Soekarno", "", "https://upload.wikimedia.org/soekarno.jpg"),
                    FigureModel("R.A. Kartini", "RA_Kartini", "", null),
                ),
            ),
        ).collectAsLazyPagingItems()
    KBBITheme {
        FigureScreen(state = FigureState(), figures = figures, onAction = {}, onNavigateBack = {})
    }
}

private const val FIGURE_IMAGE_USER_AGENT = "KBBI Android/1.0 (https://github.com/arrazyfathan/kbbi)"
