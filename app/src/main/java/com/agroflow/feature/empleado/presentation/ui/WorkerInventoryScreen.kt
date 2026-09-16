package com.agroflow.feature.empleado.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroflow.feature.empleado.presentation.EmpleadoViewModel
import com.agroflow.feature.inventory.data.InventoryItem

@Composable
fun WorkerInventoryScreen(viewModel: EmpleadoViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
    var inputAmount by remember { mutableStateOf("") }
    
    val items = viewModel.insumos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)) // Premium Green Background
            .padding(16.dp)
    ) {
        Text(
            text = "Inventario de Insumos",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF388E3C),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = CircleShape,
                                color = Color(0xFFE8F5E9)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌾", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = item.nombreItem, 
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "Stock: ${item.cantidad} ${item.unidadMedida}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Button(
                            onClick = {
                                selectedItem = item
                                inputAmount = ""
                                showDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Usar", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Usar Insumo") },
            text = {
                Column {
                    Text("¿Cuánto deseas descontar del inventario de ${selectedItem!!.nombreItem}?")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("Cantidad (${selectedItem!!.unidadMedida})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && amount <= selectedItem!!.cantidad) {
                            viewModel.updateInsumoStock(selectedItem!!.id!!, amount) {
                                viewModel.loadTasks() // or loadInventory, wait loadTasks already loads inventory
                            }
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C7A4B))
                ) {
                    Text("Confirmar Uso")
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
