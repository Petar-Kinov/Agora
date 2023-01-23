package com.example.agora.repository

import com.example.agora.model.Item
import com.example.agora.model.Response
import kotlinx.coroutines.flow.Flow

interface ItemRepositoryDao {
    fun getSellItems() : Flow<Response<List<Item>>>

    suspend fun addItemToFireStore(item: Item) : Response<Boolean>

    suspend fun deleteItemToFireStore(itemId: String) : Response<Boolean>
}
