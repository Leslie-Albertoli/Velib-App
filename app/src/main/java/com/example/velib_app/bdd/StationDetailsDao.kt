package com.example.velib_app.bdd

/*import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StationDetailsDao {
    @Query("SELECT * FROM stationDetails")
    suspend fun getAllStationDetails(): List<StationDetailsEntity>

//    @Query("SELECT station_id FROM stationDetails")
//    suspend fun getAllIdStationDetails(): List<Long>

    @Query("SELECT * FROM stationDetails WHERE station_id == :station_id_var")
    suspend fun findByStationIdStationDetails(station_id_var: Long): StationDetailsEntity

    @Insert
    suspend fun insertStationDetails(vararg stationDetails: StationDetailsEntity)

    @Delete
    suspend fun deleteStationDetails(stationDetails: StationDetailsEntity)
}*/