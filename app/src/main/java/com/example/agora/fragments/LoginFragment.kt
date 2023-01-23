package com.example.agora.fragments

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

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
//        authStateListener = AuthStateListener {
//            val user = auth.currentUser
//            if (user != null) {
//                // User is signed in
//                Log.d("AuthStateListener", "onAuthStateChanged:signed_in:" + user.uid)
//                val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
//                findNavController().navigate(action)
//            } else {
//                // User is signed out
//                Log.d("AuthStateListener", "onAuthStateChanged:signed_out")
//            }
//        }
//        auth.addAuthStateListener(authStateListener)
    }

//    override fun onStop() {
//        super.onStop()
//        auth.removeAuthStateListener(authStateListener)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        binding.keepLoggedInChip.isChecked =
//            sharedPref.getBoolean(getString(R.string.keep_me_logged_in), false)

//        binding.keepLoggedInChip.setOnClickListener {
//            editor.putBoolean(
//                getString(R.string.keep_me_logged_in),
//                binding.keepLoggedInChip.isChecked
//            ).commit()

//            Log.d(
//                TAG,
//                "onViewCreated: shared pref is ${
//                    sharedPref.getBoolean(
//                        getString(R.string.keep_me_logged_in),
//                        false
//                    )
//                }"
//            )
//        }


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
//                            val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
//                            findNavController().navigate(action)
//                            val user = auth.currentUser
//                            user?.let { }
                            val navController = findNavController()
//                            navController.popBackStack()
                            val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
                            navController.navigate(action)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        binding.peshoBtn.setOnClickListener {
            auth.signInWithEmailAndPassword("petar.kinov@gmail.com", "qwerty")
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
//                            val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
//                            findNavController().navigate(action)
//                            val user = auth.currentUser
//                            user?.let { }
                        val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
                        findNavController().navigate(action)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        binding.ivanBtn.setOnClickListener {
            auth.signInWithEmailAndPassword("i.ivanov@gmail.com", "qwerty")
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
//                            val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
//                            findNavController().navigate(action)
//                            val user = auth.currentUser
//                            user?.let { }
                        val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
                        findNavController().navigate(action)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        binding.svetlioBtn.setOnClickListener {
            auth.signInWithEmailAndPassword("s.lambev@gmail.com", "qwerty")
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
//                            val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
//                            findNavController().navigate(action)
//                            val user = auth.currentUser
//                            user?.let { }
                        val action = LoginFragmentDirections.actionLoginFragmentToHomePage()
                        findNavController().navigate(action)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }


    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
