package com.agroflow.feature.vitrina.data

data class Publicacion(
    val id: String,
    val fincaId: String,
    val tituloProducto: String,
    val descripcion: String?,
    val precio: Double,
    val cantidadDisponible: Double,
    val imagenUrl: String?,
    val estadoPublicacion: String,
    // Optional details that might come from finca for map
    val latitud: Double? = null,
    val longitud: Double? = null,
    val telefonoContacto: String? = null
)

data class CreatePublicacionRequest(
    val fincaId: String,
    val tituloProducto: String,
    val descripcion: String?,
    val precio: Double,
    val cantidadDisponible: Double,
    val imagenUrl: String?
)

data class UpdateEstadoRequest(
    val estado: String
)
