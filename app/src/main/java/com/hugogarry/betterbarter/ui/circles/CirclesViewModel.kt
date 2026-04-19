package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CirclesViewModel(
    private val circleRepository: CircleRepository = CircleRepository(ApiClient.apiService)
) : ViewModel() {

    private val _myCirclesState = MutableStateFlow<Resource<List<Circle>>>(Resource.Idle())
    val myCirclesState: StateFlow<Resource<List<Circle>>> = _myCirclesState

    private val _nearbyCirclesState = MutableStateFlow<Resource<List<Circle>>>(Resource.Idle())
    val nearbyCirclesState: StateFlow<Resource<List<Circle>>> = _nearbyCirclesState

    // New state to track the specific action of joining a circle
    private val _joinCircleState = MutableStateFlow<Resource<String>>(Resource.Idle())
    val joinCircleState: StateFlow<Resource<String>> = _joinCircleState

    init {
        fetchMyCircles()
    }

    fun fetchMyCircles() {
        viewModelScope.launch {
            _myCirclesState.value = Resource.Loading()
            _myCirclesState.value = circleRepository.getMyCircles()
        }
    }

    fun fetchNearbyCircles(lat: Double, lon: Double) {
        viewModelScope.launch {
            _nearbyCirclesState.value = Resource.Loading()
            val result = circleRepository.findNearbyCircles(lat, lon)

            if (result is Resource.Success) {
                // Filter the list: Only show circles where isMember is FALSE
                val allNearby = result.data ?: emptyList()
                val joinableCircles = allNearby.filter { !it.isMember }
                _nearbyCirclesState.value = Resource.Success(joinableCircles)
            } else {
                _nearbyCirclesState.value = result
            }
        }
    }

    fun joinCircle(circle: Circle, lat: Double, lon: Double) {
        viewModelScope.launch {
            _joinCircleState.value = Resource.Loading()
            val result = circleRepository.joinCircle(circle.id, lat, lon)

            if (result is Resource.Success) {
                _joinCircleState.value = Resource.Success("Successfully joined ${circle.name}!")

                // Refresh My Circles to show the new addition
                fetchMyCircles()

                // Remove from the nearby list locally to be snappy
                val currentNearby = (_nearbyCirclesState.value as? Resource.Success)?.data ?: emptyList()
                _nearbyCirclesState.value = Resource.Success(currentNearby.filter { it.id != circle.id })
            } else {
                _joinCircleState.value = Resource.Error(result.message ?: "Failed to join circle")
            }
        }
    }

    // Helper to reset state after the UI has handled the Toast
    fun clearJoinState() {
        _joinCircleState.value = Resource.Idle()
    }
}