package com.hugogarry.betterbarter.data.repository

import com.hugogarry.betterbarter.data.model.Chat
import com.hugogarry.betterbarter.data.model.Message
import com.hugogarry.betterbarter.data.remote.ApiService
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(private val apiService: ApiService) {

    suspend fun getMyChats(): Resource<List<Chat>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyChats()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch chats")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun getMessages(tradeId: String): Resource<List<Message>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMessages(tradeId)
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error(response.message() ?: "Failed to fetch messages")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun sendMessage(tradeId: String, text: String): Resource<Message> = withContext(Dispatchers.IO) {
        try {
            val body = mapOf("text" to text)
            val response = apiService.sendMessage(tradeId, body)
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.message() ?: "Failed to send message")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}