package com.agroflow.core.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "agroflow_offline_db"
            ).fallbackToDestructiveMigration().build()
            instance = db
            db
        }
    }
}
