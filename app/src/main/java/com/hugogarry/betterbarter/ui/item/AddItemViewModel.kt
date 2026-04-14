package com.hugogarry.betterbarter.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Category
import com.hugogarry.betterbarter.data.model.CreateItemRequest
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.model.UploadResponse
import com.hugogarry.betterbarter.data.remote.ApiClient
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.data.repository.UploadRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class CategoryState {
    object Loading : CategoryState()
    data class Success(val categories: List<Category>) : CategoryState()
    data class Error(val message: String) : CategoryState()
    object Idle : CategoryState()
}

class AddItemViewModel(
    private val itemRepository: ItemRepository = ItemRepository(ApiClient.apiService),
    private val uploadRepository: UploadRepository = UploadRepository(ApiClient.apiService)
) : ViewModel() {

    private val _addItemState = MutableStateFlow<Resource<Item>>(Resource.Idle())
    val addItemState: StateFlow<Resource<Item>> = _addItemState

    private val _categoryState = MutableStateFlow<CategoryState>(CategoryState.Idle)
    val categoryState: StateFlow<CategoryState> = _categoryState

    private val _imageUploadState = MutableStateFlow<Resource<UploadResponse>>(Resource.Idle())
    val imageUploadState: StateFlow<Resource<UploadResponse>> = _imageUploadState


    private val _uploadedImageUrl = MutableStateFlow<String?>(null)

    init {
        fetchCategories()
    }

    fun uploadItemImage(filePart: MultipartBody.Part) {
        viewModelScope.launch {
            _imageUploadState.value = Resource.Loading()
            when (val result = uploadRepository.uploadImage(filePart)) {
                is Resource.Success -> {
                    _uploadedImageUrl.value = result.data?.url
                    _imageUploadState.value = result
                }
                is Resource.Error -> {
                    _imageUploadState.value = Resource.Error(result.message ?: "Image upload failed")
                }
                else -> { /* No-op */ }
            }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _categoryState.value = CategoryState.Loading
            when (val result = itemRepository.getCategories()) {
                is Resource.Success -> _categoryState.value = CategoryState.Success(result.data ?: emptyList())
                is Resource.Error -> _categoryState.value = CategoryState.Error(result.message ?: "Unknown error fetching categories")
                else -> _categoryState.value = CategoryState.Error("Failed to fetch categories.")
            }
        }
    }

    /**
     * Validates and submits the new item for creation.
     */
    fun createItem(
        name: String,
        description: String,
        estimatedValueText: String,
        categoryId: String?,
        bestBeforeDateText: String?,
        useByDateText: String?,
        stockText: String
    ) {
        if (name.isBlank() || description.isBlank() || estimatedValueText.isBlank()) {
            _addItemState.value = Resource.Error("Please fill in all required fields.")
            return
        }
        if (categoryId.isNullOrBlank()) {
            _addItemState.value = Resource.Error("Please select an item category.")
            return
        }

        val estimatedValue = estimatedValueText.toDoubleOrNull()
        if (estimatedValue == null || estimatedValue <= 0) {
            _addItemState.value = Resource.Error("Please enter a valid estimated value.")
            return
        }

        val stock = stockText.toIntOrNull()
        if (stock == null || stock < 1) {
            _addItemState.value = Resource.Error("Please enter a valid stock quantity (1 or more).")
            return
        }

        // Clean up optional date strings: trim whitespace and set to null if blank
        val bestBeforeDate = bestBeforeDateText?.trim()?.takeIf { it.isNotBlank() }
        val useByDate = useByDateText?.trim()?.takeIf { it.isNotBlank() }

        val imageUrl = _uploadedImageUrl.value

        viewModelScope.launch {
            _addItemState.value = Resource.Loading()
            val request = CreateItemRequest(
                name = name,
                description = description,
                estimatedValue = estimatedValue,
                categoryId = categoryId,
                bestBeforeDate = bestBeforeDate,
                useByDate = useByDate,
                stock = stock,
                imageUrl = imageUrl
            )
            _addItemState.value = itemRepository.createItem(request)
        }
    }
}