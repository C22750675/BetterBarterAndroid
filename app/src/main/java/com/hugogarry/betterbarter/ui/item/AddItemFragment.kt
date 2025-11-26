package com.hugogarry.betterbarter.ui.item

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.textfield.TextInputLayout
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Category
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.os.Build


class AddItemFragment : Fragment() {

    private val viewModel: AddItemViewModel by viewModels()

    // Store all fetched categories
    private var allCategories: List<Category> = emptyList()
    // The final ID used for the CreateItemRequest
    private var selectedCategoryId: String? = null

    // UI elements for the dropdowns
    private lateinit var parentCategoryAutoCompleteTextView: AutoCompleteTextView
    private lateinit var subCategoryAutoCompleteTextView: AutoCompleteTextView
    private lateinit var subCategoryInputLayout: TextInputLayout // For visibility control

    private lateinit var bestBeforeDateEditText: EditText
    private lateinit var useByDateEditText: EditText

    private lateinit var stockEditText: EditText

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                val filePart = uriToMultipartBody(it)
                if (filePart != null) {
                    viewModel.uploadItemImage(filePart)
                } else {
                    Toast.makeText(context, "Could not read selected image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission denied to read photos", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var selectImageButton: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarAddItem)
        NavigationUI.setupWithNavController(toolbar, findNavController())

        // Initialize UI components
        val itemNameEditText = view.findViewById<EditText>(R.id.editTextItemName)
        val itemDescriptionEditText = view.findViewById<EditText>(R.id.editTextItemDescription)
        val estimatedValueEditText = view.findViewById<EditText>(R.id.editTextEstimatedValue)
        stockEditText = view.findViewById(R.id.editTextStock)

        selectImageButton = view.findViewById(R.id.buttonSelectImage)
        selectImageButton.setOnClickListener {
            requestPermissionAndPickImage()
        }


        // Dropdown Initialization
        parentCategoryAutoCompleteTextView = view.findViewById(R.id.autoCompleteTextViewParentCategory)
        subCategoryAutoCompleteTextView = view.findViewById(R.id.autoCompleteTextViewSubCategory)
        subCategoryInputLayout = view.findViewById(R.id.textInputLayoutSubCategory)

        bestBeforeDateEditText = view.findViewById(R.id.editTextBestBeforeDate)
        useByDateEditText = view.findViewById(R.id.editTextUseByDate)

        val addButton = view.findViewById<Button>(R.id.buttonAddItem)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarAddItem)
        val errorTextView = view.findViewById<TextView>(R.id.textViewErrorAddItem)

        // Set up click listener for the "List Item for Trade" button
        addButton.setOnClickListener {
            val name = itemNameEditText.text.toString().trim()
            val description = itemDescriptionEditText.text.toString().trim()
            val estimatedValueText = estimatedValueEditText.text.toString().trim()
            val bestBeforeDateText = bestBeforeDateEditText.text.toString()
            val useByDateText = useByDateEditText.text.toString()
            val stockText = stockEditText.text.toString().trim()

            viewModel.createItem(
                name = name,
                description = description,
                estimatedValueText = estimatedValueText,
                categoryId = selectedCategoryId,
                bestBeforeDateText = bestBeforeDateText,
                useByDateText = useByDateText,
                stockText = stockText
                // imageUrl is now handled by the ViewModel
            )
        }

        observeCategoryState()
        observeAddItemState(progressBar, errorTextView)
        observeImageUploadState()
    }

    private fun observeCategoryState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categoryState.collectLatest { state ->
                // Enable the parent dropdown only when data is ready
                parentCategoryAutoCompleteTextView.isEnabled = state is CategoryState.Success

                when (state) {
                    is CategoryState.Success -> {
                        allCategories = state.categories
                        setupParentCategoryDropdown(allCategories)
                    }
                    is CategoryState.Error -> {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                    is CategoryState.Loading, CategoryState.Idle -> {
                        // Handled by isEnabled state.
                    }
                }
            }
        }
    }

    private fun setupParentCategoryDropdown(categories: List<Category>) {
        // Filter for top-level categories (parentCategoryId is null)
        val parentCategories = categories.filter { it.parentCategoryId == null }
        val parentCategoryNames = parentCategories.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            parentCategoryNames
        )
        parentCategoryAutoCompleteTextView.setAdapter(adapter)

        // Listener for parent selection to drive cascading logic
        parentCategoryAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            val selectedParent = parentCategories.find { it.name == selectedName }
            val parentId = selectedParent?.id // Get the ID of the selected parent

            // Filter all categories to find the children of the selected parent
            val subcategories = allCategories.filter { it.parentCategoryId == parentId }

            subCategoryAutoCompleteTextView.setText("") // Clear old subcategory selection
            subCategoryAutoCompleteTextView.clearFocus()

            if (subcategories.isNotEmpty()) {
                // REQUIRED REVERSION: If subcategories exist, force selectedCategoryId to null.
                // This makes the form INVALID until the user selects a specific subcategory.
                selectedCategoryId = null

                subCategoryInputLayout.visibility = View.VISIBLE
                setupSubCategoryDropdown(subcategories)
            } else {
                // If NO subcategories exist:
                // Set the final ID to the parent's ID. This is a final selection.
                selectedCategoryId = parentId
                subCategoryInputLayout.visibility = View.GONE
            }
        }
    }

    private fun setupSubCategoryDropdown(subcategories: List<Category>) {
        val subcategoryNames = subcategories.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            subcategoryNames
        )
        subCategoryAutoCompleteTextView.setAdapter(adapter)

        // Listener for subcategory selection to set the final ID
        subCategoryAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            // Overwrite selectedCategoryId with the subcategory's ID
            selectedCategoryId = subcategories.find { it.name == selectedName }?.id
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

    // --- NEW: Observe the image upload state ---
    private fun observeImageUploadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.imageUploadState.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        selectImageButton.text = "Uploading..."
                        selectImageButton.isEnabled = false
                    }
                    is Resource.Success -> {
                        selectImageButton.text = "Image Uploaded!"
                        selectImageButton.isEnabled = true
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        selectImageButton.text = "Upload Failed (Retry?)"
                        selectImageButton.isEnabled = true
                    }
                    is Resource.Idle -> {
                        selectImageButton.text = "Select Item Image (Optional)"
                        selectImageButton.isEnabled = true
                    }
                }
            }
        }
    }

    // --- NEW: Copied from ProfileFragment ---
    private fun requestPermissionAndPickImage() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                imagePickerLauncher.launch("image/*")
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    private suspend fun uriToMultipartBody(uri: Uri): MultipartBody.Part? {
        // Run blocking I/O on a background thread
        return withContext(Dispatchers.IO) {
            try {
                val contentResolver = requireContext().contentResolver

                // 1. Get original file name and mime type
                val mimeType = contentResolver.getType(uri)
                var fileName = "image.jpg" // Default
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }
                }

                // 2. Read the file content into a byte array
                val inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
                val fileBytes = inputStream.readBytes()
                inputStream.close()

                // 3. Create RequestBody and MultipartBody.Part
                val requestBody = fileBytes.toRequestBody(
                    mimeType?.toMediaTypeOrNull()
                )

                MultipartBody.Part.createFormData("file", fileName, requestBody)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}