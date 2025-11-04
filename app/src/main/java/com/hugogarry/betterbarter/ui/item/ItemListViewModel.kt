package com.hugogarry.betterbarter.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ItemListViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    // Private MutableStateFlow that can be updated within the ViewModel
    private val _items = MutableStateFlow<Resource<List<Item>>>(Resource.Loading())

    // Public immutable StateFlow that the UI can observe
    val items: StateFlow<Resource<List<Item>>> = _items

    /**
     * Fetches items for a specific circle.
     * The result is posted to the _items StateFlow.
     */
    fun fetchItemsForCircle(circleId: String) {
        // Use viewModelScope to launch a coroutine that is automatically
        // cancelled when the ViewModel is cleared.
        viewModelScope.launch {
            // Set the state to Loading before making the network call
            _items.value = Resource.Loading()
            // Fetch the data from the repository
            val result = itemRepository.getItemsForCircle(circleId)
            // Post the result to the StateFlow
            _items.value = result
        }
    }
}