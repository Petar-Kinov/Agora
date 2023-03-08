package com.example.agora.data.core.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.agora.data.core.model.Item
import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.data.core.model.Response
import com.google.android.gms.tasks.Task
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
        val uploadTasks = mutableListOf<Task<Uri>>()
//
//
//        return try {
//            for (bitmap in bitmapList) {
//                val baos = ByteArrayOutputStream()
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//                val imageData = baos.toByteArray()
//
//                val imageRef = Firebase.storage.reference.child(item.storageRef).child("$imageUploadCounter")
//                val uploadTask = imageRef.putBytes(imageData)
//                uploadTasks.add(uploadTask)
//                imageUploadCounter++
//            }
//
//            Tasks.whenAllSuccess<Uri>(uploadTasks).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val downloadUrls = task.result
//                    if (downloadUrls.size == bitmapList.size) {
//                        itemRef.add(item).await()
//                        // Start the UploadService to upload the photos in the background
//                        for (i in 0 until bitmapList.size) {
//                            val intent = Intent(context, UploadService::class.java)
//                            intent.putExtra("imageRef", downloadUrls[i].toString())
//                            context.startService(intent)
//                        }
//                        Response.Success(true)
//                    } else {
//                        Response.Success(false)
//                    }
//                } else {
//                    Response.Failure(task.exception!!)
//                }
//            }.await()
//
//        } catch (e: Exception) {
//            Response.Failure(e)
//        }

        return try {
            for (bitmap in bitmapList) {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val imageData = baos.toByteArray()

                val imageRef = Firebase.storage.reference.child(item.storageRef).child("$imageUploadCounter")
                val uploadTask = imageRef.putBytes(imageData)
                //TODO  what happens if a picture is not uploaded
                uploadTask.await()
                imageUploadCounter++

//                val intent = Intent(, UploadService::class.java)
//                intent.putExtra(Intent.EXTRA_STREAM, imageRef.downloadUrl)
//                context.startService(intent)

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

    }

    override suspend fun deleteItemToFireStore(itemWithReference: ItemsWithReference): Response<Boolean> {
        //TODO make these separate jobs inside coroutine
        itemRef.document(itemWithReference.documentReference.id).delete().addOnSuccessListener {
            //TODO deletes the firestore item but not the images in storage
            val storageReference = Firebase.storage.reference.child("items").child(itemWithReference.item.storageRef)
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