package com.arrazyfathan.kbbi.feature.proverb.domain.model

import com.arrazyfathan.kbbi.core.domain.model.DataError

class ProverbPagingException(
    val dataError: DataError,
) : Exception()
