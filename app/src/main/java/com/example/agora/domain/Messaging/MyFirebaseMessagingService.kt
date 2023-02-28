package com.example.agora.domain.Messaging

import android.util.Log
import com.example.agora.util.FirestoreUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

private const val TAG = "FCM"
class MyFirebaseMessagingService : FirebaseMessagingService() {
//
    override fun onNewToken(token: String) {
        if (FirebaseAuth.getInstance().currentUser != null){
            val newRegistrationToken = FirebaseMessaging.getInstance().token
            addTokenToFirestore(newRegistrationToken.result)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.notification != null) {
            //TODO Show Notification
            Log.d(TAG, "onMessageReceived: FCM message Received")
        }
    }

    companion object {
        fun addTokenToFirestore(newRegistrationToken : String?) {
            if (newRegistrationToken == null) throw NullPointerException("FCM token is null.")

            FirestoreUtil.getFCMRegistrationToken { tokens ->
                    if (tokens.contains(newRegistrationToken)){
                        return@getFCMRegistrationToken
                    }
                    tokens.add(newRegistrationToken)
                    FirestoreUtil.setFCMRegistrationTokens(tokens)
                }
        }
    }
}