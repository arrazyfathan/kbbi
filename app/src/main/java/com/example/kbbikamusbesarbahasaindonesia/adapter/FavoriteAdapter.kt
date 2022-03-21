package com.example.kbbikamusbesarbahasaindonesia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListFavoriteBinding
import com.example.kbbikamusbesarbahasaindonesia.model.Kata

class FavoriteAdapter(
    private val listSavedKata: List<Kata>,
    private val clickListener: (Kata) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemListFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemListFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteAdapter.ViewHolder, position: Int) {
        val curretList = listSavedKata[position]

        holder.binding.kataSaved.text = curretList.kata.replaceFirstChar { it.uppercase() }
        holder.binding.lemmaSaved.text = curretList.data[0].lema

        holder.itemView.setOnClickListener {
            clickListener(curretList)
        }

    }


    override fun getItemCount(): Int {
        return listSavedKata.size
    }

}