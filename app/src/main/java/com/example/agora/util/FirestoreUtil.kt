package com.example.agora.util

import android.content.Context
import android.util.Log
import com.example.agora.data.core.model.User
import com.example.agora.data.messaging.model.*
import com.example.agora.data.messaging.recyclerViewItem.ImageMessageItem
import com.example.agora.data.messaging.recyclerViewItem.MessageItem
import com.example.agora.data.messaging.recyclerViewItem.TextMessageItem
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
                    .set(
                        mapOf(
                            "channelId" to newChannel.id,
                            "otherUserName" to otherUserName,
                            "otherUserId" to otherUserId
                        )
                    )

                firestoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels").document(currentUserId)
                    .set(
                        mapOf(
                            "channelId" to newChannel.id,
                            "otherUserName" to currentUserName,
                            "otherUserId" to currentUserId
                        ),
                        SetOptions.merge()
                    )

                onComplete(newChannel.id)
            }
    }

    fun addUsersListener(
        context: Context,
        onListen: (List<EngagedChatChannel>) -> Unit
    ): ListenerRegistration {
        return firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("engagedChatChannels")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "addChatMessageListener: Exception is ", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val items = mutableListOf<EngagedChatChannel>()
                querySnapshot?.documents?.forEach {
//                    var lastMessage = LastMessage("asdad",Calendar.getInstance().time)
                    val chatChannel = it.toObject(EngagedChatChannel::class.java)
//
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                    //not sure about the !!
                        items.add(chatChannel!!)
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

    fun sendMessage(message: Message, channelId: String, otherUserId: String) {
        chatChannelCollectionRef.document(channelId).collection("messages").add(message)

        // setting the last message inside each users engagedChatChannels/channel
        // otherwise we have to query again for each person in the contact list to get the message
        val lastMessage: LastMessage = if (message is TextMessage) {
            LastMessage(text = message.text, time = message.time, senderId = message.senderId)
        } else {
            LastMessage(text = "Photo",time =  message.time, senderId = message.senderId)
        }
        // setting lastMessage in other user doc
        currentUserDocRef.collection("engagedChatChannels").document(otherUserId)
            .set(hashMapOf("lastMessage" to lastMessage), SetOptions.merge())

        // setting lastMessage in current user doc
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