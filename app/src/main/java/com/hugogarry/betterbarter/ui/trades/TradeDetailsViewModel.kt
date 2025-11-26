package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TradeDetailsViewModel(
    private val tradeRepository: TradeRepository = TradeRepository()
) : ViewModel() {

    private val _tradeState = MutableStateFlow<Resource<Trade>>(Resource.Idle())
    val tradeState: StateFlow<Resource<Trade>> = _tradeState

    fun fetchTrade(tradeId: String) {
        viewModelScope.launch {
            _tradeState.value = Resource.Loading()
            _tradeState.value = tradeRepository.getTrade(tradeId)
        }
    }
}