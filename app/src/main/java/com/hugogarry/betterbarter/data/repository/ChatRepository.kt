package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Chat
import com.hugogarry.betterbarter.data.model.Message
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource

class ChatRepository(private val apiService: ApiService = ApiClient.apiService) {

    suspend fun getMyChats(): Resource<List<Chat>> {
        return try {
            val chats = apiService.getMyChats()
            Resource.Success(chats)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load chats")
        }
    }

    suspend fun getMessages(tradeId: String): Resource<List<Message>> {
        return try {
            val messages = apiService.getMessages(tradeId)
            Resource.Success(messages)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load messages")
        }
    }

    suspend fun sendMessage(tradeId: String, text: String): Resource<Message> {
        return try {
            val message = apiService.sendMessage(tradeId, mapOf("text" to text))
            Resource.Success(message)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send message")
        }
    }
}