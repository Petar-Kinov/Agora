package com.example.agora.ui.fragments.authentication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.agora.R
import com.example.agora.data.authentication.login.LoggedInUserView
import com.example.agora.data.authentication.login.LoginViewModelFactory
import com.example.agora.databinding.FragmentLoginBinding
import com.example.agora.domain.Messaging.MyFirebaseMessagingService
import com.example.agora.domain.auth.viewModel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


private const val TAG = "LoginFragment"
private lateinit var auth: FirebaseAuth

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(requireActivity(), LoginViewModelFactory())[AuthViewModel::class.java]
    }

    private var username : EditText? = null
    private var password : EditText? = null
    private var loading : ProgressBar? = null

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

        Log.d(TAG, "onViewCreated: onViewCreated was called ")
        username = binding.usernameET
        password = binding.passwordET
        val login = binding.signInBtn
        val register = binding.registerBtn
        loading = binding.loading
//
        register.setOnClickListener {
            val action =
               LoginFragmentDirections.actionLoginFragmentToRegisterFragment2()
            findNavController().navigate(action)
        }
//
        login.setOnClickListener {
            if (username!!.text.isEmpty()){
                Snackbar.make(it,"Please enter E-mail",Snackbar.LENGTH_LONG).show()
            } else if (password!!.text.isEmpty()){
                Snackbar.make(it, "Please enter password", Snackbar.LENGTH_SHORT).show()
            } else {
                hideKeybaord(it)
                loading?.visibility = View.VISIBLE
                authViewModel.login(username = username!!.text.toString().trim(),password = password!!.text.toString())
            }
        }

        binding.user1Btn.setOnClickListener {
            authViewModel.login("petar.kinov@gmail.com", "qwerty")
        }

        binding.user2Btn.setOnClickListener {
            authViewModel.login("i.ivanov@gmail.com", "qwerty")
        }

        binding.user3Btn.setOnClickListener {
            authViewModel.login("s.lambev@gmail.com", "qwerty")
        }

        authViewModel.loginFormState.observe(requireActivity(), Observer {
            val loginState = it ?: return@Observer

            //TODO not sure yet if i want to disable the button
//         disable login button unless both username / password is valid
//            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username!!.error = getString(loginState.usernameError)
                Snackbar.make(username!!,"${username!!.error}", Snackbar.LENGTH_SHORT).show()
            }
            if (loginState.passwordError != null) {
                password!!.error = getString(loginState.passwordError)
                Snackbar.make(password!!, "${password!!.error}", Snackbar.LENGTH_SHORT).show()
            }
        })

        authViewModel.loginResult.observe(requireActivity(), Observer {
            val loginResult = it ?: return@Observer

            loading?.visibility = View.GONE
            if (loginResult.error != null) {
                Log.d(TAG, "onViewCreated: error is ${loginResult.error}")
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                requireActivity().setResult(Activity.RESULT_OK)
            }

//        Complete and destroy login activity once successful
        })

//        username.afterTextChanged {
//            authViewModel.loginDataChanged(
//                username.text.toString(),
//                password.text.toString()
//            )
//        }
////
//        password.apply {
//            setOnEditorActionListener { _, actionId, _ ->
//                when (actionId) {
//                    EditorInfo.IME_ACTION_DONE -> {
//                        loading?.visibility = View.VISIBLE
//                        authViewModel.login(
//                            username.text.toString(),
//                            password.text.toString()
//                        )
//                    }
//                }
//                false
//            }
//        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
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
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        // TODO toast may cause a memory leak
//        Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
        Snackbar.make(binding.root,errorString, Snackbar.LENGTH_LONG).show()
        Log.d(TAG, "showLoginFailed: error string is $errorString")
    }

    /**
     * Extension function to simplify setting an afterTextChanged action to EditText components.
     */
//    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
//        this.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(editable: Editable?) {
//                afterTextChanged.invoke(editable.toString())
//            }
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
//        })
//    }

    private fun hideKeybaord(v: View) {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager!!.hideSoftInputFromWindow(v.applicationWindowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.loginFormState.removeObservers(viewLifecycleOwner)
        authViewModel.loginResult.removeObservers(viewLifecycleOwner)

        // TODO the editTexts cause a leak for some reason so ended up making them nullable
        username = null
        password = null
        loading = null

        _binding = null
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: onDestroy called")
        super.onDestroy()
    }


}
