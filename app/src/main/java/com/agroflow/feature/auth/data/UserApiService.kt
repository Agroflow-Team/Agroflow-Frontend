package com.agroflow.feature.auth.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

data class CreateUserRequest(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rolId: String? = null // For when Agricultor wants to specify role
)

data class CreateUserResponse(
    val id: String,
    val mensaje: String? = null
)

interface UserApiService {
    @POST("api/users/cliente")
    suspend fun createCliente(@Body request: CreateUserRequest): Response<CreateUserResponse>

    @POST("api/users/agricultor/create")
    suspend fun createAgricultor(@Body request: CreateUserRequest): Response<CreateUserResponse>

    @POST("api/users/admin/create")
    suspend fun createAdminUser(@Body request: CreateUserRequest): Response<CreateUserResponse>

    @retrofit2.http.PUT("api/users/{id}")
    suspend fun updateUser(@retrofit2.http.Path("id") id: String, @Body request: UpdateUserRequest): Response<Void>
}

data class UpdateUserRequest(
    val nombre: String,
    val telefono: String,
    val direccion: String,
    val fotoPerfilUrl: String?
)
