package com.example.agora.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.agora.model.Item
import com.example.agora.model.Response
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.io.ByteArrayOutputStream
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
                registration.remove()
            }
        }

//    private suspend fun getImageBitmap(imageName: String): Bitmap {
//        return withContext(Dispatchers.IO) {
//            val islandRef = Firebase.storage.reference.child(imageName)
//            val ONE_MEGABYTE: Long = 1024 * 1024
//            val result = islandRef.getBytes(ONE_MEGABYTE).await()
//            BitmapFactory.decodeByteArray(result, 0, result.size)
//        }
//    }

    override suspend fun addItemToFireStore(item: Item, bitmapList: ArrayList<Bitmap>): Response<Boolean> {
        var counter = 0
        var itemAdded = false

        val deferredResult = CompletableDeferred<Response<Boolean>>()

        for (i in 0 until bitmapList.size) {
            val baos = ByteArrayOutputStream()
            bitmapList[i].compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = Firebase.storage.reference.child(item.storageRef).child("$i").putBytes(data)
            uploadTask.addOnFailureListener {
                Log.d(TAG, "onViewCreated: Could not upload image")
                deferredResult.complete(Response.Failure(it))
            }.addOnSuccessListener { taskSnapshot ->
                counter++
                Log.d(TAG, "onViewCreated: Successful upload of image ${taskSnapshot.metadata.toString()} ")
                if (counter == bitmapList.size && !itemAdded) {
                    itemAdded = true
                    itemRef.add(item).addOnSuccessListener {
                        deferredResult.complete(Response.Success(true))
                    }.addOnFailureListener { exception ->
                        deferredResult.complete(Response.Failure(exception))
                    }
                }
            }
        }

        return deferredResult.await()
    }


    override suspend fun deleteItemToFireStore(itemId: String): Response<Boolean> {
        TODO("Not yet implemented")
    }
}