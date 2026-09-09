package com.arrazyfathan.kbbi.feature.figure.data.mapper

import com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto.FigureDto
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto.FigurePageDto
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigurePageModel

fun FigureDto.toDomain(): FigureModel =
    FigureModel(
        name = name,
        slug = slug,
        sourceUrl = sourceUrl,
        photo = photo,
        description = description,
        quotes = quotes,
    )

fun FigurePageDto.toDomain(): FigurePageModel =
    FigurePageModel(
        items = items.map(FigureDto::toDomain),
        page = pagination.page,
        totalPages = pagination.totalPages,
        hasNextPage = pagination.hasNextPage,
    )
