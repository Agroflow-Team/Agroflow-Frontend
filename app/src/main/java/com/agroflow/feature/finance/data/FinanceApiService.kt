package com.agroflow.feature.finance.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FinanceApiService {
    @GET("api/finanzas/finca/{fincaId}")
    suspend fun getBalance(@Path("fincaId") fincaId: String): Response<BalanceResponse>

    @POST("api/finanzas")
    suspend fun registrarTransaccion(@Body transaccion: Transaccion): Response<Unit>

    @GET("api/reports/finca/{fincaId}/balance-csv")
    suspend fun exportBalanceCsv(@Path("fincaId") fincaId: String): Response<okhttp3.ResponseBody>
}
