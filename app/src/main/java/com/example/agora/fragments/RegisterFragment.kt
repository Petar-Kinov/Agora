package com.example.agora.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.agora.R
import com.example.agora.util.EmailValidator
import com.example.agora.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "RegisterFragment"
class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private lateinit var firstNameET : EditText
    private lateinit var lastNameET : EditText
    private lateinit var emailET : EditText
    private lateinit var passwordET : EditText
    private lateinit var verifyPasswordET : EditText
    private lateinit var signUpBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firstNameET = binding.editTextTextPersonName
        lastNameET = binding.editTextTextPersonName2
        emailET = binding.editTextTextEmailAddress
        passwordET = binding.editTextTextPassword
        verifyPasswordET = binding.editTextTextPassword2
        signUpBtn = binding.signUpBtn

//        setCheckMarkIfNotEmpty(firstNameET)
//        setCheckMarkIfNotEmpty(lastNameET)
        setCheckMarkListener(firstNameET)
        setCheckMarkListener(lastNameET)
        setCheckMarkListener(emailET)
        setCheckMarkListener(passwordET)
        setCheckMarkListener(verifyPasswordET)

        signUpBtn.setOnClickListener {
            if (firstNameET.compoundDrawables[2] != null &&
            lastNameET.compoundDrawables[2] != null &&
            emailET.compoundDrawables[2] != null &&
            passwordET.compoundDrawables[2] != null &&
            verifyPasswordET.compoundDrawables[2] != null) {
                val email = emailET.text.toString()
                val password = passwordET.text.toString()
//                Toast.makeText(requireContext(),"Registered user ${firstNameET.text.toString()} ${lastNameET.text.toString()}",Toast.LENGTH_LONG).show()
                signUp(email,password)
            }
        }
    }

    private fun signUp(email : String, password : String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        Toast.makeText(requireContext(), "Registered user with email ${user.email}.",Toast.LENGTH_LONG).show()
                //                    updateUI(user)

                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                }
            }
    }

    private fun setCheckMarkListener(editTextView: TextView) {
        editTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }
            override fun afterTextChanged(text: Editable) {
//                if (text != null) {
                    when (editTextView) {
                        firstNameET,lastNameET -> if (text.isNotEmpty()) {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_baseline_check_24,
                                0
                            )
                        } else {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        }
                        emailET -> if (EmailValidator.isEmailValid(text.toString())) {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_baseline_check_24,
                                0
                            )
                        } else {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        }
                        passwordET ->  if (text.length >= 6) {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_baseline_check_24,
                                0
                            )
                        } else {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        }
                        verifyPasswordET ->  if (text.toString() == binding.editTextTextPassword.text.toString()) {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.ic_baseline_check_24,
                                0
                            )
                        } else {
                            editTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                        }
                    }
//                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}