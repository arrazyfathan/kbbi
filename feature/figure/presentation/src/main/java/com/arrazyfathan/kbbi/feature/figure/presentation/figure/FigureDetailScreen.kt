package com.arrazyfathan.kbbi.feature.figure.presentation.figure

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun FigureDetailRoot(
    slug: String,
    onNavigateBack: () -> Unit,
    onOpenSourceUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: FigureDetailViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(slug) { viewModel.onAction(FigureDetailAction.Load(slug)) }
    FigureDetailScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onOpenSourceUrl = onOpenSourceUrl,
        modifier = modifier,
    )
}

@Composable
fun FigureDetailScreen(
    state: FigureDetailState,
    onAction: (FigureDetailAction) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenSourceUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var heroHeightPx by remember { mutableIntStateOf(1) }
    val toolbarHeight = 56.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val collapseProgress =
        remember(listState, heroHeightPx, state.figure != null) {
            derivedStateOf {
                if (state.figure == null || listState.firstVisibleItemIndex > 0) {
                    1f
                } else {
                    (listState.firstVisibleItemScrollOffset.toFloat() / heroHeightPx.coerceAtLeast(1))
                        .coerceIn(0f, 1f)
                }
            }
        }
    val displayName = state.figure?.let(::figureDisplayName) ?: stringResource(R.string.figure_detail_title)

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
        ) {
            when {
                state.isLoading -> {
                    item(key = "loading") {
                        Box(Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                state.error != null -> {
                    item(key = "error") {
                        val context = LocalContext.current
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = toolbarHeight + 32.dp, start = 24.dp, end = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = state.error.asString(context),
                                fontFamily = InterFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(onClick = { onAction(FigureDetailAction.Retry) }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                state.figure != null -> {
                    val figure = state.figure
                    item(key = "hero") {
                        FigureHero(
                            figure = figure,
                            displayName = displayName,
                            modifier = Modifier.onSizeChanged { heroHeightPx = it.height },
                        )
                    }
                    item(key = "article") {
                        FigureArticle(figure = figure, onOpenSourceUrl = onOpenSourceUrl)
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(toolbarHeight)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 1f - collapseProgress.value }
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.58f), Color.Transparent),
                        ),
                    ),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = collapseProgress.value }
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary),
                        ),
                    ),
            )
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.navigate_back),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Text(
                    text = displayName,
                    modifier =
                        Modifier
                            .padding(start = 12.dp, end = 24.dp)
                            .graphicsLayer { alpha = collapseProgress.value },
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun figureDisplayName(figure: FigureModel): String =
    figure.name?.takeIf(String::isNotBlank) ?: figure.slug.replace('_', ' ')

@Composable
private fun FigureArticle(
    figure: FigureModel,
    onOpenSourceUrl: (String) -> Unit,
) {
    val displayName = figureDisplayName(figure)
    val quotes = figure.quotes.orEmpty().filter(String::isNotBlank)
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.figure_collection_kicker),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = displayName,
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            figure.description?.takeIf(String::isNotBlank)?.let { description ->
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(24.dp))
                Text(
                    text = description,
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (quotes.isNotEmpty()) {
                val quoteRuleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.62f)
                Spacer(Modifier.height(36.dp))
                Text(
                    text = stringResource(R.string.figure_detail_quotes),
                    fontFamily = MetropolisFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(16.dp))
                quotes.forEachIndexed { index, quote ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(22.dp))
                    Text(
                        text = "“$quote”",
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawRect(
                                        color = quoteRuleColor,
                                        size = Size(2.dp.toPx(), size.height),
                                    )
                                }.padding(start = 20.dp, top = 3.dp, bottom = 3.dp),
                        fontFamily = InterFontFamily,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(if (index == quotes.lastIndex) 0.dp else 26.dp))
                }
            }
            if (figure.sourceUrl.isNotBlank()) {
                Spacer(Modifier.height(40.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSourceUrl(figure.sourceUrl) }
                            .padding(vertical = 16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.figure_detail_source),
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = figure.sourceUrl,
                        fontFamily = InterFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun FigureHero(
    figure: FigureModel,
    displayName: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var imageFailed by remember(figure.photo) { mutableStateOf(false) }
    var imageAspectRatio by remember(figure.photo) { mutableStateOf(0.9f) }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(imageAspectRatio)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        if (figure.photo.isNullOrBlank() || imageFailed) {
            Text(
                text =
                    displayName
                        .trim()
                        .split(Regex("\\s+"))
                        .take(2)
                        .mapNotNull { it.firstOrNull() }
                        .joinToString("")
                        .uppercase(),
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 64.sp,
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
                                .set("User-Agent", "KBBI Android/1.0 (https://github.com/arrazyfathan/kbbi)")
                                .build(),
                        ).build(),
                contentDescription = stringResource(R.string.figure_detail_photo_description, displayName),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                onSuccess = { result ->
                    val size = result.painter.intrinsicSize
                    if (size.width.isFinite() && size.height.isFinite() && size.width > 0f && size.height > 0f) {
                        imageAspectRatio = size.width / size.height
                    }
                },
                onError = { imageFailed = true },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FigureDetailPreview() {
    KBBITheme {
        FigureDetailScreen(
            state =
                FigureDetailState(
                    isLoading = false,
                    figure =
                        FigureModel(
                            name = "R.A. Kartini",
                            slug = "RA_Kartini",
                            sourceUrl = "https://id.wikiquote.org/wiki/R.A._Kartini",
                            description = "A writer whose letters became a voice for education and equality.",
                            quotes = listOf("After darkness comes light."),
                        ),
                ),
            onAction = {},
            onNavigateBack = {},
            onOpenSourceUrl = {},
        )
    }
}
