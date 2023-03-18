package com.example.kbbikamusbesarbahasaindonesia.ui.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListArtiBinding

/**
 * Created by Ar Razy Fathan Rabbani on 18/03/23.
 */
class WordAdapter(private val context: Context) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {

    val list: MutableList<WordModel> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(items: List<WordModel>) {
        list.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemListArtiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: WordModel, position: Int) {
            with(binding) {
                numberItem.text = position.plus(1).toString()
                lemma.text = data.entry

                val meaningAdapter = MeaningAdapter()
                rvArtiKataChild.adapter = meaningAdapter
                meaningAdapter.submitList(data.meanings)

                btnSalin.setOnClickListener {
                    val clipboarManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    var arti = ""
                    for ((index, item) in data.meanings.withIndex()) {
                        arti += """
                    ${index + 1}. ${item.wordClass}
                    ${item.description}
                    
                        """.trimIndent()
                    }
                    val clip: ClipData = ClipData.newPlainText("arti", arti)
                    clipboarManager.setPrimaryClip(clip)
                    Toast.makeText(context, "Berhasil menyalin teks.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemListArtiBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount() = list.size
}
