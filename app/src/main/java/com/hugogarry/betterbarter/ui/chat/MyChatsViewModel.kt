package com.hugogarry.betterbarter.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Chat
import com.hugogarry.betterbarter.data.repository.ChatRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyChatsViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _chats = MutableStateFlow<Resource<List<Chat>>>(Resource.Idle())
    val chats: StateFlow<Resource<List<Chat>>> = _chats

    fun fetchChats() {
        viewModelScope.launch {
            _chats.value = Resource.Loading()
            _chats.value = chatRepository.getMyChats()
        }
    }
}