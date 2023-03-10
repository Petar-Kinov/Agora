package com.example.agora.ui.fragments.core

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.Services.DeleteItemService
import com.example.agora.Services.UploadService
import com.example.agora.data.core.model.Item
import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.databinding.FragmentSellingBinding
import com.example.agora.domain.core.viewModel.ItemsViewModel
import com.example.agora.ui.adapters.MyItemsListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime

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
//        viewModel.deleteItem(it)
        deleteItem(it)
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
            val action =
                HomePageDirections.actionHomePageToCreateAuctionFragment()
            findNavController().navigate(action)
        }

        //currently disabled , just for quick adding of items
        binding.testBtn.setOnClickListener {

            val storageReference = auth.currentUser!!.uid + LocalDateTime.now()
            val item = Item(
                seller = auth.currentUser!!.displayName.toString(),
                title = "some title",
                description = "some description",
                price = (0..100).random().toString(),
                storageRef = storageReference,
                imagesCount = 1
            )
            val resourceId =
                resources.getIdentifier("something", "drawable", requireContext().packageName)
            val uri = Uri.parse("android.resource://${requireContext().packageName}/$resourceId")
            val uriList = arrayListOf(uri)

            requireActivity().startService(
                Intent(requireContext(), UploadService::class.java)
                    .putParcelableArrayListExtra(UploadService.URI_LIST, ArrayList(uriList))
                    .putExtra(UploadService.ITEM, item)
                    .putExtra(UploadService.DOCUMENT_REF, storageReference)
                    .setAction(UploadService.ACTION_UPLOAD)
            )

        }
    }

    private fun deleteItem(itemsWithReference: ItemsWithReference) {
        requireActivity().startService(
            Intent(requireContext(), DeleteItemService::class.java)
                .putExtra(DeleteItemService.ITEM, itemsWithReference.item)
                .putExtra(DeleteItemService.REFERENCE, itemsWithReference.documentReference.path)
                .setAction(DeleteItemService.DELETE_ACTION)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        _binding = null
    }
}