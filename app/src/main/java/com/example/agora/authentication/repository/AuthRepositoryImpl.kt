package com.example.agora.authentication.repository

import android.util.Log
import com.example.agora.authentication.FirebaseHelper
import com.example.agora.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl () : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override fun signUp(user: User): Boolean {
        var isSuccessful = false
        val auth = FirebaseHelper.getInstance()

        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    isSuccessful = true
//                    val user = hashMapOf(
//                        "firstName" to firstNameET.text.toString(),
//                        "lastName" to lastNameET.text.toString()
//                    )

                    auth.currentUser?.let {
//                         when registering display name is set after you are logged in
//                         and the welcome msg name is set to null
//                        so we wait for the user.displayName to be set first

                        runBlocking {
                            launch {
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = "${user.firstName} ${user.lastName}"
                                }
                                it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                                    if (task.isSuccessful){
                                        Log.d(TAG, "signUp: Display name is  ${profileUpdates.displayName}")
                                    }
                                }.await()
                            }
                        }

//                        fireStoreDB.collection("users").document(it.uid)
//                            .set(user)
//                            .addOnSuccessListener { documentReference ->
//                                Log.d(com.example.agora.fragments.TAG, "DocumentSnapshot added")
//                            }
//                            .addOnFailureListener { e ->
//                                Log.w(com.example.agora.fragments.TAG, "Error adding document", e)
//                            }
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
        return isSuccessful
    }

    override fun logIn(email: String, password: String): Boolean {
        var isSuccessful = false
        val auth = FirebaseHelper.getInstance()
        auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                isSuccessful = true
                Log.d(TAG, "logIn: successfully logged in with email $email")
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.exception)
            }
        }
        return isSuccessful
    }

    override fun deleteUser(): Boolean {
        TODO("Not yet implemented")
    }
}