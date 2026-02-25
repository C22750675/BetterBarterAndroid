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

    // Dedicated state for rating submissions
    private val _ratingState = MutableStateFlow<Resource<Boolean>>(Resource.Idle())
    val ratingState: StateFlow<Resource<Boolean>> = _ratingState

    fun fetchTrade(tradeId: String) {
        viewModelScope.launch {
            _tradeState.value = Resource.Loading()
            _tradeState.value = tradeRepository.getTrade(tradeId)
        }
    }

    fun rateTrade(tradeId: String, score: Int, comment: String) {
        viewModelScope.launch {
            if (score !in 1..5) {
                _ratingState.value = Resource.Error("Score must be between 1 and 5")
                return@launch
            }

            _ratingState.value = Resource.Loading()

            // Call the repository to submit the rating
            val result = tradeRepository.rateTrade(tradeId, score, comment)
            _ratingState.value = result

            // Refresh the trade if the rating was successful
            // (this fetches updated rep scores and status)
            if (result is Resource.Success) {
                fetchTrade(tradeId)
            }
        }
    }
}