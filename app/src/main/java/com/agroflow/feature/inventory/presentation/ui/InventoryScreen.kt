package com.agroflow.feature.inventory.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
    val isTrabajador = SessionManager.roleId?.equals(SessionManager.ROLE_TRABAJADOR, ignoreCase = true) == true

    LaunchedEffect(finca) {
        if (finca != null) {
            inventoryViewModel.loadInventory(finca.id)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<com.agroflow.feature.inventory.data.InventoryItem?>(null) }
    var itemToDelete by remember { mutableStateOf<com.agroflow.feature.inventory.data.InventoryItem?>(null) }

    Scaffold(
        floatingActionButton = {
            if (finca != null && !isTrabajador) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Color(0xFF388E3C),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        },
        containerColor = Color(0xFFE8F5E9)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (finca == null) {
                Text(
                    "Por favor, selecciona una finca en la pestaña Fincas/Personal.", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF388E3C)
                )
                return@Column
            }

            Text(
                "Inventario de ${finca.nombre}", 
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), 
                color = Color(0xFF388E3C)
            )
            
            Spacer(Modifier.height(16.dp))

            if (inventoryViewModel.uiState is InventoryUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally), 
                    color = Color(0xFF388E3C)
                )
            }

            if (inventoryViewModel.uiState is InventoryUiState.Error) {
                Text(
                    text = (inventoryViewModel.uiState as InventoryUiState.Error).message, 
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = { inventoryViewModel.loadInventory(finca.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text("Reintentar")
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(inventoryViewModel.items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (!isTrabajador) itemToEdit = item },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF81C784))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = CircleShape,
                                color = Color(0xFFE8F5E9)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🌾", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text(
                                text = item.nombreItem, 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.tipo.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50)
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Text(
                                text = "${item.cantidad} ${item.unidadMedida}", 
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF388E3C)
                            )

                            Spacer(Modifier.height(12.dp))

                            if (isTrabajador) {
                                Button(
                                    onClick = { /* Acción de Usar */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Usar", color = Color.White)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    IconButton(
                                        onClick = { itemToEdit = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF388E3C))
                                    }
                                    IconButton(
                                        onClick = { itemToDelete = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF453A))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    val outlinedTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF388E3C),
        unfocusedBorderColor = Color(0xFF81C784),
        focusedLabelColor = Color(0xFF388E3C),
        cursorColor = Color(0xFF388E3C)
    )

    if (showCreateDialog && finca != null) {
        var nombre by remember { mutableStateOf("") }
        var cantidad by remember { mutableStateOf("0") }
        var unidad by remember { mutableStateOf("Unidad") }
        
        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Añadir Ítem", color = Color(0xFF388E3C), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        label = { Text("Nombre del ítem") },
                        singleLine = true,
                        colors = outlinedTextFieldColors,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = outlinedTextFieldColors,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = unidad, 
                        onValueChange = { unidad = it }, 
                        label = { Text("Unidad de medida (ej. kg, litros)") },
                        singleLine = true,
                        colors = outlinedTextFieldColors,
                        shape = RoundedCornerShape(14.dp),
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
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = Color(0xFF388E3C))
                }
            }
        )
    }

    if (itemToEdit != null && finca != null) {
        var nombre by remember { mutableStateOf(itemToEdit!!.nombreItem) }
        var cantidad by remember { mutableStateOf(itemToEdit!!.cantidad.toString()) }
        var unidad by remember { mutableStateOf(itemToEdit!!.unidadMedida) }
        
        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { itemToEdit = null },
            title = { Text("Editar Ítem", color = Color(0xFF388E3C), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        label = { Text("Nombre del ítem") },
                        singleLine = true,
                        colors = outlinedTextFieldColors,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = outlinedTextFieldColors,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = unidad, 
                        onValueChange = { unidad = it }, 
                        label = { Text("Unidad de medida") },
                        singleLine = true,
                        colors = outlinedTextFieldColors,
                        shape = RoundedCornerShape(14.dp),
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
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text("Actualizar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Cancelar", color = Color(0xFF388E3C))
                }
            }
        )
    }

    if (itemToDelete != null && finca != null) {
        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { itemToDelete = null },
            title = { Text("Eliminar Ítem", color = Color(0xFF388E3C), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancelar", color = Color(0xFF388E3C))
                }
            }
        )
    }
}
