package com.example.kbbikamusbesarbahasaindonesia.ui.saved

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.ui.adapter.FavoriteAdapter
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentSavedBinding
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailActivity
import com.example.kbbikamusbesarbahasaindonesia.utils.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private val binding by viewBinding(FragmentSavedBinding::bind)
    private lateinit var adapter: FavoriteAdapter

    private val viewModel: SavedViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe()
    }

    private fun observe() {
        viewModel.savedKata.observe(viewLifecycleOwner) { kata ->
            adapter = FavoriteAdapter(kata) {
                val intent = Intent(requireActivity(), DetailActivity::class.java)
                intent.putExtra("word", it.kata)
                intent.putExtra("kata", it)
                startActivity(intent)
            }
            binding.rvFavoritKata.adapter = adapter
            binding.rvFavoritKata.layoutManager = GridLayoutManager(requireActivity(), 2)

            if (adapter.isEmpty()) {
                binding.emptyLayout.isVisible = true
                binding.readingPeople.isVisible = false
            } else {
                binding.emptyLayout.isVisible = false
                binding.readingPeople.isVisible = true
            }
        }
    }
}
