package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import org.junit.Assert.assertEquals
import org.junit.Test

class WordStudySourceMappersTest {
    @Test
    fun `maps multiple entries and preserves word classes and definitions`() {
        val source =
            ListWordModel(
                word = "  batas ",
                listWords = listOf(
                    WordModel("batas", listOf(MeaningModel("n", "garis pemisah"))),
                    WordModel("batasan", listOf(MeaningModel("v", "sesuatu yang membatasi"))),
                ),
            ).toWordStudySourceModel()

        assertEquals("batas", source.word)
        assertEquals(listOf("batas", "batasan"), source.entries.map { it.headword })
        assertEquals("n", source.entries[0].definitions[0].wordClass)
        assertEquals("sesuatu yang membatasi", source.entries[1].definitions[0].description)
    }
}
