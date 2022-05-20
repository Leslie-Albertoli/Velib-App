package com.example.velib_app

import android.content.Context
import com.example.velib_app.model.Station
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class StationRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Station>
): DefaultClusterRenderer<Station>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: Station, markerOptions: MarkerOptions) {
        markerOptions.title(item.name)
            .position(LatLng(item.lat, item.lon))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN + 21))
    }

    override fun onClusterItemRendered(clusterItem: Station, marker: Marker) {
        marker.tag = clusterItem
    }
}