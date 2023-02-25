package com.example.agora.domain.auth

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
private const val TAG = "LoginDataSource"
class LoginDataSource {

//    fun login(username: String, password: String, callback: (Result<FirebaseUser>) -> Unit){
//        try {
//            // TODO: handle loggedInUser authentication
////            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
//            val auth = FirebaseHelper.getInstance()
//            auth.signInWithEmailAndPassword(username, password)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Log.d(TAG, "logIn: successfully logged in with email $username")
//                        callback(Result.Success(auth.currentUser as FirebaseUser))
//                    } else {
//                        // If sign in fails, display a message to the user.
//                        Log.w(TAG, "signInWithEmail:failure", task.exception)
//                    }
//                }
//        } catch (e: Throwable) {
//            callback(Result.Error(IOException("Error logging in", e)))
//        }
//    }

    fun logout() {
        // TODO: revoke authentication
    }
}