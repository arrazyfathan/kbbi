package com.example.kbbikamusbesarbahasaindonesia.ui.detail


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.BaseApplication
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.adapter.KataAdapter
import com.example.kbbikamusbesarbahasaindonesia.databinding.ActivityDetailBinding
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
    private val bookmarkState = MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val intent = intent

        val kata: Kata? = intent.getParcelableExtra("kata") as Kata?
        val word: String? = intent.getStringExtra("word")

        setupRecyclerView(kata, word)

        if (kata != null && word != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val isSaved = viewModel.isSaved(kata.id)
                bookmarkState.postValue(isSaved)
            }

            bookmarkState.observe(this) {
                if (it) {
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
                    if (it) {
                        viewModel.delete(kata)
                        Snackbar.make(
                            view,
                            "${word.replaceFirstChar { it.uppercase() }} dihapus dari favorit.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        bookmarkState.postValue(false)
                    } else {
                        val newKata = Kata(
                            kata = word,
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
                        bookmarkState.postValue(true)
                    }
                }
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

