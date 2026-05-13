package com.arrazyfathan.kbbi.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.arrazyfathan.kbbi.databinding.ItemListKosaKataBinding

class KosaKataAdapter(
    private var listKosaKata: ArrayList<String>,
    private val clickListener: (String) -> Unit,
) : RecyclerView.Adapter<KosaKataAdapter.KosaKataViewHolder>(),
    Filterable {
    var kataFilterList = ArrayList<String>()

    init {
        kataFilterList = listKosaKata
    }

    class KosaKataViewHolder(
        val binding: ItemListKosaKataBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): KosaKataViewHolder {
        val binding =
            ItemListKosaKataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KosaKataViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: KosaKataViewHolder,
        position: Int,
    ) {
        val data = kataFilterList[position]
        holder.binding.kosaKataList.text = data

        holder.itemView.setOnClickListener {
            clickListener(data)
        }
    }

    override fun getItemCount(): Int = kataFilterList.size

    @SuppressLint("NotifyDataSetChanged")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    kataFilterList = listKosaKata
                } else {
                    val resultList = ArrayList<String>()
                    for (row in listKosaKata) {
                        if (row.lowercase().contains(constraint.toString().lowercase())) {
                            resultList.add(row)
                        }
                    }
                    kataFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = kataFilterList
                return filterResults
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?,
            ) {
                kataFilterList =
                    when (val values = results?.values) {
                        is List<*> -> ArrayList(values.filterIsInstance<String>())
                        else -> arrayListOf()
                    }
                notifyDataSetChanged()
            }
        }
    }
}
