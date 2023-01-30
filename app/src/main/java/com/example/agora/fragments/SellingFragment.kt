package com.example.agora.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.adapters.SellItemsRecyclerAdapter
import com.example.agora.databinding.FragmentSellingBinding
import com.example.agora.model.Item
import com.example.agora.viewModel.ItemsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SellFragment : Fragment() {

    companion object {
        private const val TAG = "SellingFragment"
    }

    private var _binding: FragmentSellingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var viewModel: ItemsViewModel

    private lateinit var recyclerView: RecyclerView
    private val recyclerAdapter = SellItemsRecyclerAdapter {
        Toast.makeText(requireContext(), "${it.title} item clicked", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore

        viewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]

        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            // Get signedIn user
            val user = firebaseAuth.currentUser

            //if user is signed in, we call a helper method to save the user details to Firebase
            if (user == null) {
                // Clear recycler adapter list on sign out
                recyclerAdapter.submitList(null)
            }
        }

        auth.addAuthStateListener(listener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellingBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = recyclerAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.items.observe(viewLifecycleOwner) {
            val myItemsList = mutableListOf<Item>()
            for (item in it) {
                if (item.seller == auth.currentUser!!.displayName) {
                    myItemsList.add(item)
                }
            }
            recyclerAdapter.submitList(myItemsList)
        }

        binding.sellBtn.setOnClickListener {
            val action = HomePageDirections.actionHomePageToCreateAuctionFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}