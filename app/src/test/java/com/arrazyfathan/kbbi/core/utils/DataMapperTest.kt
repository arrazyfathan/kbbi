package com.arrazyfathan.kbbi.core.utils

import com.arrazyfathan.kbbi.core.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.core.data.source.local.entity.MeaningEntity
import com.arrazyfathan.kbbi.core.data.source.local.entity.WordEntity
import com.arrazyfathan.kbbi.core.data.source.remote.response.Meaning
import com.arrazyfathan.kbbi.core.data.source.remote.response.WordResponse
import com.arrazyfathan.kbbi.core.domain.model.MeaningModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import org.junit.Assert.assertEquals
import org.junit.Test

class DataMapperTest {
    @Test
    fun testMapResponseToDomain() {
        val meaning1 = Meaning(wordClass = "n", description = "kata benda")
        val meaning2 = Meaning(wordClass = "v", description = "kata kerja")
        val wordResponse = WordResponse(entry = "makan", meanings = listOf(meaning1, meaning2))

        val domainList = DataMapper.mapResponseToDomain(listOf(wordResponse))

        assertEquals(1, domainList.size)
        val domainWord = domainList[0]
        assertEquals("makan", domainWord.entry)
        assertEquals(2, domainWord.meanings.size)
        assertEquals("n", domainWord.meanings[0].wordClass)
        assertEquals("kata benda", domainWord.meanings[0].description)
        assertEquals("v", domainWord.meanings[1].wordClass)
        assertEquals("kata kerja", domainWord.meanings[1].description)
    }

    @Test
    fun testMapListWordEntityToDomain() {
        val meaningEntity = MeaningEntity(wordClass = "adj", description = "sifat")
        val wordEntity = WordEntity(entry = "indah", meanings = listOf(meaningEntity))
        val listWordEntity = ListWordEntity(word = "indah", listWords = listOf(wordEntity), isSaved = true)

        val domainList = DataMapper.mapListWordEntityToDomain(listOf(listWordEntity))

        assertEquals(1, domainList.size)
        val domainListWord = domainList[0]
        assertEquals("indah", domainListWord.word)
        assertEquals(1, domainListWord.listWords.size)

        val firstWord = domainListWord.listWords[0]
        assertEquals("indah", firstWord.entry)
        assertEquals(1, firstWord.meanings.size)
        assertEquals("adj", firstWord.meanings[0].wordClass)
        assertEquals("sifat", firstWord.meanings[0].description)
    }

    @Test
    fun testMapDomainToEntity() {
        val meaningModel = MeaningModel(wordClass = "adv", description = "keterangan")
        val wordModel = WordModel(entry = "cepat", meanings = listOf(meaningModel))

        val entityList = DataMapper.mapDomainToEntity(listOf(wordModel))

        assertEquals(1, entityList.size)
        val entityWord = entityList[0]
        assertEquals("cepat", entityWord.entry)
        assertEquals(1, entityWord.meanings.size)
        assertEquals("adv", entityWord.meanings[0].wordClass)
        assertEquals("keterangan", entityWord.meanings[0].description)
    }
}
