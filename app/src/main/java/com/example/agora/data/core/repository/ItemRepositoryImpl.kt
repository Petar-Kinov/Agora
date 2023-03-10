package com.example.agora.data.core.repository

import com.example.agora.data.core.model.Item
import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.data.core.model.Response
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ItemRepositoryImpl"

@Singleton
class ItemRepositoryImpl @Inject constructor(private val itemRef: CollectionReference) :
    ItemRepository {

    private var mSnapshotListener: EventListener<QuerySnapshot>? = null

    override fun getItems() = callbackFlow {
        mSnapshotListener = EventListener<QuerySnapshot> { snapshot, e ->
            val itemResponse = if (snapshot != null) {
                val itemList = mutableListOf<ItemsWithReference>()
                for (document in snapshot) {
                    val gson = Gson()
                    val item = gson.fromJson(gson.toJson(document.data), Item::class.java)
                    itemList.add(ItemsWithReference(item, document.reference))
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

}