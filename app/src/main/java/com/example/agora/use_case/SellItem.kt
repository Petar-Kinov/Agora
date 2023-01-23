package com.example.agora.use_case

import com.example.agora.model.Item
import com.example.agora.repository.ItemRepository

class SellItem(private val repositoryDao: ItemRepository) {
    suspend operator fun invoke(item: Item) = repositoryDao.addItemToFireStore(item)
}