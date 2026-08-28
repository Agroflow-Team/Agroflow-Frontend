package com.agroflow.feature.personnel.data

data class Finca(
    val id: String,
    val nombre: String,
    val fechaRegistro: String? = null
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
