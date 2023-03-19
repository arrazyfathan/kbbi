package com.example.kbbikamusbesarbahasaindonesia.ui.bookmark

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentSavedBinding
import com.example.kbbikamusbesarbahasaindonesia.ui.adapter.FavoriteAdapter
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailActivity
import com.example.kbbikamusbesarbahasaindonesia.utils.toJson
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private val binding by viewBinding(FragmentSavedBinding::bind)
    private val viewModel: SavedViewModel by viewModel()
    private lateinit var adapter: FavoriteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observe()
    }

    private fun setupView() = with(binding) {
        adapter = FavoriteAdapter { items ->
            startActivity(
                Intent(requireActivity(), DetailActivity::class.java).putExtra(
                    "data",
                    items.toJson(),
                ),
            )
        }
        rvFavoritKata.adapter = adapter
        rvFavoritKata.layoutManager = GridLayoutManager(requireActivity(), 2)
    }

    private fun observe() = with(binding) {
        viewModel.getBookmarks().observe(viewLifecycleOwner) {
            adapter.differ.submitList(it)
            if (adapter.isEmpty()) {
                emptyLayout.isVisible = true
                readingPeople.isVisible = false
            } else {
                emptyLayout.isVisible = false
                readingPeople.isVisible = true
            }
        }
    }
}