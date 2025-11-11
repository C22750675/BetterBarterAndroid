package com.hugogarry.betterbarter.ui.trades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.CreateTradeRequest
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.Trade
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.data.repository.TradeRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateTradeViewModel(
    private val itemRepository: ItemRepository = ItemRepository(),
    private val tradeRepository: TradeRepository = TradeRepository()
) : ViewModel() {

    // Holds the user's items for the dropdown
    private val _myItems = MutableStateFlow<Resource<List<Item>>>(Resource.Idle())
    val myItems: StateFlow<Resource<List<Item>>> = _myItems

    // Holds the state of the trade creation
    private val _createState = MutableStateFlow<Resource<Trade>>(Resource.Idle())
    val createState: StateFlow<Resource<Trade>> = _createState

    init {
        fetchMyItems()
    }

    private fun fetchMyItems() {
        viewModelScope.launch {
            _myItems.value = Resource.Loading()
            _myItems.value = itemRepository.getMyItems()
        }
    }

    fun createTrade(
        selectedItem: Item?,
        circleId: String,
        quantityText: String,
        description: String
    ) {
        if (selectedItem == null) {
            _createState.value = Resource.Error("Please select an item to trade.")
            return
        }

        val quantity = quantityText.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            _createState.value = Resource.Error("Please enter a valid quantity.")
            return
        }

        if (quantity > selectedItem.stock) {
            _createState.value = Resource.Error("Quantity cannot be more than your available stock (${selectedItem.stock}).")
            return
        }

        val request = CreateTradeRequest(
            itemId = selectedItem.id,
            circleId = circleId,
            quantity = quantity,
            description = description.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _createState.value = Resource.Loading()
            _createState.value = tradeRepository.createTrade(request)
        }
    }
}