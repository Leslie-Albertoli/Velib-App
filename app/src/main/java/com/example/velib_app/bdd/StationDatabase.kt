package com.example.velib_app.bdd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StationEntity::class], version = 3)
abstract class StationDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao

    companion object {
        fun createDatabase(ctx: Context): StationDatabase {
            return Room.databaseBuilder(
                ctx,
                StationDatabase::class.java, "listStation.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}