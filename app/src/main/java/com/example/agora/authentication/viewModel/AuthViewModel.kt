package com.example.agora.authentication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agora.authentication.repository.AuthRepositoryImpl
import com.example.agora.model.User
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepositoryImpl: AuthRepositoryImpl): ViewModel() {

    companion object{
        private const val TAG = "AuthViewModel"
    }

    fun logIn(email: String, password: String) = viewModelScope.launch {
        authRepositoryImpl.logIn(email,password)
    }


    fun signup(user: User) = viewModelScope.launch {
        authRepositoryImpl.signUp(user)
    }
}