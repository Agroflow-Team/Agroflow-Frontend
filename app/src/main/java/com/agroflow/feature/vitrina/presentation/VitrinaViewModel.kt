package com.agroflow.feature.vitrina.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.RetrofitClient
import com.agroflow.feature.vitrina.data.CreatePublicacionRequest
import com.agroflow.feature.vitrina.data.Publicacion
import com.agroflow.feature.vitrina.data.UpdateEstadoRequest
import com.agroflow.feature.vitrina.data.VitrinaApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

class VitrinaViewModel : ViewModel() {
    private val apiService = RetrofitClient.getRetrofit().create(VitrinaApiService::class.java)

    private val _publicacionesActivas = MutableStateFlow<List<Publicacion>>(emptyList())
    val publicacionesActivas: StateFlow<List<Publicacion>> = _publicacionesActivas

    private val _misPublicaciones = MutableStateFlow<List<Publicacion>>(emptyList())
    val misPublicaciones: StateFlow<List<Publicacion>> = _misPublicaciones

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPublicacionesActivas() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _publicacionesActivas.value = apiService.getPublicacionesActivas()
            } catch (e: Exception) {
                _error.value = "Error al cargar publicaciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMisPublicaciones(fincaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _misPublicaciones.value = apiService.getPublicacionesByFinca(fincaId)
            } catch (e: Exception) {
                _error.value = "Error al cargar mis publicaciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPublicacion(request: CreatePublicacionRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                apiService.createPublicacion(request)
                loadMisPublicaciones(request.fincaId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Error al crear publicación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPublicacionWithImage(
        context: android.content.Context,
        imageUri: android.net.Uri?,
        request: CreatePublicacionRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                var finalUrl = request.imagenUrl
                if (imageUri != null) {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    
                    val file = java.io.File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                    val out = java.io.FileOutputStream(file)
                    // Compress to 50% quality to save space
                    bitmap?.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, out) 
                    out.flush()
                    out.close()

                    val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = okhttp3.MultipartBody.Part.createFormData("file", file.name, reqFile)
                    
                    val uploadResponse = apiService.uploadImage(body)
                    val baseUrl = com.agroflow.core.session.SessionManager.baseUrl
                    finalUrl = baseUrl + uploadResponse.url.removePrefix("/")
                }

                val finalRequest = request.copy(imagenUrl = finalUrl)
                apiService.createPublicacion(finalRequest)
                loadMisPublicaciones(request.fincaId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Error al crear: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsVendida(id: String, fincaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                apiService.updateEstadoPublicacion(id, UpdateEstadoRequest("VENDIDA"))
                loadMisPublicaciones(fincaId)
            } catch (e: Exception) {
                _error.value = "Error al actualizar estado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePublicacion(id: String, request: CreatePublicacionRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                apiService.updatePublicacion(id, request)
                loadMisPublicaciones(request.fincaId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Error al editar publicación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePublicacion(id: String, fincaId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                apiService.deletePublicacion(id)
                loadMisPublicaciones(fincaId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Error al eliminar publicación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
