package com.example.agora.repository

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.agora.model.Item
import com.example.agora.model.Response
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ItemRepositoryImpl @Inject constructor(private val itemRef : CollectionReference) : ItemRepository {

    private var mSnapshotListener: EventListener<QuerySnapshot>? = null

    companion object {
        private const val TAG = "firebaseRepo"
    }

    override fun getItems() = callbackFlow {
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
            val registration = itemRef.addSnapshotListener(mSnapshotListener!!)
            awaitClose {
                if (registration != null) {
                        registration.remove()
                    }

            }

        }

    private suspend fun getImageBitmap(imageName: String): Bitmap {
        return withContext(Dispatchers.IO) {
            val islandRef = Firebase.storage.reference.child(imageName)
            val ONE_MEGABYTE: Long = 1024 * 1024
            val result = islandRef.getBytes(ONE_MEGABYTE).await()
            BitmapFactory.decodeByteArray(result, 0, result.size)
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