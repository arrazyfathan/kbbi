package com.arrazyfathan.kbbi.feature.home.presentation.home

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.observability.EventSource
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.SpaceGroteskFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.ui.LocalAppLoadingController
import com.arrazyfathan.kbbi.core.utils.VoiceRecognitionController
import com.arrazyfathan.kbbi.core.utils.VoiceRecognitionUtils
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.items as lazyColumnItems
import androidx.compose.foundation.lazy.items as lazyRowItems

private const val HOME_SEARCH_LOADING_SOURCE = "home_search"
private const val EXPLORE_TOGGLE_BOTTOM_OFFSET_DP = 96
private const val EXPLORE_TOGGLE_HORIZONTAL_MARGIN_DP = 16
private const val EXPLORE_TOGGLE_CHEVRON_DURATION_MILLIS = 240
private const val EXPLORE_TOGGLE_CONTENT_ENTER_DURATION_MILLIS = 260
private const val EXPLORE_TOGGLE_CONTENT_EXIT_DURATION_MILLIS = 200
private const val EXPLORE_TOGGLE_FLOAT_AMPLITUDE_DP = 5
private const val EXPLORE_TOGGLE_FLOAT_DURATION_MILLIS = 1500
private const val EXPLORE_TOGGLE_MOTION_BLUR_DURATION_MILLIS = 320
private const val EXPLORE_TOGGLE_MOTION_BLUR_MAX_STRETCH = 0.06f
private const val EXPLORE_TOGGLE_MOTION_BLUR_ALPHA_DROP = 0.08f
private const val EXPLORE_TOGGLE_MOTION_BLUR_MAX_RADIUS_PX = 14f

@Composable
fun HomeScreen(
    onHaptic: (KBBIHapticType) -> Unit,
    modifier: Modifier = Modifier,
    externalSearchQuery: String? = null,
    externalSearchRequestKey: Long = 0L,
    onExternalSearchConsumed: () -> Unit = {},
    focusSearchRequestKey: Long = 0L,
    randomWordRequestKey: Long = 0L,
    onShortcutConsumed: () -> Unit = {},
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val context = LocalContext.current
    val loadingController = LocalAppLoadingController.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val latestOnAction by rememberUpdatedState(viewModel::onAction)
    var ignoreNextVoiceError by remember { mutableStateOf(false) }
    val voiceRecognitionController =
        remember(context) {
            if (VoiceRecognitionUtils.isRecognitionAvailable(context)) {
                VoiceRecognitionController(
                    context = context,
                    onPartialResults = { recognizedTexts ->
                        latestOnAction(HomeAction.OnVoiceSearchPartialResult(recognizedTexts))
                    },
                    onResults = { recognizedTexts ->
                        latestOnAction(HomeAction.OnVoiceSearchFinished)
                        if (recognizedTexts.isEmpty()) {
                            latestOnAction(HomeAction.OnVoiceSearchEmptyResult)
                        } else {
                            latestOnAction(HomeAction.OnVoiceSearchResult(recognizedTexts))
                        }
                    },
                    onError = { error ->
                        latestOnAction(HomeAction.OnVoiceSearchFinished)
                        if (ignoreNextVoiceError) {
                            ignoreNextVoiceError = false
                        } else {
                            latestOnAction(HomeAction.OnVoiceSearchError(error))
                        }
                    },
                )
            } else {
                null
            }
        }

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.onAction(HomeAction.OnVoiceSearchStarted)
                voiceRecognitionController?.startListening()
                onHaptic(KBBIHapticType.ToggleOn)
            } else {
                viewModel.onAction(HomeAction.OnVoiceSearchPermissionDenied)
            }
        }

    fun startVoiceSearch() {
        if (voiceRecognitionController == null) {
            viewModel.onAction(HomeAction.OnVoiceSearchUnavailable)
            return
        }

        if (VoiceRecognitionUtils.hasRecordAudioPermission(context)) {
            viewModel.onAction(HomeAction.OnVoiceSearchStarted)
            voiceRecognitionController.startListening()
            onHaptic(KBBIHapticType.ToggleOn)
        } else {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onAction(HomeAction.OnStarted)
    }

    LaunchedEffect(externalSearchRequestKey) {
        val query = externalSearchQuery?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        viewModel.onAction(HomeAction.OnSearchSubmitted(query, EventSource.ExternalIntent))
        onExternalSearchConsumed()
    }

    LaunchedEffect(randomWordRequestKey) {
        if (randomWordRequestKey <= 0L) return@LaunchedEffect
        viewModel.onAction(HomeAction.OnRandomWordRequested)
        onShortcutConsumed()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToDetail -> {
                    onHaptic(KBBIHapticType.Confirm)
                    onNavigateToDetail(event.word)
                }

                is HomeEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                    if (event.isError) {
                        onHaptic(KBBIHapticType.Reject)
                    }
                }
            }
        }
    }

    LaunchedEffect(state.isLoading) {
        loadingController.setBlocking(HOME_SEARCH_LOADING_SOURCE, state.isLoading)
    }

    DisposableEffect(Unit) {
        onDispose {
            loadingController.setBlocking(HOME_SEARCH_LOADING_SOURCE, false)
        }
    }

    DisposableEffect(voiceRecognitionController) {
        onDispose {
            voiceRecognitionController?.destroy()
        }
    }

    HomeContent(
        state = state,
        focusSearchRequestKey = focusSearchRequestKey,
        onSearchFocusConsumed = onShortcutConsumed,
        onVoiceSearchClick = ::startVoiceSearch,
        onVoiceSearchCancel = {
            ignoreNextVoiceError = true
            voiceRecognitionController?.cancel()
            viewModel.onAction(HomeAction.OnVoiceSearchFinished)
            viewModel.onAction(HomeAction.OnVoiceSearchCancelled)
            onHaptic(KBBIHapticType.ToggleOff)
        },
        onAction = viewModel::onAction,
        onHaptic = onHaptic,
        onNavigateToProverb = onNavigateToProverb,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    state: HomeState,
    focusSearchRequestKey: Long = 0L,
    onSearchFocusConsumed: () -> Unit = {},
    onVoiceSearchClick: () -> Unit = {},
    onVoiceSearchCancel: () -> Unit = {},
    onAction: (HomeAction) -> Unit,
    onHaptic: (KBBIHapticType) -> Unit = {},
    onNavigateToProverb: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    var showExploreMenu by remember { mutableStateOf(false) }

    val voiceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler(enabled = showExploreMenu) {
        showExploreMenu = false
    }

    LaunchedEffect(focusSearchRequestKey) {
        if (focusSearchRequestKey <= 0L) return@LaunchedEffect
        searchFocusRequester.requestFocus()
        onSearchFocusConsumed()
    }

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
        // Hero Image at Bottom-Right
        Image(
            painter = painterResource(id = R.drawable.hero_home),
            contentDescription = stringResource(id = R.string.hero_image_text),
            modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight(0.35f),
            contentScale = ContentScale.FillHeight,
        )

        // Main Content Container
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Welcome Text
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.welcome_text),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.subtitle_text),
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).height(55.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            TextField(
                                value = state.searchQuery,
                                onValueChange = { text ->
                                    onAction(HomeAction.OnSearchQueryChanged(text))
                                },
                                modifier = Modifier.fillMaxWidth().height(55.dp).focusRequester(searchFocusRequester),
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.search_word_list_hint),
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = TextP,
                                    )
                                },
                                textStyle =
                                    TextStyle(
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = TextH1,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Search,
                                    ),
                                keyboardActions =
                                    KeyboardActions(
                                        onSearch = {
                                            if (state.searchQuery.isNotBlank()) {
                                                onAction(HomeAction.OnSearchSubmitted(state.searchQuery))
                                                focusManager.clearFocus()
                                            }
                                        },
                                    ),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors =
                                    TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = TextH1,
                                        unfocusedTextColor = TextH1,
                                        cursorColor = MaterialTheme.colorScheme.primary,
                                    ),
                            )

                            androidx.compose.animation.AnimatedVisibility(
                                visible = state.searchQuery.length > 2,
                                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                                modifier = Modifier.align(Alignment.CenterEnd),
                            ) {
                                Surface(
                                    onClick = {
                                        if (state.searchQuery.isNotBlank()) {
                                            onAction(HomeAction.OnSearchSubmitted(state.searchQuery))
                                            focusManager.clearFocus()
                                        }
                                    },
                                    modifier = Modifier.size(55.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_search),
                                            contentDescription = stringResource(id = R.string.button_search),
                                            tint = MaterialTheme.colorScheme.onSecondary,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            }
                        }

                        Surface(
                            onClick = {
                                onVoiceSearchClick()
                            },
                            modifier = Modifier.size(55.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (state.isVoiceListening) MaterialTheme.colorScheme.secondary else Color.White,
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_microphone),
                                    contentDescription = stringResource(id = R.string.button_voice_search),
                                    tint =
                                        if (state.isVoiceListening) {
                                            MaterialTheme.colorScheme.onSecondary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.histories.isNotEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                        ) {
                            LazyRow(
                                modifier = Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                lazyRowItems(state.histories.take(3), key = { it.word }) { history ->
                                    val firstIndex = state.histories.indexOf(history) == 0
                                    val paddingStart = if (firstIndex) 16.dp else 0.dp
                                    Card(
                                        modifier =
                                            Modifier
                                                .clickable {
                                                    onAction(HomeAction.OnSearchSubmitted(history.word))
                                                }.padding(start = paddingStart),
                                        shape = RoundedCornerShape(100.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(0.dp, Color.Transparent),
                                        elevation = CardDefaults.cardElevation(4.dp),
                                    ) {
                                        Row(
                                            modifier =
                                                Modifier
                                                    .wrapContentSize()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_history),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp),
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = history.word,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                fontFamily = InterFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.topWords.isNotEmpty(),
                        enter =
                            expandVertically(
                                animationSpec = tween(durationMillis = 280, easing = LinearOutSlowInEasing),
                                expandFrom = Alignment.Top,
                            ) + fadeIn(animationSpec = tween(durationMillis = 220, easing = LinearOutSlowInEasing)),
                        exit =
                            shrinkVertically(
                                animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing),
                                shrinkTowards = Alignment.Top,
                            ) + fadeOut(animationSpec = tween(durationMillis = 160, easing = FastOutLinearInEasing)),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = stringResource(id = R.string.top_words_label),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Medium,
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyHorizontalStaggeredGrid(
                                rows = StaggeredGridCells.Fixed(2),
                                modifier = Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 16.dp),
                                horizontalItemSpacing = 6.dp,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                items(state.topWords, key = { "${it.rank}-${it.word}" }) { topWord ->
                                    Card(
                                        modifier =
                                            Modifier.clickable {
                                                onAction(HomeAction.OnTopWordClick(topWord.word))
                                            },
                                        shape = RoundedCornerShape(32.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary),
                                        elevation = CardDefaults.cardElevation(0.dp),
                                    ) {
                                        Row(
                                            modifier =
                                                Modifier
                                                    .defaultMinSize(minHeight = 34.dp)
                                                    .padding(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "${topWord.rank}.",
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontFamily = InterFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = topWord.word,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontFamily = InterFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = state.suggestions.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopStart).padding(top = 63.dp, start = 16.dp, end = 16.dp),
                ) {
                    SearchSuggestions(
                        suggestions = state.suggestions,
                        suggestionMode = state.suggestionMode,
                        onSuggestionClick = { suggestion ->
                            onAction(HomeAction.OnSuggestionClick(suggestion))
                            focusManager.clearFocus()
                        },
                    )
                }
            }
        }

        ExploreToggle(
            isExpanded = showExploreMenu,
            onToggle = {
                onHaptic(if (showExploreMenu) KBBIHapticType.ToggleOff else KBBIHapticType.ToggleOn)
                showExploreMenu = !showExploreMenu
            },
            onNavigateToProverb = {
                showExploreMenu = false
                onNavigateToProverb()
            },
            onNavigateToSettings = {
                showExploreMenu = false
                onNavigateToSettings()
            },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = EXPLORE_TOGGLE_HORIZONTAL_MARGIN_DP.dp,
                        end = EXPLORE_TOGGLE_HORIZONTAL_MARGIN_DP.dp,
                        bottom = EXPLORE_TOGGLE_BOTTOM_OFFSET_DP.dp,
                    ),
        )

        if (state.isVoiceListening) {
            ModalBottomSheet(
                onDismissRequest = onVoiceSearchCancel,
                sheetState = voiceSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            ) {
                VoiceSearchBottomSheetContent(
                    partialText = state.voicePartialText,
                    onCancel = onVoiceSearchCancel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 32.dp),
                )
            }
        }
    }
}

@Composable
private fun ExploreToggle(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "exploreTogglePress",
    )
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = EXPLORE_TOGGLE_CHEVRON_DURATION_MILLIS),
        label = "exploreToggleChevron",
    )
    val floatTransition = rememberInfiniteTransition(label = "exploreToggleFloat")
    val floatProgress by floatTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = EXPLORE_TOGGLE_FLOAT_DURATION_MILLIS,
                        easing = LinearOutSlowInEasing,
                    ),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "exploreToggleFloatProgress",
    )
    val floatFactor by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 1f,
        animationSpec = tween(durationMillis = EXPLORE_TOGGLE_CONTENT_ENTER_DURATION_MILLIS),
        label = "exploreToggleFloatFactor",
    )
    val cornerPercent by animateFloatAsState(
        targetValue = if (isExpanded) 8f else 50f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "exploreToggleCorner",
    )
    val toggleStateDescription =
        stringResource(
            if (isExpanded) {
                R.string.explore_toggle_state_expanded
            } else {
                R.string.explore_toggle_state_collapsed
            },
        )

    val toggleShape = RoundedCornerShape(percent = cornerPercent.roundToInt())
    val toggleElevation by animateDpAsState(
        targetValue = if (isExpanded) 12.dp else 2.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "exploreToggleElevation",
    )

    // Motion blur: a short pulse fired on every morph.
    // Peaks early (front-loaded velocity), then eases back to crisp at rest.
    val motionBlur = remember { Animatable(0f) }
    LaunchedEffect(isExpanded) {
        motionBlur.snapTo(0f)
        motionBlur.animateTo(
            targetValue = 0f,
            animationSpec =
                keyframes {
                    durationMillis = EXPLORE_TOGGLE_MOTION_BLUR_DURATION_MILLIS
                    0f at 0
                    1f at EXPLORE_TOGGLE_MOTION_BLUR_DURATION_MILLIS / 3
                    0f at EXPLORE_TOGGLE_MOTION_BLUR_DURATION_MILLIS using LinearOutSlowInEasing
                },
        )
    }
    val motionBlurStrength = motionBlur.value
    val supportsBlurEffect = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    if (motionBlurStrength > 0.001f) {
                        // Fake directional motion blur along the vertical morph axis:
                        // stretch + slight fade read as motion without a heavy filter.
                        scaleX = 1f + (EXPLORE_TOGGLE_MOTION_BLUR_MAX_STRETCH * motionBlurStrength)
                        scaleY = 1f - (EXPLORE_TOGGLE_MOTION_BLUR_MAX_STRETCH * 0.5f * motionBlurStrength)
                        alpha = 1f - (EXPLORE_TOGGLE_MOTION_BLUR_ALPHA_DROP * motionBlurStrength)
                        if (supportsBlurEffect) {
                            val radius = EXPLORE_TOGGLE_MOTION_BLUR_MAX_RADIUS_PX * motionBlurStrength
                            renderEffect = BlurEffect(radius, radius, TileMode.Clamp)
                        }
                    } else {
                        scaleX = 1f
                        scaleY = 1f
                        alpha = 1f
                        renderEffect = null
                    }
                },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .offset {
                        IntOffset(
                            x = 0,
                            y =
                                (
                                    (floatProgress - 0.5f) *
                                        floatFactor *
                                        EXPLORE_TOGGLE_FLOAT_AMPLITUDE_DP *
                                        2
                                ).dp.roundToPx(),
                        )
                    }.wrapContentWidth()
                    .shadow(elevation = toggleElevation, shape = toggleShape, clip = false)
                    .clip(toggleShape)
                    .background(MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.wrapContentWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(durationMillis = EXPLORE_TOGGLE_CONTENT_ENTER_DURATION_MILLIS),
                        ) togetherWith
                            fadeOut(
                                animationSpec = tween(durationMillis = EXPLORE_TOGGLE_CONTENT_EXIT_DURATION_MILLIS),
                            ) using
                            SizeTransform(clip = false) { _, _ ->
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                )
                            }
                    },
                    contentAlignment = Alignment.BottomCenter,
                    label = "exploreToggleContent",
                ) { expanded ->
                    if (expanded) {
                        ExploreMenuContent(
                            onNavigateToProverb = onNavigateToProverb,
                            onNavigateToSettings = onNavigateToSettings,
                        )
                    } else {
                        Spacer(modifier = Modifier)
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .clickable(
                                interactionSource = interactionSource,
                                indication = LocalIndication.current,
                                onClick = onToggle,
                            ).graphicsLayer {
                                scaleX = pressScale
                                scaleY = pressScale
                            }.defaultMinSize(minHeight = 18.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .semantics {
                                stateDescription = toggleStateDescription
                            },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter =
                            painterResource(
                                id = if (isExpanded) R.drawable.ic_explore_selected else R.drawable.ic_explore,
                            ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(id = R.string.explore_title),
                        color = TextH1,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_down),
                        contentDescription = null,
                        tint = TextP,
                        modifier = Modifier.size(16.dp).rotate(chevronRotation),
                    )
                }

                if (isExpanded) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun VoiceSearchBottomSheetContent(
    partialText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VoiceListeningAnimation()

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(id = R.string.voice_search_sheet_title),
            color = TextH1,
            fontSize = 20.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.voice_search_sheet_subtitle),
            color = TextP,
            fontSize = 14.sp,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.voice_search_detected_label),
                    color = TextP,
                    fontSize = 12.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = partialText.ifBlank { stringResource(id = R.string.voice_search_listening) },
                    color = if (partialText.isBlank()) TextP else TextH1,
                    fontSize = 16.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = if (partialText.isBlank()) FontWeight.Normal else FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondary,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun VoiceListeningAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "voice_listening")
    val outerPulseScale by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1100),
                repeatMode = RepeatMode.Restart,
            ),
        label = "voice_outer_pulse_scale",
    )
    val outerPulseAlpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1100),
                repeatMode = RepeatMode.Restart,
            ),
        label = "voice_outer_pulse_alpha",
    )
    val innerPulseScale by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.08f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 860),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "voice_inner_pulse_scale",
    )

    Box(
        modifier = modifier.size(132.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(108.dp)
                    .graphicsLayer {
                        scaleX = outerPulseScale
                        scaleY = outerPulseScale
                        alpha = outerPulseAlpha
                    }.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(999.dp)),
        )

        Box(
            modifier =
                Modifier
                    .size(92.dp)
                    .graphicsLayer {
                        scaleX = innerPulseScale
                        scaleY = innerPulseScale
                    }.background(MaterialTheme.colorScheme.background, RoundedCornerShape(999.dp)),
        )

        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_microphone),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchSuggestions(
    suggestions: List<String>,
    suggestionMode: HomeSuggestionMode,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
            if (suggestionMode == HomeSuggestionMode.DidYouMean) {
                item {
                    Text(
                        text = stringResource(id = R.string.did_you_mean_label),
                        color = TextP,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier =
                            Modifier.padding(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = 8.dp,
                            ),
                    )
                }
            }

            lazyColumnItems(suggestions, key = { it }) { suggestion ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                        tint = TextP,
                        modifier = Modifier.size(16.dp),
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = suggestion,
                        color = TextH1,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreview() {
    KBBITheme {
        HomeContent(
            state =
                HomeState(
                    histories =
                        listOf(
                            HistoryModel("Kamus"),
                            HistoryModel("Indonesia"),
                            HistoryModel("Pintar"),
                            HistoryModel("Belajar"),
                            HistoryModel("Membaca"),
                        ),
                    topWords =
                        listOf(
                            TopWordUi(1, "hati"),
                            TopWordUi(2, "aturan"),
                            TopWordUi(3, "abah"),
                            TopWordUi(4, "audio"),
                            TopWordUi(5, "bahagia"),
                        ),
                ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentLoadingPreview() {
    KBBITheme {
        HomeContent(
            state = HomeState(searchQuery = "Belajar", isLoading = true),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentSuggestionsPreview() {
    KBBITheme {
        HomeContent(
            state =
                HomeState(
                    searchQuery = "bel",
                    suggestions = listOf("belajar", "belakang", "belanja", "pembelajaran"),
                ),
            onAction = {},
        )
    }
}
