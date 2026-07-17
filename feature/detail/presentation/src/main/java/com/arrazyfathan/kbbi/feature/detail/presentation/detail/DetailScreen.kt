package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.ui.AppAlertState
import com.arrazyfathan.kbbi.core.presentation.ui.AppAlertType
import com.arrazyfathan.kbbi.core.presentation.ui.AppTopAlert
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

private const val DETAIL_ALERT_DURATION_MILLIS = 2_200L

@Composable
fun DetailScreen(
    listWordModel: ListWordModel,
    viewModel: DetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var alertState by remember { mutableStateOf<AppAlertState?>(null) }
    var alertKey by remember { mutableIntStateOf(0) }

    fun showAlert(
        message: UiText,
        type: AppAlertType = AppAlertType.Success,
    ) {
        alertState = AppAlertState(message = message, type = type)
        alertKey++
    }

    LaunchedEffect(listWordModel.word) {
        viewModel.onAction(DetailAction.OnStarted(listWordModel.word.lowercase()))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.ShowMessage -> {
                    showAlert(UiText.StringResource(event.messageResId))
                }
            }
        }
    }

    LaunchedEffect(alertKey) {
        if (alertState != null) {
            delay(DETAIL_ALERT_DURATION_MILLIS.milliseconds)
            alertState = null
        }
    }

    DetailContent(
        listWordModel = listWordModel,
        state = state,
        onAction = viewModel::onAction,
        onShowAlert = { message, type -> showAlert(message, type) },
        alertState = alertState,
    )
}

@Composable
fun DetailContent(
    listWordModel: ListWordModel,
    state: DetailState,
    onAction: (DetailAction) -> Unit,
    onShowAlert: (UiText, AppAlertType) -> Unit,
    alertState: AppAlertState?,
) {
    val context = LocalContext.current
    val sharedFromKbbi = stringResource(R.string.shared_from_kbbi)
    val lazyListState = rememberLazyListState()
    val bookmarkInteractionSource = remember { MutableInteractionSource() }
    val isBookmarkPressed by bookmarkInteractionSource.collectIsPressedAsState()
    val bookmarkButtonScale by animateFloatAsState(
        targetValue = if (isBookmarkPressed) 0.92f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "bookmark-button-scale",
    )
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

    Box(
        modifier = Modifier.fillMaxSize().background(BlueBg),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 132.dp),
        ) {
            // Header spacing and expanded title
            item {
                Spacer(
                    modifier = Modifier.statusBarsPadding().height(96.dp),
                )
                Text(
                    text = listWordModel.word.replaceFirstChar { it.uppercase() },
                    color = TextH1,
                    fontSize = 34.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp),
                )
                listWordModel.visitorCount?.let { visitorCount ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.word_visitor_count, visitorCount),
                        color = TextP,
                        fontSize = 14.sp,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 20.dp),
                    )
                } ?: Spacer(modifier = Modifier.height(20.dp))
            }

            // Word details cards
            itemsIndexed(listWordModel.listWords) { index, wordModel ->
                WordEntryCard(
                    index = index,
                    wordModel = wordModel,
                    onCopyClick = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("meaning", wordModel.toDefinitionCopyText())
                        clipboardManager.setPrimaryClip(clip)
                        onShowAlert(UiText.StringResource(R.string.copy_success), AppAlertType.Success)
                    },
                    onShareClick = {
                        context.sharePlainText(
                            text =
                                wordModel.toDefinitionShareText(
                                    rootWord = listWordModel.word,
                                    sourceLabel = sharedFromKbbi,
                                ),
                        )
                    },
                )
            }
        }

        // Top Custom Collapsed Toolbar (displays title on scroll)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(elevation = if (collapsedTitleAlpha > 0f) 8.dp else 0.dp)
                    .background(BlueBg)
                    .statusBarsPadding()
                    .height(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = listWordModel.word.replaceFirstChar { it.uppercase() },
                color = TextH1,
                fontSize = 26.sp,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer(alpha = collapsedTitleAlpha),
            )
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp)
                    .width(180.dp)
                    .height(54.dp)
                    .graphicsLayer {
                        scaleX = bookmarkButtonScale
                        scaleY = bookmarkButtonScale
                    }.shadow(
                        elevation = if (isBookmarkPressed) 8.dp else 14.dp,
                        shape = RoundedCornerShape(100.dp),
                        clip = false,
                    ).clip(RoundedCornerShape(100.dp))
                    .background(if (state.isSaved) TextH1 else Color.White)
                    .clickable(
                        interactionSource = bookmarkInteractionSource,
                        indication = ripple(),
                    ) {
                        onAction(
                            DetailAction.OnBookmarkClick(
                                listWordModel.word.lowercase(),
                                listWordModel.listWords,
                                listWordModel.visitorCount,
                            ),
                        )
                    }.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter =
                    painterResource(
                        id = if (state.isSaved) R.drawable.book_solid else R.drawable.book,
                    ),
                contentDescription = stringResource(id = R.string.bookmark),
                tint = if (state.isSaved) Color.White else TextH1,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text =
                    stringResource(
                        id = if (state.isSaved) R.string.bookmarked else R.string.bookmark,
                    ),
                color = if (state.isSaved) Color.White else TextH1,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }

        AppTopAlert(state = alertState)
    }
}

@Composable
fun WordEntryCard(
    index: Int,
    wordModel: WordModel,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onCopyClick,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(44.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.copy),
                            contentDescription = stringResource(id = R.string.copy),
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.copy),
                            color = Color.White,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onShareClick,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(44.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.share),
                            contentDescription = stringResource(id = R.string.share),
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.share),
                            color = Color.White,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
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

private fun WordModel.toDefinitionCopyText(): String =
    meanings
        .mapIndexed { index, meaning ->
            meaning.toDefinitionLine(position = index)
        }.joinToString(separator = "\n\n")

private fun WordModel.toDefinitionShareText(
    rootWord: String,
    sourceLabel: String,
): String {
    val meaningsText =
        meanings
            .mapIndexed { index, meaning ->
                meaning.toDefinitionLine(position = index)
            }.joinToString(separator = "\n")

    return buildString {
        appendLine(rootWord)
        appendLine(meaningsText)
        appendLine()
        append(sourceLabel)
    }
}

private fun MeaningModel.toDefinitionLine(position: Int): String {
    val cleanWordClass = wordClass.replace(Regex("""\[(.*?)]"""), " ").trim()
    val cleanDescription = description.replace(Regex("\\?(.*)"), "").trim()

    return "${position + 1}. $cleanWordClass $cleanDescription".trim()
}

private fun Context.sharePlainText(text: String) {
    val sendIntent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

    startActivity(
        Intent.createChooser(
            sendIntent,
            getString(R.string.share_definition),
        ),
    )
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
            onShowAlert = { _, _ -> },
            alertState = null,
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
            onShowAlert = { _, _ -> },
            alertState =
                AppAlertState(
                    message = UiText.DynamicString("Bookmarked"),
                    type = AppAlertType.Success,
                ),
        )
    }
}
