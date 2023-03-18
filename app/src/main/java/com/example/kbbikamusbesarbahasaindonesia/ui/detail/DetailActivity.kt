package com.example.kbbikamusbesarbahasaindonesia.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.ActivityDetailBinding
import com.example.kbbikamusbesarbahasaindonesia.ui.adapter.WordAdapter
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityDetailBinding::inflate)
    private val viewModel: DetailViewModel by viewModel()
    private lateinit var wordAdapter: WordAdapter
    private var wordForHistory: String? = ""
    private lateinit var listWordModel: ListWordModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        handleIntent()
        setupRecyclerView()
        setupView()

        /*if (kata != null && word != null) {
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
                            R.drawable.book_solid,
                        ),
                    )
                } else {
                    binding.bookmark.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@DetailActivity,
                            R.drawable.book,
                        ),
                    )
                }

                binding.btnBookContainer.setOnClickListener { view ->
                    if (saveState) {
                        viewModel.delete(kata)
                        Snackbar.make(
                            view,
                            "${word.replaceFirstChar { it.uppercase() }} dihapus dari favorit.",
                            Snackbar.LENGTH_SHORT,
                        ).show()
                    } else {
                        val newKata = Kata(
                            kata = word.lowercase(),
                            data = kata.data,
                            message = kata.message,
                            status = kata.status,
                            isSaved = true,
                        )
                        viewModel.insert(newKata)
                        Snackbar.make(
                            view,
                            "${word.replaceFirstChar { it.uppercase() }} berhasil disimpan.",
                            Snackbar.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }

        viewModel.isHistoryExist.observe(this) {
            if (it == false) {
                viewModel.insertHistory(History(kata = wordForHistory!!))
            }
        }*/
    }

    private fun setupView() {
        with(binding) {
            kosaKata.text = listWordModel.word
        }
    }

    private fun handleIntent() {
        val intent = intent
        val dataFromIntent = intent.getStringExtra("data")
        listWordModel = Gson().fromJson(dataFromIntent, ListWordModel::class.java)
        val word: String? = intent.getStringExtra("word")
        wordForHistory = word
    }

    private fun setupRecyclerView() {
        wordAdapter = WordAdapter(this)
        binding.rvArtiKata.adapter = wordAdapter
        wordAdapter.submitList(listWordModel.listWords)
    }
}
