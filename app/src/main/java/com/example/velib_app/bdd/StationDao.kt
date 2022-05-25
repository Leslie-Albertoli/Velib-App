package com.example.velib_app.bdd

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StationDao {
    @Query("SELECT * FROM station")
    suspend fun getAllSattion(): List<StationEntity>

    @Query("SELECT * FROM station WHERE station_id == :station_id_var")
    suspend fun findByStationIdStation(station_id_var: Long): StationEntity

    @Insert
    suspend fun insertStation(vararg station: StationEntity)

    @Delete
    suspend fun deleteStation(station: StationEntity)
}