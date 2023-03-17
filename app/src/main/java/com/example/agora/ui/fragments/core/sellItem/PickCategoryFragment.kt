package com.example.agora.ui.fragments.core.sellItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.agora.databinding.FragmentPickCategoryBinding
import com.google.android.material.snackbar.Snackbar

class PickCategoryFragment : Fragment() {

    private var _binding : FragmentPickCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPickCategoryBinding.inflate(layoutInflater, container, false)

        val categoryRadioGroup = binding.categoryRedioGrop

        binding.continueBtn.setOnClickListener {
            val checkedCategory = categoryRadioGroup.checkedRadioButtonId
            if (checkedCategory == -1) {
                // No radio button is selected
                Snackbar.make(it,"Please select a category" , Snackbar.LENGTH_LONG).show()
            } else {
                val selectedRadioButton = categoryRadioGroup.findViewById<RadioButton>(checkedCategory)
                val selectedValue = selectedRadioButton.text.toString()
                val action =
                    PickCategoryFragmentDirections.actionPickCategoryFragmentToCreateAuctionFragment(selectedValue)
                findNavController().navigate(action)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}