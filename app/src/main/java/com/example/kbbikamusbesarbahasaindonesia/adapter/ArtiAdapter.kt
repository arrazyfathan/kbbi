package com.example.kbbikamusbesarbahasaindonesia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListChildArtiBinding
import com.example.kbbikamusbesarbahasaindonesia.model.Arti

class ArtiAdapter(
    private val listArti: List<Arti>
) : RecyclerView.Adapter<ArtiAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemListChildArtiBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemListChildArtiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtiAdapter.ViewHolder, position: Int) {
        val arti = listArti[position]
        holder.binding.deskripsi.text = "${position+1}. ${arti.kelas_kata}\n${arti.deskripsi}"

    }

    override fun getItemCount(): Int = listArti.size
}