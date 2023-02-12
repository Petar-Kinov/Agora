package com.example.agora.authentication.authFragments

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.agora.R
import com.example.agora.authentication.login.LoggedInUserView
import com.example.agora.authentication.login.LoginViewModel
import com.example.agora.authentication.login.LoginViewModelFactory
import com.example.agora.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "LoginFragment"
private lateinit var auth: FirebaseAuth

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(requireActivity(), LoginViewModelFactory())
            .get(LoginViewModel::class.java)
    }

//    private val viewModel: AuthViewModel by lazy {
//        AuthViewModel(AuthRepositoryImpl())
//    }

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
        loginViewModel.loginFormState.removeObservers(viewLifecycleOwner)
        loginViewModel.loginResult.removeObservers(viewLifecycleOwner)
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
//        login.setOnClickListener {
//            val action = LoginFragmentDirections.actionLoginFragmentToMainActivity()
//            findNavController().navigate(action)
//            Log.d(TAG, "onViewCreated: activity is $activity")
//            activity?.finish()
//        }
//
//        binding.signInBtn.setOnClickListener {
//            val email = binding.usernameET.text.toString()
//            val password = binding.passwordET.text.toString()
//            if (email.isEmpty() || password.isEmpty()) {
//                Toast.makeText(
//                    requireContext(), "Please enter Email and password",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                viewModel.logIn(email,password)
//            }
//        }
//
        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            loginViewModel.login(username.text.toString(), password.text.toString())
        }

        binding.peshoBtn.setOnClickListener {
            loginViewModel.login("petar.kinov@gmail.com", "qwerty")
        }

        binding.ivanBtn.setOnClickListener {
            loginViewModel.login("i.ivanov@gmail.com", "qwerty")
        }

        binding.svetlioBtn.setOnClickListener {
            loginViewModel.login("s.lambev@gmail.com", "qwerty")
        }



        // Generated from android studio !!!!!!!!!!!!!!!!!!!!




        loginViewModel.loginFormState.observe(requireActivity(), Observer {
            val loginState = it ?: return@Observer

//         disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(requireActivity(), Observer {

            Log.d(TAG, "onViewCreated: observer called with Log in result  : $it")

            // this observer is being called twice . no idea why
            val loginResult = it ?: return@Observer

//            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                requireActivity().setResult(Activity.RESULT_OK)
            }


//        Complete and destroy login activity once successful

        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

        }

    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience

        // Main Activity was being started twice for some reason
        // so i set it's launch mode to singleTop

        val navOption = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setPopUpTo(R.id.loginActivity,true)
            .build()

        val action = LoginFragmentDirections.actionLoginFragmentToMainActivity()
        findNavController().navigate(action,navOption)
//        requireActivity().finishAffinity()

//        Toast.makeText(
//            requireContext(),
//            "$welcome $displayName",
//            Toast.LENGTH_LONG
//        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
//        Toast.makeText(requireContext(), errorString, Toast.LENGTH_SHORT).show()
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



    override fun onDestroy() {
        Log.d(TAG, "onDestroy: onDestroy called")
        super.onDestroy()
    }


}
