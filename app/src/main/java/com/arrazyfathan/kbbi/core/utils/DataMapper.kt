package com.arrazyfathan.kbbi.core.utils

import com.arrazyfathan.kbbi.core.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.core.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.core.data.source.local.entity.MeaningEntity
import com.arrazyfathan.kbbi.core.data.source.local.entity.WordEntity
import com.arrazyfathan.kbbi.core.data.source.remote.dto.MeaningDto
import com.arrazyfathan.kbbi.core.data.source.remote.dto.WordDto
import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.MeaningModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
object DataMapper {
    fun mapWordDtosToDomain(input: List<WordDto>): List<WordModel> =
        input.map {
            WordModel(
                it.entry,
                mapMeaningDtosToDomain(it.meanings),
            )
        }

    private fun mapMeaningDtosToDomain(input: List<MeaningDto>): List<MeaningModel> =
        input.map {
            MeaningModel(
                it.wordClass,
                it.description,
            )
        }

    fun mapListWordEntityToDomain(input: List<ListWordEntity>): List<ListWordModel> =
        input.map {
            ListWordModel(
                word = it.word,
                listWords = mapEntitiesToDomain(it.listWords),
            )
        }

    fun mapEntitiesToDomain(input: List<WordEntity>): List<WordModel> =
        input.map {
            WordModel(
                it.entry,
                mapMeaningEntitiesToMeaningDomain(it.meanings),
            )
        }

    private fun mapMeaningEntitiesToMeaningDomain(input: List<MeaningEntity>): List<MeaningModel> =
        input.map {
            MeaningModel(
                it.wordClass,
                it.description,
            )
        }

    fun mapDomainToEntity(input: List<WordModel>): List<WordEntity> =
        input.map {
            WordEntity(
                it.entry,
                mapMeaningDomainToMeaningEntity(it.meanings),
            )
        }

    private fun mapMeaningDomainToMeaningEntity(input: List<MeaningModel>): List<MeaningEntity> =
        input.map {
            MeaningEntity(
                it.wordClass,
                it.description,
            )
        }

    fun mapHistoryEntitiesToDomain(input: List<HistoryEntity>): List<HistoryModel> =
        input.map {
            HistoryModel(word = it.word)
        }

    fun mapHistoryDomainToEntity(input: HistoryModel): HistoryEntity = HistoryEntity(word = input.word)
}
