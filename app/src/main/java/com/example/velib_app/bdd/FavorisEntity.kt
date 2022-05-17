package com.example.velib_app.bdd

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoris")
data class FavorisEntity(
    @PrimaryKey val uid: Int,
    /*@PrimaryKey(autoGenerate = true)
    private int key;*/

    @ColumnInfo(name = "station_id") val station_id: Long?
)
