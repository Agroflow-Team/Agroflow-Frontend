package com.agroflow.feature.empleado.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.agroflow.feature.empleado.presentation.EmpleadoViewModel
import com.agroflow.feature.inventory.data.InventoryItem

@Composable
fun WorkerInventoryScreen(viewModel: EmpleadoViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Inventario de Insumos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.insumos) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌾", style = MaterialTheme.typography.titleLarge)
                        }

                        Spacer(Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.nombreItem,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Stock: ${item.cantidad} ${item.unidadMedida}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { 
                                selectedItem = item
                                showDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("Usar", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedItem != null) {
        var cantidadUsada by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Usar Insumo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Stock actual: ${selectedItem!!.cantidad} ${selectedItem!!.unidadMedida}")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = cantidadUsada,
                        onValueChange = { cantidadUsada = it },
                        label = { Text("Cantidad a descontar") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val cantidadDesc = cantidadUsada.toDoubleOrNull() ?: 0.0
                    viewModel.updateInsumoStock(
                        itemId = selectedItem!!.id!!,
                        cantidadUsada = cantidadDesc
                    ) {
                        showDialog = false
                    }
                }) {
                    Text("Descontar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
