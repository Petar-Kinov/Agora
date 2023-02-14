package com.example.agora.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.agora.authentication.data.LoginDataSource
import com.example.agora.authentication.data.AuthRepository

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class LoginViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                authRepository = AuthRepository(
                    dataSource = LoginDataSource()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}