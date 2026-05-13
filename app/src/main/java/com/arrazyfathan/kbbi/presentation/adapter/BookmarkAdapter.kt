package com.arrazyfathan.kbbi.presentation.adapter

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.databinding.ItemBookmarkBinding
import com.arrazyfathan.kbbi.utils.gone
import com.arrazyfathan.kbbi.utils.visible

class BookmarkAdapter(
    private val context: Context,
    private val bookmarkListener: BookmarkListener,
) : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {
    private companion object {
        const val DELETE_BUTTON_ANIMATION_DURATION_MS = 300L
        const val VIBRATION_DURATION_MS = 50L
        const val LONG_PRESS_DELAY_MS = 1000L
    }

    interface BookmarkListener {
        fun onClickListener(model: ListWordModel)

        fun onDeleteListener(model: ListWordModel)
    }

    inner class ViewHolder(
        val binding: ItemBookmarkBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListWordModel) {
            with(binding) {
                savedWordText.text = data.word.replaceFirstChar { it.uppercase() }
                savedEntryText.text = data.listWords[0].entry

                item.setOnClickListener {
                    bookmarkListener.onClickListener(data)
                }

                val vibrator = context.getSystemService(Vibrator::class.java)

                val transition = Slide(Gravity.END)
                transition.apply {
                    duration = DELETE_BUTTON_ANIMATION_DURATION_MS
                    addTarget(deleteButton)
                    interpolator = OvershootInterpolator()
                }

                item.setOnLongClickListener {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val vibrationEffect =
                                VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
                            vibrator?.vibrate(vibrationEffect)
                        } else {
                            vibrateLegacy(vibrator)
                        }
                        TransitionManager.beginDelayedTransition(root, transition)
                        deleteButton.visible()
                    }, LONG_PRESS_DELAY_MS)
                    true
                }

                cancelButton.setOnClickListener {
                    TransitionManager.beginDelayedTransition(root, transition)
                    deleteButton.gone()
                }

                deleteButton.setOnClickListener {
                    TransitionManager.beginDelayedTransition(root, transition)
                    bookmarkListener.onDeleteListener(data)
                    deleteButton.gone()
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(differ.currentList[position])
    }

    private val diffCallback =
        object : DiffUtil.ItemCallback<ListWordModel>() {
            override fun areItemsTheSame(
                oldItem: ListWordModel,
                newItem: ListWordModel,
            ): Boolean = oldItem.word == newItem.word

            override fun areContentsTheSame(
                oldItem: ListWordModel,
                newItem: ListWordModel,
            ): Boolean = oldItem == newItem
        }

    val differ = AsyncListDiffer(this, diffCallback)

    fun isEmpty(): Boolean = differ.currentList.isEmpty()

    override fun getItemCount(): Int = differ.currentList.size

    @Suppress("DEPRECATION")
    private fun vibrateLegacy(vibrator: Vibrator?) {
        vibrator?.vibrate(VIBRATION_DURATION_MS)
    }
}
