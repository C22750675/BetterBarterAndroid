package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeStatus
import com.hugogarry.betterbarter.data.model.UpdateTradeStatusRequest
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TradesViewModel(
    private val tradeRepository: TradeRepository = TradeRepository(ApiClient.apiService)
) : ViewModel() {

    private val _trades = MutableStateFlow<Resource<List<Trade>>>(Resource.Idle())
    val trades: StateFlow<Resource<List<Trade>>> = _trades

    private val _actionStatus = MutableStateFlow<Resource<String>>(Resource.Idle())
    val actionStatus: StateFlow<Resource<String>> = _actionStatus

    fun fetchMyTrades() {
        viewModelScope.launch {
            _trades.value = Resource.Loading()
            _trades.value = tradeRepository.getMyTrades()
        }
    }

    fun updateStatus(trade: Trade, newStatus: TradeStatus) {
        viewModelScope.launch {
            _actionStatus.value = Resource.Loading()
            val request = UpdateTradeStatusRequest(newStatus)
            val result = tradeRepository.updateTradeStatus(trade.id, request)

            if (result is Resource.Success) {
                _actionStatus.value = Resource.Success("Trade Updated!")
                fetchMyTrades()
            } else {
                _actionStatus.value = Resource.Error(result.message ?: "Error")
            }
        }
    }

    fun deleteTrade(tradeId: String) {
        viewModelScope.launch {
            _actionStatus.value = Resource.Loading()
            val result = tradeRepository.deleteTrade(tradeId)
            if (result is Resource.Success) {
                _actionStatus.value = Resource.Success("Trade proposal deleted.")
                fetchMyTrades() // Refresh list
            } else {
                _actionStatus.value = Resource.Error(result.message ?: "Failed to delete trade")
            }
        }
    }

    // Reset status message after showing Toast
    fun clearStatus() {
        _actionStatus.value = Resource.Idle()
    }

    // Clear list error state to prevent queued Toasts on back navigation
    fun clearError() {
        if (_trades.value is Resource.Error) {
            _trades.value = Resource.Idle()
        }
    }
}