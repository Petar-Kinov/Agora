package com.example.agora.domain.Messaging.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agora.data.Messaging.Model.Chat
import com.example.agora.data.Messaging.repository.ChatsRepository
import kotlinx.coroutines.launch

class ChatsViewModel(val chatsRepository: ChatsRepository) :ViewModel(){

    val chatsList: LiveData<List<Chat>>
            get() = _chatsList
    private val _chatsList = MutableLiveData<List<Chat>>()

    fun getChats() =viewModelScope.launch {
        _chatsList.postValue(chatsRepository.getChats())
    }

}