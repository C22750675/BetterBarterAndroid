package com.hugogarry.betterbarter.ui.map

import android.Manifest
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
import androidx.core.graphics.toColorInt
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

        // Set a minimum zoom level to prevent the world map from appearing too small/repeated.
        // A zoom of 3.0 or 4.0 ensures the map fills the container width on most devices.
        mapView.minZoomLevel = 4.0

        // Restrict the scrollable area to the actual world bounds.
        // This prevents the user from scrolling infinitely into empty space.
        mapView.setScrollableAreaLimitLatitude(85.0, -85.0, 0)
        mapView.setScrollableAreaLimitLongitude(-180.0, 180.0, 0)

        val mapController = mapView.controller

        // Set map state from ViewModel
        val centerPoint = viewModel.lastMapCenter ?: viewModel.defaultLocation
        val zoomLevel = viewModel.lastMapZoom ?: viewModel.defaultZoom
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

        view.findViewById<FloatingActionButton>(R.id.fab_zoom_in).setOnClickListener {
            mapView.controller.zoomIn()
        }

        view.findViewById<FloatingActionButton>(R.id.fab_zoom_out).setOnClickListener {
            mapView.controller.zoomOut()
        }

        observeCirclesState()

        if (viewModel.userLocation == null) {
            requestLocationPermissions()
        } else {
            updateMyLocationMarker(viewModel.userLocation!!)
            viewModel.fetchNearbyCircles(
                viewModel.userLocation!!.latitude,
                viewModel.userLocation!!.longitude
            )
        }
    }

    private fun observeCirclesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.circlesState.collectLatest { resource ->
                when (resource) {
                    is Resource.Success -> {
                        drawCircles(resource.data)
                    }
                    is Resource.Error -> {
                        Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> { }
                    is Resource.Idle -> { }
                }
            }
        }
    }

    private fun drawCircles(circles: List<Circle>?) {
        mapView.overlays.removeAll(circlePolygons)
        circlePolygons.clear()

        if (circles == null) return

        val sortedCircles = circles.sortedByDescending { it.radius }

        sortedCircles.forEach { circle ->
            val centerPoint = GeoPoint(circle.origin.coordinates[1], circle.origin.coordinates[0])

            val parsedColor = try {
                circle.color.toColorInt()
            } catch (_: IllegalArgumentException) {
                "#3498DB".toColorInt()
            }

            val polygon = Polygon(mapView).apply {
                points = Polygon.pointsAsCircle(centerPoint, circle.radius.toDouble())
                fillColor = Color.argb(40, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                strokeColor = Color.argb(100, Color.red(parsedColor), Color.green(parsedColor), Color.blue(parsedColor))
                strokeWidth = 2.0f
                id = circle.id
                title = circle.name
                setOnClickListener { _, _, _ ->
                    Toast.makeText(context, "Circle: $title", Toast.LENGTH_SHORT).show()
                    true
                }
            }
            circlePolygons.add(polygon)
        }

        mapView.overlays.addAll(circlePolygons)
        mapView.invalidate()
    }

    @SuppressLint("MissingPermission")
    private fun centerMapOnUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = GeoPoint(location.latitude, location.longitude)
                    val targetZoom = 15.0

                    updateMyLocationMarker(userLocation)
                    mapView.controller.animateTo(userLocation, targetZoom, 1000L)

                    viewModel.userLocation = userLocation
                    viewModel.lastMapCenter = userLocation
                    viewModel.lastMapZoom = targetZoom

                    viewModel.fetchNearbyCircles(userLocation.latitude, userLocation.longitude)

                } else {
                    Toast.makeText(context, "Could not determine location.", Toast.LENGTH_SHORT).show()
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
            mapView.overlays.add(myLocationMarker)
        }

        myLocationMarker?.position = position
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
    }

    override fun onPause() {
        super.onPause()
        viewModel.lastMapCenter = mapView.mapCenter as GeoPoint
        viewModel.lastMapZoom = mapView.zoomLevelDouble

        mapView.onPause()
    }
}