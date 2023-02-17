package com.example.agora.data.authentication.repository

import android.util.Log
import com.example.agora.data.core.model.User
import com.example.agora.util.FirebaseHelper

private const val TAG = "AuthRepositoryImpl"
class AuthRepositoryImpl {
    fun signUp(user: User, callback : (Boolean) -> Unit){
        var isSuccessful = false
        val auth = FirebaseHelper.getInstance()

        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    callback(true)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    fun logIn(email: String, password: String): Boolean {
        var isSuccessful = false
//        val auth = FirebaseHelper.getInstance()
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    isSuccessful = true
//                    Log.d(TAG, "logIn: successfully logged in with email $email")
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(TAG, "signInWithEmail:failure", task.exception)
//                }
//            }
        return isSuccessful
    }

    fun deleteUser(): Boolean {
        TODO("Not yet implemented")
    }
}