package com.example.kbbikamusbesarbahasaindonesia.utils

import android.R.attr.onClick
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.jakewharton.rxbinding4.view.longClicks


/**
 * Created by Ar Razy Fathan Rabbani on 16/03/23.
 */

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T,
) =
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
    setOnTouchListener(object : OnTouchListener {
        private val longPressTimeout = 1000L
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (v.isPressed && event.action == MotionEvent.ACTION_DOWN) {
                val eventDuration = event.eventTime - event.downTime
                if (eventDuration > longPressTimeout) {
                    listener.invoke()
                }
            }
            return false
        }
    })
}
