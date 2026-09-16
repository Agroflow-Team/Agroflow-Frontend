package com.agroflow.feature.sales.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        floatingActionButton = {
            if (finca != null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Registrar Venta")
                }
            }
        },
        containerColor = Color(0xFFF1F8E9) // Ecosistema transparent / light background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Ventas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1C1C1E),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(totalSales),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1B5E20) // Ecosistema green
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Historial de Ventas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFA5D6A7), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Categoría / Producto", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = Color.Black)
                    Text("Fecha", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), color = Color.Black)
                    Text("Monto", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), color = Color.Black)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    items(salesTransactions.reversed()) { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tx.categoria,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = tx.fechaTransaccion.take(10),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(0.7f)
                            )
                            Text(
                                text = "+ ${formatCurrency(tx.montoTotal)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF30D158),
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }

        if (showCreateDialog && finca != null) {
            var productName by remember { mutableStateOf("") }
            var quantity by remember { mutableStateOf("") }
            var amount by remember { mutableStateOf("") }

            AlertDialog(
                shape = RoundedCornerShape(16.dp),
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
                                focusedContainerColor = Color(0xFFF3EFE7),
                                unfocusedContainerColor = Color(0xFFF3EFE7).copy(alpha = 0.5f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        TextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            placeholder = { Text("Cantidad (ej. 100 kg)") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF3EFE7),
                                unfocusedContainerColor = Color(0xFFF3EFE7).copy(alpha = 0.5f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(14.dp),
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
                                focusedContainerColor = Color(0xFFF3EFE7),
                                unfocusedContainerColor = Color(0xFFF3EFE7).copy(alpha = 0.5f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
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
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    return formatter.format(amount)
}
