package com.example.agora.fragments.authFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.agora.authentication.repository.AuthRepositoryImpl
import com.example.agora.authentication.viewModel.AuthViewModel
import com.example.agora.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginFragment"
private lateinit var auth: FirebaseAuth

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by lazy {
        AuthViewModel(AuthRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

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
                viewModel.logIn(email,password)
            }
        }

        binding.peshoBtn.setOnClickListener {
            viewModel.logIn("petar.kinov@gmail.com", "qwerty")
        }

        binding.ivanBtn.setOnClickListener {
            viewModel.logIn("i.ivanov@gmail.com", "qwerty")
        }

        binding.svetlioBtn.setOnClickListener {
            viewModel.logIn("s.lambev@gmail.com", "qwerty")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
