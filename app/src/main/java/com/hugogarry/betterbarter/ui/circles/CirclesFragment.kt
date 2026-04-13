package com.hugogarry.betterbarter.ui.circles

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CirclesFragment : Fragment() {

    private val viewModel: CirclesViewModel by viewModels()

    // Adapters
    private lateinit var myCirclesAdapter: CirclesAdapter
    private lateinit var nearbyCirclesAdapter: CirclesAdapter

    // Views
    private lateinit var rvMyCircles: RecyclerView
    private lateinit var rvNearbyCircles: RecyclerView
    private lateinit var pbMyCircles: ProgressBar
    private lateinit var pbNearby: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            getCurrentLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_circles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        rvMyCircles = view.findViewById(R.id.recyclerViewMyCircles)
        rvNearbyCircles = view.findViewById(R.id.recyclerViewNearbyCircles)
        pbMyCircles = view.findViewById(R.id.progressBarMyCircles)
        pbNearby = view.findViewById(R.id.progressBarNearby)
        errorTextView = view.findViewById(R.id.textViewErrorCircles)

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddCircle)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_circlesFragment_to_createCircleFragment)
        }

        setupRecyclerViews()
        observeViewModel()

        // Try to get location for nearby circles
        checkLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        // Refresh My Circles to show newly created or joined circles immediately
        viewModel.fetchMyCircles()

        // Also refresh nearby circles to update member status or show new ones
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }

    private fun setupRecyclerViews() {
        // My Circles
        myCirclesAdapter = CirclesAdapter(showJoinButton = false)
        myCirclesAdapter.onItemClick = { circle ->
            val action = CirclesFragmentDirections
                .actionCirclesFragmentToCircleDetailsFragment(circle.id)
            findNavController().navigate(action)
        }
        rvMyCircles.apply {
            adapter = myCirclesAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Nearby Circles
        nearbyCirclesAdapter = CirclesAdapter(showJoinButton = true)
        nearbyCirclesAdapter.onJoinClick = { circle ->
            viewModel.joinCircle(circle)
        }
        // Allow clicking on a nearby circle to see its details before joining
        nearbyCirclesAdapter.onItemClick = { circle ->
            val action = CirclesFragmentDirections
                .actionCirclesFragmentToCircleDetailsFragment(circle.id)
            findNavController().navigate(action)
        }
        rvNearbyCircles.apply {
            adapter = nearbyCirclesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        // Observe My Circles
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.myCirclesState.collectLatest { resource ->
                pbMyCircles.isVisible = resource is Resource.Loading
                rvMyCircles.isVisible = resource is Resource.Success

                if (resource is Resource.Success) {
                    val circles = resource.data ?: emptyList()
                    myCirclesAdapter.submitList(circles)

                    errorTextView.isVisible = circles.isEmpty()
                    if (circles.isEmpty()) {
                        errorTextView.text = "You haven't joined any circles yet."
                    }
                } else if (resource is Resource.Error) {
                    errorTextView.isVisible = true
                    errorTextView.text = resource.message
                }
            }
        }

        // Observe Nearby Circles
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.nearbyCirclesState.collectLatest { resource ->
                pbNearby.isVisible = resource is Resource.Loading
                rvNearbyCircles.isVisible = resource is Resource.Success

                if (resource is Resource.Success) {
                    val circles = resource.data ?: emptyList()
                    nearbyCirclesAdapter.submitList(circles)
                }
            }
        }

        // Observe Join Action Status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.joinCircleState.collectLatest { resource ->
                if (resource is Resource.Success) {
                    Toast.makeText(context, resource.data, Toast.LENGTH_SHORT).show()
                    viewModel.clearJoinState()
                } else if (resource is Resource.Error) {
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearJoinState()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                viewModel.fetchNearbyCircles(location.latitude, location.longitude)
            }
        }
    }
}