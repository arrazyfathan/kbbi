package com.example.kbbikamusbesarbahasaindonesia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.data.KosaKata
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListKosaKataBinding
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import kotlin.math.sign


class KosaKataAdapter(
    private var listKosaKata: KosaKata,
    private val clickListener: (String) -> Unit
) : RecyclerView.Adapter<KosaKataAdapter.KosaKataViewHolder>() {



    inner class KosaKataViewHolder(val binding: ItemListKosaKataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): KosaKataViewHolder{
        val binding = ItemListKosaKataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KosaKataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KosaKataViewHolder, position: Int) {
        val data = listKosaKata[position]
        holder.binding.kosaKataList.text = data

        holder.itemView.setOnClickListener {
           clickListener(data)
        }
    }

    override fun getItemCount(): Int = listKosaKata.size

    /*fun submitList(kosaKata: KosaKata) {
        val oldList = listKosaKata
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            KosaKataDiffCallback(
                oldList,
                kosaKata
            )
        )
        listKosaKata = kosaKata
        diffResult.dispatchUpdatesTo(this)
    }*/

    class KosaKataDiffCallback(
        private var oldData: KosaKata,
        private var newData: KosaKata
    ): DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldData.size
        }

        override fun getNewListSize(): Int {
            return newData.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData[oldItemPosition] == newData[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldData == newData
        }
    }

    /* OnClick Listener */
  /*  private var onItemClickListener:((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }*/
}