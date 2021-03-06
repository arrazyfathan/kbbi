package com.example.kbbikamusbesarbahasaindonesia.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.kbbikamusbesarbahasaindonesia.R
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentHomeBinding
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import com.example.kbbikamusbesarbahasaindonesia.services.ServiceBuilder
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailActivity
import com.example.kbbikamusbesarbahasaindonesia.utils.SwipeListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var word = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editTextSearch.addTextChangedListener { editable ->
            editable?.let {
                word = editable.toString()
            }
        }

        binding.homeButtonSearch.setOnClickListener {
            getResponse()
        }

        view.setOnTouchListener(object : SwipeListener(requireActivity()) {
            override fun onSwipeTop() {
                showBottomDialog()
            }
        })



    }

    private fun getResponse() {
        binding.editTextSearch.onEditorAction(EditorInfo.IME_ACTION_DONE)
        if (word.isNotEmpty() && word.isNotBlank()){
            val requestWord = ServiceBuilder.retrofit.getArtiKata(word)
            showLoadingState(true)
            requestWord.enqueue(object : Callback<Kata>{
                override fun onResponse(call: Call<Kata>, response: Response<Kata>) {
                    if (response.isSuccessful) {
                        val responseKata = response.body()!!
                        val kata = Kata(
                            kata = word,
                            data = responseKata.data,
                            message = responseKata.message,
                            status = responseKata.status,
                            isSaved = false
                        )
                        val intent = Intent(requireActivity(), DetailActivity::class.java)
                        intent.putExtra("word", word)
                        intent.putExtra("kata", kata)
                        startActivity(intent)
                        showLoadingState(false)

                    } else {
                        showLoadingState(false)
                        Toast.makeText(requireContext(), response.message(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Kata>, t: Throwable) {
                    showLoadingState(false)
                    Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                }

            })
        }else{
            showLoadingState(false)
            Toast.makeText(requireContext(), "Kata tidak boleh kossong", Toast.LENGTH_SHORT).show()
        }
    }

    fun showLoadingState(state: Boolean) {
        if (state) binding.loadingState.visibility =
            View.VISIBLE else binding.loadingState.visibility = View.GONE
    }

    private fun showBottomDialog() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.home_bottom_sheet_dialog)
        bottomSheetDialog.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}

