package com.example.kbbikamusbesarbahasaindonesia.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.*
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
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
            text.length > 2 -> revealButtonSearch()
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
        val transition = Slide(Gravity.END)
        transition.apply {
            duration = 600
            addTarget(binding.homeButtonSearch)
            interpolator = OvershootInterpolator()
        }
        TransitionManager.beginDelayedTransition(binding.root, transition)
        binding.homeButtonSearch.visible()
    }

    private fun hideButtonSearch() {
        val transition = Slide(Gravity.END)
        transition.apply {
            duration = 600
            addTarget(binding.homeButtonSearch)
            interpolator = AnticipateOvershootInterpolator()
        }
        TransitionManager.beginDelayedTransition(binding.homeContainer, transition)
        binding.homeButtonSearch.invisible()
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
