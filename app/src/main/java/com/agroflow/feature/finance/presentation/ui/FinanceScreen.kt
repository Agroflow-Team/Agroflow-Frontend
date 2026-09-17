package com.agroflow.feature.finance.presentation.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.feature.finance.presentation.FinanceUiState
import com.agroflow.feature.finance.presentation.FinanceViewModel
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

val AppleGreen = Color(0xFF30D158)
val AppleRed = Color(0xFFFF453A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(personnelViewModel: PersonnelViewModel, financeViewModel: FinanceViewModel = viewModel()) {
    val finca = personnelViewModel.selectedFinca

    LaunchedEffect(finca) {
        if (finca != null) {
            financeViewModel.loadBalance(finca.id)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf(0) } // 0: Día, 1: Semana, 2: Mes

    Scaffold(
        floatingActionButton = {
            if (finca != null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva Transacción")
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
                    "Por favor, selecciona una finca en la pestaña Fincas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                return@Column
            }

            Text("Finanzas de ${finca.nombre}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
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
                // Period filter buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Día", "Semana", "Mes").forEachIndexed { index, label ->
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
                    0 -> balance.transacciones.filter { it.fechaTransaccion.take(10) == today.toString() } // Día
                    1 -> balance.transacciones.filter { 
                        val txDate = LocalDate.parse(it.fechaTransaccion.take(10))
                        txDate.isAfter(today.minusWeeks(1)) || txDate.isEqual(today.minusWeeks(1))
                    } // Semana
                    else -> balance.transacciones.filter {
                        val txDate = LocalDate.parse(it.fechaTransaccion.take(10))
                        txDate.isAfter(today.minusMonths(1)) || txDate.isEqual(today.minusMonths(1))
                    } // Mes
                }

                val filteredIngresosList = filteredTransactions.filter { it.tipoMovimiento == "INGRESO" }
                val filteredEgresosList = filteredTransactions.filter { it.tipoMovimiento == "EGRESO" }

                val totalIngresos = filteredIngresosList.sumOf { it.montoTotal }
                val totalEgresos = filteredEgresosList.sumOf { it.montoTotal }
                val balanceTotal = totalIngresos - totalEgresos

                // 3 Metrics Cards
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "Ingresos", amount = totalIngresos, color = Color(0xFF1B5E20), modifier = Modifier.weight(1f))
                    MetricCard(title = "Egresos", amount = totalEgresos, color = AppleRed, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                MetricCard(title = "Balance Total", amount = balanceTotal, color = if (balanceTotal >= 0) Color(0xFF1B5E20) else AppleRed, modifier = Modifier.fillMaxWidth())
                
                Spacer(Modifier.height(16.dp))

                // Action buttons (Floating chips style)
                val context = LocalContext.current
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        onClick = { financeViewModel.exportReport(finca.id, context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Excel", color = Color(0xFF1C1C1E), fontWeight = FontWeight.SemiBold)


                        }
                    }
                    Surface(
                        onClick = { financeViewModel.exportReport(finca.id, context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PDF", color = Color(0xFF1C1C1E), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Historial (Ingresos y Egresos)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFA5D6A7), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Concepto", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = Color.Black)
                    Text("Fecha", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), color = Color.Black)
                    Text("Monto", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), color = Color.Black)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    items(filteredTransactions.reversed()) { tx ->
                        val isIngreso = tx.tipoMovimiento == "INGRESO"
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
                                text = "${if (isIngreso) "+" else "-"} ${formatCurrency(tx.montoTotal)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isIngreso) AppleGreen else AppleRed,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }

        if (showCreateDialog && finca != null) {
            var tipo by remember { mutableStateOf("INGRESO") }
            var categoria by remember { mutableStateOf("") }
            var monto by remember { mutableStateOf("") }

            AlertDialog(
                shape = RoundedCornerShape(16.dp),
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
                            value = monto,
                            onValueChange = { monto = it },
                            placeholder = { Text("Monto") },
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
}

@Composable
fun MetricCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1C1C1E), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    return formatter.format(amount)
}
