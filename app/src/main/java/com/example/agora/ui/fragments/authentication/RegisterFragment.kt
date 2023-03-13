package com.example.agora.ui.fragments.authentication

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.example.agora.R
import com.example.agora.data.authentication.login.LoginViewModelFactory
import com.example.agora.data.core.model.User
import com.example.agora.databinding.FragmentRegisterBinding
import com.example.agora.domain.Messaging.MyFirebaseMessagingService
import com.example.agora.domain.auth.viewModel.AuthViewModel
import com.example.agora.util.EmailValidator
import com.example.agora.util.GlideApp
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


private const val TAG = "RegisterFragment"

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStoreDB: FirebaseFirestore
    private lateinit var pickMediaActivityResultLauncher: ActivityResultLauncher<String>
    private lateinit var glideApp : RequestManager

    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(requireActivity(), LoginViewModelFactory())
            .get(AuthViewModel::class.java)
    }

    private lateinit var usernameET: EditText
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var verifyPasswordET: EditText
    private lateinit var signUpBtn: Button
    private lateinit var avatarUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        glideApp = GlideApp.with(this)

        val drawableId = R.drawable.avatar
        avatarUri = Uri.parse("android.resource://" + requireActivity().packageName + "/" + drawableId)

        pickMediaActivityResultLauncher =  registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                avatarUri = uri

            val options: RequestOptions = RequestOptions()
                .circleCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)

            Glide.with(this).load(avatarUri).apply(options).into(binding.avatarIV)
        }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        fireStoreDB = Firebase.firestore

        binding.avatarIV.setOnClickListener {
            pickMediaActivityResultLauncher.launch("image/*")
        }

        usernameET = binding.editTextTextPersonName
        emailET = binding.editTextTextEmailAddress
        passwordET = binding.editTextTextPassword
        verifyPasswordET = binding.editTextTextPassword2
        signUpBtn = binding.signUpBtn

        setCheckMarkListener(listOf(usernameET, emailET, passwordET, verifyPasswordET))
        showTooltipOnClick(listOf(binding.usernameInfoBtn,binding.emailInfoBtn,binding.passwordInfoBtn,binding.repeatPasswordInfoBtn))

        signUpBtn.setOnClickListener {
            if (usernameET.compoundDrawables[2] != null &&
                emailET.compoundDrawables[2] != null &&
                passwordET.compoundDrawables[2] != null &&
                verifyPasswordET.compoundDrawables[2] != null
            ) {
                val user = User(
                    usernameET.text.toString(),
                    emailET.text.toString(),
                    passwordET.text.toString(),
                    registrationTokens = mutableListOf()
                )

                val imageView = binding.avatarIV
                val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                imageView.draw(canvas)

                authViewModel.signup(user, bitmap)
            } else {
                Snackbar.make(it,"Please fill all the fields",Snackbar.LENGTH_LONG).show()
            }
        }

        authViewModel.signUpIsResult.observe(requireActivity(), Observer {
            val result = it ?: return@Observer

            requireActivity().setResult(Activity.RESULT_OK)
            findNavController().navigate(R.id.mainActivity)

            FirebaseMessaging.getInstance().token.addOnCompleteListener { completedTask ->
                if (completedTask.isSuccessful) {
                    val registrationToken = completedTask.result
                    MyFirebaseMessagingService.addTokenToFirestore(registrationToken)
                } else {
                    val exception = completedTask.exception
                    Log.w(TAG, "Fetching FCM registration token failed", exception)
                }
            }
        })

        return view
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
                        usernameET -> if (text.isNotEmpty()) {
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
            }).also { textWatcher ->
                // Set the TextWatcher as a tag on the EditText view
                editTextView.tag = textWatcher
            }
        }

    }

    private fun showTooltipOnClick(infoBtns : List<ImageButton>){
        for(button in infoBtns){
            button.setOnClickListener {
                button.performLongClick()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.signUpIsResult.removeObservers(viewLifecycleOwner)
        listOf(usernameET, emailET, passwordET, verifyPasswordET).forEach { editText ->
            editText.removeTextChangedListener(editText.tag as? TextWatcher)
        }
        _binding = null
    }
}