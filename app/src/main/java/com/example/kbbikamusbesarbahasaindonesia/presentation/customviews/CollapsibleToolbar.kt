package com.example.kbbikamusbesarbahasaindonesia.presentation.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.appbar.AppBarLayout

/**
 * Created by Ar Razy Fathan Rabbani on 21/03/23.
 */
class CollapsibleToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MotionLayout(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        progress = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? AppBarLayout)?.addOnOffsetChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        (parent as? AppBarLayout)?.removeOnOffsetChangedListener(this)
        super.onDetachedFromWindow()
    }
}