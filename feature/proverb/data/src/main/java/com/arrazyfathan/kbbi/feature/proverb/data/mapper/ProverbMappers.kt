package com.arrazyfathan.kbbi.feature.proverb.data.mapper

import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto.ProverbDetailDto
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto.ProverbDto
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto.ProverbPageDto
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbPageModel

fun ProverbDto.toProverb(): ProverbModel =
    ProverbModel(
        text = text,
        letter = letter,
        slug = slug,
        sourceUrl = sourceUrl,
    )

fun ProverbDetailDto.toProverbDetail(): ProverbDetailModel =
    ProverbDetailModel(
        text = text,
        letter = letter,
        slug = slug,
        sourceUrl = sourceUrl,
        meaning = meaning,
    )

fun ProverbPageDto.toProverbPage(): ProverbPageModel =
    ProverbPageModel(
        items = items.map { it.toProverb() },
        page = pagination.page,
        totalPages = pagination.totalPages,
        hasNextPage = pagination.hasNextPage,
    )
