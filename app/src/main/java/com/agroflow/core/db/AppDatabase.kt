package com.agroflow.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agroflow.feature.inventory.data.local.InventoryDao
import com.agroflow.feature.inventory.data.local.InventoryEntity
import com.agroflow.feature.tasks.data.local.TaskDao
import com.agroflow.feature.tasks.data.local.TaskEntity

@Database(entities = [TaskEntity::class, InventoryEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun inventoryDao(): InventoryDao
}
