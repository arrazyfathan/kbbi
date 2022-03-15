package com.example.kbbikamusbesarbahasaindonesia.ui.home

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.example.kbbikamusbesarbahasaindonesia.databinding.FragmentHomeBinding
import com.example.kbbikamusbesarbahasaindonesia.utils.OnSwipeListener
import kotlin.math.abs

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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

        view.setOnTouchListener(object : OnSwipeListener(requireActivity()){
            override fun onSwipeTop() {
                Toast.makeText(requireContext(), "UP", Toast.LENGTH_SHORT).show()
            }
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}

