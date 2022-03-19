package com.example.kbbikamusbesarbahasaindonesia.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListArtiBinding
import com.example.kbbikamusbesarbahasaindonesia.model.Data

class KataAdapter(
    private val listData: List<Data>
) : RecyclerView.Adapter<KataAdapter.ArtiKataViewHolder>() {

    inner class ArtiKataViewHolder(val binding: ItemListArtiBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArtiKataViewHolder {
        val binding = ItemListArtiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtiKataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtiKataViewHolder, position: Int) {
        val data = listData[position]

        holder.binding.apply {
            numberItem.text = (position.plus(1)).toString()
            lemma.text = data.lema
        }

        holder.binding.rvArtiKataChild.apply {
            layoutManager = LinearLayoutManager(holder.binding.rvArtiKataChild.context)
            adapter = ArtiAdapter(listData[position].arti)
            recycledViewPool
        }

        holder.binding.btnSalin.setOnClickListener {
            val clipboarManager = holder.itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("arti", data.lema)
            clipboarManager.setPrimaryClip(clip)
            Toast.makeText(holder.itemView.context, "Berhasil menyalin teks.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = listData.size



}