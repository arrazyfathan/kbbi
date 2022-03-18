package com.example.kbbikamusbesarbahasaindonesia.ui.detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.BaseApplication
import com.example.kbbikamusbesarbahasaindonesia.adapter.KataAdapter
import com.example.kbbikamusbesarbahasaindonesia.databinding.ActivityDetailBinding
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import com.google.android.material.snackbar.Snackbar

class DetailActivity : AppCompatActivity() {


    private lateinit var adapter: KataAdapter
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels {
        DetailViewModelFactory((application as BaseApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = intent

        val kata: Kata? = intent.getParcelableExtra("kata") as Kata?
        val word: String? = intent.getStringExtra("word")

        if (kata != null && word != null) {
            binding.kosaKata.text = word.replaceFirstChar { it.uppercase() }
            binding.rvArtiKata.layoutManager = LinearLayoutManager(this)
            adapter = KataAdapter(kata.data)
            binding.rvArtiKata.adapter = adapter

        }

        binding.btnBookContainer.setOnClickListener {
            val newKata = kata?.let { it1 ->
                Kata(
                    kata = word!!,
                    data = kata.data,
                    message = kata.message,
                    status = it1.status,
                    isSaved = true
                )
            }
            viewModel.insert(newKata)
            Snackbar.make(it, "$word berhasil disimpan", Snackbar.LENGTH_SHORT).show()
        }


    }


}