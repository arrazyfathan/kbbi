package com.arrazyfathan.kbbi.utils

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

/**
 * Created by Ar Razy Fathan Rabbani on 16/03/23.
 */

private const val LONG_PRESS_TIMEOUT_MS = 1000L

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline bindingInflater: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.onLongClick(listener: () -> Unit) {
    setOnTouchListener(
        object : OnTouchListener {
            override fun onTouch(
                v: View,
                event: MotionEvent,
            ): Boolean {
                if (v.isPressed && event.action == MotionEvent.ACTION_DOWN) {
                    val eventDuration = event.eventTime - event.downTime
                    if (eventDuration > LONG_PRESS_TIMEOUT_MS) {
                        listener.invoke()
                    }
                }
                return false
            }
        },
    )
}
