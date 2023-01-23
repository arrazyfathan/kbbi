package com.example.kbbikamusbesarbahasaindonesia.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kbbikamusbesarbahasaindonesia.databinding.ItemHistoryAdapterBinding
import com.example.kbbikamusbesarbahasaindonesia.model.History

/**
 * Created by Ar Razy Fathan Rabbani on 19/01/23.
 */
class HistoryAdapter(
    private val listHistory: List<History>,
    private val clickListener: (History) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHistoryAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemHistoryAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.ViewHolder, position: Int) {
        val curretList = listHistory[position]

        holder.binding.textHistory.text = curretList.kata.replaceFirstChar { it.uppercase() }

        holder.itemView.setOnClickListener {
            clickListener(curretList)
        }
    }

    fun isEmpty(): Boolean = listHistory.isEmpty()

    override fun getItemCount(): Int {
        return listHistory.size.coerceAtMost(7)
    }
}
