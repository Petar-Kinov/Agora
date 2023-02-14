package com.example.agora.authentication.data

import android.util.Log
import com.example.agora.authentication.FirebaseHelper
import com.example.agora.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
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

    fun login(username: String, password: String, callback : (Result<FirebaseUser>) -> Unit){
        // handle login
        val auth = FirebaseHelper.getInstance()
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "logIn: successfully logged in with email $username")
                    callback(Result.Success(auth.currentUser as FirebaseUser))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    callback(Result.Error(task.exception as Exception))
                }
            }

//        if (user is Result.Success) {
//            setLoggedInUser(result.data)
//        }
    }

    fun signUp(user: User, callback : (Boolean) -> Unit){
        val auth = FirebaseHelper.getInstance()
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder()
                        .setDisplayName("${user.firstName} ${user.lastName}")
                        .build())

                    Log.d(TAG, "signUp: display name is ${auth.currentUser?.displayName}")
                    callback(true)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    private fun setLoggedInUser(firebaseUser: FirebaseUser) {
        this.user = firebaseUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}