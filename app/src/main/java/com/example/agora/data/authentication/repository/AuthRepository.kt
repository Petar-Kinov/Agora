package com.example.agora.data.authentication.repository

import android.util.Log
import com.example.agora.data.authentication.model.Result
import com.example.agora.data.core.model.User
import com.example.agora.domain.auth.LoginDataSource
import com.example.agora.util.FirebaseHelper
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
        //TODO use this for logging out
        user = null
        dataSource.logout()
    }

    fun login(username: String, password: String, callback: (Result<FirebaseUser>) -> Unit) {
        // handle login
        val auth = FirebaseHelper.getInstance()
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(
                        TAG,
                        "logIn: successfully logged in with email $username"
                    )
                    callback(Result.Success(auth.currentUser as FirebaseUser))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        TAG,
                        "signInWithEmail:failure",
                        task.exception
                    )
                    callback(Result.Error(task.exception as Exception))
                }
            }

//        if (user is Result.Success) {
//            setLoggedInUser(result.data)
//        }
    }

    fun signUp(user: User, callback: (Boolean) -> Unit) {
        val auth = FirebaseHelper.getInstance()
        auth.createUserWithEmailAndPassword(user.email.trim(), user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(user.username)
                            .build()
                    )
                    val userHashMap = hashMapOf(
                        "username" to user.username
                    )
                    Firebase.firestore.collection("users").document(auth.currentUser!!.uid)
                        .set(userHashMap)
                    //TODO add onSuccessListener

                    Log.d(
                        TAG,
                        "signUp: display name is ${auth.currentUser?.displayName}"
                    )
                    callback(true)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        TAG,
                        "createUserWithEmail:failure",
                        task.exception
                    )
                }
            }
    }

    private fun setLoggedInUser(firebaseUser: FirebaseUser) {
        this.user = firebaseUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}