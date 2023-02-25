package com.example.agora.util

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.agora.data.Messaging.Model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.auth.User

private const val TAG = "FirestoreUtil"

object FirestoreUtil {
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

    fun addUsersListener(context :Context, onListen: (List<Person>) -> Unit) : ListenerRegistration{
        return firestoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG,"addChatMessageListener: Exception is ", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val items = mutableListOf<Person>()
                querySnapshot?.documents?.forEach {
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(Person(it["username"] as String,it.id))
                }
                onListen(items)
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

    fun removeListener(registration: ListenerRegistration) = registration.remove()

}