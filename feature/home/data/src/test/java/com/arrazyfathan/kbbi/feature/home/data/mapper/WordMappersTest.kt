package com.arrazyfathan.kbbi.feature.home.data.mapper

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.MeaningEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.MeaningDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.TranslatedDefinitionDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.TranslatedEntryDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.TranslateDataDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.WordDto
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
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
        val listWordEntity =
            ListWordEntity(
                word = "indah",
                listWords = listOf(wordEntity),
                visitorCount = 9,
                isSaved = true,
            )

        val domainListWord = listWordEntity.toDomain()

        assertEquals("indah", domainListWord.word)
        assertEquals(9, domainListWord.visitorCount)
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

    @Test
    fun translateDataDtoMapsToDomainModel() {
        val definition =
            TranslatedDefinitionDto(
                wordClass = "v",
                description = "berusaha memperoleh kepandaian atau ilmu",
                translation = "attempt to gain knowledge or skill",
            )
        val entry = TranslatedEntryDto(headword = "belajar", definitions = listOf(definition))
        val dto =
            TranslateDataDto(
                word = "belajar",
                translation = "learn",
                from = "id",
                to = "en",
                provider = "google",
                entries = listOf(entry),
            )

        val domain = dto.toDomain()

        assertEquals("belajar", domain.word)
        assertEquals("learn", domain.translation)
        assertEquals("id", domain.from)
        assertEquals("en", domain.to)
        assertEquals("google", domain.provider)
        assertEquals(1, domain.entries.size)
        assertEquals("belajar", domain.entries[0].headword)
        assertEquals(1, domain.entries[0].meanings.size)
        assertEquals("v", domain.entries[0].meanings[0].wordClass)
        assertEquals("attempt to gain knowledge or skill", domain.entries[0].meanings[0].translation)
    }
}
