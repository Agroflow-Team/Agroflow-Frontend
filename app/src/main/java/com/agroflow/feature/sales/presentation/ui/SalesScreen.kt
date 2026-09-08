package com.agroflow.feature.sales.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.feature.finance.presentation.FinanceUiState
import com.agroflow.feature.finance.presentation.FinanceViewModel
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SalesScreen(
    personnelViewModel: PersonnelViewModel,
    financeViewModel: FinanceViewModel = viewModel()
) {
    val finca = personnelViewModel.selectedFinca

    LaunchedEffect(finca) {
        if (finca != null) {
            financeViewModel.loadBalance(finca.id)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (finca == null) {
            Text(
                "Selecciona una finca",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            return@Column
        }

        Text(
            "Registro de Ventas",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        if (financeViewModel.uiState is FinanceUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (financeViewModel.uiState is FinanceUiState.Error) {
            Text(
                text = (financeViewModel.uiState as FinanceUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(onClick = { financeViewModel.loadBalance(finca.id) }) {
                Text("Reintentar")
            }
        }

        val balance = financeViewModel.balance
        if (balance != null) {
            val salesTransactions = balance.transacciones.filter { it.tipoMovimiento == "INGRESO" }
            val totalSales = salesTransactions.sumOf { it.montoTotal }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Total Ventas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(totalSales),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF30D158) // Apple Green
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)
            ) {
                Text("Registrar Venta", color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Historial de Ventas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                items(salesTransactions.reversed()) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = tx.categoria,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tx.fechaTransaccion.take(10),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Text(
                                text = "+ ${formatCurrency(tx.montoTotal)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF30D158) // Apple Green
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog && finca != null) {
        var productName by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }

        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nueva Venta", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextField(
                        value = productName,
                        onValueChange = { productName = it },
                        placeholder = { Text("Producto (ej. Aguacate Hass)") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        placeholder = { Text("Cantidad (ej. 100 kg)") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = amount,
                        onValueChange = { amount = it },
                        placeholder = { Text("Monto Total") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                        val categoria = if (quantity.isNotBlank()) "$productName ($quantity)" else productName
                        financeViewModel.registrarTransaccion(finca.id, "INGRESO", categoria, parsedAmount) {
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Guardar Venta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    return formatter.format(amount)
}
