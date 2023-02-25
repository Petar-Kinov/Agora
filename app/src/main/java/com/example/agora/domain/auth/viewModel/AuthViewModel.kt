package com.example.agora.domain.auth.viewModel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.agora.R
import com.example.agora.data.authentication.login.LoggedInUserView
import com.example.agora.data.authentication.login.LoginResult
import com.example.agora.data.authentication.model.Result
import com.example.agora.data.authentication.repository.AuthRepository
import com.example.agora.data.core.model.User
import com.example.agora.ui.fragments.authentication.LoginFormState

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _signUpIsSuccessful = MutableLiveData<Boolean>()
    val signUpIsSuccessful: LiveData<Boolean> = _signUpIsSuccessful

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        if (isPasswordValid(username) && isPasswordValid(password)){
            authRepository.login(username, password){ result ->
                if (result is Result.Success) {
                    _loginResult.value =
                        LoginResult(success = result.data.displayName?.let { LoggedInUserView(displayName = it) })
                } else {
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                    Log.d("TAG", "login: ${_loginResult.value}")
                }
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    fun signup(user: User) {
        authRepository.signUp(user){
            _signUpIsSuccessful.postValue(it)
        }
    }
}