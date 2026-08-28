package com.agroflow.feature.auth.data

data class LoginRequest(
    val correo: String,
    val clave: String
)