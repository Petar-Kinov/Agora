package com.example.agora.domain.messaging.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agora.data.messaging.model.EngagedChatChannel
import com.example.agora.data.messaging.repository.ChatsRepository
import kotlinx.coroutines.launch

class ChatsViewModel(val chatsRepository: ChatsRepository) : ViewModel() {

    val chatsList: LiveData<List<EngagedChatChannel>>
        get() = _chatsList
    private val _chatsList = MutableLiveData<List<EngagedChatChannel>>()

    fun getChats() = viewModelScope.launch {
        _chatsList.postValue(chatsRepository.getChats())
    }

}