package com.hugogarry.betterbarter.ui.circles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.data.model.CreateCircleRequest
import com.hugogarry.betterbarter.data.model.RequestPoint
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.CircleRepository
import com.hugogarry.betterbarter.data.repository.UploadRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class CreateCircleViewModel(
    private val circleRepository: CircleRepository = CircleRepository(ApiClient.apiService),
    private val uploadRepository: UploadRepository = UploadRepository(ApiClient.apiService)
) : ViewModel() {

    private val _createState = MutableStateFlow<Resource<Circle>>(Resource.Idle())
    val createState: StateFlow<Resource<Circle>> = _createState

    private val _uploadState = MutableStateFlow<Resource<String>>(Resource.Idle())
    val uploadState: StateFlow<Resource<String>> = _uploadState

    private var currentImageUrl: String? = null

    fun uploadCircleImage(filePart: MultipartBody.Part) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading()
            val result = uploadRepository.uploadImage(filePart)
            if (result is Resource.Success) {
                currentImageUrl = result.data?.url
            }
            _uploadState.value = result.data?.url?.let { Resource.Success(it) }
                ?: Resource.Error(result.message ?: "Upload failed")
        }
    }

    fun createCircle(
        name: String,
        radiusMeters: Int?,
        latitude: Double?,
        longitude: Double?,
        color: String,
        description: String
    ) {
        if (name.isBlank() || description.isBlank() || radiusMeters == null || latitude == null || longitude == null) {
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
            description = description,
            imageUrl = currentImageUrl // Send the image URL to backend
        )

        viewModelScope.launch {
            _createState.value = Resource.Loading()
            _createState.value = circleRepository.createCircle(request)
        }
    }
}