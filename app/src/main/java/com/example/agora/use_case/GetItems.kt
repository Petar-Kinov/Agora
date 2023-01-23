package com.example.agora.use_case

import com.example.agora.repository.ItemRepositoryDao

class GetItems (private val repositoryDao: ItemRepositoryDao) {
    operator fun invoke() = repositoryDao.getSellItems()
}