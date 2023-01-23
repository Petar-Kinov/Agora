package com.example.agora.use_case

import com.example.agora.repository.ItemRepository

class GetItems (private val repositoryDao: ItemRepository) {
    operator fun invoke() = repositoryDao.getItems()
}