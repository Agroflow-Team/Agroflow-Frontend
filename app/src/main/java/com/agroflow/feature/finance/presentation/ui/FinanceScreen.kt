package com.agroflow.feature.finance.presentation.ui

import androidx.compose.foundation.background
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
import com.agroflow.core.theme.AppleDarkGrey
import com.agroflow.core.theme.AppleGreen
import com.agroflow.core.theme.AppleRed
import com.agroflow.feature.finance.presentation.FinanceUiState
import com.agroflow.feature.finance.presentation.FinanceViewModel
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinanceScreen(personnelViewModel: PersonnelViewModel, financeViewModel: FinanceViewModel = viewModel()) {
    val finca = personnelViewModel.selectedFinca

    LaunchedEffect(finca) {
        if (finca != null) {
            financeViewModel.loadBalance(finca.id)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (finca == null) {
            Text("Por favor, selecciona una finca en la pestaña Fincas.", style = MaterialTheme.typography.bodyLarge)
            return@Column
        }

        Text("Finanzas de ${finca.nombre}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))

        if (financeViewModel.uiState is FinanceUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.primary)
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
            // Metrics Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(
                    title = "Ingresos",
                    amount = balance.totalIngresos,
                    color = AppleGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Egresos",
                    amount = balance.totalEgresos,
                    color = AppleRed,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            MetricCard(
                title = "Utilidad Neta",
                amount = balance.utilidadNeta,
                color = if (balance.utilidadNeta >= 0) AppleGreen else AppleRed,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Registrar Nueva Transacción", color = MaterialTheme.colorScheme.onPrimary)
            }
            
            Spacer(Modifier.height(8.dp))
            
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = { financeViewModel.exportReport(finca.id, context) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Exportar Reporte (CSV)", color = MaterialTheme.colorScheme.onSecondary)
            }

            Spacer(Modifier.height(16.dp))
            Text("Transacciones Recientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                items(balance.transacciones.reversed()) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppleDarkGrey)
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
                            
                            val isIngreso = tx.tipoMovimiento == "INGRESO"
                            Text(
                                text = "${if (isIngreso) "+" else "-"} ${formatCurrency(tx.montoTotal)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isIngreso) AppleGreen else AppleRed
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog && finca != null) {
        var tipo by remember { mutableStateOf("INGRESO") }
        var categoria by remember { mutableStateOf("") }
        var monto by remember { mutableStateOf("") }

        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nueva Transacción", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        FilterChip(
                            selected = tipo == "INGRESO",
                            onClick = { tipo = "INGRESO" },
                            label = { Text("Ingreso") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppleGreen.copy(alpha = 0.2f),
                                selectedLabelColor = AppleGreen
                            )
                        )
                        FilterChip(
                            selected = tipo == "EGRESO",
                            onClick = { tipo = "EGRESO" },
                            label = { Text("Egreso") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppleRed.copy(alpha = 0.2f),
                                selectedLabelColor = AppleRed
                            )
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        placeholder = { Text("Categoría (ej. Venta, Salario)") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = monto,
                        onValueChange = { monto = it },
                        placeholder = { Text("Monto") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = monto.toDoubleOrNull() ?: 0.0
                        financeViewModel.registrarTransaccion(finca.id, tipo, categoria, amount) {
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Guardar")
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

@Composable
fun MetricCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppleDarkGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    return formatter.format(amount)
}
