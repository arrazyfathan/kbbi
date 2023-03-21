package com.example.kbbikamusbesarbahasaindonesia.presentation.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.ActivityDetailBinding
import com.example.kbbikamusbesarbahasaindonesia.presentation.adapter.WordAdapter
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityDetailBinding::inflate)
    private val viewModel: DetailViewModel by viewModel()

    private lateinit var wordAdapter: WordAdapter
    private lateinit var listWordModel: ListWordModel

    private var stateBookmark = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        handleIntent()
        setupRecyclerView()
        checkIfWordSaved()
        setupView()
        observe()
    }

    private fun observe() {
        viewModel.resultBookmark.observe(this) {
            if (it == -1L) {
                setBookmarkState(false)
            } else {
                setBookmarkState(true)
                Toast.makeText(this, "Kata berhasil disimpan.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.resultDelete.observe(this) {
            if (it) {
                setBookmarkState(false)
                Toast.makeText(this@DetailActivity, "Kata berhasil dihapus.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                setBookmarkState(true)
            }
        }
    }

    private fun checkIfWordSaved() {
        viewModel.checkIsWordSaved(listWordModel.word).observe(this) { saved ->
            setBookmarkState(saved)
            stateBookmark = saved
        }
    }

    private fun setBookmarkState(isSaved: Boolean) {
        if (isSaved) {
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
    }

    private fun setupView() {
        with(binding) {
            kosaKata.text = listWordModel.word.replaceFirstChar { it.uppercase() }
            btnBookContainer.setOnClickListener {
                if (stateBookmark) deleteBookmark() else performBookmark()
            }
        }
    }

    private fun deleteBookmark() {
        viewModel.delete(listWordModel.word.lowercase())
    }

    private fun performBookmark() {
        viewModel.bookmark(listWordModel.word.lowercase(), listWordModel.listWords, true)
    }

    private fun handleIntent() {
        val intent = intent
        val dataFromIntent = intent.getStringExtra("data")
        listWordModel = Gson().fromJson(dataFromIntent, ListWordModel::class.java)
    }

    private fun setupRecyclerView() {
        wordAdapter = WordAdapter(this)
        binding.rvArtiKata.adapter = wordAdapter
        wordAdapter.submitList(listWordModel.listWords)
    }
}
