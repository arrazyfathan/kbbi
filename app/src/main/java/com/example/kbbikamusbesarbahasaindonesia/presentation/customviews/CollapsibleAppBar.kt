package com.example.kbbikamusbesarbahasaindonesia.presentation.customviews

import android.content.Context
import android.util.AttributeSet
import com.example.kbbikamusbesarbahasaindonesia.R
import com.google.android.material.appbar.AppBarLayout

/**
 * Created by Ar Razy Fathan Rabbani on 21/03/23.
 */
class CollapsibleAppbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppBarLayout(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {
    private val toolbarElevation = context.resources.getDimension(R.dimen.toolbar_elevation)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addOnOffsetChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        removeOnOffsetChangedListener(this)
        super.onDetachedFromWindow()
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        val progress = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
        appBarLayout.elevation = if (progress == 1f) {
            toolbarElevation
        } else {
            0f
        }
    }
}
