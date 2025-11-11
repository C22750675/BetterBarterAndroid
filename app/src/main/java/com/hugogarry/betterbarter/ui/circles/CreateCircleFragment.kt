// In: app/src/main/java/com/hugogarry/betterbarter/ui/circles/CreateCircleFragment.kt

package com.hugogarry.betterbarter.ui.circles

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateCircleFragment : Fragment() {

    private val viewModel: CreateCircleViewModel by viewModels()

    // UI Components
    private lateinit var nameEditText: EditText
    private lateinit var radiusEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var setLocationButton: Button
    private lateinit var createButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var colorSwatchLayout: LinearLayout

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    // Color Picker State
    private var selectedColor: String = "#3498DB" // Default blue
    private var colorSwatches = mutableListOf<View>()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            getCurrentLocation()
        } else {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_circle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Find all views
        nameEditText = view.findViewById(R.id.editTextName)
        radiusEditText = view.findViewById(R.id.editTextRadius)
        descriptionEditText = view.findViewById(R.id.editTextRules)
        setLocationButton = view.findViewById(R.id.buttonSetLocation)
        createButton = view.findViewById(R.id.buttonCreate)
        progressBar = view.findViewById(R.id.progressBarCreate)
        errorTextView = view.findViewById(R.id.textViewError)
        colorSwatchLayout = view.findViewById(R.id.linearLayoutColorSwatches)

        // Setup UI
        setupColorSwatches()

        // Setup Listeners
        setLocationButton.setOnClickListener {
            requestLocationPermission()
        }

        createButton.setOnClickListener {
            val description = descriptionEditText.text.toString()

            viewModel.createCircle(
                name = nameEditText.text.toString().trim(),
                radiusMeters = radiusEditText.text.toString().toIntOrNull(),
                latitude = currentLatitude,
                longitude = currentLongitude,
                color = selectedColor,
                description = description
            )
        }

        observeCreateState()
    }

    private fun observeCreateState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createState.collectLatest { resource ->
                progressBar.isVisible = resource is Resource.Loading
                errorTextView.isVisible = resource is Resource.Error

                when (resource) {
                    is Resource.Success -> {
                        Toast.makeText(context, "Circle '${resource.data?.name}' created!", Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    is Resource.Error -> {
                        errorTextView.text = resource.message
                    }
                    is Resource.Loading, is Resource.Idle -> {
                        // Handled by visibility
                    }
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                setLocationButton.text = "Location Set (At Your Position)"
                Toast.makeText(context, "Location set to current position", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not get location. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Dynamically creates and configures the color swatches.
     */
    private fun setupColorSwatches() {
        // Clear any swatches from XML (if you had them) and the list
        colorSwatchLayout.removeAllViews()
        colorSwatches.clear()

        // Define our colors (Tag = hex string, ResId = drawable)
        val colors = listOf(
            Pair("#3498DB", R.drawable.color_swatch_blue),
            Pair("#E74C3C", R.drawable.color_swatch_red),
            Pair("#2ECC71", R.drawable.color_swatch_green),
            Pair("#9B59B6", R.drawable.color_swatch_purple),
            Pair("#E67E22", R.drawable.color_swatch_orange)
        )

        val swatchSize = (48 * resources.displayMetrics.density).toInt() // 48dp
        val margin = (8 * resources.displayMetrics.density).toInt() // 8dp

        colors.forEach { (hex, drawableRes) ->
            val swatch = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(swatchSize, swatchSize).apply {
                    leftMargin = margin
                    rightMargin = margin
                }
                setBackgroundResource(drawableRes)
                tag = hex
                setOnClickListener {
                    // Update the selection state
                    selectedColor = it.tag as String
                    colorSwatches.forEach { sw -> sw.isSelected = (sw.tag == selectedColor) }
                }
            }
            colorSwatches.add(swatch)
            colorSwatchLayout.addView(swatch)
        }

        // Select the first one (blue) by default
        colorSwatches.firstOrNull()?.isSelected = true
    }
}