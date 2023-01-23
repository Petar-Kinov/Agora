package com.example.agora.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.agora.databinding.FragmentCreateAuctionBinding
import com.example.agora.viewModel.ItemsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.agora.model.Item


class CreateAuctionFragment : DialogFragment() {

    private var _binding: FragmentCreateAuctionBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private lateinit var viewModel: ItemsViewModel

    private lateinit var sellBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore
        viewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]
    }

    companion object {
        private const val TAG = "CreateAuctionFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAuctionBinding.inflate(inflater, container, false)
        val view = binding.root
        sellBtn = binding.createBtn
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sellBtn.setOnClickListener {
            val title = binding.titleET.text.toString()
            val description = binding.descriptionET.text.toString()
            val price = binding.priceET.text.toString()
            
            if (title.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty()){
                val item = Item(seller = auth.currentUser?.displayName!!, title = title, description = description, price = price )
                viewModel.sellItem(item)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show()
            }
            
        }
    }
}