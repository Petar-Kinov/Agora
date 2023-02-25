package com.example.agora.domain.Messaging.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agora.data.Messaging.Model.Person
import com.example.agora.data.Messaging.repository.ChatsRepository
import kotlinx.coroutines.launch

class ChatsViewModel(val chatsRepository: ChatsRepository) :ViewModel(){

    val chatsList: LiveData<List<Person>>
            get() = _chatsList
    private val _chatsList = MutableLiveData<List<Person>>()

    fun getChats() =viewModelScope.launch {
        _chatsList.postValue(chatsRepository.getChats())
    }

}