package com.example.agora.domain.core.use_case

import android.graphics.Bitmap
import com.example.agora.data.core.model.Item
import com.example.agora.data.core.repository.ItemRepository

class SellItem(private val repositoryDao: ItemRepository) {
    suspend operator fun invoke(item: Item, bitmapList : ArrayList<Bitmap>) = repositoryDao.addItemToFireStore(item,bitmapList)
}