package com.arrazyfathan.kbbi.feature.proverb.domain.usecase

import androidx.paging.PagingData
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ProverbUseCasesTest {
    private val fakeRepository = FakeProverbRepository()

    @Test
    fun getProverbMeaningReturnsRepositoryResult() =
        runBlocking {
            val detail =
                ProverbDetailModel(
                    text = "Air beriak tanda tak dalam",
                    letter = "A",
                    slug = "air-beriak-tanda-tak-dalam",
                    sourceUrl = "https://example.com/proverb/air-beriak-tanda-tak-dalam",
                    meaning = "orang yang banyak bicara biasanya tidak berilmu",
                )
            fakeRepository.meaningResult = AppResult.Success(detail)

            val result = GetProverbMeaningUseCase(fakeRepository)(detail.slug)

            assertTrue(result is AppResult.Success)
            assertEquals(detail.slug, fakeRepository.requestedSlug)
            assertEquals(detail, (result as AppResult.Success).data)
        }

    @Test
    fun getProverbMeaningReturnsRepositoryError() =
        runBlocking {
            fakeRepository.meaningResult = AppResult.Error(DataError.NotFound)

            val result = GetProverbMeaningUseCase(fakeRepository)("missing-proverb")

            assertTrue(result is AppResult.Error)
            assertEquals("missing-proverb", fakeRepository.requestedSlug)
            assertSame(DataError.NotFound, (result as AppResult.Error).error)
        }

    @Test
    fun getListProverbsDelegatesQueryToRepository() =
        runBlocking {
            val pagingData = PagingData.from(listOf(ProverbModel("Ada gula ada semut", "A", "ada-gula-ada-semut", null)))
            fakeRepository.proverbs = flowOf(pagingData)

            val result = GetListProverbsUseCase(fakeRepository)("gula").first()

            assertEquals("gula", fakeRepository.requestedQuery)
            assertSame(pagingData, result)
        }
}

private class FakeProverbRepository : ProverbRepository {
    var requestedQuery: String? = null
    var requestedSlug: String? = null
    var proverbs: Flow<PagingData<ProverbModel>> = flowOf(PagingData.empty())
    var meaningResult: AppResult<ProverbDetailModel, DataError> = AppResult.Error(DataError.Unknown)

    override fun getListProverbs(query: String): Flow<PagingData<ProverbModel>> {
        requestedQuery = query
        return proverbs
    }

    override suspend fun getProverbMeaning(slug: String): AppResult<ProverbDetailModel, DataError> {
        requestedSlug = slug
        return meaningResult
    }
}
