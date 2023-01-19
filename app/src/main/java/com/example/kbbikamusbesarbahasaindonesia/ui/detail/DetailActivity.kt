package com.example.kbbikamusbesarbahasaindonesia.ui.detail

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.BaseApplication
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.adapter.KataAdapter
import com.example.kbbikamusbesarbahasaindonesia.databinding.ActivityDetailBinding
import com.example.kbbikamusbesarbahasaindonesia.model.History
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var adapter: KataAdapter
    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels {
        DetailViewModelFactory((application as BaseApplication).repository)
    }

    private var wordForHistory: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = intent

        val kata: Kata? = intent.getParcelableExtra("kata") as Kata?
        val word: String? = intent.getStringExtra("word")
        wordForHistory = word
        setupRecyclerView(kata, word)

        if (kata != null && word != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.isHistoryExist(word)
            }

            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.isSaved(kata.id.lowercase())
            }

            viewModel.saveState.observe(this) { saveState ->
                if (saveState) {
                    binding.bookmark.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@DetailActivity,
                            R.drawable.book_solid
                        )
                    )
                } else {
                    binding.bookmark.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@DetailActivity,
                            R.drawable.book
                        )
                    )
                }

                binding.btnBookContainer.setOnClickListener { view ->
                    if (saveState) {
                        viewModel.delete(kata)
                        Snackbar.make(
                            view,
                            "${word.replaceFirstChar { it.uppercase() }} dihapus dari favorit.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        val newKata = Kata(
                            kata = word.lowercase(),
                            data = kata.data,
                            message = kata.message,
                            status = kata.status,
                            isSaved = true
                        )
                        viewModel.insert(newKata)
                        Snackbar.make(
                            view,
                            "${word.replaceFirstChar { it.uppercase() }} berhasil disimpan.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewModel.isHistoryExist.observe(this) {
            if (it == false) {
                viewModel.insertHistory(History(kata = wordForHistory!!))
            }
        }
    }

    private fun setupRecyclerView(kata: Kata?, word: String?) {
        if (kata != null && word != null) {
            binding.kosaKata.text = word.replaceFirstChar { it.uppercase() }
            binding.rvArtiKata.layoutManager = LinearLayoutManager(this)
            adapter = KataAdapter(kata.data)
            binding.rvArtiKata.adapter = adapter
        }
    }
}
