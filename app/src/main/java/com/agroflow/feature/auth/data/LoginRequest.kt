package com.agroflow.feature.auth.data

data class LoginRequest(
    val correo: String,
    val clave: String,
    val fcmToken: String? = null
)

data class UpdateFcmTokenRequest(
    val usuarioId: String,
    val fcmToken: String
)