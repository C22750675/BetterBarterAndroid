package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.TradeApplication
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ApplyTradeViewModel(
    private val itemRepository: ItemRepository = ItemRepository(),
    private val tradeRepository: TradeRepository = TradeRepository()
) : ViewModel() {

    private val _myItems = MutableStateFlow<Resource<List<Item>>>(Resource.Idle())
    val myItems: StateFlow<Resource<List<Item>>> = _myItems

    private val _applyState = MutableStateFlow<Resource<TradeApplication>>(Resource.Idle())
    val applyState: StateFlow<Resource<TradeApplication>> = _applyState

    // Holds the target trade details for value comparison
    private val _targetTrade = MutableStateFlow<Resource<Trade>>(Resource.Idle())
    val targetTrade: StateFlow<Resource<Trade>> = _targetTrade

    init {
        fetchMyItems()
    }

    private fun fetchMyItems() {
        viewModelScope.launch {
            _myItems.value = Resource.Loading()
            _myItems.value = itemRepository.getMyItems()
        }
    }

    fun fetchTargetTrade(tradeId: String) {
        viewModelScope.launch {
            _targetTrade.value = Resource.Loading()
            _targetTrade.value = tradeRepository.getTrade(tradeId)
        }
    }

    fun submitApplication(tradeId: String, selectedItem: Item?, quantityText: String, message: String) {
        if (selectedItem == null) {
            _applyState.value = Resource.Error("Please select an item to offer.")
            return
        }

        val quantity = quantityText.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            _applyState.value = Resource.Error("Please enter a valid quantity.")
            return
        }

        if (quantity > selectedItem.stock) {
            _applyState.value = Resource.Error("You only have ${selectedItem.stock} of this item.")
            return
        }

        viewModelScope.launch {
            _applyState.value = Resource.Loading()
            _applyState.value = tradeRepository.applyForTrade(tradeId, selectedItem.id, quantity, message)
        }
    }
}