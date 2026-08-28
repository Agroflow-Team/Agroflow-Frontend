package com.agroflow.feature.personnel.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PersonnelApiService {
    @GET("api/fincas")
    suspend fun getFincas(): Response<List<Finca>>

    @GET("api/trabajadores")
    suspend fun getTrabajadores(): Response<List<Trabajador>>

    @GET("api/trabajadores/finca/{fincaId}")
    suspend fun getTrabajadoresByFinca(@Path("fincaId") fincaId: String): Response<List<Trabajador>>
}
