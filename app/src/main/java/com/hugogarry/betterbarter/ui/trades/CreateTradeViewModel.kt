package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.CreateTradeRequest
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.model.UpdateTradeRequest
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateTradeViewModel(
    private val itemRepository: ItemRepository = ItemRepository(ApiClient.apiService),
    private val tradeRepository: TradeRepository = TradeRepository(ApiClient.apiService)
) : ViewModel() {

    private val _myItems = MutableStateFlow<Resource<List<Item>>>(Resource.Idle())
    val myItems: StateFlow<Resource<List<Item>>> = _myItems

    private val _existingTrade = MutableStateFlow<Resource<Trade>>(Resource.Idle())
    val existingTrade: StateFlow<Resource<Trade>> = _existingTrade

    private val _actionState = MutableStateFlow<Resource<Trade>>(Resource.Idle())
    val actionState: StateFlow<Resource<Trade>> = _actionState

    init {
        fetchMyItems()
    }

    private fun fetchMyItems() {
        viewModelScope.launch {
            _myItems.value = Resource.Loading()
            _myItems.value = itemRepository.getMyItems()
        }
    }

    fun fetchTradeForEditing(tradeId: String) {
        viewModelScope.launch {
            _existingTrade.value = Resource.Loading()
            _existingTrade.value = tradeRepository.getTrade(tradeId)
        }
    }

    fun createTrade(selectedItem: Item?, circleId: String, quantityText: String, description: String) {
        if (!validate(selectedItem, quantityText)) return

        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val request = CreateTradeRequest(
                itemId = selectedItem!!.id,
                circleId = circleId,
                quantity = quantityText.toInt(),
                description = description
            )
            _actionState.value = tradeRepository.createTrade(request)
        }
    }

    fun updateTrade(tradeId: String, selectedItem: Item?, quantityText: String, description: String) {
        if (!validate(selectedItem, quantityText)) return

        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val request = UpdateTradeRequest(
                itemId = selectedItem!!.id,
                quantity = quantityText.toInt(),
                description = description
            )
            _actionState.value = tradeRepository.updateTrade(tradeId, request)
        }
    }

    private fun validate(selectedItem: Item?, quantityText: String): Boolean {
        if (selectedItem == null) {
            _actionState.value = Resource.Error("Please select an item.")
            return false
        }
        val q = quantityText.toIntOrNull()
        if (q == null || q <= 0) {
            _actionState.value = Resource.Error("Enter a valid quantity.")
            return false
        }
        if (q > selectedItem.stock) {
            _actionState.value = Resource.Error("Insufficient stock.")
            return false
        }
        return true
    }
}