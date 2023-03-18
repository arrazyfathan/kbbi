package com.example.kbbikamusbesarbahasaindonesia.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.MeaningModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListChildArtiBinding

/**
 * Created by Ar Razy Fathan Rabbani on 18/03/23.
 */
class MeaningAdapter : RecyclerView.Adapter<MeaningAdapter.ViewHolder>() {

    val list: MutableList<MeaningModel> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(items: List<MeaningModel>) {
        list.addAll(items)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemListChildArtiBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MeaningModel, position: Int) {
            with(binding) {
                val number = "${position + 1}. "
                val regex = Regex("\\[(.*?)\\]")
                val colorBoldString = ForegroundColorSpan(Color.parseColor("#2E494C"))
                val boloString = StyleSpan(Typeface.BOLD)
                val kelasKata = data.wordClass.replace(regex, " ").toSpannable()
                kelasKata.setSpan(
                    colorBoldString,
                    0,
                    kelasKata.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                kelasKata.setSpan(
                    boloString,
                    0,
                    kelasKata.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                val description = " ${data.description.replace(Regex("\\?(.*)"), "")}"
                deskripsi.text = TextUtils.concat(number, kelasKata, description)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeaningAdapter.ViewHolder {
        return ViewHolder(
            ItemListChildArtiBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: MeaningAdapter.ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
