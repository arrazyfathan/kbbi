package com.arrazyfathan.kbbi.presentation.common

import com.arrazyfathan.kbbi.core.domain.model.DataError

fun DataError.toMessage(): String =
    when (this) {
        DataError.EmptyQuery -> "Word cannot be empty"
        DataError.NotFound -> "Data not found"
        DataError.NoInternet -> "Tidak ada koneksi internet"
        is DataError.Remote -> message
        DataError.Unknown -> "Error occurred"
    }
