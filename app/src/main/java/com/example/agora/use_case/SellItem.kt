package com.example.agora.use_case

import com.example.agora.model.Item
import com.example.agora.repository.ItemRepositoryDao

class SellItem(private val repositoryDao: ItemRepositoryDao) {
    suspend operator fun invoke(item: Item) = repositoryDao.addItemToFireStore(item)
}