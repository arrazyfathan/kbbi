package com.arrazyfathan.kbbi.feature.home.presentation.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueSecondary
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.KbbiFormFactorPreviews
import com.arrazyfathan.kbbi.core.presentation.designsystem.KbbiCompactExpandedPreviews
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

private const val HOME_SEARCH_LOADING_SOURCE = "home_search"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    externalSearchQuery: String? = null,
    externalSearchRequestKey: Long = 0L,
    onExternalSearchConsumed: () -> Unit = {},
    focusSearchRequestKey: Long = 0L,
    randomWordRequestKey: Long = 0L,
    onShortcutConsumed: () -> Unit = {},
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
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
        } else {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onAction(HomeAction.OnStarted)
    }

    LaunchedEffect(externalSearchRequestKey) {
        val query = externalSearchQuery?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        viewModel.onAction(HomeAction.OnSearchSubmitted(query))
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
                    onNavigateToDetail(event.word)
                }

                is HomeEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
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
        onNavigateToProverb = onNavigateToProverb,
        onNavigateToSettings = onNavigateToSettings,
        onVoiceSearchClick = ::startVoiceSearch,
        onVoiceSearchCancel = {
            ignoreNextVoiceError = true
            voiceRecognitionController?.cancel()
            viewModel.onAction(HomeAction.OnVoiceSearchFinished)
            viewModel.onAction(HomeAction.OnVoiceSearchCancelled)
        },
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    focusSearchRequestKey: Long = 0L,
    onSearchFocusConsumed: () -> Unit = {},
    onNavigateToProverb: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onVoiceSearchClick: () -> Unit = {},
    onVoiceSearchCancel: () -> Unit = {},
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }
    var showBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val voiceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(focusSearchRequestKey) {
        if (focusSearchRequestKey <= 0L) return@LaunchedEffect
        searchFocusRequester.requestFocus()
        onSearchFocusConsumed()
    }

    Box(
        modifier =
            modifier.fillMaxSize().background(BluePrimary).statusBarsPadding().pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitFirstDown(requireUnconsumed = false)
                        var totalDragY = 0f
                        var isSwipeDetected = false
                        do {
                            val event = awaitPointerEvent()
                            val dragChange = event.changes.firstOrNull()
                            if (dragChange != null && dragChange.pressed) {
                                val deltaY = dragChange.position.y - dragChange.previousPosition.y
                                totalDragY += deltaY
                                if (totalDragY < -150f) { // Swipe up threshold
                                    isSwipeDetected = true
                                    dragChange.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed } && !isSwipeDetected)

                        if (isSwipeDetected) {
                            showBottomSheet = true
                        }
                    }
                }
            },
    ) {
        // Hero Image at Bottom-Right
        Image(
            painter = painterResource(id = R.drawable.hero_home),
            contentDescription = stringResource(id = R.string.hero_image_text),
            modifier = Modifier.align(Alignment.BottomEnd).fillMaxHeight(0.35f).widthIn(max = 480.dp),
            contentScale = ContentScale.FillHeight,
        )

        // Main Content Container
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Welcome Text
            Text(
                text = stringResource(id = R.string.welcome_text),
                color = Color.White,
                fontSize = 28.sp,
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 36.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(id = R.string.subtitle_text),
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                                cursorColor = BluePrimary,
                            ),
                    )

                    this@Column.AnimatedVisibility(
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
                            color = BlueSecondary,
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_search),
                                    contentDescription = stringResource(id = R.string.button_search),
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                }

                Surface(
                    onClick = {
                        showBottomSheet = false
                        onVoiceSearchClick()
                    },
                    modifier = Modifier.size(55.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (state.isVoiceListening) BlueSecondary else Color.White,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_microphone),
                            contentDescription = stringResource(id = R.string.button_voice_search),
                            tint = if (state.isVoiceListening) Color.White else BluePrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.suggestions.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                SearchSuggestions(
                    suggestions = state.suggestions,
                    suggestionMode = state.suggestionMode,
                    onSuggestionClick = { suggestion ->
                        onAction(HomeAction.OnSuggestionClick(suggestion))
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // History Section
            if (state.histories.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.history_label),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyHorizontalStaggeredGrid(
                    rows = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().height(84.dp),
                    horizontalItemSpacing = 10.dp,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.histories.take(5), key = { it.word }) { history ->
                        Card(
                            modifier =
                                Modifier.clickable {
                                    onAction(HomeAction.OnSearchSubmitted(history.word))
                                },
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, Color.White),
                            elevation = CardDefaults.cardElevation(0.dp),
                        ) {
                            Row(
                                modifier = Modifier.defaultMinSize(minHeight = 34.dp).padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_history),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = history.word,
                                    color = Color.White,
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

        // Swipe Up Prompter at the bottom
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .clickable {
                        showBottomSheet = true
                    }.padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val swipeComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.swipeblue))
            LottieAnimation(
                composition = swipeComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(50.dp),
            )

            Text(
                text = stringResource(id = R.string.swipe_label),
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
            )
        }

        // Modal Bottom Sheet Menu
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                sheetMaxWidth = 640.dp,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 32.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.home_menu_title),
                        color = TextH1,
                        fontSize = 20.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(id = R.string.home_menu_subtitle),
                        color = TextP,
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HomeMenuCard(
                            icon = R.drawable.ic_proverb,
                            title = stringResource(id = R.string.proverb_menu_title),
                            subtitle = stringResource(id = R.string.proverb_menu_subtitle),
                            onClick = {
                                showBottomSheet = false
                                onNavigateToProverb()
                            },
                            modifier = Modifier.weight(1f),
                        )

                        HomeMenuCard(
                            icon = R.drawable.settings,
                            title = stringResource(id = R.string.settings_menu_title),
                            subtitle = stringResource(id = R.string.settings_menu_subtitle),
                            onClick = {
                                showBottomSheet = false
                                onNavigateToSettings()
                            },
                            modifier = Modifier.weight(1f),
                        )
                        HomeMenuPlaceholderCard(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (state.isVoiceListening) {
            ModalBottomSheet(
                onDismissRequest = onVoiceSearchCancel,
                sheetState = voiceSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                sheetMaxWidth = 640.dp,
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
            color = BlueBg,
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
            color = BlueSecondary,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = Color.White,
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
                    }.background(BluePrimary, RoundedCornerShape(999.dp)),
        )

        Box(
            modifier =
                Modifier
                    .size(92.dp)
                    .graphicsLayer {
                        scaleX = innerPulseScale
                        scaleY = innerPulseScale
                    }.background(BlueBg, RoundedCornerShape(999.dp)),
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
                    tint = BluePrimary,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeMenuCard(
    icon: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(120.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BlueBg),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = TextH1,
                fontSize = 13.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextP,
                fontSize = 11.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun HomeMenuPlaceholderCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BlueBg),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {}
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
        Column(modifier = Modifier.fillMaxWidth()) {
            if (suggestionMode == HomeSuggestionMode.DidYouMean) {
                Text(
                    text = stringResource(id = R.string.did_you_mean_label),
                    color = TextP,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                )
            }

            suggestions.forEach { suggestion ->
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

@KbbiFormFactorPreviews
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
                ),
            onNavigateToProverb = {},
            onAction = {},
        )
    }
}

@KbbiCompactExpandedPreviews
@Composable
fun HomeContentLoadingPreview() {
    KBBITheme {
        HomeContent(
            state = HomeState(searchQuery = "Belajar", isLoading = true),
            onNavigateToProverb = {},
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
            onNavigateToProverb = {},
            onAction = {},
        )
    }
}
