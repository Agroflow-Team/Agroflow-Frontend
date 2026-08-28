package com.agroflow.feature.auth.data

data class LoginResponse(
    val usuarioId: String,
    val correo: String,
    val rolId: String,
    val token: String
)