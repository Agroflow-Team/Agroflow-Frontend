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
import com.agroflow.core.theme.AppleGreen
import com.agroflow.core.theme.AppleRed
import com.agroflow.feature.finance.presentation.FinanceUiState
import com.agroflow.feature.finance.presentation.FinanceViewModel
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import java.text.NumberFormat
import java.time.LocalDate
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
    var selectedPeriod by remember { mutableStateOf(3) } // 0: Hoy, 1: Semana, 2: Mes, 3: Todo

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
            // Period filter tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Hoy", "Esta Semana", "Este Mes", "Todo").forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedPeriod == index,
                        onClick = { selectedPeriod = index },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            val today = LocalDate.now()
            val filteredTransactions = when(selectedPeriod) {
                0 -> balance.transacciones.filter { it.fechaTransaccion.take(10) == today.toString() } // Hoy
                1 -> balance.transacciones.filter { 
                    val txDate = LocalDate.parse(it.fechaTransaccion.take(10))
                    txDate.isAfter(today.minusWeeks(1)) || txDate.isEqual(today.minusWeeks(1))
                } // Semana
                2 -> balance.transacciones.filter {
                    val txDate = LocalDate.parse(it.fechaTransaccion.take(10))
                    txDate.isAfter(today.minusMonths(1)) || txDate.isEqual(today.minusMonths(1))
                } // Mes
                else -> balance.transacciones // Todo
            }

            val filteredIngresosList = filteredTransactions.filter { it.tipoMovimiento == "INGRESO" }
            val filteredEgresosList = filteredTransactions.filter { it.tipoMovimiento == "EGRESO" }

            val totalIngresosFiltered = filteredIngresosList.sumOf { it.montoTotal }
            val totalEgresosFiltered = filteredEgresosList.sumOf { it.montoTotal }
            val utilidadNetaFiltered = totalIngresosFiltered - totalEgresosFiltered

            // Metrics Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(
                    title = "Ingresos",
                    amount = totalIngresosFiltered,
                    color = AppleGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Egresos",
                    amount = totalEgresosFiltered,
                    color = AppleRed,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            MetricCard(
                title = "Utilidad Neta",
                amount = utilidadNetaFiltered,
                color = if (utilidadNetaFiltered >= 0) AppleGreen else AppleRed,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)
            ) {
                Text("Registrar Nueva Transacción", color = MaterialTheme.colorScheme.onPrimary)
            }
            
            Spacer(Modifier.height(8.dp))
            
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = { financeViewModel.exportReport(finca.id, context) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Text("Descargar CSV", color = MaterialTheme.colorScheme.onSecondary)
            }

            Spacer(Modifier.height(16.dp))
            
            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                item {
                    Text("Ingresos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppleGreen)
                    Spacer(Modifier.height(8.dp))
                }
                items(filteredIngresosList.reversed()) { tx ->
                    TransactionItem(tx = tx, isIngreso = true)
                }
                
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Egresos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppleRed)
                    Spacer(Modifier.height(8.dp))
                }
                items(filteredEgresosList.reversed()) { tx ->
                    TransactionItem(tx = tx, isIngreso = false)
                }
            }
        }
    }

    if (showCreateDialog && finca != null) {
        var tipo by remember { mutableStateOf("INGRESO") }
        var categoria by remember { mutableStateOf("") }
        var monto by remember { mutableStateOf("") }

        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
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
                            focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7),
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7).copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
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
                            focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7),
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7).copy(alpha = 0.5f),
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
fun TransactionItem(tx: com.agroflow.feature.finance.data.Transaccion, isIngreso: Boolean) {
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
                text = "${if (isIngreso) "+" else "-"} ${formatCurrency(tx.montoTotal)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isIngreso) AppleGreen else AppleRed
            )
        }
    }
}

@Composable
fun MetricCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
