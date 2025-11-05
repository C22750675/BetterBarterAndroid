package com.hugogarry.betterbarter.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var itemsAdapter: ProfileItemsAdapter

    private lateinit var usernameTextView: TextView
    private lateinit var reputationTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var profileImageView: ShapeableImageView

    // Image Picker Launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // REQUIRED FIX: Launch coroutine to convert Uri off the main thread
            viewLifecycleOwner.lifecycleScope.launch {
                val filePart = uriToMultipartBody(it) // Runs in background
                if (filePart != null) {
                    viewModel.uploadProfilePicture(filePart) // Runs on main thread
                } else {
                    Toast.makeText(context, "Could not read selected image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find all views
        usernameTextView = view.findViewById(R.id.textViewUsername)
        reputationTextView = view.findViewById(R.id.textViewReputation)
        bioTextView = view.findViewById(R.id.textViewBio)
        profileImageView = view.findViewById(R.id.imageViewProfile)
        val fabAddItem = view.findViewById<FloatingActionButton>(R.id.fabAddItem)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewProfileItems)

        // Setup RecyclerView
        itemsAdapter = ProfileItemsAdapter()
        recyclerView.adapter = itemsAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Setup FAB click listener
        fabAddItem.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addItemFragment)
        }

        // NEW: Click listener for profile image
        profileImageView.setOnClickListener {
            requestPermissionAndPickImage()
        }

        // Observe UI state from the ViewModel
        observeUiState()
    }

    // Permission Launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission denied to read photos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                // TODO: Handle loading state with a ProgressBar
                // progressBar.isVisible = state.isLoading

                if (state.user != null) {
                    // Call new function to update UI
                    updateProfileUI(state.user)
                }

                itemsAdapter.submitList(state.items)

                if (state.error != null) {
                    Toast.makeText(context, "Error: ${state.error}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Function to bind user data to the UI
    private fun updateProfileUI(user: User) {
        usernameTextView.text = user.username
        // Format reputation score
        reputationTextView.text = getString(R.string.reputation_score_format, user.reputationScore)

        // Handle nullable bio
        if (!user.bio.isNullOrBlank()) {
            bioTextView.text = user.bio
            bioTextView.isVisible = true
        } else {
            bioTextView.isVisible = false
        }

        val baseUrl = com.hugogarry.betterbarter.BuildConfig.BASE_URL.removeSuffix("/api/")
        val fullImageUrl = user.profilePictureUrl?.let { "$baseUrl/api/uploads$it" }

        profileImageView.load(fullImageUrl) {
            placeholder(R.drawable.ic_profile)
            error(R.drawable.ic_profile)
        }
    }

    // Function to handle permissions
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
            // TODO: shouldShowRequestPermissionRationale(...) -> show an explanation
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