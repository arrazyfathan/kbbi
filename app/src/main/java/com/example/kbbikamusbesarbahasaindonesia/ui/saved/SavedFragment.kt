package com.example.kbbikamusbesarbahasaindonesia.ui.saved

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kbbikamusbesarbahasaindonesia.BaseApplication
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.adapter.FavoriteAdapter
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentSavedBinding
import com.example.kbbikamusbesarbahasaindonesia.repository.KataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: FavoriteAdapter

    private val viewModel: SavedViewModel by viewModels {
        SavedViewModelFactory((activity?.applicationContext as BaseApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.savedKata.observe(viewLifecycleOwner) { kata ->
            adapter = FavoriteAdapter(kata)
            binding.rvFavoritKata.adapter = adapter
            binding.rvFavoritKata.layoutManager = GridLayoutManager(requireContext(), 2)
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}
