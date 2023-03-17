package com.example.agora.ui.fragments.core

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agora.data.core.model.HeaderItem
import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.databinding.FragmentBuyingBinding
import com.example.agora.domain.core.viewModel.ItemsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

private const val TAG = "BuyingFragment"

class BuyFragment : Fragment() {

    private var _binding: FragmentBuyingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ItemsViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore

    private var recyclerView: RecyclerView? = null
//    private val recyclerAdapter = ItemsRecyclerAdapter {
//
//        val action = HomePageDirections.actionHomePageToItemDetailsFragment(it.item)
//        findNavController().navigate(action)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore
        viewModel = ViewModelProvider(requireActivity())[ItemsViewModel::class.java]
        viewModel.getItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentBuyingBinding.inflate(inflater, container, false)
        val view = binding.root

        val adapter = GroupAdapter<GroupieViewHolder>().apply {
            spanCount = 2
            this.setOnItemClickListener { item, view ->
                // retrieve the document reference of the clicked item from the Item object
                Log.d(TAG, "bind: item ${(item as ItemsWithReference).item.title} clicked")
                val action =
                    HomePageDirections.actionHomePageToItemDetailsFragment((item as ItemsWithReference).item)
                findNavController().navigate(action)
            }
        }

        recyclerView = binding.recyclerView
        recyclerView?.apply {
            layoutManager = GridLayoutManager(requireContext(), adapter.spanCount).apply {
                spanSizeLookup = adapter.spanSizeLookup
            }
        }
//        recyclerView!!.layoutManager = GridLayoutManager(requireContext(), 1)
//        recyclerView!!.adapter = recyclerAdapter

        viewModel.items.observe(viewLifecycleOwner) {items ->
            if (items != null) {
                categorizeAndDisplayItems(items, adapter)
            }
//            recyclerAdapter.submitList(it)
        }

        recyclerView?.adapter = adapter
        return view
    }

    private fun categorizeAndDisplayItems(items : List<ItemsWithReference> ,adapter: GroupAdapter<GroupieViewHolder>) {
        val itemsByCategory = items.groupBy { it.item.category }

        itemsByCategory.forEach { (category,items) ->
            ExpandableGroup(HeaderItem(title = category),true).apply {
                add(Section(items))
                adapter.add(this)
            }
//            val section = Section(HeaderItem(title = category))
//            items.forEach { item ->
//                val sectionAdapter = GroupAdapter<GroupieViewHolder>().apply {
//                    add(item)
//                }
//                section.add(Group(adapter))
//            }
//            adapter.add(section)
        }

//        adapter.update(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView?.adapter = null
        recyclerView = null
        _binding = null
    }

}


