package com.example.agora.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.agora.R
import com.example.agora.adapters.ViewPagerAdapter
import com.example.agora.databinding.FragmentHomePageBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "HomePage"

class HomePage : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore

        Log.d(TAG, "onCreate: activity is $activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        val view = binding.root

        val name = auth.currentUser?.displayName
        binding.welcomeMsg.text = getString(R.string.welcome_msg, name)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewPager
        viewPager.adapter = ViewPagerAdapter(childFragmentManager,viewLifecycleOwner.lifecycle)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (position == 0) {
                tab.text = getString(R.string.auctions)
            } else {
                tab.text = getString(R.string.my_items)
            }
        }.attach()

        binding.popupMenuBtn.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.pop_up_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.bids -> Toast.makeText(
                        requireContext(),
                        "You clicked ${item.title}",
                        Toast.LENGTH_LONG
                    ).show()
                    R.id.auctions -> Toast.makeText(
                        requireContext(),
                        "You clicked ${item.title}",
                        Toast.LENGTH_LONG
                    ).show()
                    R.id.settings -> {
//                        val action = HomePageDirections.actionHomePageToSettingsFragment()
//                        findNavController().navigate(action)
                    }
                    R.id.logout -> logOut()
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
        val action = HomePageDirections.actionHomePageToLoginActivity()
        findNavController().navigate(action)
        requireActivity().viewModelStore.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPager.adapter = null
        _binding = null
    }
}