package com.example.agora.fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.agora.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginFragment"
private lateinit var auth: FirebaseAuth
private lateinit var sharedPref: SharedPreferences
private lateinit var editor: SharedPreferences.Editor

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
            findNavController().navigate(action)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        sharedPref = this.requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        editor = sharedPref.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: shared pref is ${sharedPref.getBoolean("a",false)}")

        binding.keepLoggedInChip.isChecked = sharedPref.getBoolean("a",false)

        binding.keepLoggedInChip.setOnClickListener {
            editor.putBoolean("a",binding.keepLoggedInChip.isChecked).commit()

            Log.d(TAG, "onViewCreated: shared pref is ${sharedPref.getBoolean("a",false)}")
        }

                    

        binding.registerBtn.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }

        binding.signInBtn.setOnClickListener {
            val email = binding.usernameET.text.toString()
            val password = binding.passwordET.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(), "Please enter Email and password",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
                            findNavController().navigate(action)
                            val user = auth.currentUser
                            user?.let {  }
//                        updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
//                        updateUI(null)
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}