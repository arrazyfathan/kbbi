package com.example.kbbikamusbesarbahasaindonesia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.data.KosaKata
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListKosaKataBinding


class KosaKataAdapter(
    val listKosaKata: KosaKata
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
            onItemClickListener?.let {
                it(data)
            }
        }
    }

    override fun getItemCount(): Int = listKosaKata.size

    /* OnClick Listener */
    private var onItemClickListener:((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }
}