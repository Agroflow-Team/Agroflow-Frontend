package com.agroflow.feature.personnel.data

data class Finca(
    val id: String,
    val nombre: String,
    val fechaRegistro: String? = null
)

data class CreateFincaRequest(
    val nombre: String
)

data class Trabajador(
    val id: String,
    val usuarioId: String,
    val fincaId: String,
    val nombreCompleto: String,
    val documento: String,
    val tarifaHora: Double,
    val fechaRegistro: String? = null
)

data class CreateTrabajadorRequest(
    val fincaId: String,
    val nombreCompleto: String,
    val documento: String,
    val tarifaHora: Double,
    val correo: String,
    val clave: String
)

data class UpdateTrabajadorRequest(
    val nombreCompleto: String,
    val documento: String,
    val tarifaHora: Double
)

data class TrabajadorSummaryResponse(
    val trabajadorId: String,
    val nombreCompleto: String,
    val tarifaHora: Double,
    val totalHorasTrabajadas: Double,
    val salarioEstimado: Double,
    val tareasCompletadas: Int,
    val tareasEnProgreso: Int,
    val tareasPendientes: Int
)
