package com.example.agora.Services

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.util.Log
import com.example.agora.data.core.model.Item
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class DeleteItemService: BaseTaskService() {

    private lateinit var storageRef: StorageReference

    override fun onCreate() {
        super.onCreate()

        storageRef = Firebase.storage.reference
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent:$startId")

        if (DELETE_ACTION == intent.action) {

            val item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(ITEM, Item::class.java) as Item
            } else {
                intent.getParcelableExtra<Parcelable>(ITEM) as Item
            }

            val docRefPath = intent.getStringExtra(REFERENCE)!!
            val docRef =  FirebaseFirestore.getInstance().document(docRefPath)

            docRef.delete().addOnSuccessListener {
                //TODO deletes the firestore item but not the images in storage
                val storageReference = Firebase.storage.reference.child("items").child(item.storageRef)
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

        }

        return START_REDELIVER_INTENT
    }

    companion object {
        private const val TAG = "DeleteItemService"
        const val DELETE_ACTION = "delete_action"
        const val REFERENCE = "reference"
        const val ITEM = "item"
    }
}