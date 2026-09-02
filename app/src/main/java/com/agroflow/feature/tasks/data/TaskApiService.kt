package com.agroflow.feature.tasks.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskApiService {
    @POST("api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<Task>

    @GET("api/tasks/worker/{trabajadorId}")
    suspend fun getTasksByWorker(@Path("trabajadorId") trabajadorId: String): Response<List<Task>>

    @GET("api/tasks/finca/{fincaId}")
    suspend fun getTasksByFinca(@Path("fincaId") fincaId: String): Response<List<Task>>

    @PATCH("api/tasks/{taskId}/progress")
    suspend fun updateProgress(
        @Path("taskId") taskId: String,
        @Body request: UpdateProgressRequest
    ): Response<Task>
}
