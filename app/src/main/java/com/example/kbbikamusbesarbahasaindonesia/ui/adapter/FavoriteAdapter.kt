package com.example.kbbikamusbesarbahasaindonesia.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListFavoriteBinding

class FavoriteAdapter(
    private val listener: (ListWordModel) -> Unit,
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemListFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListWordModel) {
            with(binding) {
                kataSaved.text = data.word.replaceFirstChar { it.uppercase() }
                lemmaSaved.text = data.listWords[0].entry

                root.setOnClickListener {
                    listener(data)
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

    val diffCallback = object : DiffUtil.ItemCallback<ListWordModel>() {
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
