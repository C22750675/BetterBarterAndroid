package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.CreateCircleRequest
import com.hugogarry.betterbarter.data.model.RequestPoint
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateCircleViewModel(
    private val circleRepository: CircleRepository = CircleRepository()
) : ViewModel() {

    private val _createState = MutableStateFlow<Resource<Circle>>(Resource.Idle())
    val createState: StateFlow<Resource<Circle>> = _createState

    fun createCircle(
        name: String,
        radiusMeters: Int?,
        latitude: Double?,
        longitude: Double?,
        color: String
    ) {
        if (name.isBlank() || radiusMeters == null || latitude == null || longitude == null) {
            _createState.value = Resource.Error("Please fill in all fields and set a location.")
            return
        }
        if (radiusMeters <= 0) {
            _createState.value = Resource.Error("Radius must be a positive number.")
            return
        }

        // Backend requires [longitude, latitude]
        val originPoint = RequestPoint(coordinates = listOf(longitude, latitude))

        val request = CreateCircleRequest(
            name = name,
            origin = originPoint,
            radius = radiusMeters,
            color = color,

        )

        viewModelScope.launch {
            _createState.value = Resource.Loading()
            _createState.value = circleRepository.createCircle(request)
        }
    }
}