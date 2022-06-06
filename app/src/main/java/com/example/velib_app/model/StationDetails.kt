package com.example.velib_app.model

data class StationDetails(
    val station_id: Long,
    val numBikesAvailable: Int,
    val num_bikes_available_types: List<Map<String, Int>>,
    val numDocksAvailable: Int,
    val is_installed: Int,
    val is_returning: Int,
    val is_renting: Int
)