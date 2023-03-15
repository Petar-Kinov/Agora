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
import com.google.firebase.firestore.SetOptions

private const val TAG = "FirestoreUtil"

object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid}")

    private val chatChannelCollectionRef = firestoreInstance.collection("chatChannels")

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            it.toObject(User::class.java)?.let { it1 -> onComplete(it1) }
        }
    }

    fun getOrCreateChatChannel(
        otherUserId: String,
        otherUserName: String,
        onComplete: (channelId: String) -> Unit
    ) {
        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if (it.exists()) {
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
                val currentUserName = FirebaseAuth.getInstance().currentUser!!.displayName

                val newChannel = chatChannelCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                currentUserDocRef.collection("engagedChatChannels").document(otherUserId)
                    .set(mapOf("channelId" to newChannel.id, "otherUserName" to otherUserName))

                firestoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels").document(currentUserId)
                    .set(
                        mapOf("channelId" to newChannel.id, "otherUserName" to currentUserName),
                        SetOptions.merge()
                    )

                onComplete(newChannel.id)
            }
    }

    // get all the people currently registered
    //TODO get only the people you have chats with
    fun addUsersListener(context: Context, onListen: (List<Person>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("engagedChatChannels")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "addChatMessageListener: Exception is ", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val items = mutableListOf<Person>()
                querySnapshot?.documents?.forEach {
//                    var lastMessage = LastMessage("asdad",Calendar.getInstance().time)
                    val last = it.toObject(EngagedChatChannel::class.java)
//
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(Person(it["otherUserName"] as String, it.id, last!!.lastMessage))
                }
                onListen(items)
            }
    }


    fun addChatMessageListener(
        channelId: String,
        context: Context,
        onListen: (List<MessageItem<*>>) -> Unit
    ): ListenerRegistration {
        return chatChannelCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "addChatMessageListener: Exception is ", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val items = mutableListOf<MessageItem<*>>()
                querySnapshot!!.documents.forEach {
                    if (it["type"] == MessageType.TEXT) {
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                    } else {
                        items.add(
                            ImageMessageItem(
                                it.toObject(ImageMessage::class.java)!!,
                                context
                            )
                        )
                    }
                }
                onListen(items)
            }
    }

    fun getLastMessage(channelId: String, callback: (lastMessage: String, time: String) -> Unit) {
        chatChannelCollectionRef.document(channelId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val message = querySnapshot.getString("lastMessage") as String
                val time = querySnapshot.getString("time") as String

                Log.d(TAG, "getLastMessage: message is $message and time is $time")
                callback(message, time)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting last message: $exception")
            }
    }

    fun sendMessage(message: Message, channelId: String, otherUserId: String) {
        chatChannelCollectionRef.document(channelId).collection("messages").add(message)


        // setting the last message inside each users engagedChatChannels/channel
        // otherwise we have to query again for each person in the contact list to get the message
        val lastMessage: LastMessage
        if (message is TextMessage) {
            lastMessage = LastMessage(message.text, message.time)
        } else {
            lastMessage = LastMessage("Photo", message.time)
        }
        currentUserDocRef.collection("engagedChatChannels").document(otherUserId)
            .set(hashMapOf("lastMessage" to lastMessage), SetOptions.merge())
        firestoreInstance.document("users/$otherUserId").collection("engagedChatChannels")
            .document(FirebaseAuth.getInstance().currentUser!!.uid).set(
            hashMapOf(
                "lastMessage" to lastMessage
            ), SetOptions.merge()
        )

    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    //region FCM

    fun getFCMRegistrationToken(onComplete: (tokens: MutableList<String>) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            onComplete(user.registrationTokens)
        }
    }

    fun setFCMRegistrationTokens(registrationToken: MutableList<String>) {
        currentUserDocRef.update(mapOf("registrationTokens" to registrationToken))
    }

    //endregion FCM
}