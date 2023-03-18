package com.example.kbbikamusbesarbahasaindonesia.core.utils

import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.MeaningEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.WordEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.response.Meaning
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.response.WordResponse
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.MeaningModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
object DataMapper {

    fun mapResponseToEntities(input: List<WordResponse>): List<WordEntity> {
        val wordList = ArrayList<WordEntity>()
        input.map {
            val wordEntity = WordEntity(
                entry = it.entry,
                meanings = mapMeaningResponseToMeaningEntity(it.meanings),
            )
            wordList.add(wordEntity)
        }
        return wordList
    }

    private fun mapMeaningResponseToMeaningEntity(input: List<Meaning>): List<MeaningEntity> {
        return input.map {
            MeaningEntity(
                it.wordClass,
                it.description,
            )
        }
    }

    fun mapResponseToDomain(input: List<WordResponse>): List<WordModel> {
        return input.map {
            WordModel(
                it.entry,
                mapMeaningResponseToMeaningDomain(it.meanings),
            )
        }
    }

    private fun mapMeaningResponseToMeaningDomain(input: List<Meaning>): List<MeaningModel> {
        return input.map {
            MeaningModel(
                it.wordClass,
                it.description,
            )
        }
    }

    fun mapEntitiesToDomain(input: List<WordEntity>): List<WordModel> {
        return input.map {
            WordModel(
                it.entry,
                mapMeaningEntitiesToMeaningDomain(it.meanings),
            )
        }
    }

    private fun mapMeaningEntitiesToMeaningDomain(input: List<MeaningEntity>): List<MeaningModel> {
        return input.map {
            MeaningModel(
                it.wordClass,
                it.description,
            )
        }
    }

    fun mapDomainToEntity(input: List<WordModel>): List<WordEntity> {
        return input.map {
            WordEntity(
                it.entry,
                mapMeaningDomainToMeaningEntity(it.meanings),
            )
        }
    }

    private fun mapMeaningDomainToMeaningEntity(input: List<MeaningModel>): List<MeaningEntity> {
        return input.map {
            MeaningEntity(
                it.wordClass,
                it.description,
            )
        }
    }
}
