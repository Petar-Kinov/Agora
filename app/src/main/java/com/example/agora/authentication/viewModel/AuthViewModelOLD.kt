package com.example.agora.authentication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agora.authentication.repository.AuthRepositoryImpl
import com.example.agora.model.User
import kotlinx.coroutines.launch

// NOT IN USE
class AuthViewModelOLD(private val authRepositoryImpl: AuthRepositoryImpl): ViewModel() {

    companion object{
        private const val TAG = "AuthViewModel"
    }

    val signUpIsSuccessful: LiveData<Boolean>
            get() = _signUpIsSuccessful
    private val _signUpIsSuccessful = MutableLiveData<Boolean>()

    fun logIn(email: String, password: String) = viewModelScope.launch {
        authRepositoryImpl.logIn(email,password)
    }

    fun signup(user: User) {
        authRepositoryImpl.signUp(user){
            _signUpIsSuccessful.postValue(it)
        }
    }


}