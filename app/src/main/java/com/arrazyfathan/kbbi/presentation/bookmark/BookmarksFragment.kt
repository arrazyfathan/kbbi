package com.arrazyfathan.kbbi.presentation.bookmark

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.databinding.FragmentBookmarksBinding
import com.arrazyfathan.kbbi.presentation.adapter.BookmarkAdapter
import com.arrazyfathan.kbbi.presentation.customviews.CustomDialog
import com.arrazyfathan.kbbi.presentation.detail.DetailActivity
import com.arrazyfathan.kbbi.presentation.home.MainActivity
import com.arrazyfathan.kbbi.utils.applySystemBarPadding
import com.arrazyfathan.kbbi.utils.toJson
import com.arrazyfathan.kbbi.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.utils.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarksFragment : Fragment(R.layout.fragment_bookmarks) {
    private val binding by viewBinding(FragmentBookmarksBinding::bind)
    private val viewModel: BookmarksViewModel by viewModel()
    private lateinit var adapter: BookmarkAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applySystemBarPadding(applyTop = true)
        setupView()
        observe()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.updateSystemBarStyle(
            ContextCompat.getColor(requireContext(), R.color.blue_primary),
        )
    }

    private fun setupView() =
        with(binding) {
            adapter =
                BookmarkAdapter(
                    requireContext(),
                    object : BookmarkAdapter.BookmarkListener {
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
            rvBookmarks.adapter = adapter
        }

    private fun showDialogDelete(model: ListWordModel) {
        CustomDialog
            .Builder(requireContext())
            .setTitle(getString(R.string.delete_word_title))
            .setMessage(getString(R.string.delete_word_message))
            .isCancelable(false)
            .setOkTitle(getString(R.string.delete))
            .setCancelTitle(getString(R.string.cancel))
            .onResponse { type ->
                when (type) {
                    CustomDialog.ResponseType.YES -> removeWordFromBookmark(word = model.word)
                    CustomDialog.ResponseType.NO -> {}
                }
            }.build()
    }

    private fun removeWordFromBookmark(word: String) {
        viewModel.removeFromBookmark(word)
    }

    private fun observe() =
        with(binding) {
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
