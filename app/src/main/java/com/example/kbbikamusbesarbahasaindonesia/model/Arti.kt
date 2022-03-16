package com.example.kbbikamusbesarbahasaindonesia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Arti(
    val deskripsi: String,
    val kelas_kata: String
) : Parcelable