package com.example.kbbikamusbesarbahasaindonesia.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Data(
    val arti: List<Arti>,
    val lema: String
) : Parcelable