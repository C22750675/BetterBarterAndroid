package com.hugogarry.betterbarter.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Message
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeStatus
import com.hugogarry.betterbarter.data.repository.ChatRepository
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val tradeRepository: TradeRepository = TradeRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<Resource<List<Message>>>(Resource.Idle())
    val messages: StateFlow<Resource<List<Message>>> = _messages

    private val _sendMessageState = MutableStateFlow<Resource<Message>>(Resource.Idle())
    val sendMessageState: StateFlow<Resource<Message>> = _sendMessageState

    private val _tradeDetails = MutableStateFlow<Resource<Trade>>(Resource.Idle())
    val tradeDetails: StateFlow<Resource<Trade>> = _tradeDetails

    // State for completing a trade
    private val _completeTradeState = MutableStateFlow<Resource<Trade>>(Resource.Idle())
    val completeTradeState: StateFlow<Resource<Trade>> = _completeTradeState

    fun fetchMessages(tradeId: String) {
        viewModelScope.launch {
            _messages.value = Resource.Loading()
            _messages.value = chatRepository.getMessages(tradeId)
        }
    }

    fun fetchTradeDetails(tradeId: String) {
        viewModelScope.launch {
            _tradeDetails.value = Resource.Loading()
            _tradeDetails.value = tradeRepository.getTrade(tradeId)
        }
    }

    fun sendMessage(tradeId: String, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _sendMessageState.value = Resource.Loading()
            val result = chatRepository.sendMessage(tradeId, text)
            _sendMessageState.value = result

            if (result is Resource.Success) {
                // Refresh messages or append locally
                fetchMessages(tradeId)
            }
        }
    }

    fun completeTrade(tradeId: String) {
        viewModelScope.launch {
            _completeTradeState.value = Resource.Loading()
            // Using TradeRepository to update the status to completed
            val result = tradeRepository.updateStatus(tradeId, TradeStatus.completed)
            _completeTradeState.value = result

            // If successful, re-fetch trade details to update UI (hide the menu item, update status text)
            if (result is Resource.Success) {
                fetchTradeDetails(tradeId)
            }
        }
    }
}