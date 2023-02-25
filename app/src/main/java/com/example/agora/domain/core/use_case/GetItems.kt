package com.example.agora.domain.core.use_case

import com.example.agora.data.core.repository.ItemRepository

class GetItems (private val repositoryDao: ItemRepository) {
    operator fun invoke() = repositoryDao.getItems()
}