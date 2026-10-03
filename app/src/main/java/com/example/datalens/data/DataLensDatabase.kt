package com.example.datalens.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AnalysisEntity::class], version = 1, exportSchema = false)
abstract class DataLensDatabase : RoomDatabase() {

    abstract fun analysisDao(): AnalysisDao

    companion object {
        @Volatile
        private var INSTANCE: DataLensDatabase? = null

        fun getDatabase(context: Context): DataLensDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataLensDatabase::class.java,
                    "datalens_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
