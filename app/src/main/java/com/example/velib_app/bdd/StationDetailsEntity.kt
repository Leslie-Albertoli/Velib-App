package com.example.velib_app.bdd

/*import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stationDetails")
data class StationDetailsEntity (
    @PrimaryKey
    @ColumnInfo(name = "station_id") val station_id: Long,
    @ColumnInfo(name = "numBikesAvailable") val numBikesAvailable: Int?,
    @ColumnInfo(name = "num_bikes_available_types") val firstName: List<Map<String, Int>>?,
    @ColumnInfo(name = "numDocksAvailable") val numDocksAvailable: Int?,
    @ColumnInfo(name = "is_installed") val is_installed: Int?,
    @ColumnInfo(name = "is_returning") val is_returning: Int?,
    @ColumnInfo(name = "is_renting") val is_renting: Int?
)*/



/*    fun isStationDetails(stationDetailsDao: StationDetailsDao, stationId: Long): Boolean {
        var isStationDetails = false
        runBlocking {
            val findByStationIdStationDetails: StationDetailsEntity =
                stationDetailsDao.findByStationIdStationDetails(stationId)
            isStationDetails = findByStationIdStationDetails != null
        }
        return isStationDetails
    }

    fun insertStationDetails(
        stationDetailsDao: StationDetailsDao,
        stationId: Long,
        numBikesAvailable: Int?,
        num_bikes_available_types: List<Map<String, Int>>?,
        numDocksAvailable: Int?,
        is_installed: Int?,
        is_returning: Int?,
        is_renting: Int?
    ) {
        val stationIdLongFavorisEntityStationDetails: StationDetailsEntity = StationDetailsEntity(
            stationId,
            numBikesAvailable,
            num_bikes_available_types,
            numDocksAvailable,
            is_installed,
            is_returning,
            is_renting
        )
        runBlocking {
            stationDetailsDao.insertStationDetails(stationIdLongFavorisEntityStationDetails)
        }
    }*/