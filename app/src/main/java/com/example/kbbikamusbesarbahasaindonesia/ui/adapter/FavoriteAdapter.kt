package com.example.kbbikamusbesarbahasaindonesia.ui.adapter

import android.content.Context
import android.os.Build
import android.os.Handler
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
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListFavoriteBinding
import com.example.kbbikamusbesarbahasaindonesia.utils.gone
import com.example.kbbikamusbesarbahasaindonesia.utils.visible

class FavoriteAdapter(
    private val context: Context,
    private val favoriteListener: FavoriteListener,
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    interface FavoriteListener {
        fun onClickListener(model: ListWordModel)
        fun onDeleteListener(model: ListWordModel)
    }

    inner class ViewHolder(val binding: ItemListFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListWordModel) {
            with(binding) {
                kataSaved.text = data.word.replaceFirstChar { it.uppercase() }
                lemmaSaved.text = data.listWords[0].entry

                item.setOnClickListener {
                    favoriteListener.onClickListener(data)
                }

                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(Vibrator::class.java)
                } else {
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                val transition = Slide(Gravity.END)
                transition.apply {
                    duration = 300
                    addTarget(btnDelete)
                    interpolator = OvershootInterpolator()
                }

                item.setOnLongClickListener {
                    Handler().postDelayed({
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val vibrationEffect =
                                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                            vibrator?.vibrate(vibrationEffect)
                        } else {
                            vibrator?.vibrate(50)
                        }
                        TransitionManager.beginDelayedTransition(root, transition)
                        btnDelete.visible()
                    }, 1000L)
                    true
                }

                btnCancel.setOnClickListener {
                    TransitionManager.beginDelayedTransition(root, transition)
                    btnDelete.gone()
                }

                btnDelete.setOnClickListener {
                    TransitionManager.beginDelayedTransition(root, transition)
                    favoriteListener.onDeleteListener(data)
                    btnDelete.gone()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemListFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    private val diffCallback = object : DiffUtil.ItemCallback<ListWordModel>() {
        override fun areItemsTheSame(oldItem: ListWordModel, newItem: ListWordModel): Boolean {
            return oldItem.word == newItem.word
        }

        override fun areContentsTheSame(oldItem: ListWordModel, newItem: ListWordModel): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun isEmpty(): Boolean = differ.currentList.isEmpty()

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}
