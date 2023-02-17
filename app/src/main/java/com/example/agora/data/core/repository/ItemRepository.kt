package com.example.agora.data.core.repository

import android.graphics.Bitmap
import com.example.agora.data.core.model.Item
import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.data.core.model.Response
import kotlinx.coroutines.flow.Flow

interface ItemRepository {

    fun getItems() : Flow<Response<List<ItemsWithReference>>>

    suspend fun addItemToFireStore(item: Item, bitmapList : ArrayList<Bitmap>) : Response<Boolean>

    suspend fun deleteItemToFireStore(itemWithReference: ItemsWithReference) : Response<Boolean>
}
