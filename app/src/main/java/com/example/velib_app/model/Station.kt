package com.example.velib_app.model

data class Station(
    val station_id: Long,
    val name: String,
    val lat: Double,
    val lon: Double,
    val capacity: Int,
    val station_code: String
) {

}