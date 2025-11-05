package com.hugogarry.betterbarter.ui.map

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hugogarry.betterbarter.BuildConfig
import com.hugogarry.betterbarter.R
import com.hugogarry.betterbarter.data.model.Circle
import com.hugogarry.betterbarter.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class MapFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // User location overlays
    private var myLocationMarker: Marker? = null
    private var pulsePolygon: Polygon? = null
    private var pulseAnimator: ValueAnimator? = null

    private var circlePolygons = mutableListOf<Polygon>()

    // Get a reference to the ViewModel (using activityViewModels)
    private val viewModel: MapViewModel by activityViewModels()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            centerMapOnUserLocation()
        } else {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().load(requireContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()))

        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.map_view)

        // Set up the custom tile source for CARTO Voyager
        val cartoVoyagerSource = object : OnlineTileSourceBase(
            "CARTO Voyager", 1, 19, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
            )
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                        MapTileIndex.getX(pMapTileIndex) + "/" +
                        MapTileIndex.getY(pMapTileIndex) + ".png"
            }
        }
        mapView.setTileSource(cartoVoyagerSource)

        mapView.setBuiltInZoomControls(false)

        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller

        // Set map state from ViewModel
        val centerPoint = viewModel.lastKnownLocation ?: viewModel.defaultLocation
        val zoomLevel = viewModel.lastKnownZoom ?: viewModel.defaultZoom
        mapController.setZoom(zoomLevel)
        mapController.setCenter(centerPoint)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        view.findViewById<FloatingActionButton>(R.id.fab_center_location).setOnClickListener {
            centerMapOnUserLocation()
        }

        observeCirclesState()

        if (viewModel.lastKnownLocation == null) {
            // If we don't have a location, request one
            requestLocationPermissions()
        } else {
            // If we already have a location (e.g., from rotation),
            // update the marker and fetch circles immediately
            updateMyLocationMarker(viewModel.lastKnownLocation!!)
            viewModel.fetchNearbyCircles(
                viewModel.lastKnownLocation!!.latitude,
                viewModel.lastKnownLocation!!.longitude
            )
        }
    }

    private fun observeCirclesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.circlesState.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // We have circles, let's draw them
                        drawCircles(resource.data)
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        // You could show a loading indicator here
                    }
                    is Resource.Idle -> {
                        // Nothing to do
                    }
                }
            }
        }
    }

    private fun drawCircles(circles: List<Circle>?) {
        // Clear all old circle overlays
        mapView.overlays.removeAll(circlePolygons)
        circlePolygons.clear()

        if (circles == null) return

        // Loop through each circle and create overlays
        circles.forEach { circle ->
            // Backend uses [longitude, latitude], GeoPoint uses (latitude, longitude)
            val centerPoint = GeoPoint(circle.origin.coordinates[1], circle.origin.coordinates[0])

            // Parse the color
            val parsedColor = try {
                Color.parseColor(circle.color)
            } catch (e: IllegalArgumentException) {
                Color.parseColor("#3498DB") // Default blue on error
            }

            // 4. Create the radius polygon
            val polygon = Polygon(mapView).apply {
                points = Polygon.pointsAsCircle(centerPoint, circle.radius.toDouble())
                // Set fill and stroke based on the circle's color
                fillColor = Color.argb(40, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                strokeColor = Color.argb(100, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                strokeWidth = 2.0f

                // You can still add info for when the polygon is clicked
                // This is just an example
                id = circle.id
                title = circle.name
                setOnClickListener { _, _, _ ->
                    Toast.makeText(context, "Circle: $title", Toast.LENGTH_SHORT).show()
                    true
                }
            }

            //  Add to our tracking list
            circlePolygons.add(polygon)
        }

        //  Add new overlays to the map
        mapView.overlays.addAll(circlePolygons)
        mapView.invalidate() // Redraw the map
    }

    @SuppressLint("MissingPermission")
    private fun centerMapOnUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = GeoPoint(location.latitude, location.longitude)
                    val targetZoom = 18.0

                    updateMyLocationMarker(userLocation)
                    mapView.controller.animateTo(userLocation, targetZoom, 1000L)

                    // Save state to ViewModel
                    viewModel.lastKnownLocation = userLocation
                    viewModel.lastKnownZoom = targetZoom

                    viewModel.fetchNearbyCircles(userLocation.latitude, userLocation.longitude)

                } else {
                    Toast.makeText(context, "Could not determine location.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Request permissions if not granted
            requestLocationPermissions()
        }
    }

    private fun updateMyLocationMarker(position: GeoPoint) {
        if (myLocationMarker == null) {
            myLocationMarker = Marker(mapView).apply {
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_user_location_dot)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            pulsePolygon = Polygon(mapView).apply {
                fillColor = Color.argb(50, 200, 200, 100) // Example pulse color
                strokeWidth = 0f
            }
            // Add pulse *before* marker so marker is on top
            mapView.overlays.add(pulsePolygon)
            mapView.overlays.add(myLocationMarker)
        }
        myLocationMarker?.position = position
        pulsePolygon?.let { it.points = Polygon.pointsAsCircle(position, 1.0) } // Small pulse
        mapView.invalidate()
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        pulseAnimator?.start()
    }

    override fun onPause() {
        super.onPause()
        // Save the map's current state to the ViewModel
        viewModel.lastKnownLocation = mapView.mapCenter as GeoPoint
        viewModel.lastKnownZoom = mapView.zoomLevelDouble

        mapView.onPause()
        pulseAnimator?.cancel()
    }
}