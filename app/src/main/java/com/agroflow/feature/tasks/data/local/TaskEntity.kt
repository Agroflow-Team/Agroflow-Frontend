package com.agroflow.feature.tasks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val fincaId: String,
    val trabajadorId: String,
    val titulo: String,
    val descripcion: String?,
    val estado: String,
    val novedades: String?,
    val horasEstimadas: Double,
    val horasReales: Double,
    val isSyncPending: Boolean
)
