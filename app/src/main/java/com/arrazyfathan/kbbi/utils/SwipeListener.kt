package com.arrazyfathan.kbbi.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs

open class SwipeListener(
    context: Context?,
) : OnTouchListener {
    private companion object {
        const val SWIPE_THRESHOLD = 100
        const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    private val gestureDetector: GestureDetector

    override fun onTouch(
        view: View,
        motionEvent: MotionEvent,
    ): Boolean = gestureDetector.onTouchEvent(motionEvent)

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float,
        ): Boolean = handleFling(e1 ?: return false, e2, velocityX, velocityY)

        private fun handleFling(
            startEvent: MotionEvent,
            endEvent: MotionEvent,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            val diffY = endEvent.y - startEvent.y
            val diffX = endEvent.x - startEvent.x

            return if (abs(diffX) > abs(diffY)) {
                handleHorizontalFling(diffX, velocityX)
            } else {
                handleVerticalFling(diffY, velocityY)
            }
        }

        private fun handleHorizontalFling(
            diffX: Float,
            velocityX: Float,
        ): Boolean {
            if (abs(diffX) <= SWIPE_THRESHOLD || abs(velocityX) <= SWIPE_VELOCITY_THRESHOLD) {
                return false
            }

            if (diffX > 0) {
                onSwipeRight()
            } else {
                onSwipeLeft()
            }
            return true
        }

        private fun handleVerticalFling(
            diffY: Float,
            velocityY: Float,
        ): Boolean {
            if (abs(diffY) <= SWIPE_THRESHOLD || abs(velocityY) <= SWIPE_VELOCITY_THRESHOLD) {
                return false
            }

            if (diffY > 0) {
                onSwipeBottom()
            } else {
                onSwipeTop()
            }
            return true
        }
    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}

    open fun onSwipeTop() {}

    open fun onSwipeBottom() {}

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }
}
