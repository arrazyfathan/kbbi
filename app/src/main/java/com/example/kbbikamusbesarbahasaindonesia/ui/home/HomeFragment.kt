package com.example.kbbikamusbesarbahasaindonesia.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.*
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
import com.example.kbbikamusbesarbahasaindonesia.utils.SwipeListener
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
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
        }
    }

    private fun setupView() {
        binding.homeButtonSearch.setOnClickListener {
            getMeaningOfWord(binding.editTextSearch.text.toString())
        }

        view?.setOnTouchListener(object : SwipeListener(requireActivity()) {
            override fun onSwipeTop() {
                showBottomDialog()
            }
        })

        adapter = HistoryAdapter { word ->
            getMeaningOfWord(word)
        }
        binding.rvHistory.adapter = adapter
    }

    private fun getMeaningOfWord(word: String) {
        viewModel.getMeaningOfWord(word)
            .observe(viewLifecycleOwner) { result ->
                if (result != null) {
                    when (result) {
                        is Resource.Loading -> binding.loadingState.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.loadingState.visibility = View.GONE
                            navigateToDetail(result)
                            saveWordToHistory(word)
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

    private fun saveWordToHistory(word: String) {
        viewModel.addToHistory(HistoryEntity(word.lowercase()))
    }

    private fun navigateToDetail(result: Resource<List<WordModel>>?) {
        val listWordModel = ListWordModel(
            word = binding.editTextSearch.text.toString(),
            listWords = result?.data!!,
        )
        val dataJson = Gson().toJson(listWordModel)
        val intent = Intent(requireActivity(), DetailActivity::class.java)
        intent.putExtra("data", dataJson)
        startActivity(intent)
    }

    private fun showBottomDialog() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.home_bottom_sheet_dialog)
        bottomSheetDialog.show()
    }
}
