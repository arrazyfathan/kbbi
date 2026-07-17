package com.arrazyfathan.kbbi.feature.home.data.mapper

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.MeaningEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.MeaningDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.WordDto
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel

fun WordDto.toDomain(): WordModel =
    WordModel(
        entry = entry,
        meanings = meanings.map { it.toDomain() },
    )

fun List<WordDto>.toWordModels(): List<WordModel> = map { it.toDomain() }

private fun MeaningDto.toDomain(): MeaningModel =
    MeaningModel(
        wordClass = wordClass,
        description = description,
    )

fun ListWordEntity.toDomain(): ListWordModel =
    ListWordModel(
        word = word,
        listWords = listWords.map { it.toDomain() },
        visitorCount = visitorCount,
    )

fun List<HistoryEntity>.toHistoryModels(): List<HistoryModel> = map { it.toDomain() }

private fun HistoryEntity.toDomain(): HistoryModel = HistoryModel(word = word)

private fun WordEntity.toDomain(): WordModel =
    WordModel(
        entry = entry,
        meanings = meanings.map { it.toDomain() },
    )

private fun MeaningEntity.toDomain(): MeaningModel =
    MeaningModel(
        wordClass = wordClass,
        description = description,
    )

fun List<WordModel>.toWordEntities(): List<WordEntity> = map { it.toEntity() }

fun HistoryModel.toEntity(): HistoryEntity = HistoryEntity(word = word)

private fun WordModel.toEntity(): WordEntity =
    WordEntity(
        entry = entry,
        meanings = meanings.map { it.toEntity() },
    )

private fun MeaningModel.toEntity(): MeaningEntity =
    MeaningEntity(
        wordClass = wordClass,
        description = description,
    )
