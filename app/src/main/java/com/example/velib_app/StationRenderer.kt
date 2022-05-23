package com.example.velib_app

import android.content.Context
import android.view.LayoutInflater
import androidx.core.content.ContextCompat.getSystemService
import com.example.velib_app.model.Station
import com.example.velib_app.utils.BitmapHelper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.BubbleIconFactory
import com.google.maps.android.ui.IconGenerator

class StationRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Station>
): DefaultClusterRenderer<Station>(context, map, clusterManager) {

    private val customIcon: BitmapDescriptor by lazy {
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.marker_image,
        )
    }


    override fun onBeforeClusterItemRendered(item: Station, markerOptions: MarkerOptions) {
        markerOptions.title(item.name)
            .position(LatLng(item.lat, item.lon))
            .icon(customIcon) // BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN + 21)
    }

    override fun onClusterItemRendered(clusterItem: Station, marker: Marker) {
        marker.tag = clusterItem
    }



}