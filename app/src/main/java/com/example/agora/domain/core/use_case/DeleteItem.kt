package com.example.agora.domain.core.use_case

import com.example.agora.data.core.model.ItemsWithReference
import com.example.agora.data.core.repository.ItemRepository

class DeleteItem(private val repositoryDao: ItemRepository) {
    suspend operator fun invoke(itemsWithReference : ItemsWithReference) = repositoryDao.deleteItemToFireStore(itemsWithReference)
}