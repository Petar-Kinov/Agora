package com.example.agora.repository

import android.graphics.Bitmap
import com.example.agora.model.Item
import com.example.agora.model.ItemsWithReference
import com.example.agora.model.Response
import kotlinx.coroutines.flow.Flow

interface ItemRepository {

    fun getItems() : Flow<Response<List<ItemsWithReference>>>

    suspend fun addItemToFireStore(item: Item, bitmapList : ArrayList<Bitmap>) : Response<Boolean>

    suspend fun deleteItemToFireStore(itemWithReference: ItemsWithReference) : Response<Boolean>
}
