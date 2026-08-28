package com.agroflow.feature.inventory.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventario WHERE fincaId = :fincaId AND tipo = :tipo")
    fun getInventoryByFincaAndType(fincaId: String, tipo: String): List<InventoryEntity>

    @Query("SELECT * FROM inventario WHERE fincaId = :fincaId")
    fun getInventoryByFinca(fincaId: String): List<InventoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(items: List<InventoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: InventoryEntity)

    @Query("UPDATE inventario SET cantidad = cantidad - :cantidadUsada, isSyncPending = 1, syncAction = 'UPDATE' WHERE id = :itemId")
    fun discountStock(itemId: String, cantidadUsada: Double)

    @Query("SELECT * FROM inventario WHERE isSyncPending = 1")
    fun getPendingSyncItems(): List<InventoryEntity>

    @Query("UPDATE inventario SET isSyncPending = 0 WHERE id = :itemId")
    fun markAsSynced(itemId: String)

    @Query("SELECT * FROM inventario WHERE id = :itemId LIMIT 1")
    fun getItemById(itemId: String): InventoryEntity?
}
