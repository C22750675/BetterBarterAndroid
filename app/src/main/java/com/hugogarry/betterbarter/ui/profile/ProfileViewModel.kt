package com.hugogarry.betterbarter.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.UpdateProfileRequest
import com.hugogarry.betterbarter.data.model.User
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.AuthRepository
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.data.repository.UploadRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody


// A data class to hold the combined state of the profile screen
data class ProfileUiState(
    val user: User? = null,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(ApiClient.apiService),
    private val itemRepository: ItemRepository = ItemRepository(ApiClient.apiService),
    private val uploadRepository: UploadRepository = UploadRepository(ApiClient.apiService)
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        fetchProfileData()
    }

    fun fetchProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userResultDeferred = async { authRepository.getProfile() }
            val itemsResultDeferred = async { itemRepository.getMyItems() }

            val userResult = userResultDeferred.await()
            val itemsResult = itemsResultDeferred.await()

            val currentState = _uiState.value
            if (userResult is Resource.Success && itemsResult is Resource.Success) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    user = userResult.data,
                    items = itemsResult.data ?: emptyList(),
                    error = null // Clear previous errors
                )
            } else {
                val userError = if (userResult !is Resource.Success) userResult.message else ""
                val itemError = if (itemsResult !is Resource.Success) itemsResult.message else ""
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = "$userError\n$itemError".trim()
                )
            }
        }
    }

    fun uploadProfilePicture(filePart: MultipartBody.Part) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Step 1: Upload the image file
            when (val uploadResult = uploadRepository.uploadImage(filePart)) {
                is Resource.Success -> {
                    // Step 2: On success, update the user's profile with the new URL
                    val newUrl = uploadResult.data?.url
                    if (newUrl == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = "Upload succeeded but no URL was returned.")
                        return@launch
                    }

                    val dto = UpdateProfileRequest(profilePictureUrl = newUrl)
                    when (val profileResult = authRepository.updateProfile(dto)) {
                        is Resource.Success -> {
                            // Update the UI state with the fresh user object
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                user = profileResult.data,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = profileResult.message)
                        }
                        else -> { /* No-op */ }
                    }
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = uploadResult.message)
                }
                else -> { /* No-op */ }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}