package com.arrazyfathan.kbbi.utils

import com.google.gson.Gson

fun Any.toJson(): String = Gson().toJson(this)
