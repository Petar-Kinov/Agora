package com.example.agora.data.core.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.agora.data.core.model.Item
import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.data.core.model.Response
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ItemRepositoryImpl"
@Singleton
class ItemRepositoryImpl @Inject constructor(private val itemRef : CollectionReference) :
    ItemRepository {

    private var mSnapshotListener: EventListener<QuerySnapshot>? = null

    override fun getItems() = callbackFlow {
            mSnapshotListener = EventListener<QuerySnapshot> { snapshot , e->
                val itemResponse = if (snapshot != null) {
                    val itemList = mutableListOf<ItemsWithReference>()
                    for (document in snapshot) {
                        val gson = Gson()
                        val item = gson.fromJson(gson.toJson(document.data), Item::class.java)
                        itemList.add(ItemsWithReference(item,document.reference))
                    }
                    itemList.sortBy { it.item.storageRef }
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

        var imageUploadCounter = 0

        return try {
            for (bitmap in bitmapList) {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val imageData = baos.toByteArray()

                val imageRef = Firebase.storage.reference.child(item.storageRef).child("$imageUploadCounter")
                val uploadTask = imageRef.putBytes(imageData)
                //todo  what happens if a picture is not uploaded
                uploadTask.await()
                imageUploadCounter++
            }

            if (imageUploadCounter == bitmapList.size) {
                //TODO  finish upload in background if app is closed before that
                //TODO check if the upload was successful
                itemRef.add(item).await()
                Response.Success(true)
            } else {
                Response.Success(false)
            }
        } catch (e: Exception) {
            Response.Failure(e)
        }


//        var counter = 0
//        var itemAdded = false
//
//        val deferredResult = CompletableDeferred<Response<Boolean>>()
//
//        for (i in 0 until bitmapList.size) {
//            val baos = ByteArrayOutputStream()
//            bitmapList[i].compress(Bitmap.CompressFormat.JPEG, 100, baos)
//            val data = baos.toByteArray()
//            val uploadTask = Firebase.storage.reference.child(item.storageRef).child("$i").putBytes(data)
//            uploadTask.addOnFailureListener {
//                Log.d(TAG, "onViewCreated: Could not upload image")
//                deferredResult.complete(Response.Failure(it))
//            }.addOnSuccessListener { taskSnapshot ->
//                counter++
//                Log.d(TAG, "onViewCreated: Successful upload of image ${taskSnapshot.metadata.toString()} ")
//                if (counter == bitmapList.size && !itemAdded) {
//                    itemAdded = true
//                    itemRef.add(item).addOnSuccessListener {
//                        deferredResult.complete(Response.Success(true))
//                    }.addOnFailureListener { exception ->
//                        deferredResult.complete(Response.Failure(exception))
//                    }
//                }
//            }
//        }
//
//        return deferredResult.await()
    }


    override suspend fun deleteItemToFireStore(item: ItemsWithReference): Response<Boolean> {
        //TODO make these separate jobs inside coroutine possibly
        itemRef.document(item.documentReference.id).delete().addOnSuccessListener {
            val storageReference = Firebase.storage.reference.child(item.item.storageRef)
            storageReference.listAll().addOnSuccessListener { listResult ->
                val items = listResult.items
                val tasks = items.map { it.delete() }
                Tasks.whenAllSuccess<Void>(tasks).addOnSuccessListener {
                    Log.d(TAG, "deleteItemToFireStore: Pictures deleted successfully from Firebase storage")
                }.addOnFailureListener { exception ->
                    Log.d(TAG, "deleteItemToFireStore: Failed to delete pictures from Firebase storage", exception)
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "deleteItemToFireStore: Failed to list contents of folder in Firebase storage", exception)
            }

            Log.d(TAG, "deleteItemToFireStore: Item deleted successfully from Firestore")
        }.addOnFailureListener {
            Log.d(TAG, "deleteItemToFireStore: Failed to delete item")
        }
                return Response.Success(true)
    }
}