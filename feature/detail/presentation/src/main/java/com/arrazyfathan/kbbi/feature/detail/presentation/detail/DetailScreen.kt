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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.ui.AppAlertState
import com.arrazyfathan.kbbi.core.presentation.ui.AppAlertType
import com.arrazyfathan.kbbi.core.presentation.ui.AppTopAlert
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.feature.home.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslatedMeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslatedWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordStudyModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

private const val DETAIL_ALERT_DURATION_MILLIS = 2_200L

@Composable
fun DetailScreen(
    listWordModel: ListWordModel,
    onHaptic: (KBBIHapticType) -> Unit,
    onNavigateToAiSettings: () -> Unit = {},
) {
    val viewModel: DetailViewModel = koinViewModel()
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

    val outputLanguage = if (LocalConfiguration.current.locales[0].language == "id") "id" else "en"

    LaunchedEffect(listWordModel, outputLanguage) {
        viewModel.onAction(DetailAction.OnStarted(listWordModel, outputLanguage))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DetailEvent.BookmarkChanged -> {
                    showAlert(UiText.StringResource(event.messageResId))
                    onHaptic(
                        if (event.isSaved) KBBIHapticType.ToggleOn else KBBIHapticType.ToggleOff,
                    )
                }

                is DetailEvent.TranslationChanged -> {
                    onHaptic(
                        if (event.enabled) KBBIHapticType.ToggleOn else KBBIHapticType.ToggleOff,
                    )
                }

                is DetailEvent.ShowError -> {
                    showAlert(
                        message = UiText.StringResource(event.messageResId),
                        type = AppAlertType.Failed,
                    )
                    onHaptic(KBBIHapticType.Reject)
                }

                DetailEvent.NavigateToAiSettings -> {
                    onNavigateToAiSettings()
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
        onHaptic = onHaptic,
    )
}

@Composable
fun DetailContent(
    listWordModel: ListWordModel,
    state: DetailState,
    onAction: (DetailAction) -> Unit,
    onShowAlert: (UiText, AppAlertType) -> Unit,
    alertState: AppAlertState?,
    onHaptic: (KBBIHapticType) -> Unit = {},
) {
    val context = LocalContext.current
    val sharedFromKbbi = stringResource(R.string.shared_from_kbbi)
    val lazyListState = rememberLazyListState()
    val translatedWord =
        remember(state.translation, state.isTranslationEnabled) {
            if (state.isTranslationEnabled) state.translation?.translation else null
        }
    val translationsByHeadword =
        remember(state.translation) {
            state.translation
                ?.entries
                ?.associate { entry -> entry.headword to entry.meanings.map { it.translation } }
                .orEmpty()
        }
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
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 132.dp),
        ) {
            // Header spacing and expanded title
            item {
                Spacer(
                    modifier = Modifier.statusBarsPadding().height(96.dp),
                )
                Text(
                    text =
                        (translatedWord ?: listWordModel.word).replaceFirstChar { it.uppercase() },
                    color = TextH1,
                    fontSize = 34.sp,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(top = 20.dp),
                )
                listWordModel.visitorCount?.let { visitorCount ->
                    Spacer(modifier = Modifier.height(12.dp))
                    VisitorCountChip(
                        visitorCount = visitorCount,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 20.dp),
                    )
                } ?: Spacer(modifier = Modifier.height(20.dp))
                TranslateToggleRow(
                    isEnabled = state.isTranslationEnabled,
                    isLoading = state.isTranslationLoading,
                    onToggle = { enabled ->
                        onAction(
                            DetailAction.OnTranslateToggled(
                                word = listWordModel.word,
                                enabled = enabled,
                            ),
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
                )
            }

            // Word details cards
            itemsIndexed(listWordModel.listWords) { index, wordModel ->
                WordEntryCard(
                    index = index,
                    wordModel = wordModel,
                    translatedWord = translatedWord,
                    isTranslationEnabled = state.isTranslationEnabled,
                    translationProvider = state.translation?.provider,
                    translationsByHeadword = translationsByHeadword,
                    onCopyClick = {
                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("meaning", wordModel.toDefinitionCopyText())
                        clipboardManager.setPrimaryClip(clip)
                        onShowAlert(UiText.StringResource(R.string.copy_success), AppAlertType.Success)
                        onHaptic(KBBIHapticType.Confirm)
                    },
                    onShareClick = {
                        context.sharePlainText(
                            text =
                                wordModel.toDefinitionShareText(
                                    rootWord = listWordModel.word,
                                    sourceLabel = sharedFromKbbi,
                                ),
                        )
                        onHaptic(KBBIHapticType.ContextClick)
                    },
                )
            }

            item(key = "ai-word-study") {
                AiWordStudyCard(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }

        // Top Custom Collapsed Toolbar (displays title on scroll)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(elevation = if (collapsedTitleAlpha > 0f) 8.dp else 0.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
                    .height(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text =
                    (translatedWord ?: listWordModel.word).replaceFirstChar { it.uppercase() },
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
private fun AiWordStudyCard(
    state: DetailState,
    onAction: (DetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AiWordStudyHeader(state.aiConfiguration.providerMode)
            when {
                state.isWordStudyLoading -> {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.ai_word_study_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextH1,
                        )
                    }
                }

                state.wordStudy != null -> {
                    WordStudyResultContent(state.wordStudy)
                    OutlinedButton(
                        onClick = { onAction(DetailAction.OnGenerateWordStudy) },
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.ai_word_study_regenerate))
                    }
                }

                state.wordStudyErrorResId != null -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            text = stringResource(state.wordStudyErrorResId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onAction(DetailAction.OnRetryWordStudy) },
                            shape = RoundedCornerShape(100.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                        if (state.aiConfiguration.providerMode == AiProviderMode.CUSTOM) {
                            OutlinedButton(
                                onClick = { onAction(DetailAction.OnConfigureAi) },
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.ai_word_study_configure))
                            }
                        }
                    }
                }

                else -> {
                    val customIncomplete =
                        state.aiConfiguration.providerMode == AiProviderMode.CUSTOM &&
                            !state.aiConfiguration.isCustomConfigurationComplete
                    Text(
                        text =
                            stringResource(
                                if (customIncomplete) {
                                    R.string.ai_word_study_custom_incomplete
                                } else {
                                    R.string.ai_word_study_intro
                                },
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextP,
                    )
                    Button(
                        onClick = {
                            onAction(
                                if (customIncomplete) DetailAction.OnConfigureAi else DetailAction.OnGenerateWordStudy,
                            )
                        },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TextH1),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(
                                if (customIncomplete) {
                                    R.string.ai_word_study_configure
                                } else {
                                    R.string.ai_word_study_generate
                                },
                            ),
                        )
                    }
                }
            }
            AiDisclaimer()
        }
    }
}

@Composable
private fun AiWordStudyHeader(providerMode: AiProviderMode) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_auto_awesome),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ai_word_study_title),
                style = MaterialTheme.typography.titleLarge,
                color = TextH1,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text =
                    stringResource(
                        if (providerMode == AiProviderMode.BACKEND) {
                            R.string.ai_settings_backend_title
                        } else {
                            R.string.ai_settings_custom_title
                        },
                    ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun WordStudyResultContent(result: WordStudyModel) {
    Text(
        text = result.explanation,
        style = MaterialTheme.typography.bodyLarge,
        color = TextH1,
        lineHeight = 24.sp,
    )
    WordStudyNumberedList(stringResource(R.string.ai_word_study_examples), result.examples)
    WordStudyNumberedList(stringResource(R.string.ai_word_study_usage_notes), result.usageNotes)
    RelatedWords(result.relatedWords)
    Text(
        text =
            if (result.providerMode == AiProviderMode.BACKEND) {
                stringResource(R.string.ai_word_study_attribution_backend)
            } else {
                stringResource(R.string.ai_word_study_attribution_custom, result.provider, result.model)
            },
        style = MaterialTheme.typography.labelSmall,
        color = TextP,
    )
}

@Composable
private fun WordStudyNumberedList(
    title: String,
    values: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextH1,
            fontWeight = FontWeight.SemiBold,
        )
        values.forEachIndexed { index, value ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 1.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(9.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextP,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelatedWords(values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.ai_word_study_related_words),
            style = MaterialTheme.typography.titleSmall,
            color = TextH1,
            fontWeight = FontWeight.SemiBold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            values.forEach { word ->
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = word,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AiDisclaimer() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = null,
            tint = TextP,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.ai_word_study_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = TextP,
        )
    }
}

@Composable
fun WordEntryCard(
    index: Int,
    wordModel: WordModel,
    translatedWord: String?,
    isTranslationEnabled: Boolean,
    translationProvider: String?,
    translationsByHeadword: Map<String, List<String>>,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
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
                    text =
                        (translatedWord ?: wordModel.entry).replaceFirstChar { it.uppercase() },
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
                    val translations = translationsByHeadword[wordModel.entry]
                    val displayDescription =
                        if (isTranslationEnabled) {
                            translations?.getOrNull(meaningIndex) ?: meaning.description
                        } else {
                            meaning.description
                        }
                    val annotatedText =
                        buildMeaningText(
                            position = meaningIndex,
                            wordClass = meaning.wordClass,
                            rawDescription = displayDescription,
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

            if (shouldShowTranslationProvider(isTranslationEnabled, translationProvider)) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        stringResource(
                            R.string.translated_by_provider,
                            translationProviderDisplayName(checkNotNull(translationProvider)),
                        ),
                    color = TextP.copy(alpha = 0.72f),
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 46.dp),
                )
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
fun VisitorCountChip(
    modifier: Modifier = Modifier,
    visitorCount: Int,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_eye),
            contentDescription = null,
            tint = TextH1,
            modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.word_visitor_count, visitorCount),
            color = TextH1,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
        )
    }
}

@Composable
fun TranslateToggleRow(
    isEnabled: Boolean,
    isLoading: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White)
                    .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TranslateSegment(
                text = stringResource(R.string.show_original),
                selected = !isEnabled,
                onClick = { onToggle(false) },
            )
            TranslateSegment(
                text = stringResource(R.string.show_english_translation),
                selected = isEnabled,
                onClick = { onToggle(true) },
            )
        }
        if (isLoading) {
            Spacer(modifier = Modifier.width(12.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = TextH1,
            )
        }
    }
}

@Composable
private fun TranslateSegment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = if (selected) Color.White else TextH1,
        fontFamily = InterFontFamily,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (selected) TextH1 else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 9.dp),
    )
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

@Preview(showBackground = true)
@Composable
fun DetailContentTranslatedPreview() {
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

    val translated =
        TranslateModel(
            word = "belajar",
            translation = "learn",
            from = "id",
            to = "en",
            provider = "google",
            entries =
                listOf(
                    TranslatedWordModel(
                        headword = "belajar",
                        meanings =
                            listOf(
                                TranslatedMeaningModel(
                                    wordClass = "v",
                                    description = "berusaha memperoleh kepandaian atau ilmu",
                                    translation = "attempt to gain knowledge or skill",
                                ),
                            ),
                    ),
                ),
        )

    KBBITheme {
        DetailContent(
            listWordModel = sampleListWordModel,
            state =
                DetailState(
                    isSaved = false,
                    isTranslationEnabled = true,
                    translation = translated,
                ),
            onAction = {},
            onShowAlert = { _, _ -> },
            alertState = null,
        )
    }
}
