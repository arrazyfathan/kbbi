package com.arrazyfathan.kbbi.presentation.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.MeaningModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.presentation.theme.BlueBg
import com.arrazyfathan.kbbi.presentation.theme.InterFontFamily
import com.arrazyfathan.kbbi.presentation.theme.KBBITheme
import com.arrazyfathan.kbbi.presentation.theme.TextH1
import com.arrazyfathan.kbbi.presentation.theme.TextP
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreen(
    listWordModel: ListWordModel,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(listWordModel.word) {
        viewModel.onAction(DetailAction.OnStarted(listWordModel.word.lowercase()))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.ShowMessage -> {
                    Toast.makeText(context, event.messageResId, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DetailContent(
        listWordModel = listWordModel,
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun DetailContent(
    listWordModel: ListWordModel,
    state: DetailState,
    onAction: (DetailAction) -> Unit,
) {
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val collapsedTitleAlpha by remember {
        derivedStateOf {
            val progress =
                if (lazyListState.firstVisibleItemIndex > 0) {
                    1f
                } else {
                    (lazyListState.firstVisibleItemScrollOffset / 300f).coerceIn(0f, 1f)
                }
            when {
                progress <= 0.7f -> 0f
                progress >= 0.8f -> 1f
                else -> (progress - 0.7f) / 0.1f
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(BlueBg),
        containerColor = BlueBg,
        bottomBar = {
            // Bookmark Floating Bar
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier =
                        Modifier.clickable {
                            onAction(
                                DetailAction.OnBookmarkClick(
                                    listWordModel.word.lowercase(),
                                    listWordModel.listWords,
                                ),
                            )
                        },
                    shape = RoundedCornerShape(100.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = if (state.isSaved) TextH1 else Color.White,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter =
                                painterResource(
                                    id = if (state.isSaved) R.drawable.book_solid else R.drawable.book,
                                ),
                            contentDescription = "Bookmark",
                            tint = if (state.isSaved) Color.White else TextH1,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text =
                                stringResource(
                                    id = if (state.isSaved) R.string.bookmarked else R.string.bookmark,
                                ),
                            color = if (state.isSaved) Color.White else TextH1,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(BlueBg)
                    .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
            ) {
                // Header spacing and expanded title
                item {
                    Spacer(
                        modifier = Modifier.statusBarsPadding().height(90.dp),
                    )
                    Text(
                        text = listWordModel.word.replaceFirstChar { it.uppercase() },
                        color = TextH1,
                        fontSize = 30.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    )
                }

                // Word details cards
                itemsIndexed(listWordModel.listWords) { index, wordModel ->
                    WordEntryCard(
                        index = index,
                        wordModel = wordModel,
                        onCopyClick = {
                            val clipboardManager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            var copiedText = ""
                            for ((i, item) in wordModel.meanings.withIndex()) {
                                val regex = Regex("""\[(.*?)]""")
                                val cleanWordClass = item.wordClass.replace(regex, " ").trim()
                                val cleanDescription = item.description.replace(Regex("\\?(.*)"), "")
                                copiedText += "${i + 1}. $cleanWordClass $cleanDescription\n\n"
                            }
                            val clip = ClipData.newPlainText("meaning", copiedText.trim())
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }

            // Top Custom Collapsed Toolbar (displays title on scroll)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(BlueBg)
                        .statusBarsPadding()
                        .height(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = listWordModel.word.replaceFirstChar { it.uppercase() },
                    color = TextH1,
                    fontSize = 24.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer(alpha = collapsedTitleAlpha),
                )
            }
        }
    }
}

@Composable
fun WordEntryCard(
    index: Int,
    wordModel: WordModel,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Circular Badge
                Box(
                    modifier = Modifier.size(30.dp).background(TextH1, shape = RoundedCornerShape(100.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Color.White,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = wordModel.entry,
                    color = TextH1,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Meanings list (aligned start with entry_text, i.e., 30.dp circular badge + 16.dp spacer = 46.dp)
            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 46.dp),
            ) {
                wordModel.meanings.forEachIndexed { meaningIndex, meaning ->
                    val annotatedText =
                        buildMeaningText(
                            position = meaningIndex,
                            wordClass = meaning.wordClass,
                            rawDescription = meaning.description,
                        )

                    Text(
                        text = annotatedText,
                        color = TextP,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Copy Button (aligned end with meanings, height 50dp)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Button(
                    onClick = onCopyClick,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(50.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.copy),
                            contentDescription = stringResource(id = R.string.copy),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.copy),
                            color = Color.White,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun buildMeaningText(
    position: Int,
    wordClass: String,
    rawDescription: String,
): AnnotatedString {
    val number = "${position + 1}. "
    val regex = Regex("""\[(.*?)]""")
    val cleanWordClass = wordClass.replace(regex, " ")
    val cleanDescription = " ${rawDescription.replace(Regex("\\?(.*)"), "")}"

    return buildAnnotatedString {
        append(number)
        withStyle(
            style =
                SpanStyle(
                    color = Color(0xFF2E494C),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
        ) {
            append(cleanWordClass)
        }
        append(cleanDescription)
    }
}

@Preview(showBackground = true)
@Composable
fun DetailContentPreview() {
    val sampleListWordModel =
        ListWordModel(
            word = "belajar",
            listWords =
                listOf(
                    WordModel(
                        entry = "belajar",
                        meanings =
                            listOf(
                                MeaningModel(wordClass = "v", description = "berusaha memperoleh kepandaian atau ilmu"),
                                MeaningModel(wordClass = "v", description = "berlatih"),
                                MeaningModel(wordClass = "v", description = "berubah tingkah laku atau tanggapan"),
                            ),
                    ),
                ),
        )

    KBBITheme {
        DetailContent(
            listWordModel = sampleListWordModel,
            state = DetailState(isSaved = false),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailContentSavedPreview() {
    val sampleListWordModel =
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
        )

    KBBITheme {
        DetailContent(
            listWordModel = sampleListWordModel,
            state = DetailState(isSaved = true),
            onAction = {},
        )
    }
}
