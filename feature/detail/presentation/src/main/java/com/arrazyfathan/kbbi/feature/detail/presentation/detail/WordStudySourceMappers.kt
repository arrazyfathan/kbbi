package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyDefinitionModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyEntryModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudySourceModel

internal fun ListWordModel.toWordStudySourceModel(): WordStudySourceModel =
    WordStudySourceModel(
        word = word.trim(),
        aiGenerated = aiGenerated,
        entries = listWords.map { entry ->
            WordStudyEntryModel(
                headword = entry.entry,
                definitions = entry.meanings.map { meaning ->
                    WordStudyDefinitionModel(meaning.wordClass, meaning.description)
                },
            )
        },
    )
