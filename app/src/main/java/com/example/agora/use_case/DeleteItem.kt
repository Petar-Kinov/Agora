package com.example.agora.use_case

import com.example.agora.model.Item
import com.example.agora.model.ItemsWithReference
import com.example.agora.repository.ItemRepository

class DeleteItem(private val repositoryDao: ItemRepository) {
    suspend operator fun invoke(itemsWithReference : ItemsWithReference) = repositoryDao.deleteItemToFireStore(itemsWithReference)
}