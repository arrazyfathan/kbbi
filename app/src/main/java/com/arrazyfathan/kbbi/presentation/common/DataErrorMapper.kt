package com.arrazyfathan.kbbi.presentation.common

import com.arrazyfathan.kbbi.core.domain.model.DataError

fun DataError.toMessage(): String =
    when (this) {
        DataError.EmptyQuery -> "Word cannot be empty"
        DataError.NoInternet -> "Tidak ada koneksi internet"
        DataError.BadRequest -> "Permintaan tidak valid"
        DataError.RequestTimeout -> "Koneksi terlalu lama"
        DataError.Unauthorized -> "Akses tidak diizinkan"
        DataError.Forbidden -> "Akses ditolak"
        DataError.NotFound -> "Data not found"
        DataError.Conflict -> "Data konflik"
        DataError.TooManyRequests -> "Terlalu banyak permintaan"
        DataError.PayloadTooLarge -> "Data terlalu besar"
        DataError.ServerError -> "Server sedang bermasalah"
        DataError.ServiceUnavailable -> "Layanan sedang tidak tersedia"
        DataError.EmptyBody -> "Data kosong"
        DataError.Serialization -> "Format data tidak valid"
        is DataError.Remote -> message
        DataError.Unknown -> "Error occurred"
    }
