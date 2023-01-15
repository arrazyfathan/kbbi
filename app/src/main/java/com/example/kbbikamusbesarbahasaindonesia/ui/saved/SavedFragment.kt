package com.example.kbbikamusbesarbahasaindonesia.ui.saved

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.BaseApplication
import com.example.kbbikamusbesarbahasaindonesia.adapter.FavoriteAdapter
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentSavedBinding
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailActivity

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FavoriteAdapter

    private val viewModel: SavedViewModel by viewModels {
        SavedViewModelFactory((activity?.applicationContext as BaseApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
