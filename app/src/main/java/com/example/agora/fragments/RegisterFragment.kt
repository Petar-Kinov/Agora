package com.example.agora.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.agora.R
import com.example.agora.authentication.repository.AuthRepositoryImpl
import com.example.agora.authentication.viewModel.AuthViewModel
import com.example.agora.util.EmailValidator
import com.example.agora.databinding.FragmentRegisterBinding
import com.example.agora.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "RegisterFragment"

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStoreDB: FirebaseFirestore

    private val viewModel: AuthViewModel by lazy {
        AuthViewModel(AuthRepositoryImpl(FirebaseAuth.getInstance()))
    }

    private lateinit var firstNameET: EditText
    private lateinit var lastNameET: EditText
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var verifyPasswordET: EditText
    private lateinit var signUpBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fireStoreDB = Firebase.firestore

        firstNameET = binding.editTextTextPersonName
        lastNameET = binding.editTextTextPersonName2
        emailET = binding.editTextTextEmailAddress
        passwordET = binding.editTextTextPassword
        verifyPasswordET = binding.editTextTextPassword2
        signUpBtn = binding.signUpBtn

        //set listeners to EditText fields to check if the input is valid
        setCheckMarkListener(listOf(firstNameET, lastNameET, emailET, passwordET, verifyPasswordET))

        signUpBtn.setOnClickListener {
            if (firstNameET.compoundDrawables[2] != null &&
                lastNameET.compoundDrawables[2] != null &&
                emailET.compoundDrawables[2] != null &&
                passwordET.compoundDrawables[2] != null &&
                verifyPasswordET.compoundDrawables[2] != null
            ) {
                val user = User(
                    firstNameET.text.toString(),
                    lastNameET.text.toString(),
                    emailET.text.toString(),
                    passwordET.text.toString()
                )
                viewModel.signup(user)
            } else {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setCheckMarkListener(editTextViewList: List<TextView>) {
        for (editTextView in editTextViewList) {
            editTextView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //ignore
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //ignore
                }

                override fun afterTextChanged(text: Editable) {
                    when (editTextView) {
                        firstNameET, lastNameET -> if (text.isNotEmpty()) {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0, 0, R.drawable.ic_baseline_check_24, 0
                            )
                        } else {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        }
                        emailET -> if (EmailValidator.isEmailValid(text.toString())) {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0, 0, R.drawable.ic_baseline_check_24, 0
                            )
                        } else {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        }
                        passwordET -> if (text.length >= 6) {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0, 0, R.drawable.ic_baseline_check_24, 0
                            )
                        } else {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        }
                        verifyPasswordET -> if (text.toString() == binding.editTextTextPassword.text.toString()) {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0, 0, R.drawable.ic_baseline_check_24, 0
                            )
                        } else {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        }
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}