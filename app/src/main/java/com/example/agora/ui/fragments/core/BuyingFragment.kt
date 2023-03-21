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
    private val categoryGroups = HashMap<String, ExpandableGroup>()
    private lateinit var myAdapter: GroupAdapter<GroupieViewHolder>

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

        myAdapter = GroupAdapter<GroupieViewHolder>().apply {
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
        recyclerView!!.apply {
            layoutManager = GridLayoutManager(requireContext(), myAdapter.spanCount).apply {
                spanSizeLookup = myAdapter.spanSizeLookup
            }
        }
        recyclerView!!.adapter = myAdapter
//        recyclerView!!.layoutManager = GridLayoutManager(requireContext(), 1)
//        recyclerView!!.adapter = recyclerAdapter

        viewModel.items.observe(viewLifecycleOwner) { items ->
            if (items != null) {
                categorizeAndDisplayItems(items, myAdapter)
                Log.d(
                    TAG,
                    "onCreateView:********************************************************** called categorizeAndDisplayItems"
                )
            }
//            recyclerAdapter.submitList(it)
        }


        return view
    }


    private fun categorizeAndDisplayItems(
        items: List<ItemsWithReference>,
        adapter: GroupAdapter<GroupieViewHolder>
    ) {

        adapter.clear()

        // Create a map of categories to items
        val itemsByCategory = items.groupBy { it.item.category }

        /* TODO had to clear the adapter and again add the groups to it because updating them was causing the recycler view to be empty after creating a new item  */
        myAdapter.clear()

        // Iterate over the categories
        for ((category, itemsInCategory) in itemsByCategory) {
            // Check if there is already an ExpandableGroup for this category
            val group = categoryGroups[category]

            //TODO update the sections if they already exist
//            if (group != null) {
//                // Update the existing group with the new items
//
//                val section = group.getGroup(1) as Section
//                Log.d(TAG, "categorizeAndDisplayItems: group item count is ${group.itemCount}")
//                for (i in 0 until group.itemCount) {
//                    Log.d(TAG, "item is ${group.getItem(i)}")
//                }
//
//                Log.d(TAG, "group is ${group.getGroup(1)}")
//
//                section.update(items)
//
//
//                Log.d(TAG, "Updated Category ${(group.getGroup(0).getItem(0) as HeaderItem).title}")
//                for (i in 0 until group.getGroup(1).itemCount)
//                    Log.d(TAG, "Updated item ${(group.getGroup(1).getItem(i) as ItemsWithReference).item.title}")
//                Log.d(TAG, "--------------------------------------------------------------------------")
//            } else {

            // Create a new ExpandableGroup for this category and add it to the adapter
            val header = HeaderItem(title = category)
            val section = Section(itemsInCategory)
            val expandableGroup = ExpandableGroup(header).apply {
                add(section)
                isExpanded = true
            }
            categoryGroups[category] = expandableGroup
            adapter.add(expandableGroup)

            Log.d(
                TAG,
                "Added Category ${(expandableGroup.getGroup(0).getItem(0) as HeaderItem).title}"
            )
            for (i in 0 until expandableGroup.getGroup(1).itemCount)
                Log.d(
                    TAG,
                    "added item ${
                        (expandableGroup.getGroup(1).getItem(i) as ItemsWithReference).item.title
                    }"
                )
            Log.d(TAG, "--------------------------------------------------------------------------")
//            }
        }

        Log.d(TAG, "Items in adapter = ${adapter.itemCount}")
        // Remove any ExpandableGroups that no longer have items
        val categoriesToRemove = ArrayList<String>()
        for ((category, group) in categoryGroups) {
            if (!itemsByCategory.containsKey(category)) {
                adapter.remove(group)
                categoriesToRemove.add(category)
                Log.d(TAG, "removed category $category")
            }
        }
        for (category in categoriesToRemove) {
            categoryGroups.remove(category)
            Log.d(TAG, "removed category $category")
        }
        Log.d(TAG, "Items in adapter = ${adapter.itemCount}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.items.removeObservers(viewLifecycleOwner)
        recyclerView?.adapter = null
        recyclerView = null
        _binding = null
    }
}


