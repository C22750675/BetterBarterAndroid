package com.hugogarry.betterbarter.ui.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.User
import com.hugogarry.betterbarter.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    // UI Layouts for visibility toggling
    private lateinit var cardBio: View
    private lateinit var layoutEmptyInventory: View
    private lateinit var recyclerView: RecyclerView

    // Image Picker Launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewLifecycleOwner.lifecycleScope.launch {
                val filePart = uriToMultipartBody(it)
                if (filePart != null) {
                    viewModel.uploadProfilePicture(filePart)
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
        val toolbar = view.findViewById<Toolbar>(R.id.toolbarProfile)
        val editProfileImageBadge = view.findViewById<View>(R.id.editProfileImageBadge)

        // Find layouts for visibility logic
        cardBio = view.findViewById(R.id.cardBio)
        layoutEmptyInventory = view.findViewById(R.id.layoutEmptyInventory)
        recyclerView = view.findViewById(R.id.recyclerViewProfileItems)

        // Setup Toolbar Menu for Logout
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    viewModel.logout()
                    // Navigate to auth flow and clear backstack
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.main_flow, true)
                        .build()
                    findNavController().navigate(R.id.auth_flow, null, navOptions)
                    true
                }
                else -> false
            }
        }

        // Setup RecyclerView
        itemsAdapter = ProfileItemsAdapter()
        recyclerView.adapter = itemsAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Setup FAB click listener
        fabAddItem.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addItemFragment)
        }

        // Click listeners for profile image and its edit badge
        profileImageView.setOnClickListener {
            requestPermissionAndPickImage()
        }
        editProfileImageBadge.setOnClickListener {
            requestPermissionAndPickImage()
        }

        // Observe UI state from the ViewModel
        observeUiState()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data whenever the fragment becomes visible (e.g., returning from AddItemFragment)
        viewModel.fetchProfileData()
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
                if (state.user != null) {
                    updateProfileUI(state.user)
                }

                itemsAdapter.submitList(state.items)

                // Toggle empty inventory state
                val isEmpty = state.items.isEmpty()
                layoutEmptyInventory.isVisible = isEmpty
                recyclerView.isVisible = !isEmpty

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

        // Hide the entire card if the bio is empty
        if (!user.bio.isNullOrBlank()) {
            bioTextView.text = user.bio
            cardBio.isVisible = true
        } else {
            cardBio.isVisible = false
        }

        // Use SessionManager for URL
        val currentApiUrl = SessionManager.getServerUrl()
        val baseUrl = currentApiUrl.removeSuffix("api/")
        val fullImageUrl = user.profilePictureUrl?.let { "${baseUrl}api/imageUploads$it" }

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
                var fileName = "image.jpg"
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
                val requestBody = fileBytes.toRequestBody(mimeType?.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("file", fileName, requestBody)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}