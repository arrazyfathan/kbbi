package com.arrazyfathan.kbbi.presentation.detail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.databinding.ActivityDetailBinding
import com.arrazyfathan.kbbi.presentation.adapter.WordAdapter
import com.arrazyfathan.kbbi.utils.applySystemBarMargin
import com.arrazyfathan.kbbi.utils.applySystemBarPadding
import com.arrazyfathan.kbbi.utils.enableEdgeToEdgeSystemBars
import com.arrazyfathan.kbbi.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.utils.viewBinding
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
        enableEdgeToEdgeSystemBars()
        setContentView(binding.root)
        binding.appBar.applySystemBarPadding(applyTop = true)
        binding.rvWordEntries.applySystemBarPadding(applyBottom = true)
        binding.bookmarkActionContainer.applySystemBarMargin(applyBottom = true)
        updateSystemBarStyle(ContextCompat.getColor(this, R.color.blue_bg))
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
                Toast.makeText(this, R.string.word_saved_success, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.resultDelete.observe(this) {
            if (it) {
                setBookmarkState(false)
                Toast
                    .makeText(this@DetailActivity, R.string.word_deleted_success, Toast.LENGTH_SHORT)
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

    private fun setBookmarkState(isSaved: Boolean) =
        with(binding) {
            if (isSaved) {
                bookmarkIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@DetailActivity,
                        R.drawable.book_solid,
                    ),
                )
                bookmarkIcon.imageTintList =
                    ContextCompat.getColorStateList(this@DetailActivity, R.color.white)
                bookmarkText.text = getString(R.string.bookmarked)
                bookmarkText.setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.white))
                bookmarkActionContainer.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this@DetailActivity,
                        R.color.text_h1,
                    ),
                )
            } else {
                bookmarkIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@DetailActivity,
                        R.drawable.book,
                    ),
                )
                bookmarkText.text = getString(R.string.bookmark)
                bookmarkIcon.imageTintList =
                    ContextCompat.getColorStateList(this@DetailActivity, R.color.text_h1)
                bookmarkText.setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.text_h1))
                bookmarkActionContainer.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this@DetailActivity,
                        R.color.white,
                    ),
                )
            }
        }

    private fun setupView() {
        with(binding) {
            expandedTitle.text = listWordModel.word.replaceFirstChar { it.uppercase() }
            collapsedTitle.text = listWordModel.word.replaceFirstChar { it.uppercase() }
            bookmarkActionContainer.setOnClickListener {
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
        binding.rvWordEntries.adapter = wordAdapter
        wordAdapter.submitList(listWordModel.listWords)
    }
}
