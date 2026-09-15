package com.agroflow.core.fcm

import android.util.Log
import com.agroflow.core.RetrofitClient
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.auth.data.UpdateFcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FcmHelper {
    private const val TAG = "FcmHelper"

    fun syncFcmTokenWithBackend(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        val userId = SessionManager.userId ?: return
        scope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                if (!token.isNullOrBlank()) {
                    Log.d(TAG, "Sincronizando token FCM para usuario $userId: ${token.take(15)}...")
                    val response = RetrofitClient.authApi.updateFcmToken(UpdateFcmTokenRequest(userId, token))
                    if (response.isSuccessful) {
                        Log.d(TAG, "Token FCM sincronizado con éxito en el backend.")
                    } else {
                        Log.e(TAG, "Error del servidor al sincronizar FCM token: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo/sincronizando token FCM: ${e.message}")
            }
        }
    }

    fun syncTokenDirectly(token: String, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        val userId = SessionManager.userId ?: return
        scope.launch {
            try {
                val response = RetrofitClient.authApi.updateFcmToken(UpdateFcmTokenRequest(userId, token))
                if (response.isSuccessful) {
                    Log.d(TAG, "Token FCM actualizado en backend tras onNewToken.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando FCM token tras onNewToken: ${e.message}")
            }
        }
    }
}
