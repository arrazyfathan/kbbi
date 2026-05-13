package com.arrazyfathan.kbbi.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.arrazyfathan.kbbi.databinding.ItemWordListBinding

class WordListAdapter(
    private var wordList: ArrayList<String>,
    private val clickListener: (String) -> Unit,
) : RecyclerView.Adapter<WordListAdapter.WordViewHolder>(),
    Filterable {
    var filteredWordList = ArrayList<String>()

    init {
        filteredWordList = wordList
    }

    class WordViewHolder(
        val binding: ItemWordListBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): WordViewHolder {
        val binding =
            ItemWordListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: WordViewHolder,
        position: Int,
    ) {
        val data = filteredWordList[position]
        holder.binding.wordText.text = data

        holder.itemView.setOnClickListener {
            clickListener(data)
        }
    }

    override fun getItemCount(): Int = filteredWordList.size

    @SuppressLint("NotifyDataSetChanged")
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchQuery = constraint.toString()
                if (searchQuery.isEmpty()) {
                    filteredWordList = wordList
                } else {
                    val resultList = ArrayList<String>()
                    for (row in wordList) {
                        if (row.lowercase().contains(constraint.toString().lowercase())) {
                            resultList.add(row)
                        }
                    }
                    filteredWordList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredWordList
                return filterResults
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?,
            ) {
                filteredWordList =
                    when (val values = results?.values) {
                        is List<*> -> ArrayList(values.filterIsInstance<String>())
                        else -> arrayListOf()
                    }
                notifyDataSetChanged()
            }
        }
    }
}
