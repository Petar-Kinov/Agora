package com.example.agora.domain.Messaging.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.agora.data.Messaging.repository.ChatsRepository
import com.example.agora.data.authentication.repository.AuthRepository
import com.example.agora.domain.auth.LoginDataSource
import com.example.agora.domain.auth.viewModel.AuthViewModel

@Suppress("UNCHECKED_CAST")
class ChatsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatsViewModel::class.java)) {
            return ChatsViewModel(
                chatsRepository = ChatsRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
