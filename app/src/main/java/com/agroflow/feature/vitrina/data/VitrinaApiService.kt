package com.agroflow.feature.vitrina.data

import retrofit2.http.*

interface VitrinaApiService {
    @GET("api/vitrina")
    suspend fun getPublicacionesActivas(): List<Publicacion>

    @GET("api/vitrina/finca/{fincaId}")
    suspend fun getPublicacionesByFinca(@Path("fincaId") fincaId: String): List<Publicacion>

    @POST("api/vitrina")
    suspend fun createPublicacion(@Body request: CreatePublicacionRequest): Publicacion

    @PATCH("api/vitrina/{id}/estado")
    suspend fun updateEstadoPublicacion(
        @Path("id") id: String,
        @Body request: UpdateEstadoRequest
    ): Publicacion

    @PUT("api/vitrina/{id}")
    suspend fun updatePublicacion(
        @Path("id") id: String,
        @Body request: CreatePublicacionRequest
    ): Publicacion

    @DELETE("api/vitrina/{id}")
    suspend fun deletePublicacion(@Path("id") id: String)
}
