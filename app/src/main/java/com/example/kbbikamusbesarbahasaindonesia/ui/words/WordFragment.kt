package com.example.kbbikamusbesarbahasaindonesia.ui.words

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.core.data.Resource
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.KosaKata
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentWordBinding
import com.example.kbbikamusbesarbahasaindonesia.ui.adapter.KosaKataAdapter
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailActivity
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException

class WordFragment : Fragment(R.layout.fragment_word) {

    private val binding by viewBinding(FragmentWordBinding::bind)
    private val viewModel: WordViewModel by viewModel()
    private lateinit var adapter: KosaKataAdapter

    private var filtered = KosaKata()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

        binding.editTextSearchWord.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(char: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(char: Editable?) {
                if (char.toString().isEmpty()) {
                    binding.rvListKosaKata.adapter = KosaKataAdapter(list) {
                        getMeaningOfWord(it)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.filter.filter(char)
                    binding.rvListKosaKata.adapter = KosaKataAdapter(adapter.kataFilterList) {
                        getMeaningOfWord(it)
                    }
                }
            }
        })
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
        )
        val dataJson = Gson().toJson(listWordModel)
        val intent = Intent(requireActivity(), DetailActivity::class.java)
        intent.putExtra("data", dataJson)
        startActivity(intent)
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

    fun showLoadingState(state: Boolean) {
        if (state) {
            binding.loadingState.visibility =
                View.VISIBLE
        } else {
            binding.loadingState.visibility = View.GONE
        }
    }
}
