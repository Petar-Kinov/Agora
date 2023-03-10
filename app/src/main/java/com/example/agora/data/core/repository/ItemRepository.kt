package com.example.agora.data.core.repository

import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.data.core.model.Response
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems() : Flow<Response<List<ItemsWithReference>>>
}
