package com.example.agora.ui.fragments.core.sellItem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.agora.databinding.FragmentSelectImageSourceBinding
import com.example.agora.util.ImageSource

class SelectImageSourceDialogFragment : DialogFragment() {

    var listener: OnImageSourceSelectedListener? = null

    private var _binding: FragmentSelectImageSourceBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectImageSourceBinding.inflate(layoutInflater,container,false)

        binding.imageFromStorageBtn.setOnClickListener {
            listener?.onImageSourceSelected(ImageSource.GALLERY)
            dismiss()
        }

        binding.imageFromCameraBtn.setOnClickListener {
            listener?.onImageSourceSelected(ImageSource.CAMERA)
            dismiss()
        }

        return binding.root
    }

    interface OnImageSourceSelectedListener {
        fun onImageSourceSelected(source: ImageSource)
    }

}