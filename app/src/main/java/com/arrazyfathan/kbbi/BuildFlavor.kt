package com.arrazyfathan.kbbi

import android.content.Context

fun Context.isProductionFlavor(): Boolean = resources.getBoolean(R.bool.is_production_flavor)
