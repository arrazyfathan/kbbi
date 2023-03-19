package com.example.kbbikamusbesarbahasaindonesia.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.core.data.Resource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentHomeBinding
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailActivity
import com.example.kbbikamusbesarbahasaindonesia.ui.home.adapter.HistoryAdapter
import com.example.kbbikamusbesarbahasaindonesia.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding4.widget.textChanges
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.hypot

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding(FragmentHomeBinding::bind)
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var adapter: HistoryAdapter

    private val viewModel: HomeViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observe()
    }

    private fun observe() {
        viewModel.getAllHistories().observe(viewLifecycleOwner) { histories ->
            adapter.differ.submitList(histories)
            if (adapter.differ.currentList.isEmpty()) binding.historyLabel.gone() else binding.historyLabel.visible()
        }
    }

    @SuppressLint("CheckResult")
    private fun setupView() = with(binding) {
        homeButtonSearch.setOnClickListener {
            getMeaningOfWord(editTextSearch.text.toString())
            hideKeyboard()
        }

        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (editTextSearch.text.isNotBlank()) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) getMeaningOfWord(editTextSearch.text.toString())
                hideKeyboard()
            }
            true
        }

        editTextSearch.textChanges()
            .skipInitialValue()
            .subscribe { text ->
                val filteredText = text.toString().replace(" ", "")
                if (editTextSearch.text.toString() != filteredText) {
                    editTextSearch.setText(filteredText)
                    editTextSearch.setSelection(filteredText.length)
                }
                enableSearchButton(text)
            }

        view?.setOnTouchListener(object : SwipeListener(requireActivity()) {
            override fun onSwipeTop() {
                showBottomDialog()
            }
        })

        adapter = HistoryAdapter { word ->
            getMeaningOfWord(word)
        }
        rvHistory.adapter = adapter
    }

    private fun enableSearchButton(text: CharSequence) {
        when {
            text.length == 2 && !text.contains(" ") -> revealButtonSearch()
            text.length < 2 -> hideButtonSearch()
        }
    }

    private fun getMeaningOfWord(word: String) {
        viewModel.getMeaningOfWord(word).observe(viewLifecycleOwner) { result ->
            if (result != null) {
                when (result) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> {
                        showLoading(false)
                        saveWordToHistory(word)
                        binding.editTextSearch.setText("")
                        navigateToDetail(result, word)
                    }
                    is Resource.Error -> {
                        showLoading(false)
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

    private fun revealButtonSearch() {
        val button = binding.homeButtonSearch
        val cx = button.width / 2
        val cy = button.height / 2
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim = ViewAnimationUtils.createCircularReveal(button, cx, cy, 0F, finalRadius).apply {
            duration = 300
            interpolator = LinearInterpolator()
        }
        anim.start()
        button.visible()
    }

    private fun hideButtonSearch() {
        val button = binding.homeButtonSearch
        val cx = button.width / 2
        val cy = button.height / 2
        val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
        val anim =
            ViewAnimationUtils.createCircularReveal(button, cx, cy, initialRadius, 0F).apply {
                duration = 300
                interpolator = LinearInterpolator()
            }
        button.invisible()
        anim.start()
    }

    fun showLoading(isLoading: Boolean) {
        if (isLoading) binding.loadingState.visible() else binding.loadingState.gone()
    }

    private fun saveWordToHistory(word: String) {
        viewModel.addToHistory(HistoryEntity(word.lowercase()))
    }

    private fun navigateToDetail(result: Resource<List<WordModel>>?, word: String) {
        val listWordModel = ListWordModel(
            word = word,
            listWords = result?.data!!,
        ).toJson()
        startActivity(
            Intent(requireActivity(), DetailActivity::class.java).putExtra(
                "data",
                listWordModel,
            ),
        )
    }

    private fun showBottomDialog() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.home_bottom_sheet_dialog)
        bottomSheetDialog.show()
    }
}
