package com.example.agora.data.Messaging.repository

import com.example.agora.data.Messaging.recyclerViewItem.Person
import com.example.agora.data.core.model.TestUser
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatsRepository {

    private var mSnapshotListener: EventListener<QuerySnapshot>? = null


    suspend fun getChats(): List<Person> = withContext(Dispatchers.IO) {
        try {
            Firebase.firestore.collection("users").get().await().let { snapshot ->
                if (!snapshot.isEmpty) {
                    snapshot.documents.mapNotNull { document ->
                        val gson = Gson()
                        val user = gson.fromJson(gson.toJson(document.data), TestUser::class.java)
                        if (user != null) Person(user.username, document.id)
                        else null
                    }.sortedBy { it.name }
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}