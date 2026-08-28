package com.agroflow.feature.inventory.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventario")
data class InventoryEntity(
    @PrimaryKey
    val id: String,
    val fincaId: String,
    val registradoPorTrabajadorId: String,
    val nombreItem: String,
    val tipo: String,
    val cantidad: Double,
    val unidadMedida: String,
    val costoUnitario: Double,
    val eliminado: Boolean,
    val isSyncPending: Boolean,
    val syncAction: String
)
