package com.example.velib_app

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : SupportMapFragment(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null

    fun MyMapFragment() {
        getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        // Set default position
        // Add a marker in Sydney and move the camera
        val vietnam = LatLng(14.0583, 108.2772) // 14.0583° N, 108.2772° E
        googleMap!!.addMarker(MarkerOptions().position(vietnam).title("Marker in Vietnam"))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLng(vietnam))
        googleMap?.setOnMapClickListener { latLng ->
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title(latLng.latitude.toString() + " : " + latLng.longitude)
            // Clear previously click position.
            googleMap?.clear()
            // Zoom the Marker
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
            // Add Marker on Map
            googleMap?.addMarker(markerOptions)
        }
    }

}