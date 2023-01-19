package com.example.agora.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.agora.R
import com.example.agora.Util.EmailValidator
import com.example.agora.databinding.FragmentRegisterBinding


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val firstNameEditText = binding.editTextTextPersonName
        val lastNameEditText = binding.editTextTextPersonName2
        val emailEditText = binding.editTextTextEmailAddress
        val passwordEditText = binding.editTextTextPassword
        val verifyPasswordEditText = binding.editTextTextPassword2

        setCheckMarkIfNotEmpty(firstNameEditText)
        setCheckMarkIfNotEmpty(lastNameEditText)
        setCheckMarkForEmail(emailEditText)
        setCheckMarkForPassword(passwordEditText)
        setCheckMarkIfPasswordsAreTheSame(verifyPasswordEditText)
    }

    private fun setCheckMarkIfNotEmpty(nameTextView: TextView) {
        nameTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }

            override fun afterTextChanged(text: Editable?) {
                if (text != null && text.isNotEmpty()) {
                    nameTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_check_24,
                        0
                    )
                } else {
                    nameTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
        })
    }

    private fun setCheckMarkForEmail(emailTextView: TextView) {
        emailTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }

            override fun afterTextChanged(text: Editable?) {
                if (text != null && EmailValidator.isEmailValid(text.toString())) {
                    emailTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_check_24,
                        0
                    )
                } else {
                    emailTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
        })
    }

    private fun setCheckMarkForPassword(passwordTextView: TextView) {
        passwordTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }

            override fun afterTextChanged(text: Editable?) {
                if (text != null && text.length >= 6) {
                    passwordTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_check_24,
                        0
                    )
                } else {
                    passwordTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
        })
    }

    private fun setCheckMarkIfPasswordsAreTheSame(verifyPasswordEditText: TextView) {
        verifyPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //ignore
            }

            override fun afterTextChanged(text: Editable?) {
                if (text != null && text.toString() == binding.editTextTextPassword.text.toString()) {
                    verifyPasswordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_check_24,
                        0
                    )
                } else {
                    verifyPasswordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}