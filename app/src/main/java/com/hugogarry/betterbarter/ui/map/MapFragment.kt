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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hugogarry.betterbarter.BuildConfig
import com.hugogarry.betterbarter.R
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
    private var myLocationMarker: Marker? = null
    private var pulsePolygon: Polygon? = null
    private var pulseAnimator: ValueAnimator? = null

    // Get a reference to the ViewModel
    private val viewModel: MapViewModel by activityViewModels()
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            centerMapOnUserLocation()
        } else {
            Toast.makeText(context, "Location permission is required to show your position.", Toast.LENGTH_LONG).show()
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

        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller

        // Set map state from ViewModel
        // If a location is saved, use it. Otherwise, use the defaults.
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

        // Only request location if we don't already have one
        if (viewModel.lastKnownLocation == null) {
            requestLocationPermissions()
        } else {
            // If we have a location, just update the marker
            updateMyLocationMarker(viewModel.lastKnownLocation!!)
        }
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

                    // REQUIRED FIX: Save the new state to the ViewModel
                    viewModel.lastKnownLocation = userLocation
                    viewModel.lastKnownZoom = targetZoom
                } else {
                    Toast.makeText(context, "Could not determine your location.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
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
                fillColor = Color.argb(50, 200, 200, 100)
                strokeWidth = 0f
            }
            mapView.overlays.add(pulsePolygon)
            mapView.overlays.add(myLocationMarker)
        }
        myLocationMarker?.position = position
        pulsePolygon?.let { it.points = Polygon.pointsAsCircle(position, 1.0) }
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
        mapView.onPause()
        pulseAnimator?.cancel()
    }
}