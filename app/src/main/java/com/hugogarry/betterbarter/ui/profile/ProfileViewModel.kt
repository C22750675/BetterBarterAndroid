package com.hugogarry.betterbarter.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.User
import com.hugogarry.betterbarter.data.repository.AuthRepository
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// A data class to hold the combined state of the profile screen
data class ProfileUiState(
    val user: User? = null,
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        fetchProfileData()
    }

    fun fetchProfileData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)

            // Fetch user and items concurrently for better performance
            val userResultDeferred = async { authRepository.getProfile() }
            val itemsResultDeferred = async { itemRepository.getMyItems() }

            val userResult = userResultDeferred.await()
            val itemsResult = itemsResultDeferred.await()

            if (userResult is Resource.Success && itemsResult is Resource.Success) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    user = userResult.data,
                    items = itemsResult.data ?: emptyList()
                )
            } else {
                // Combine error messages
                val errorMessage = (userResult.message ?: "") + "\n" + (itemsResult.message ?: "")
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    error = errorMessage.trim()
                )
            }
        }
    }
}