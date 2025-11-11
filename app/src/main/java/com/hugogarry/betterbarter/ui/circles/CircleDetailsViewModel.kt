package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CircleDetailsViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val circleRepository = CircleRepository()
    private val itemRepository = ItemRepository()

    // Get the circleId from the navigation arguments
    val circleId: String = savedStateHandle.get<String>("circleId")!!

    // StateFlow for the Circle's details
    private val _circleDetails = MutableStateFlow<Resource<Circle>>(Resource.Idle())
    val circleDetails: StateFlow<Resource<Circle>> = _circleDetails

    // StateFlow for the list of Items in the circle
    private val _circleItems = MutableStateFlow<Resource<List<Item>>>(Resource.Idle())
    val circleItems: StateFlow<Resource<List<Item>>> = _circleItems

    init {
        fetchCircleDetails()
        fetchCircleItems()
    }

    fun fetchCircleDetails() {
        viewModelScope.launch {
            _circleDetails.value = Resource.Loading()
            _circleDetails.value = circleRepository.getCircleDetails(circleId)
        }
    }

    fun fetchCircleItems() {
        viewModelScope.launch {
            _circleItems.value = Resource.Loading()
            _circleItems.value = itemRepository.getItemsForCircle(circleId)
        }
    }
}