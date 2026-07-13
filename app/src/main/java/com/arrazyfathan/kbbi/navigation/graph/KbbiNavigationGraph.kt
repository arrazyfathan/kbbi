package com.arrazyfathan.kbbi.navigation.graph

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark.BookmarksScreenContent
import com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark.BookmarksState
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailContent
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailState
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeContent
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeState
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbScreen
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbState
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordListScreenContent
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordListState
import com.arrazyfathan.kbbi.navigation.DetailNavRoute
import com.arrazyfathan.kbbi.navigation.Screen
import com.github.skydoves.navgraph.annotations.NavDestination
import com.github.skydoves.navgraph.annotations.NavEdge
import com.github.skydoves.navgraph.annotations.NavPreview
import kotlinx.coroutines.flow.MutableStateFlow

@NavEdge(to = DetailNavRoute::class, label = "search result")
@NavEdge(to = Screen.Proverb::class, label = "proverb menu")
@NavEdge(to = Screen.WordList::class, label = "bottom nav")
@NavEdge(to = Screen.Bookmarks::class, label = "bottom nav")
@NavDestination(route = Screen.Home::class)
@Composable
fun GraphHomeScreen() {
    KBBITheme {
        HomeContent(
            state =
                HomeState(
                    searchQuery = "bahasa",
                    histories =
                        listOf(
                            HistoryModel(word = "belajar"),
                            HistoryModel(word = "aksara"),
                            HistoryModel(word = "makna"),
                        ),
                    suggestions = listOf("bahasa", "berbahasa", "kebahasaan"),
                ),
            onNavigateToProverb = {},
            onAction = {},
        )
    }
}

@NavEdge(to = DetailNavRoute::class, label = "open word")
@NavEdge(to = Screen.Home::class, label = "bottom nav")
@NavEdge(to = Screen.Bookmarks::class, label = "bottom nav")
@NavDestination(route = Screen.WordList::class)
@Composable
fun GraphWordListScreen() {
    KBBITheme {
        WordListScreenContent(
            state =
                WordListState(
                    words = sampleDictionary,
                    filteredWords = sampleDictionary,
                ),
            onAction = {},
        )
    }
}

@NavEdge(to = DetailNavRoute::class, label = "open saved")
@NavEdge(to = Screen.Home::class, label = "bottom nav")
@NavEdge(to = Screen.WordList::class, label = "bottom nav")
@NavDestination(route = Screen.Bookmarks::class)
@Composable
fun GraphBookmarksScreen() {
    KBBITheme {
        BookmarksScreenContent(
            state = BookmarksState(bookmarks = sampleBookmarks),
            onNavigateToDetail = {},
            onAction = {},
        )
    }
}

@NavEdge(to = Screen.Home::class, label = "back")
@NavDestination(route = Screen.Proverb::class)
@Composable
fun GraphProverbScreen() {
    val proverbs = MutableStateFlow(PagingData.from(sampleProverbs)).collectAsLazyPagingItems()

    KBBITheme {
        ProverbScreen(
            state =
                ProverbState(
                    selectedProverb =
                        ProverbDetailModel(
                            text = "Air beriak tanda tak dalam",
                            letter = "A",
                            slug = "Air_beriak_tanda_tak_dalam",
                            sourceUrl = null,
                            meaning = "Orang yang banyak bicara biasanya kurang ilmunya.",
                        ),
                ),
            proverbs = proverbs,
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@NavEdge(to = Screen.Bookmarks::class, label = "bookmark")
@NavDestination(route = DetailNavRoute::class)
@Composable
fun GraphWordDetailScreen() {
    KBBITheme {
        DetailContent(
            listWordModel = sampleDetailWord,
            state = DetailState(isSaved = false),
            onAction = {},
            onShowAlert = { _, _ -> },
            alertState = null,
        )
    }
}

@NavPreview(route = Screen.Home::class, primary = true)
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
internal fun GraphHomePreview() = GraphHomeScreen()

@NavPreview(route = Screen.WordList::class, primary = true)
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
internal fun GraphWordListPreview() = GraphWordListScreen()

@NavPreview(route = Screen.Bookmarks::class, primary = true)
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
internal fun GraphBookmarksPreview() = GraphBookmarksScreen()

@NavPreview(route = Screen.Proverb::class, primary = true)
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
internal fun GraphProverbPreview() = GraphProverbScreen()

@NavPreview(route = DetailNavRoute::class, primary = true)
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
internal fun GraphWordDetailPreview() = GraphWordDetailScreen()

private val sampleDictionary =
    listOf(
        "Abjad",
        "Baca",
        "Cacing",
        "Dadu",
        "Ember",
        "Fajar",
        "Gawai",
        "Hikayat",
        "Ilmu",
        "Jelajah",
    )

private val sampleDetailWord =
    ListWordModel(
        word = "bahasa",
        listWords =
            listOf(
                WordModel(
                    entry = "bahasa",
                    meanings =
                        listOf(
                            MeaningModel(
                                wordClass = "n",
                                description = "sistem lambang bunyi yang arbitrer, digunakan oleh anggota masyarakat",
                            ),
                            MeaningModel(
                                wordClass = "n",
                                description = "percakapan atau perkataan yang baik dan sopan",
                            ),
                        ),
                ),
            ),
    )

private val sampleBookmarks =
    listOf(
        sampleDetailWord,
        ListWordModel(
            word = "aksara",
            listWords =
                listOf(
                    WordModel(
                        entry = "aksara",
                        meanings = listOf(MeaningModel(wordClass = "n", description = "sistem tanda grafis")),
                    ),
                ),
        ),
        ListWordModel(
            word = "makna",
            listWords =
                listOf(
                    WordModel(
                        entry = "makna",
                        meanings = listOf(MeaningModel(wordClass = "n", description = "arti atau maksud")),
                    ),
                ),
        ),
    )

private val sampleProverbs =
    listOf(
        ProverbModel("Air beriak tanda tak dalam", "A", "Air_beriak_tanda_tak_dalam", null),
        ProverbModel("Bagai air di daun talas", "B", "Bagai_air_di_daun_talas", null),
        ProverbModel("Cepat kaki ringan tangan", "C", "Cepat_kaki_ringan_tangan", null),
        ProverbModel("Darah daging sendiri", "D", "Darah_daging_sendiri", null),
        ProverbModel("Emas bersepuh perak", "E", "Emas_bersepuh_perak", null),
    )
