package com.example.velib_app.bdd

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavorisDao {
    @Query("SELECT * FROM favoris WHERE station_id LIKE :station_id")
    fun findByStationId(station_id: Long): FavorisEntity

    @Insert
    fun insertAll(vararg favoris: FavorisEntity)

    @Delete
    fun delete(favoris: FavorisEntity)
    abstract fun getAll(): List<FavorisEntity>
}
