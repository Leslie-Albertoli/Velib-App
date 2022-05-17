package com.example.velib_app.bdd

import androidx.room.Database
import androidx.room.RoomDatabase

//@Database(entities = arrayOf(FavorisEntity::class), version = 1)
//@Database(entities = [FavorisEntity::class], version = 1)
abstract class FavorisDatabase : RoomDatabase() {
    abstract fun favorisDao(): FavorisDao
}