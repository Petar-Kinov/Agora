package com.example.agora.authentication.authFragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.agora.R
import com.example.agora.authentication.login.AuthViewModel
import com.example.agora.authentication.login.LoggedInUserView
import com.example.agora.authentication.login.LoginViewModelFactory
import com.example.agora.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth


private const val TAG = "LoginFragment"
private lateinit var auth: FirebaseAuth

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by lazy {
        ViewModelProvider(requireActivity(), LoginViewModelFactory())
            .get(AuthViewModel::class.java)
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

    override fun onDestroyView() {
        super.onDestroyView()
        authViewModel.loginFormState.removeObservers(viewLifecycleOwner)
        authViewModel.loginResult.removeObservers(viewLifecycleOwner)
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: onViewCreated was called ")
        val username = binding.usernameET
        val password = binding.passwordET
        val login = binding.signInBtn
        val register = binding.registerBtn
        val loading = binding.loading
//
        register.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment2()
            findNavController().navigate(action)
        }
//
        login.setOnClickListener {
            if (username.text.isEmpty()){
                Toast.makeText(requireContext(), "Please enter E-mail", Toast.LENGTH_SHORT).show()
            } else if (password.text.isEmpty()){
                Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show()
            } else {
                hideKeybaord(it)
                loading.visibility = View.VISIBLE
                authViewModel.login(username = username.text.toString(),password = password.text.toString())
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
                username.error = getString(loginState.usernameError)
                Toast.makeText(requireContext(),"${username.error}", Toast.LENGTH_SHORT).show()
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
                Toast.makeText(requireContext(), "${password.error}", Toast.LENGTH_SHORT).show()
            }
        })

        authViewModel.loginResult.observe(requireActivity(), Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
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
//
        password.apply {
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        loading.visibility = View.VISIBLE
                        authViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                    }
                }
                false
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        // TODO : initiate successful logged in experience
        findNavController().navigate(R.id.mainActivity)

    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
        Log.d(TAG, "showLoginFailed: error string is $errorString")
    }

    /**
     * Extension function to simplify setting an afterTextChanged action to EditText components.
     */
    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun hideKeybaord(v: View) {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager!!.hideSoftInputFromWindow(v.applicationWindowToken, 0)
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy: onDestroy called")
        super.onDestroy()
    }


}
