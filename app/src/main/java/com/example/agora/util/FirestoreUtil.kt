package com.example.agora.util

import android.content.Context
import android.util.Log
import com.example.agora.data.Messaging.Model.*
import com.example.agora.data.Messaging.recyclerViewItem.ImageMessageItem
import com.example.agora.data.Messaging.recyclerViewItem.MessageItem
import com.example.agora.data.Messaging.recyclerViewItem.Person
import com.example.agora.data.Messaging.recyclerViewItem.TextMessageItem
import com.example.agora.data.core.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private const val TAG = "FirestoreUtil"

object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance()}

    private val currentUserDocRef : DocumentReference
    get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid}")

    private val chatChannelCollectionRef = firestoreInstance.collection("chatChannels")

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            it.toObject(User::class.java)?.let { it1 -> onComplete(it1) }
        }
    }

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

    // get all the people currently registered
    //TODO get only the people you have chats with
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


    fun addChatMessageListener(channelId: String, context: Context,onListen: (List<MessageItem<*>>) -> Unit): ListenerRegistration {
        return chatChannelCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot , firebaseFirestoreException ->
                if (firebaseFirestoreException != null){
                    Log.e(TAG, "addChatMessageListener: Exception is ", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val items = mutableListOf<MessageItem<*>>()
                querySnapshot!!.documents.forEach{
                    if (it["type"] == MessageType.TEXT){
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!,context))
                    } else {
                        items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!,context))
                    }
                }
                onListen(items)
            }
    }

    fun sendMessage(message : Message, channelId: String) {
        chatChannelCollectionRef.document(channelId).collection("messages").add(message)
    }
    fun removeListener(registration: ListenerRegistration) = registration.remove()

    //region FCM

    fun getFCMRegistrationToken(onComplete: (tokens : MutableList<String>) -> Unit){
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationToken : MutableList<String>){
        currentUserDocRef.update(mapOf("registrationTokens" to registrationToken))
    }

    //endregion FCM
}