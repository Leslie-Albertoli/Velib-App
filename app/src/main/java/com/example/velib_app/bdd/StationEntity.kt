package com.example.velib_app.bdd

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "station")
data class StationEntity(
    @PrimaryKey
    @ColumnInfo(name = "station_id") val station_id: Long,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "lat") val lat: Double?,
    @ColumnInfo(name = "lon") val lon: Double?,
    @ColumnInfo(name = "capacity") val capacity: Int?,
    @ColumnInfo(name = "stationCode") val stationCode: String?,
    @ColumnInfo(name = "numBikesAvailable") val numBikesAvailable: Int?,
    @ColumnInfo(name = "numBikesAvailableTypesMechanical") val numBikesAvailableTypesMechanical: Int?,
    @ColumnInfo(name = "numBikesAvailableTypesElectrical") val numBikesAvailableTypesElectrical: Int?,
    @ColumnInfo(name = "numDocksAvailable") val numDocksAvailable: Int?,
    @ColumnInfo(name = "is_installed") val is_installed: Int?,
    @ColumnInfo(name = "is_returning") val is_returning: Int?,
    @ColumnInfo(name = "is_renting") val is_renting: Int?
)