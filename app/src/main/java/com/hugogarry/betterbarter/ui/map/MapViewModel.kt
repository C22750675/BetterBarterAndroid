package com.hugogarry.betterbarter.ui.map

import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

class MapViewModel : ViewModel() {

    // Default location (Dublin)
    val defaultLocation = GeoPoint(53.3498, -6.2603)
    val defaultZoom = 12.0

    // Stored state
    var lastKnownLocation: GeoPoint? = null
    var lastKnownZoom: Double? = null
}