package com.example.agora.repository

import com.example.agora.model.Item
import com.example.agora.model.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
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


//    suspend fun getItemsToSell(): List<Item> {
//        val auth = FirebaseAuth.getInstance()
//        val firebaseDB = Firebase.firestore
//        val itemsList: MutableList<Item> = ArrayList()
//        val collection = auth.currentUser?.let {
//            firebaseDB.collection("users").document(it.uid).collection("itemsToSell")
//        }
//        withContext(Dispatchers.IO) {
//            async {
//                collection?.get()?.addOnCompleteListener { result ->
//                    if (result.isSuccessful) {
//                        for (document in result.getResult()) {
//                            val title = document.data.get("title") as String
//                            val description = document.data.get("description") as String
//                            val price = document.data.get("price") as Double
//                            itemsList.add(
//                                Item(
//                                    name = title,
//                                    description = description,
//                                    price = price
//                                )
//                            )
//                        }
//                    }
//                }
//                    ?.addOnFailureListener { exception ->
//                        Log.d(TAG, "Error getting documents: ", exception)
//                    }
//            }
//        }.await()
//
//        return itemsList
//    }

    override fun getSellItems() = callbackFlow {
        val auth = FirebaseAuth.getInstance()
        val query = itemRef.whereEqualTo("sellerId" , auth.currentUser?.uid)
        mSnapshotListener = EventListener<QuerySnapshot> { snapshot , e->
            val itemResponse = if (snapshot != null) {
                val itemList = mutableListOf<Item>()
                for (documents in snapshot) {
                    val  sellerId = documents.get("sellerId") as String
                    val name = documents.get("title") as String
                    val description = documents.get("description") as String
                    val price = (documents.get("price") as String)
                    val item = Item(sellerId,name,description,price)
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