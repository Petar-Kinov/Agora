package com.example.agora.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.agora.R
import com.example.agora.databinding.FragmentHomePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private const val TAG = "HomePage"

class HomePage : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private lateinit var _binding: FragmentHomePageBinding
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor


    private lateinit var firstName: String
    private lateinit var lastName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseDB = Firebase.firestore
        sharedPref = this.requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        editor = sharedPref.edit()
    }

    override fun onPause() {
        super.onPause()
        if (!sharedPref.getBoolean("a",false)) {
            auth.signOut()
//            val action = HomePageDirections.actionHomePageToLoginFragment()
//            findNavController().navigate(action)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userRef = firebaseDB.collection("users")

        auth.addAuthStateListener { firebaseAuth ->
            // Get signedIn user
            val user = firebaseAuth.currentUser

            //if user is signed in, we call a helper method to save the user details to Firebase
            if (user != null) {
                // User is signed in
                // you could place other firebase code
                //logic to save the user details to Firebase
                userRef.document(user.uid).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document.exists()) {
                            firstName = document.get("firstName") as String
                            lastName = document.get("lastName") as String
                            binding.welcomeMsg.text =
                                getString(R.string.welcome_msg, "$firstName $lastName")
                        }
                    } else {
                        Log.d(TAG, "onViewCreated: Could not get document")
                    }
                }
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out")
                val action = HomePageDirections.actionHomePageToLoginFragment()
                findNavController().navigate(action)
            }
        }


//        auth.currentUser?.let {
//
//        }

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
                    R.id.settings -> Toast.makeText(
                        requireContext(),
                        "You clicked ${item.title}",
                        Toast.LENGTH_LONG
                    ).show()
                    R.id.logout -> logOut()
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }
}