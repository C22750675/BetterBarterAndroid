package com.hugogarry.betterbarter.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class MapViewModel(
    private val circleRepository: CircleRepository = CircleRepository(ApiClient.apiService)
) : ViewModel() {

    // Default location (Dublin)
    val defaultLocation = GeoPoint(53.3498, -6.2603)
    val defaultZoom = 12.0

    // Stored state for map position
    var lastMapCenter: GeoPoint? = null
    var lastMapZoom: Double? = null

    // Stored state for the user's actual location
    var userLocation: GeoPoint? = null

    private val _circlesState = MutableStateFlow<Resource<List<Circle>>>(Resource.Idle())
    val circlesState: StateFlow<Resource<List<Circle>>> = _circlesState

    /**
     * Fetches circles from the repository based on a location.
     */
    fun fetchNearbyCircles(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _circlesState.value = Resource.Loading()
            _circlesState.value = circleRepository.findNearbyCircles(latitude, longitude)
        }
    }
}