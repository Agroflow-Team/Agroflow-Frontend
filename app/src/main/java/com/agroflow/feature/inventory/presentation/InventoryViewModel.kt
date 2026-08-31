package com.agroflow.feature.inventory.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agroflow.core.RetrofitClient
import com.agroflow.feature.inventory.data.CreateInventoryItemRequest
import com.agroflow.feature.inventory.data.InventoryItem
import com.agroflow.feature.inventory.data.UpdateStockRequest
import kotlinx.coroutines.launch

sealed class InventoryUiState {
    object Idle : InventoryUiState()
    object Loading : InventoryUiState()
    object Success : InventoryUiState()
    data class Error(val message: String) : InventoryUiState()
}

class InventoryViewModel : ViewModel() {
    var uiState by mutableStateOf<InventoryUiState>(InventoryUiState.Idle)
        private set

    var items by mutableStateOf<List<InventoryItem>>(emptyList())
        private set

    fun loadInventory(fincaId: String) {
        viewModelScope.launch {
            android.util.Log.d("InventoryViewModel", "loadInventory called for fincaId: $fincaId")
            uiState = InventoryUiState.Loading
            try {
                android.util.Log.d("InventoryViewModel", "Making API request to getInventoryByFinca")
                val response = RetrofitClient.inventoryApi.getInventoryByFinca(fincaId)
                android.util.Log.d("InventoryViewModel", "API response received: ${response.code()}")
                if (response.isSuccessful) {
                    items = response.body() ?: emptyList()
                    android.util.Log.d("InventoryViewModel", "Loaded ${items.size} items")
                    uiState = InventoryUiState.Success
                } else {
                    android.util.Log.e("InventoryViewModel", "API Error: ${response.errorBody()?.string()}")
                    uiState = InventoryUiState.Error("Error al cargar inventario: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("InventoryViewModel", "Exception: ${e.message}", e)
                uiState = InventoryUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun addItem(request: CreateInventoryItemRequest, onComplete: () -> Unit) {
        viewModelScope.launch {
            uiState = InventoryUiState.Loading
            try {
                val response = RetrofitClient.inventoryApi.addItem(request)
                if (response.isSuccessful) {
                    uiState = InventoryUiState.Success
                    loadInventory(request.fincaId)
                    onComplete()
                } else {
                    uiState = InventoryUiState.Error("Error al crear ítem: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = InventoryUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun updateStock(fincaId: String, itemId: String, request: UpdateStockRequest, onComplete: () -> Unit) {
        viewModelScope.launch {
            uiState = InventoryUiState.Loading
            try {
                val response = RetrofitClient.inventoryApi.updateStock(itemId, request)
                if (response.isSuccessful) {
                    uiState = InventoryUiState.Success
                    loadInventory(fincaId)
                    onComplete()
                } else {
                    uiState = InventoryUiState.Error("Error al descontar stock: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = InventoryUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun editItem(fincaId: String, itemId: String, request: com.agroflow.feature.inventory.data.UpdateInventoryItemRequest, onComplete: () -> Unit) {
        viewModelScope.launch {
            uiState = InventoryUiState.Loading
            try {
                val response = RetrofitClient.inventoryApi.editItem(itemId, request)
                if (response.isSuccessful) {
                    uiState = InventoryUiState.Success
                    loadInventory(fincaId)
                    onComplete()
                } else {
                    uiState = InventoryUiState.Error("Error al editar ítem: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = InventoryUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun deleteItem(fincaId: String, itemId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            uiState = InventoryUiState.Loading
            try {
                val response = RetrofitClient.inventoryApi.deleteItem(itemId)
                if (response.isSuccessful) {
                    uiState = InventoryUiState.Success
                    loadInventory(fincaId)
                    onComplete()
                } else {
                    uiState = InventoryUiState.Error("Error al eliminar ítem: ${response.code()}")
                }
            } catch (e: Exception) {
                uiState = InventoryUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }
}
