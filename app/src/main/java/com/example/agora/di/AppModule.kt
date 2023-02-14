package com.example.agora.di

import com.example.agora.repository.ItemRepositoryImpl
import com.example.agora.repository.ItemRepository
import com.example.agora.use_case.DeleteItem
import com.example.agora.use_case.GetItems
import com.example.agora.use_case.SellItem
import com.example.agora.use_case.UseCases
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideUserRef() =
        Firebase.firestore.collection("items")

    @Provides
    fun provideItemRepository(itemRef: CollectionReference): ItemRepository =
        ItemRepositoryImpl(itemRef)

    @Provides
    fun provideUseCases(
        repo: ItemRepository
    ) = UseCases(
        getItems = GetItems(repo),
        sellItem = SellItem(repo),
        deleteItem = DeleteItem(repo)
    )
}