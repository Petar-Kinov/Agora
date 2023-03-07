package com.example.agora.data.authentication.repository

import android.util.Log
import com.example.agora.data.authentication.model.Result
import com.example.agora.data.core.model.User
import com.example.agora.domain.auth.LoginDataSource
import com.example.agora.util.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthRepository"

class AuthRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: FirebaseUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        FirebaseAuth.getInstance().signOut()
    }

    fun deleteUser() {
        auth.currentUser!!.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "deleteUser: user was deleted")
            }
        }
    }

    suspend fun login(username: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(username, password).await()
            Result.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "login: exception is ${e.toString()}")
            Result.Error(e)
        }
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(
//                        TAG,
//                        "logIn: successfully logged in with email $username"
//                    )
//                    callback(Result.Success(auth.currentUser as FirebaseUser))
//                } else {
//                    // If sign in fails, display a message to the user.
//                    Log.w(
//                        TAG,
//                        "signInWithEmail:failure",
//                        task.exception
//                    )
//                    callback(Result.Error(task.exception as Exception))
//                }
//            }

//        if (user is Result.Success) {
//            setLoggedInUser(result.data)
//        }
    }

    suspend fun signUp(user: User): Result<FirebaseUser> {
        val auth = FirebaseHelper.getInstance()
        return try {
            val result =
                auth.createUserWithEmailAndPassword(user.email.trim(), user.password).await()
            updateDisplayName(user.username)
            //TODO add onSuccessListener to check if the user was added to firestore
            addUserToFireStore(user.username)
            Result.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e)
        }
    }

    private fun addUserToFireStore(username: String) {
        Firebase.firestore.collection("users").document(auth.currentUser!!.uid)
            .set(hashMapOf("username" to username))
    }

    private suspend fun updateDisplayName(username: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)?.await()
    }

    private fun setLoggedInUser(firebaseUser: FirebaseUser) {
        this.user = firebaseUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    companion object {
        val auth = FirebaseHelper.getInstance()
    }
}