package com.example.agora.repository

import android.util.Log
import com.example.agora.model.Item
import com.example.agora.model.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(private val itemRef : CollectionReference) : ItemRepositoryDao {

    private var mSnapshotListener: EventListener<QuerySnapshot>? = null

    companion object {
        private const val TAG = "firebaseRepo"
    }


    override fun getSellItems() = callbackFlow {
        val auth = FirebaseAuth.getInstance()
        val query = itemRef.whereEqualTo("seller" , auth.currentUser?.displayName)
        mSnapshotListener = EventListener<QuerySnapshot> { snapshot , e->
            val itemResponse = if (snapshot != null) {
                val itemList = mutableListOf<Item>()
                for (document in snapshot) {
                    val gson = Gson()
                    val item = gson.fromJson(gson.toJson(document.data), Item::class.java)
                    itemList.add(item)
                }
                Response.Success(itemList)
            } else {
                Response.Failure(e)
            }
            trySend(itemResponse)
        }
//        val snapshotListener =
//                itemRef.whereEqualTo("sellerId" , auth.currentUser?.uid).addSnapshotListener() { snapshot, e ->

        val registration = query.addSnapshotListener(mSnapshotListener!!)

        awaitClose {
            if (registration != null) {
                registration.remove()
            }
        }
    }

    override suspend fun addItemToFireStore(item : Item): Response<Boolean> {
        return try {
            itemRef.add(item).await()
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    override suspend fun deleteItemToFireStore(itemId: String): Response<Boolean> {
        TODO("Not yet implemented")
    }
}