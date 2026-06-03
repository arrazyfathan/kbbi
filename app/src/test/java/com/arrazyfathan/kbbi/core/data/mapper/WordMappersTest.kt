package com.arrazyfathan.kbbi.core.data.mapper

import com.arrazyfathan.kbbi.core.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.core.data.source.local.entity.MeaningEntity
import com.arrazyfathan.kbbi.core.data.source.local.entity.WordEntity
import com.arrazyfathan.kbbi.core.data.source.remote.dto.MeaningDto
import com.arrazyfathan.kbbi.core.data.source.remote.dto.WordDto
import com.arrazyfathan.kbbi.core.domain.model.MeaningModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import org.junit.Assert.assertEquals
import org.junit.Test

class WordMappersTest {
    @Test
    fun wordDtosMapToDomainModels() {
        val meaning1 = MeaningDto(wordClass = "n", description = "kata benda")
        val meaning2 = MeaningDto(wordClass = "v", description = "kata kerja")
        val wordDto = WordDto(entry = "makan", meanings = listOf(meaning1, meaning2))

        val domainList = listOf(wordDto).toWordModels()

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
    fun listWordEntityMapsToDomainModel() {
        val meaningEntity = MeaningEntity(wordClass = "adj", description = "sifat")
        val wordEntity = WordEntity(entry = "indah", meanings = listOf(meaningEntity))
        val listWordEntity = ListWordEntity(word = "indah", listWords = listOf(wordEntity), isSaved = true)

        val domainListWord = listWordEntity.toDomain()

        assertEquals("indah", domainListWord.word)
        assertEquals(1, domainListWord.listWords.size)
        assertEquals("indah", domainListWord.listWords[0].entry)
        assertEquals("adj", domainListWord.listWords[0].meanings[0].wordClass)
        assertEquals("sifat", domainListWord.listWords[0].meanings[0].description)
    }

    @Test
    fun domainModelsMapToWordEntities() {
        val meaningModel = MeaningModel(wordClass = "adv", description = "keterangan")
        val wordModel = WordModel(entry = "cepat", meanings = listOf(meaningModel))

        val entityList = listOf(wordModel).toWordEntities()

        assertEquals(1, entityList.size)
        val entityWord = entityList[0]
        assertEquals("cepat", entityWord.entry)
        assertEquals(1, entityWord.meanings.size)
        assertEquals("adv", entityWord.meanings[0].wordClass)
        assertEquals("keterangan", entityWord.meanings[0].description)
    }
}
