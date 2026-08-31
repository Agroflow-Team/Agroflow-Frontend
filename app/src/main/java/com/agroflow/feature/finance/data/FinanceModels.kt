package com.agroflow.feature.finance.data


data class Transaccion(
    val fincaId: String,
    val tipoMovimiento: String, // "INGRESO" or "EGRESO"
    val categoria: String,
    val montoTotal: Double,
    val fechaTransaccion: String
)

data class BalanceResponse(
    val totalIngresos: Double,
    val totalEgresos: Double,
    val utilidadNeta: Double,
    val transacciones: List<Transaccion>
)
