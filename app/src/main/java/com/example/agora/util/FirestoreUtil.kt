package com.example.agora.util

import android.content.Context
import android.util.Log
import com.example.agora.data.Messaging.Model.ChatChannel
import com.example.agora.data.Messaging.Model.MessageType
import com.example.agora.data.Messaging.Model.TextMessage
import com.example.agora.data.Messaging.Model.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private const val TAG = "FirestoreUtil"

class FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}

    private val currentUserDocRef : DocumentReference
    get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid}")

    private val chatChannelCollectionRef = firestoreInstance.collection("chatChannels")

    fun getOrCreateChatChannel(otherUserId : String,onComplete : (channelId : String) -> Unit){
        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if (it.exists()){
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                val newChannel = chatChannelCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                currentUserDocRef.collection("engagedChatChannels").document(otherUserId).set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels").document(currentUserId)
                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }

    fun addChatMessageListener(channelId: String, context: Context,onListen: (List<TextMessageItem>) -> Unit): ListenerRegistration {
        return chatChannelCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot , firebaseFirestoreException ->
                if (firebaseFirestoreException != null){
                    Log.e(TAG, "addChatMessageListener: Exception is ", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<TextMessageItem>()
                querySnapshot?.documents?.forEach{
                    if (it["type"] == MessageType.TEXT){
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!,context))
                    } else {
                        TODO("Add picture message")
                    }
                    onListen(items)
                }
            }

    }

}