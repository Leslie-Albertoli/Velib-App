package com.example.velib_app.bdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavorisEntity::class], version = 1)
abstract class FavorisDatabase : RoomDatabase() {
    abstract fun favorisDao(): FavorisDao


    companion object {
        fun createDatabase (ctx : Context): FavorisDatabase {
             return Room.databaseBuilder(
                ctx,
                FavorisDatabase::class.java, "listFavoris.db"
            ).build()
        }
    }
}