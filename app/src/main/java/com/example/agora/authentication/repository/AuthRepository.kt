package com.example.agora.authentication.repository

import com.example.agora.model.User

interface AuthRepository {

    fun signUp(user: User) : Boolean

    fun logIn(email: String, password : String) : Boolean

    fun deleteUser() : Boolean
}