package com.example.agora.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.agora.GlideApp
import com.example.agora.R
import com.example.agora.databinding.FragmentItemDetailsBinding
import com.example.agora.model.Item

class ItemDetailsFragment : Fragment() {

    private var _binding : FragmentItemDetailsBinding? = null
    private val binding get() = _binding!!

    private val args : ItemDetailsFragmentArgs by navArgs()
    private lateinit var item : Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = args.item
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentItemDetailsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlideApp.with(requireContext()).load(item.downloadUrl).into(binding.detailPictureIV)
        binding.titleTV.text = item.title
        binding.detailDescriptionTV.text = item.description
        binding.priceTextView.text = item.price
        binding.sellerTV.text = item.seller
    }
}