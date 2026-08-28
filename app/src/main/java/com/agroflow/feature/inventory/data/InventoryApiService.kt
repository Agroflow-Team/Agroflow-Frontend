package com.agroflow.feature.inventory.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface InventoryApiService {
    @POST("api/inventory")
    suspend fun addItem(@Body request: CreateInventoryItemRequest): Response<InventoryItem>

    @GET("api/inventory/finca/{fincaId}")
    suspend fun getInventoryByFinca(@Path("fincaId") fincaId: String): Response<List<InventoryItem>>

    @PATCH("api/inventory/{itemId}/stock")
    suspend fun updateStock(
        @Path("itemId") itemId: String,
        @Body request: UpdateStockRequest
    ): Response<InventoryItem>

    @retrofit2.http.PUT("api/inventory/{itemId}")
    suspend fun editItem(
        @Path("itemId") itemId: String,
        @Body request: UpdateInventoryItemRequest
    ): Response<InventoryItem>
}
