package com.example.agora.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout.DispatchChangeEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.adapters.MyItemsListAdapter
import com.example.agora.databinding.FragmentSellingBinding
import com.example.agora.model.ItemsWithReference
import com.example.agora.viewModel.ItemsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "SellingFragment"
class SellFragment : Fragment() {
    private var _binding: FragmentSellingBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var viewModel: ItemsViewModel

    private var recyclerView: RecyclerView? = null
    private val recyclerAdapter = MyItemsListAdapter {
        viewModel.deleteItem(it)
        Toast.makeText(requireContext(), "${it.item.title} item clicked", Toast.LENGTH_SHORT).show()
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
        recyclerView!!.layoutManager = LinearLayoutManager(requireContext())
        recyclerView!!.adapter = recyclerAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.items.observe(viewLifecycleOwner) {
            val myItemsList = mutableListOf<ItemsWithReference>()
            for (item in it) {
                if (item.item.seller == auth.currentUser!!.displayName) {
                    myItemsList.add(item)
                }
            }
            Log.d(TAG, "onViewCreated: list is ${myItemsList.toString()}")
            recyclerAdapter.submitList(myItemsList)
        }

        binding.sellBtn.setOnClickListener {
            val action = HomePageDirections.actionHomePageToCreateAuctionFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        _binding = null
    }
}