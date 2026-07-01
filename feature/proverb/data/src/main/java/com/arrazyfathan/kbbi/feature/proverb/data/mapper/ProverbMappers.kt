package com.arrazyfathan.kbbi.feature.proverb.data.mapper

import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto.ProverbDetailDto
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto.ProverbDto
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto.ProverbPageDto
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity
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

fun ProverbPageModel.toCachedProverbs(query: String): List<CachedProverbEntity> =
    items.mapIndexed { index, proverb ->
        CachedProverbEntity(
            query = query,
            page = page,
            position = index,
            totalPages = totalPages,
            hasNextPage = hasNextPage,
            text = proverb.text,
            letter = proverb.letter,
            slug = proverb.slug,
            sourceUrl = proverb.sourceUrl,
        )
    }

fun List<CachedProverbEntity>.toProverbPage(page: Int): ProverbPageModel =
    ProverbPageModel(
        items = map { it.toProverb() },
        page = page,
        totalPages = firstOrNull()?.totalPages ?: page,
        hasNextPage = firstOrNull()?.hasNextPage == true,
    )

fun ProverbDetailModel.toCachedProverbDetail(): CachedProverbDetailEntity =
    CachedProverbDetailEntity(
        text = text,
        letter = letter,
        slug = slug,
        sourceUrl = sourceUrl,
        meaning = meaning,
    )

fun CachedProverbDetailEntity.toProverbDetail(): ProverbDetailModel =
    ProverbDetailModel(
        text = text,
        letter = letter,
        slug = slug,
        sourceUrl = sourceUrl,
        meaning = meaning,
    )

private fun CachedProverbEntity.toProverb(): ProverbModel =
    ProverbModel(
        text = text,
        letter = letter,
        slug = slug,
        sourceUrl = sourceUrl,
    )
