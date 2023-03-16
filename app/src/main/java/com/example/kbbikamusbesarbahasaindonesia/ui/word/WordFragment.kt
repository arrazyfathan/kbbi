package com.example.kbbikamusbesarbahasaindonesia.ui.word

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
import com.example.kbbikamusbesarbahasaindonesia.adapter.KosaKataAdapter
import com.example.kbbikamusbesarbahasaindonesia.data.KosaKata
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentWordBinding
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import com.example.kbbikamusbesarbahasaindonesia.services.ServiceBuilder
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailActivity
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class WordFragment : Fragment(R.layout.fragment_word) {

    private val binding by viewBinding(FragmentWordBinding::bind)

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
            getResponse(it)
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
                        getResponse(it)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.filter.filter(char)
                    binding.rvListKosaKata.adapter = KosaKataAdapter(adapter.kataFilterList) {
                        getResponse(it)
                    }
                }
            }
        })

        /* adapter.setOnItemClickListener {
             getResponse(it)
         }*/
    }

    private fun getResponse(word: String) {
        val request = ServiceBuilder.retrofit.getArtiKata(word)
        showLoadingState(true)
        request.enqueue(object : Callback<Kata> {
            override fun onResponse(call: Call<Kata>, response: Response<Kata>) {
                if (response.isSuccessful) {
                    val responseKata = response.body()!!
                    val kata = Kata(
                        kata = word,
                        data = responseKata.data,
                        message = responseKata.message,
                        status = responseKata.status,
                        isSaved = false,
                    )
                    val intent = Intent(requireActivity(), DetailActivity::class.java)
                    intent.putExtra("word", word)
                    intent.putExtra("kata", kata)
                    startActivity(intent)
                    showLoadingState(false)
                } else {
                    showLoadingState(false)
                    Toast.makeText(requireContext(), response.message(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Kata>, t: Throwable) {
                showLoadingState(false)
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
            }
        })
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
