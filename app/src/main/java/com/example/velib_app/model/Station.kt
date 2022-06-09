package com.example.velib_app.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class Station(
    val station_id: Long,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    val stationCode: String,
    val rental_methods: List<String>
): ClusterItem {

    override fun getPosition(): LatLng = LatLng(lat, lon)

    override fun getTitle(): String = name

    override fun getSnippet(): String = stationCode

}