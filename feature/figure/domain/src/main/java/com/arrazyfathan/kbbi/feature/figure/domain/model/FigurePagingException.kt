package com.arrazyfathan.kbbi.feature.figure.domain.model

import com.arrazyfathan.kbbi.core.domain.model.DataError

class FigurePagingException(
    val dataError: DataError,
) : Exception()
