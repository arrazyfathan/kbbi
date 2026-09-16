package com.arrazyfathan.kbbi.feature.home.presentation.home

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TopWordsRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.AddSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetTopWordsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordSuggestionsUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordWithHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var topWordsRepository: FakeTopWordsRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        topWordsRepository = FakeTopWordsRepository()
        viewModel =
            HomeViewModel(
                searchWordWithHistory =
                    SearchWordWithHistoryUseCase(
                        searchWord = SearchWordUseCase(FakeWordSearchRepository()),
                        addSearchHistory = AddSearchHistoryUseCase(FakeSearchHistoryRepository()),
                    ),
                observeSearchHistory = ObserveSearchHistoryUseCase(FakeSearchHistoryRepository()),
                getWordEntries = GetWordEntriesUseCase(FakeWordCatalogRepository()),
                getWordSuggestions = GetWordSuggestionsUseCase(),
                getTopWords = GetTopWordsUseCase(topWordsRepository),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads and caps top words at five on start`() =
        runTest {
            topWordsRepository.result =
                AppResult.Success(
                    (1..7).map { index -> TopWordModel("word$index", index.toLong()) },
                )

            viewModel.onAction(HomeAction.OnStarted)
            advanceUntilIdle()

            assertEquals(5, topWordsRepository.requestedLimit)
            assertEquals(
                listOf(
                    TopWordUi(1, "word1"),
                    TopWordUi(2, "word2"),
                    TopWordUi(3, "word3"),
                    TopWordUi(4, "word4"),
                    TopWordUi(5, "word5"),
                ),
                viewModel.state.value.topWords,
            )
        }

    @Test
    fun `hides top words when request fails`() =
        runTest {
            topWordsRepository.result = AppResult.Error(DataError.Remote("unavailable"))

            viewModel.onAction(HomeAction.OnStarted)
            advanceUntilIdle()

            assertEquals(emptyList<TopWordUi>(), viewModel.state.value.topWords)
        }
}

private class FakeTopWordsRepository : TopWordsRepository {
    var requestedLimit: Int? = null
    var result: AppResult<List<TopWordModel>, DataError> = AppResult.Success(emptyList())

    override suspend fun getTopWords(limit: Int): AppResult<List<TopWordModel>, DataError> {
        requestedLimit = limit
        return result
    }
}

private class FakeSearchHistoryRepository : SearchHistoryRepository {
    override suspend fun addToHistory(history: HistoryModel) = Unit

    override fun getAllHistories(): Flow<List<HistoryModel>> = flowOf(emptyList())

    override suspend fun clearHistory() = Unit
}

private class FakeWordCatalogRepository : WordCatalogRepository {
    override suspend fun getWords(): List<String> = emptyList()
}

private class FakeWordSearchRepository : WordSearchRepository {
    override suspend fun getMeaningOfWord(word: String): AppResult<ListWordModel, DataError> =
        AppResult.Error(DataError.NotFound)
}
