package com.example.kbbikamusbesarbahasaindonesia.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemHistoryAdapterBinding

/**
 * Created by Ar Razy Fathan Rabbani on 19/01/23.
 */
class HistoryAdapter(
    private val listener: (String) -> Unit,
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHistoryAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: HistoryEntity) {
            with(binding) {
                textHistory.text = data.word.replaceFirstChar { it.uppercase() }
                root.setOnClickListener {
                    listener(data.word)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemHistoryAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    private val diffCallback = object : DiffUtil.ItemCallback<HistoryEntity>() {
        override fun areItemsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
            return oldItem.word == newItem.word
        }

        override fun areContentsTheSame(oldItem: HistoryEntity, newItem: HistoryEntity): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun isEmpty(): Boolean = differ.currentList.isEmpty()

    override fun getItemCount() = differ.currentList.size
}
