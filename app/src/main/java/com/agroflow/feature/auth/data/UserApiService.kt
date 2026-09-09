package com.agroflow.feature.auth.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.Response

import com.google.gson.annotations.SerializedName

data class CreateUserRequest(
    @SerializedName("name") val nombre: String,
    @SerializedName("email") val correo: String,
    @SerializedName("password") val contrasena: String,
    @SerializedName("requestedRole") val rolId: String? = null
)

data class CreateUserResponse(
    val id: String,
    val mensaje: String? = null
)

data class UpdateUserRequest(
    val nombre: String,
    val telefono: String = "",
    val direccion: String = "",
    val fotoPerfilUrl: String? = null,
    val correo: String? = null
)

interface UserApiService {
    @POST("api/users/cliente")
    suspend fun createCliente(@Body request: CreateUserRequest): Response<CreateUserResponse>

    @PUT("api/users/profile")
    suspend fun updateProfile(@Body request: UpdateUserRequest): Response<Void>

    @POST("api/users/admin/create")
    suspend fun createAdminUser(@Body request: CreateUserRequest): Response<CreateUserResponse>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body request: UpdateUserRequest): Response<Void>
}
