package com.example.velib_app.api

import com.example.velib_app.model.Station
import com.example.velib_app.model.StationDetails
import retrofit2.http.GET

interface StationService {

    @GET("station_information.json")
    suspend fun getStations() : GetStationsResults

    @GET("station_status.json")
    suspend fun getStationDetails() : GetStationsDetailsResults
}

data class GetStationsResults(val data: StationResult)

data class GetStationsDetailsResults(val data: StationDetailsResult)

data class StationResult(val stations: List<Station>)

data class StationDetailsResult(val stations: List<StationDetails>)