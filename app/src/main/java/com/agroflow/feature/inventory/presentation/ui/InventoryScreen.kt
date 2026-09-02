package com.agroflow.feature.inventory.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.inventory.data.CreateInventoryItemRequest
import com.agroflow.feature.inventory.data.TipoItemEnum
import com.agroflow.feature.inventory.presentation.InventoryUiState
import com.agroflow.feature.inventory.presentation.InventoryViewModel
import com.agroflow.feature.personnel.presentation.PersonnelViewModel

@Composable
fun InventoryScreen(personnelViewModel: PersonnelViewModel, inventoryViewModel: InventoryViewModel = viewModel()) {
    val finca = personnelViewModel.selectedFinca

    LaunchedEffect(finca) {
        if (finca != null) {
            inventoryViewModel.loadInventory(finca.id)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<com.agroflow.feature.inventory.data.InventoryItem?>(null) }
    var itemToDelete by remember { mutableStateOf<com.agroflow.feature.inventory.data.InventoryItem?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (finca == null) {
            Text("Por favor, selecciona una finca en la pestaña Fincas/Personal.", style = MaterialTheme.typography.bodyLarge)
            return@Column
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Inventario de ${finca.nombre}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Text("Añadir", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        
        Spacer(Modifier.height(16.dp))

        if (inventoryViewModel.uiState is InventoryUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.primary)
        }

        if (inventoryViewModel.uiState is InventoryUiState.Error) {
            Text(
                text = (inventoryViewModel.uiState as InventoryUiState.Error).message, 
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(onClick = { inventoryViewModel.loadInventory(finca.id) }) {
                Text("Reintentar")
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(inventoryViewModel.items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        // iOS style circle icon on the left
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text("🌾", style = MaterialTheme.typography.titleLarge)
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        // Center Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.nombreItem, 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Right Side Quantity & Edit
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text(
                                text = "${item.cantidad} ${item.unidadMedida}", 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row {
                                TextButton(
                                    onClick = { itemToEdit = item },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Editar", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium))
                                }
                                Spacer(Modifier.width(8.dp))
                                TextButton(
                                    onClick = { itemToDelete = item },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Eliminar", color = androidx.compose.ui.graphics.Color(0xFFFF453A), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showCreateDialog && finca != null) {
        var nombre by remember { mutableStateOf("") }
        var cantidad by remember { mutableStateOf("0") }
        var unidad by remember { mutableStateOf("Unidad") }
        
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Añadir Ítem", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        placeholder = { Text("Nombre del ítem") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        placeholder = { Text("Cantidad") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = unidad, 
                        onValueChange = { unidad = it }, 
                        placeholder = { Text("Unidad de medida (ej. kg, litros)") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trabajadorId = personnelViewModel.trabajadores.firstOrNull()?.id ?: "00000000-0000-0000-0000-000000000000"
                        
                        val request = CreateInventoryItemRequest(
                            fincaId = finca.id,
                            registradoPorTrabajadorId = trabajadorId,
                            nombreItem = nombre,
                            tipo = TipoItemEnum.INSUMO,
                            cantidad = cantidad.toDoubleOrNull() ?: 0.0,
                            unidadMedida = unidad,
                            costoUnitario = 0.0
                        )
                        inventoryViewModel.addItem(request) {
                            showCreateDialog = false
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }

    if (itemToEdit != null && finca != null) {
        var nombre by remember { mutableStateOf(itemToEdit!!.nombreItem) }
        var cantidad by remember { mutableStateOf(itemToEdit!!.cantidad.toString()) }
        var unidad by remember { mutableStateOf(itemToEdit!!.unidadMedida) }
        
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { itemToEdit = null },
            title = { Text("Editar Ítem", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        placeholder = { Text("Nombre del ítem") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        placeholder = { Text("Cantidad") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = unidad, 
                        onValueChange = { unidad = it }, 
                        placeholder = { Text("Unidad de medida") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val request = com.agroflow.feature.inventory.data.UpdateInventoryItemRequest(
                            nombreItem = nombre,
                            cantidad = cantidad.toDoubleOrNull() ?: 0.0,
                            unidadMedida = unidad
                        )
                        itemToEdit?.id?.let { itemId ->
                            inventoryViewModel.editItem(finca.id, itemId, request) {
                                itemToEdit = null
                            }
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Actualizar", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }

    if (itemToDelete != null && finca != null) {
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { itemToDelete = null },
            title = { Text("Eliminar Ítem", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = { Text("¿Estás seguro de eliminar '${itemToDelete!!.nombreItem}' del inventario?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.id?.let { itemId ->
                            inventoryViewModel.deleteItem(finca.id, itemId) {
                                itemToDelete = null
                            }
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFF453A))
                ) {
                    Text("Eliminar", color = androidx.compose.ui.graphics.Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }
}
