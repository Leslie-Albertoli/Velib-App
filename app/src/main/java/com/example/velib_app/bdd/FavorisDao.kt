package com.example.velib_app.bdd

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavorisDao {
    @Query("SELECT * FROM favoris")
    suspend fun getAll(): List<FavorisEntity>

    @Query("SELECT station_id FROM favoris")
    suspend fun getAllId(): List<Long>

    @Query("SELECT * FROM favoris WHERE station_id == :station_id_var")
    suspend fun findByStationId(station_id_var: Long): FavorisEntity

    @Insert
    suspend fun insert(vararg favoris: FavorisEntity)

    @Delete
    suspend fun delete(favoris: FavorisEntity)
}
