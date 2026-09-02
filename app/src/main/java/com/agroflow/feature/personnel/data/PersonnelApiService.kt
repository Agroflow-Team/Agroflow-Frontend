package com.agroflow.feature.personnel.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PersonnelApiService {
    @GET("api/fincas")
    suspend fun getFincas(): Response<List<Finca>>

    @POST("api/fincas")
    suspend fun createFinca(@Body request: CreateFincaRequest): Response<Finca>

    @GET("api/trabajadores")
    suspend fun getTrabajadores(): Response<List<Trabajador>>

    @GET("api/trabajadores/finca/{fincaId}")
    suspend fun getTrabajadoresByFinca(@Path("fincaId") fincaId: String): Response<List<Trabajador>>

    @POST("api/trabajadores")
    suspend fun createTrabajador(@Body request: CreateTrabajadorRequest): Response<Trabajador>

    @PUT("api/trabajadores/{id}")
    suspend fun updateTrabajador(@Path("id") id: String, @Body request: UpdateTrabajadorRequest): Response<Trabajador>

    @DELETE("api/trabajadores/{id}")
    suspend fun deleteTrabajador(@Path("id") id: String): Response<Void>

    @GET("api/trabajadores/{id}/summary")
    suspend fun getTrabajadorSummary(@Path("id") id: String): Response<TrabajadorSummaryResponse>
}
