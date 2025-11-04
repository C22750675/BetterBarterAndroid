package com.hugogarry.betterbarter.ui.item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Category
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddItemFragment : Fragment() {

    private val viewModel: AddItemViewModel by viewModels()

    // Store the fetched categories and the ID of the selection
    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: String? = null

    private lateinit var categoryAutoCompleteTextView: AutoCompleteTextView

    // NEW: Declare date EditTexts
    private lateinit var bestBeforeDateEditText: EditText
    private lateinit var useByDateEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        val itemNameEditText = view.findViewById<EditText>(R.id.editTextItemName)
        val itemDescriptionEditText = view.findViewById<EditText>(R.id.editTextItemDescription)
        val estimatedValueEditText = view.findViewById<EditText>(R.id.editTextEstimatedValue)
        categoryAutoCompleteTextView = view.findViewById(R.id.autoCompleteTextViewCategory)
        bestBeforeDateEditText = view.findViewById(R.id.editTextBestBeforeDate) // NEW
        useByDateEditText = view.findViewById(R.id.editTextUseByDate)         // NEW

        val addButton = view.findViewById<Button>(R.id.buttonAddItem)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarAddItem)
        val errorTextView = view.findViewById<TextView>(R.id.textViewErrorAddItem)

        // Set up click listener for the "List Item for Trade" button
        addButton.setOnClickListener {
            val name = itemNameEditText.text.toString().trim()
            val description = itemDescriptionEditText.text.toString().trim()
            val estimatedValueText = estimatedValueEditText.text.toString().trim()

            // NEW: Get values from optional date fields
            val bestBeforeDateText = bestBeforeDateEditText.text.toString()
            val useByDateText = useByDateEditText.text.toString()

            // Pass all required and optional data to the ViewModel
            viewModel.createItem(
                name = name,
                description = description,
                estimatedValueText = estimatedValueText,
                categoryId = selectedCategoryId,
                bestBeforeDateText = bestBeforeDateText, // NEW
                useByDateText = useByDateText             // NEW
            )
        }

        observeCategoryState(progressBar)
        // Observe the item creation state
        observeAddItemState(progressBar, errorTextView)
    }

    private fun observeCategoryState(progressBar: ProgressBar) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryState.collectLatest { state ->
                // Disable the dropdown until categories are loaded
                categoryAutoCompleteTextView.isEnabled = state is CategoryState.Success

                when (state) {
                    is CategoryState.Success -> {
                        categories = state.categories
                        setupCategoryDropdown(state.categories)
                    }
                    is CategoryState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                    is CategoryState.Loading, CategoryState.Idle -> {
                        // Handled by isEnabled state, no further action needed
                    }
                }
            }
        }
    }

    // Setup dropdown adapter
    private fun setupCategoryDropdown(categories: List<Category>) {
        // Map Category objects to a list of just their names for the adapter
        val categoryNames = categories.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categoryNames
        )
        categoryAutoCompleteTextView.setAdapter(adapter)

        // Listener to capture the selected category's ID
        categoryAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedCategoryName = parent.getItemAtPosition(position) as String
            // Find the corresponding Category object and store its ID
            selectedCategoryId = categories.find { it.name == selectedCategoryName }?.id
        }
    }

    private fun observeAddItemState(progressBar: ProgressBar, errorTextView: TextView) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addItemState.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                errorTextView.isVisible = resource is Resource.Error

                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Item Listed: ${resource.data?.name}", Toast.LENGTH_LONG).show()
                        // Navigate back to the Profile screen after successful creation
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        errorTextView.text = resource.message
                    }
                    is Resource.Loading, is Resource.Idle -> {
                        // UI visibility handled above.
                    }
                }
            }
        }
    }
}