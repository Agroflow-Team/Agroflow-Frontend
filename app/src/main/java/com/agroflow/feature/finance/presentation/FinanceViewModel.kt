package com.agroflow.feature.finance.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.RetrofitClient
import com.agroflow.feature.finance.data.BalanceResponse
import com.agroflow.feature.finance.data.Transaccion
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class FinanceUiState {
    object Idle : FinanceUiState()
    object Loading : FinanceUiState()
    object Success : FinanceUiState()
    data class Error(val message: String) : FinanceUiState()
}

class FinanceViewModel : ViewModel() {
    var uiState by mutableStateOf<FinanceUiState>(FinanceUiState.Idle)
        private set

    var balance by mutableStateOf<BalanceResponse?>(null)
        private set

    fun loadBalance(fincaId: String) {
        viewModelScope.launch {
            uiState = FinanceUiState.Loading
            try {
                val response = RetrofitClient.financeApi.getBalance(fincaId)
                if (response.isSuccessful) {
                    balance = response.body()
                    uiState = FinanceUiState.Success
                } else {
                    uiState = FinanceUiState.Error("Error al cargar balance: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = FinanceUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun registrarTransaccion(
        fincaId: String,
        tipo: String,
        categoria: String,
        monto: Double,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            uiState = FinanceUiState.Loading
            try {
                val fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val transaccion = Transaccion(
                    fincaId = fincaId,
                    tipoMovimiento = tipo,
                    categoria = categoria,
                    montoTotal = monto,
                    fechaTransaccion = fecha
                )
                val response = RetrofitClient.financeApi.registrarTransaccion(transaccion)
                if (response.isSuccessful) {
                    uiState = FinanceUiState.Success
                    loadBalance(fincaId)
                    onComplete()
                } else {
                    uiState = FinanceUiState.Error("Error al registrar transacción: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = FinanceUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }
    fun exportReport(fincaId: String, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.financeApi.exportBalanceCsv(fincaId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val file = java.io.File(context.cacheDir, "reporte_finca.csv")
                        file.writeBytes(body.bytes())
                        android.widget.Toast.makeText(context, "Reporte guardado en descargas", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Error al descargar reporte", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    android.widget.Toast.makeText(context, "Error: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Error de red: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
