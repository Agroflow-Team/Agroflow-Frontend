package com.agroflow.feature.profile.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.RetrofitClient
import com.agroflow.feature.auth.data.UpdateUserRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel : ViewModel() {

    fun updateUserProfile(
        context: Context,
        id: String,
        nombre: String,
        telefono: String,
        direccion: String,
        imageUri: Uri?,
        onSuccess: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var finalUrl: String? = null

                if (imageUri != null && imageUri.scheme == "content") {
                    try {
                        val inputStream = context.contentResolver.openInputStream(imageUri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        
                        val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                        val out = FileOutputStream(file)
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, out)
                        out.flush()
                        out.close()

                        val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, reqFile)
                        
                        val uploadResponse = RetrofitClient.vitrinaApi.uploadImage(body)
                        val baseUrl = com.agroflow.core.session.SessionManager.baseUrl
                        finalUrl = baseUrl + uploadResponse.url.removePrefix("/")
                    } catch (e: Exception) {
                        onError("Error subiendo foto: ${e.message}")
                        return@launch
                    }
                } else if (imageUri != null) {
                    finalUrl = imageUri.toString() // Si ya es una URL web o no es content uri
                }

                val request = UpdateUserRequest(
                    nombre = nombre,
                    telefono = telefono,
                    direccion = direccion,
                    fotoPerfilUrl = finalUrl
                )
                
                val response = RetrofitClient.userApi.updateUser(id, request)
                if (response.isSuccessful) {
                    onSuccess(finalUrl)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onError("Error ${response.code()}: $errorBody")
                }
            } catch (e: Exception) {
                onError("Error de conexión: ${e.message}")
            }
        }
    }
}
