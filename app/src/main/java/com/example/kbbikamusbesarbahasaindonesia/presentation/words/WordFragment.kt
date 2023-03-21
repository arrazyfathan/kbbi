package com.example.kbbikamusbesarbahasaindonesia.presentation.words

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.core.data.Resource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.KosaKata
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentWordBinding
import com.example.kbbikamusbesarbahasaindonesia.presentation.adapter.KosaKataAdapter
import com.example.kbbikamusbesarbahasaindonesia.presentation.detail.DetailActivity
import com.example.kbbikamusbesarbahasaindonesia.utils.toJson
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.jakewharton.rxbinding4.widget.textChanges
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class WordFragment : Fragment(R.layout.fragment_word) {

    private val binding by viewBinding(FragmentWordBinding::bind)
    private val viewModel: WordViewModel by viewModel()
    private lateinit var adapter: KosaKataAdapter

    @SuppressLint("CheckResult", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jsonFileString = getJsonDataFromAsset(requireActivity(), "entries.json")
        val gson = GsonBuilder().create()
        val listKosaKata = object : TypeToken<KosaKata>() {}.type
        val list: KosaKata = gson.fromJson(jsonFileString, listKosaKata)

        binding.rvListKosaKata.layoutManager = LinearLayoutManager(requireContext())
        adapter = KosaKataAdapter(list) {
            getMeaningOfWord(it)
        }
        binding.rvListKosaKata.adapter = adapter

        binding.editTextSearchWord.textChanges()
            .skipInitialValue()
            .subscribe { text ->
                if (text.toString().isEmpty()) {
                    binding.rvListKosaKata.adapter = KosaKataAdapter(list) {
                        getMeaningOfWord(it)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.filter.filter(text)
                    binding.rvListKosaKata.adapter = KosaKataAdapter(adapter.kataFilterList) {
                        getMeaningOfWord(it)
                    }
                }
            }
    }

    private fun getMeaningOfWord(word: String) {
        viewModel.getMeaningOfWord(word).observe(viewLifecycleOwner) { result ->
            if (result != null) {
                when (result) {
                    is Resource.Loading -> showLoadingState(true)
                    is Resource.Success -> {
                        showLoadingState(false)
                        navigateToDetail(result, word)
                    }
                    is Resource.Error -> {
                        binding.loadingState.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "${result.message}",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }

    private fun navigateToDetail(result: Resource.Success<List<WordModel>>, word: String) {
        val listWordModel = ListWordModel(
            word = word,
            listWords = result.data!!,
        ).toJson()
        startActivity(
            Intent(requireActivity(), DetailActivity::class.java).putExtra(
                "data",
                listWordModel,
            ),
        )
    }

    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun showLoadingState(state: Boolean) {
        if (state) {
            binding.loadingState.visibility =
                View.VISIBLE
        } else {
            binding.loadingState.visibility = View.GONE
        }
    }
}
