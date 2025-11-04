package com.hugogarry.betterbarter.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hugogarry.betterbarter.data.model.Category
import com.hugogarry.betterbarter.data.model.CreateItemRequest
import com.hugogarry.betterbarter.data.model.Item
import com.hugogarry.betterbarter.data.repository.ItemRepository
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CategoryState {
    object Loading : CategoryState()
    data class Success(val categories: List<Category>) : CategoryState()
    data class Error(val message: String) : CategoryState()
    object Idle : CategoryState()
}

class AddItemViewModel(
    private val itemRepository: ItemRepository = ItemRepository()
) : ViewModel() {

    private val _addItemState = MutableStateFlow<Resource<Item>>(Resource.Idle())
    val addItemState: StateFlow<Resource<Item>> = _addItemState

    private val _categoryState = MutableStateFlow<CategoryState>(CategoryState.Idle)
    val categoryState: StateFlow<CategoryState> = _categoryState

    init {
        fetchCategories()
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
     * UPDATED: Now includes bestBeforeDateText and useByDateText for optional date inputs.
     */
    fun createItem(
        name: String,
        description: String,
        estimatedValueText: String,
        categoryId: String?,
        bestBeforeDateText: String?, // NEW PARAMETER
        useByDateText: String? // NEW PARAMETER
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

        // Clean up optional date strings: trim whitespace and set to null if blank
        val bestBeforeDate = bestBeforeDateText?.trim()?.takeIf { it.isNotBlank() }
        val useByDate = useByDateText?.trim()?.takeIf { it.isNotBlank() }

        viewModelScope.launch {
            _addItemState.value = Resource.Loading()
            val request = CreateItemRequest(
                name = name,
                description = description,
                estimatedValue = estimatedValue,
                categoryId = categoryId,
                bestBeforeDate = bestBeforeDate, // Use the cleaned optional value
                useByDate = useByDate // Use the cleaned optional value
            )
            _addItemState.value = itemRepository.createItem(request)
        }
    }
}