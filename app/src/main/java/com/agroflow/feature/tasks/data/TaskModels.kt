package com.agroflow.feature.tasks.data

enum class TaskStatus {
    PENDIENTE, EN_PROGRESO, COMPLETADA, CANCELADA
}

data class Task(
    val id: String? = null,
    val fincaId: String,
    val trabajadorId: String,
    val loteId: String? = null,
    val titulo: String,
    val descripcion: String,
    val fechaAsignacion: String? = null,
    val fechaCompletada: String? = null,
    val horasEstimadas: Double? = null,
    val horasReales: Double? = null,
    val horasInvertidas: Double? = null,
    val estado: TaskStatus = TaskStatus.PENDIENTE,
    val novedades: String? = null,
    val severidadNovedad: String? = null,
    val eliminado: Boolean = false,
    val estadoSincronizacion: String = "PENDIENTE"
) {
    // El backend envía horasInvertidas, el frontend usa horasReales
    val horasEfectivas: Double get() = horasReales ?: horasInvertidas ?: 0.0
}

data class CreateTaskRequest(
    val fincaId: String,
    val trabajadorId: String,
    val loteId: String?,
    val titulo: String,
    val descripcion: String,
    val estado: TaskStatus?
)

data class UpdateProgressRequest(
    val trabajadorId: String,
    val nuevasHoras: Double,
    val novedades: String,
    val severidadNovedad: String? = null,
    val nuevoEstado: TaskStatus
)
