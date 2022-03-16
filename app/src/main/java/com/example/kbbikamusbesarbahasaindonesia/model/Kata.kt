package com.example.kbbikamusbesarbahasaindonesia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Kata(
    val data: List<Data>,
    val message: String,
    val status: Boolean
) : Parcelable