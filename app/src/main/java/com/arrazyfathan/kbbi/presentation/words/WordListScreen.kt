package com.arrazyfathan.kbbi.presentation.words

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.data.Resource
import com.arrazyfathan.kbbi.core.data.source.local.WordList
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.presentation.detail.DetailActivity
import com.arrazyfathan.kbbi.presentation.theme.BlueBg
import com.arrazyfathan.kbbi.presentation.theme.BluePrimary
import com.arrazyfathan.kbbi.presentation.theme.InterFontFamily
import com.arrazyfathan.kbbi.presentation.theme.MetropolisFontFamily
import com.arrazyfathan.kbbi.utils.toJson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    modifier: Modifier = Modifier,
    viewModel: WordViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    var searchQuery by remember { mutableStateOf("") }
    var wordsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var filteredList by remember { mutableStateOf<List<String>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }
    var activeSearchWord by remember { mutableStateOf<String?>(null) }

    // Load words in background
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val jsonString =
                try {
                    context.assets
                        .open("entries.json")
                        .bufferedReader()
                        .use { it.readText() }
                } catch (_: Exception) {
                    null
                }
            if (jsonString != null) {
                val gson = GsonBuilder().create()
                val wordListType = object : TypeToken<WordList>() {}.type
                val words: WordList = gson.fromJson(jsonString, wordListType)
                wordsList = words
                filteredList = words
            }
        }
    }

    // Filter list on search query change
    LaunchedEffect(searchQuery, wordsList) {
        filteredList =
            if (searchQuery.isEmpty()) {
                wordsList
            } else {
                wordsList.filter { it.contains(searchQuery, ignoreCase = true) }
            }
    }

    // Handle fetching word details
    if (activeSearchWord != null) {
        val wordToSearch = activeSearchWord!!
        val liveData = remember(wordToSearch) { viewModel.getMeaningOfWord(wordToSearch) }
        val resourceState by liveData.observeAsState()

        LaunchedEffect(resourceState) {
            when (val resource = resourceState) {
                is Resource.Loading -> {
                    isLoading = true
                }
                is Resource.Success -> {
                    isLoading = false
                    activeSearchWord = null

                    val listWordModel =
                        ListWordModel(
                            word = wordToSearch,
                            listWords = resource.data ?: emptyList(),
                        ).toJson()

                    val intent =
                        Intent(context, DetailActivity::class.java).apply {
                            putExtra("data", listWordModel)
                        }
                    context.startActivity(intent)
                }
                is Resource.Error -> {
                    isLoading = false
                    activeSearchWord = null
                    Toast.makeText(context, resource.message ?: "Error occurred", Toast.LENGTH_SHORT).show()
                }
                null -> {}
            }
        }
    }

    val lazyListState = rememberLazyListState()

    val minHeaderHeightPx = with(density) { 56.dp.toPx() }
    val maxHeaderHeightPx = with(density) { 120.dp.toPx() }
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
                    return if (delta > 0 &&
                        lazyListState.firstVisibleItemIndex == 0 &&
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
        modifier =
            modifier
                .fillMaxSize()
                .background(BlueBg),
        containerColor = BlueBg,
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection),
            ) {
                // Collapsing App Bar / Header
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(headerHeight.value)
                            .background(BluePrimary)
                            .statusBarsPadding(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(id = R.string.word_list_title),
                        fontFamily = MetropolisFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }

                // Search Input Field
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
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
                            color = Color.White,
                        )
                    },
                    textStyle =
                        TextStyle(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.White,
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
                            focusedContainerColor = BluePrimary,
                            unfocusedContainerColor = BluePrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                        ),
                )

                // Words LazyColumn
                LazyColumn(
                    state = lazyListState,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                ) {
                    items(filteredList, key = { it }) { word ->
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .clickable {
                                        activeSearchWord = word
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

            // Search Loading Overlay
            if (isLoading) {
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
        }
    }
}
