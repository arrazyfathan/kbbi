package com.arrazyfathan.kbbi.presentation.common

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable

@Stable
class AppLoadingController {
    private val loadingSources = mutableStateMapOf<String, Boolean>()

    val isBlocking: Boolean
        get() = loadingSources.values.any { it }

    fun setBlocking(
        source: String,
        isLoading: Boolean,
    ) {
        if (isLoading) {
            loadingSources[source] = true
        } else {
            loadingSources.remove(source)
        }
    }
}

val LocalAppLoadingController =
    staticCompositionLocalOf<AppLoadingController> {
        error("AppLoadingController is not provided")
    }

@Composable
fun rememberAppLoadingController(): AppLoadingController = remember { AppLoadingController() }
