package com.arrazyfathan.kbbi.core.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

private const val DARK_ICON_LUMINANCE_THRESHOLD = 0.5

private data class InitialPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

private data class InitialMargin(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

fun Activity.updateSystemBarStyle(
    @ColorInt statusBarColor: Int,
    @ColorInt navigationBarColor: Int = statusBarColor,
) {
    val useDarkStatusIcons = ColorUtils.calculateLuminance(statusBarColor) > DARK_ICON_LUMINANCE_THRESHOLD
    val useDarkNavigationIcons = ColorUtils.calculateLuminance(navigationBarColor) > DARK_ICON_LUMINANCE_THRESHOLD
    WindowInsetsControllerCompat(window, window.decorView).apply {
        isAppearanceLightStatusBars = useDarkStatusIcons
        isAppearanceLightNavigationBars = useDarkNavigationIcons
    }
}

fun View.applySystemBarPadding(
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false,
) {
    val initialPadding = InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = initialPadding.left + if (applyLeft) systemBars.left else 0,
            top = initialPadding.top + if (applyTop) systemBars.top else 0,
            right = initialPadding.right + if (applyRight) systemBars.right else 0,
            bottom = initialPadding.bottom + if (applyBottom) systemBars.bottom else 0,
        )
        insets
    }
    requestInsets()
}

fun View.applySystemBarMargin(
    applyLeft: Boolean = false,
    applyTop: Boolean = false,
    applyRight: Boolean = false,
    applyBottom: Boolean = false,
) {
    val layoutParams = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    val initialMargin =
        InitialMargin(
            left = layoutParams.leftMargin,
            top = layoutParams.topMargin,
            right = layoutParams.rightMargin,
            bottom = layoutParams.bottomMargin,
        )

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin = initialMargin.left + if (applyLeft) systemBars.left else 0
            topMargin = initialMargin.top + if (applyTop) systemBars.top else 0
            rightMargin = initialMargin.right + if (applyRight) systemBars.right else 0
            bottomMargin = initialMargin.bottom + if (applyBottom) systemBars.bottom else 0
        }
        insets
    }
    requestInsets()
}

fun View.applyBottomNavigationInsets() {
    val initialPadding = InitialPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    val baseHeight = layoutParams.height

    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(
            left = initialPadding.left + systemBars.left,
            right = initialPadding.right + systemBars.right,
            bottom = initialPadding.bottom + systemBars.bottom,
        )
        if (baseHeight > 0) {
            view.updateLayoutParams {
                height = baseHeight + systemBars.bottom
            }
        }
        insets
    }
    requestInsets()
}

private fun View.requestInsets() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        doOnAttach { attachedView -> attachedView.requestApplyInsets() }
    }
}
