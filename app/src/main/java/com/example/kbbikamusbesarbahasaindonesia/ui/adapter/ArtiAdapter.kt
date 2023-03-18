package com.example.kbbikamusbesarbahasaindonesia.ui.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.core.text.toSpanned
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemListChildArtiBinding
import com.example.kbbikamusbesarbahasaindonesia.model.Arti

class ArtiAdapter(
    private val listArti: List<Arti>
) : RecyclerView.Adapter<ArtiAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemListChildArtiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemListChildArtiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val arti = listArti[position]

        val number = "${position + 1}. "
        val regex = Regex("\\[(.*?)\\]")
        val colorBoldString = ForegroundColorSpan(Color.parseColor("#2E494C"))
        val boloString = StyleSpan(Typeface.BOLD)
        val kelasKata =arti.kelas_kata.replace(regex, " ").toSpannable()
        kelasKata.setSpan(colorBoldString, 0, kelasKata.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        kelasKata.setSpan(boloString, 0, kelasKata.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val deskripsi = " ${arti.deskripsi.replace(Regex("\\?(.*)"), "")}"

        holder.binding.deskripsi.text = TextUtils.concat(number, kelasKata, deskripsi)

    }

    override fun getItemCount(): Int = listArti.size
}