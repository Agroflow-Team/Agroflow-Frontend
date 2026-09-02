package com.agroflow.core

import com.agroflow.core.session.SessionManager
import com.agroflow.feature.auth.data.AuthApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        SessionManager.token?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    fun getRetrofit(): Retrofit {
        val targetUrl = SessionManager.baseUrl
        if (retrofit == null || currentBaseUrl != targetUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(targetUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            currentBaseUrl = targetUrl
            
            // Invalidate cached APIs
            _authApi = null
            _personnelApi = null
            _taskApi = null
            _inventoryApi = null
            _financeApi = null
            _userApi = null
        }
        return retrofit!!
    }

    private var _authApi: AuthApiService? = null
    val authApi: AuthApiService
        get() {
            if (_authApi == null) {
                _authApi = getRetrofit().create(AuthApiService::class.java)
            }
            return _authApi!!
        }

    private var _personnelApi: com.agroflow.feature.personnel.data.PersonnelApiService? = null
    val personnelApi: com.agroflow.feature.personnel.data.PersonnelApiService
        get() {
            if (_personnelApi == null) {
                _personnelApi = getRetrofit().create(com.agroflow.feature.personnel.data.PersonnelApiService::class.java)
            }
            return _personnelApi!!
        }

    private var _taskApi: com.agroflow.feature.tasks.data.TaskApiService? = null
    val taskApi: com.agroflow.feature.tasks.data.TaskApiService
        get() {
            if (_taskApi == null) {
                _taskApi = getRetrofit().create(com.agroflow.feature.tasks.data.TaskApiService::class.java)
            }
            return _taskApi!!
        }

    private var _inventoryApi: com.agroflow.feature.inventory.data.InventoryApiService? = null
    val inventoryApi: com.agroflow.feature.inventory.data.InventoryApiService
        get() {
            if (_inventoryApi == null) {
                _inventoryApi = getRetrofit().create(com.agroflow.feature.inventory.data.InventoryApiService::class.java)
            }
            return _inventoryApi!!
        }

    private var _financeApi: com.agroflow.feature.finance.data.FinanceApiService? = null
    val financeApi: com.agroflow.feature.finance.data.FinanceApiService
        get() {
            if (_financeApi == null) {
                _financeApi = getRetrofit().create(com.agroflow.feature.finance.data.FinanceApiService::class.java)
            }
            return _financeApi!!
        }

    private var _userApi: com.agroflow.feature.auth.data.UserApiService? = null
    val userApi: com.agroflow.feature.auth.data.UserApiService
        get() {
            if (_userApi == null) {
                _userApi = getRetrofit().create(com.agroflow.feature.auth.data.UserApiService::class.java)
            }
            return _userApi!!
        }

    // A simple getter for the legacy "api" reference used in ViewModel
    val api: AuthApiService get() = authApi
}