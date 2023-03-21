package com.example.kbbikamusbesarbahasaindonesia.presentation.bookmark

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentSavedBinding
import com.example.kbbikamusbesarbahasaindonesia.presentation.adapter.FavoriteAdapter
import com.example.kbbikamusbesarbahasaindonesia.presentation.customviews.CustomDialog
import com.example.kbbikamusbesarbahasaindonesia.presentation.detail.DetailActivity
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
        adapter = FavoriteAdapter(
            requireContext(),
            object : FavoriteAdapter.FavoriteListener {
                override fun onClickListener(model: ListWordModel) {
                    startActivity(
                        Intent(requireActivity(), DetailActivity::class.java).putExtra(
                            "data",
                            model.toJson(),
                        ),
                    )
                }

                override fun onDeleteListener(model: ListWordModel) {
                    showDialogDelete(model)
                }
            },
        )
        rvFavoritKata.adapter = adapter
    }

    private fun showDialogDelete(model: ListWordModel) {
        CustomDialog.Builder(requireContext())
            .setTitle("Hapus kata?")
            .setMessage("Anda yakin ingin menghapus kata?")
            .isCancelable(false)
            .setOkTitle("Hapus")
            .setCancelTitle("Batal")
            .onResponse { type ->
                when (type) {
                    CustomDialog.ResponseType.YES -> removeWordFromBookmark(word = model.word)
                    CustomDialog.ResponseType.NO -> {}
                }
            }
            .build()
    }

    private fun removeWordFromBookmark(word: String) {
        viewModel.removeFromBookmark(word)
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
