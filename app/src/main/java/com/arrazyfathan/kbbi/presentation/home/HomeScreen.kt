package com.arrazyfathan.kbbi.presentation.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.presentation.theme.BlueBg
import com.arrazyfathan.kbbi.presentation.theme.BluePrimary
import com.arrazyfathan.kbbi.presentation.theme.BlueSecondary
import com.arrazyfathan.kbbi.presentation.theme.InterFontFamily
import com.arrazyfathan.kbbi.presentation.theme.KBBITheme
import com.arrazyfathan.kbbi.presentation.theme.MetropolisFontFamily
import com.arrazyfathan.kbbi.presentation.theme.SpaceGroteskFontFamily
import com.arrazyfathan.kbbi.presentation.theme.TextH1
import com.arrazyfathan.kbbi.presentation.theme.TextP
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.onAction(HomeAction.OnStarted)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToDetail -> {
                    searchQuery = ""
                    onNavigateToDetail(event.dataJson)
                }

                is HomeEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HomeContent(
        state = state,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    var showBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(BluePrimary)
                .statusBarsPadding()
                .pointerInput(Unit) {
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
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxHeight(0.35f),
            contentScale = ContentScale.FillHeight,
        )

        // Main Content Container
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
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

            // Search Bar Row
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { text ->
                        val filteredText = text.replace(" ", "")
                        onSearchQueryChange(filteredText)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(55.dp),
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
                                if (searchQuery.isNotBlank()) {
                                    onAction(HomeAction.OnSearchSubmitted(searchQuery))
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

                // Search Button (Slides In / Out)
                this@Column.AnimatedVisibility(
                    visible = searchQuery.length > 2,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Surface(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                onAction(HomeAction.OnSearchSubmitted(searchQuery))
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(84.dp),
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
                                modifier =
                                    Modifier
                                        .defaultMinSize(minHeight = 34.dp)
                                        .padding(horizontal = 16.dp),
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

        // Search Loading Overlay
        if (state.isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    val searchLoadingComposition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.loading_search),
                    )
                    LottieAnimation(
                        composition = searchLoadingComposition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        // Modal Bottom Sheet Menu
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(bottom = 32.dp),
                ) {
                    Text(
                        text = "Menu",
                        color = TextH1,
                        fontSize = 20.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Temukan fitur lainnya (Comming Soon)",
                        color = TextP,
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Three placeholder cards side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        repeat(3) {
                            Card(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(120.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = BlueBg),
                                elevation = CardDefaults.cardElevation(0.dp),
                            ) {}
                        }
                    }
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
                ),
            searchQuery = "",
            onSearchQueryChange = {},
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentLoadingPreview() {
    KBBITheme {
        HomeContent(
            state = HomeState(isLoading = true),
            searchQuery = "Belajar",
            onSearchQueryChange = {},
            onAction = {},
        )
    }
}
