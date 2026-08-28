package com.agroflow.feature.inventory.data

enum class TipoItemEnum {
    INSUMO, HERRAMIENTA, MAQUINARIA, SEMILLA
}

data class InventoryItem(
    val id: String? = null,
    val fincaId: String,
    val registradoPorTrabajadorId: String,
    val nombreItem: String,
    val tipo: TipoItemEnum,
    val cantidad: Double,
    val unidadMedida: String,
    val fechaActualizacion: String? = null,
    val eliminado: Boolean = false,
    val costoUnitario: Double? = null,
    val estadoSincronizacion: String = "PENDIENTE"
)

data class CreateInventoryItemRequest(
    val fincaId: String,
    val registradoPorTrabajadorId: String,
    val nombreItem: String,
    val tipo: TipoItemEnum,
    val cantidad: Double,
    val unidadMedida: String,
    val costoUnitario: Double? = null
)

data class UpdateStockRequest(
    val cantidadUsada: Double
)

data class UpdateInventoryItemRequest(
    val nombreItem: String,
    val cantidad: Double,
    val unidadMedida: String
)
