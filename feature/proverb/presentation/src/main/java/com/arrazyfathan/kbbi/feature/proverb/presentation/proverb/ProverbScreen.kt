package com.arrazyfathan.kbbi.feature.proverb.presentation.proverb

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueBg
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.BlueSecondary
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.presentation.designsystem.MetropolisFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextH1
import com.arrazyfathan.kbbi.core.presentation.designsystem.TextP
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbPagingException
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProverbRoot(
    modifier: Modifier = Modifier,
    viewModel: ProverbViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val proverbs = viewModel.proverbs.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProverbEvent.ShowMessage -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ProverbScreen(
        state = state,
        proverbs = proverbs,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProverbScreen(
    state: ProverbState,
    proverbs: LazyPagingItems<ProverbModel>,
    onAction: (ProverbAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BlueBg,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            ProverbHeader()
            ProverbSearchField(
                value = state.searchQuery,
                onValueChange = { onAction(ProverbAction.OnSearchQueryChanged(it)) },
                onSearch = { focusManager.clearFocus() },
            )
            ProverbList(
                proverbs = proverbs,
                onProverbClick = { onAction(ProverbAction.OnProverbClicked(it)) },
                modifier = Modifier.weight(1f),
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

@Composable
private fun ProverbHeader() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(BluePrimary)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Text(
            text = stringResource(id = R.string.proverb_screen_title),
            fontFamily = MetropolisFontFamily,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = 28.sp,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(id = R.string.proverb_screen_subtitle),
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.82f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun ProverbSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(58.dp),
        placeholder = {
            Text(
                text = stringResource(id = R.string.search_proverb_hint),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.72f),
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = stringResource(id = R.string.button_search),
                tint = Color.White,
                modifier = Modifier.size(20.dp),
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
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        singleLine = true,
        shape = RoundedCornerShape(0.dp),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = BlueSecondary,
                unfocusedContainerColor = BlueSecondary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
            ),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProverbList(
    proverbs: LazyPagingItems<ProverbModel>,
    onProverbClick: (ProverbModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val proverbKey = proverbs.itemKey { it.slug }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 86.dp),
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

        when (val refresh = proverbs.loadState.refresh) {
            is LoadState.Loading -> {
                item(key = "refresh-loading") {
                    FullWidthLoading(modifier = Modifier.padding(top = 48.dp))
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

        when (val append = proverbs.loadState.append) {
            is LoadState.Loading -> {
                item(key = "append-loading") {
                    FullWidthLoading(modifier = Modifier.padding(vertical = 18.dp))
                }
            }

            is LoadState.Error -> {
                item(key = "append-error") {
                    ErrorState(
                        loadState = append,
                        onRetry = proverbs::retry,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun LetterHeader(letter: String) {
    Surface(
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
        shape = CircleShape,
        color = BluePrimary,
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
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
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
            Text(
                text = proverb.letter,
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = BluePrimary.copy(alpha = 0.32f),
                modifier = Modifier.padding(start = 12.dp),
            )
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
            fontSize = 13.sp,
            color = BluePrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = BluePrimary,
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
            Text(
                text = proverb.meaning?.takeIf { it.isNotBlank() }
                    ?: stringResource(id = R.string.proverb_meaning_empty),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = TextH1,
                lineHeight = 24.sp,
            )
        }
    }
}

@Composable
private fun FullWidthLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = BluePrimary,
            strokeWidth = 3.dp,
        )
    }
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
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(
                text = stringResource(id = R.string.retry),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
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
        Box(modifier = Modifier.background(BlueBg).padding(8.dp)) {
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
