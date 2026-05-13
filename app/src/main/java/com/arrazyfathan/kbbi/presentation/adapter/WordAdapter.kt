package com.arrazyfathan.kbbi.presentation.adapter

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.databinding.ItemWordEntryBinding

/**
 * Created by Ar Razy Fathan Rabbani on 18/03/23.
 */
class WordAdapter(
    private val context: Context,
) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {
    val list: MutableList<WordModel> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(items: List<WordModel>) {
        list.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemWordEntryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            data: WordModel,
            position: Int,
        ) {
            with(binding) {
                numberItem.text = position.plus(1).toString()
                entryText.text = data.entry

                val meaningAdapter = MeaningAdapter()
                rvMeaningItems.adapter = meaningAdapter
                meaningAdapter.submitList(data.meanings)

                copyButton.setOnClickListener {
                    val clipboardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    var copiedText = ""
                    for ((index, item) in data.meanings.withIndex()) {
                        copiedText +=
                            """
                            ${index + 1}. ${item.wordClass}
                            ${item.description}
                            
                            """.trimIndent()
                    }
                    val clip: ClipData = ClipData.newPlainText("meaning", copiedText)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            ItemWordEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(list[position], position)
    }

    override fun getItemCount() = list.size
}
