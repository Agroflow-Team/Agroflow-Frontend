package com.agroflow.feature.empleado.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.empleado.presentation.EmpleadoViewModel
import com.agroflow.feature.tasks.data.Task
import com.agroflow.feature.tasks.data.TaskStatus
import com.agroflow.feature.tasks.presentation.ui.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoDashboardScreen(
    onLogout: () -> Unit,
    viewModel: EmpleadoViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    var selectedTab by remember { mutableStateOf(0) }
    var selectedTaskToUpdate by remember { mutableStateOf<Task?>(null) }
    var showAddInsumo by remember { mutableStateOf(false) }
    var selectedInsumoToUpdate by remember { mutableStateOf<com.agroflow.feature.inventory.data.InventoryItem?>(null) }
    var selectedInsumoToEdit by remember { mutableStateOf<com.agroflow.feature.inventory.data.InventoryItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Operario", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = {
                        SessionManager.clearSession()
                        onLogout()
                    }) {
                        Text("Salir", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    icon = { Text("📋") },
                    label = { Text("Mis Tareas") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Text("🌾") },
                    label = { Text("Insumos") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            
            if (selectedTab == 0) {
                // TAREAS
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Mis Estadísticas", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(8.dp))
                        Text("Horas Trabajadas (Completadas): ${viewModel.totalHorasTrabajadas} hrs", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Salario Estimado: $${viewModel.salarioEstimado}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Mis Tareas Asignadas", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(viewModel.tasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                val barColor = when (task.estado) {
                                    TaskStatus.COMPLETADA -> androidx.compose.ui.graphics.Color(0xFF30D158)
                                    TaskStatus.PENDIENTE -> androidx.compose.ui.graphics.Color(0xFFFF9F0A)
                                    TaskStatus.CANCELADA -> androidx.compose.ui.graphics.Color(0xFFFF453A)
                                    else -> androidx.compose.ui.graphics.Color(0xFF0A84FF)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(5.dp)
                                        .background(barColor)
                                )
                                Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Text(task.titulo, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                        StatusBadge(task.estado)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text("Horas Reportadas: ${task.horasReales ?: 0.0}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (!task.novedades.isNullOrBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text("Novedades: ${task.novedades}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Button(
                                        onClick = { selectedTaskToUpdate = task },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                                        modifier = Modifier.fillMaxWidth().height(40.dp)
                                    ) {
                                        Text("Reportar Progreso", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // INSUMOS
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Insumos de la Finca", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Button(
                        onClick = { showAddInsumo = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    ) {
                        Text("Añadir", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Spacer(Modifier.height(8.dp))

                if (viewModel.insumos.isEmpty()) {
                    Text("No hay insumos o aún no se ha cargado la finca.", color = MaterialTheme.colorScheme.onBackground)
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(viewModel.insumos) { insumo ->
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
                                        text = insumo.nombreItem, 
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Stock: ${insumo.cantidad} ${insumo.unidadMedida}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Right Side Quantity & Edit
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                    TextButton(
                                        onClick = { selectedInsumoToEdit = insumo },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Editar", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Button(
                                        onClick = { selectedInsumoToUpdate = insumo },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Text("Usar", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogo para actualizar estado de tarea
    if (selectedTaskToUpdate != null) {
        var nuevasHoras by remember { mutableStateOf(selectedTaskToUpdate!!.horasReales?.toString() ?: "0.0") }
        var novedades by remember { mutableStateOf(selectedTaskToUpdate!!.novedades ?: "") }
        var estadoSeleccionado by remember { mutableStateOf(selectedTaskToUpdate!!.estado) }

        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { selectedTaskToUpdate = null },
            title = { Text("Reportar Progreso: ${selectedTaskToUpdate!!.titulo}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(
                        value = nuevasHoras,
                        onValueChange = { nuevasHoras = it },
                        placeholder = { Text("Horas Trabajadas") },
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
                        value = novedades,
                        onValueChange = { novedades = it },
                        placeholder = { Text("Novedades / Notas") },
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
                    Spacer(Modifier.height(16.dp))
                    Text("Estado de la tarea:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    TaskStatus.values().forEach { status ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            RadioButton(
                                selected = estadoSeleccionado == status,
                                onClick = { estadoSeleccionado = status },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Text(status.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTaskProgress(
                            taskId = selectedTaskToUpdate!!.id!!,
                            nuevasHoras = nuevasHoras.toDoubleOrNull() ?: 0.0,
                            novedades = novedades,
                            estado = estadoSeleccionado
                        ) {
                            selectedTaskToUpdate = null
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar (Sync local)", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTaskToUpdate = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }

    if (showAddInsumo) {
        var nombre by remember { mutableStateOf("") }
        var cantidad by remember { mutableStateOf("") }
        var unidad by remember { mutableStateOf("") }
        
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showAddInsumo = false },
            title = { Text("Añadir Nuevo Insumo", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        placeholder = { Text("Nombre del insumo") },
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
                        placeholder = { Text("Unidad (ej. Kg, Litros)") },
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
                        val fincaId = viewModel.currentFincaId ?: return@Button
                        viewModel.addInsumo(
                            fincaId = fincaId,
                            nombre = nombre,
                            cantidad = cantidad.toDoubleOrNull() ?: 0.0,
                            unidad = unidad
                        ) {
                            showAddInsumo = false
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar Localmente", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddInsumo = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }

    if (selectedInsumoToUpdate != null) {
        var cantidadUsada by remember { mutableStateOf("") }
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { selectedInsumoToUpdate = null },
            title = { Text("Usar Insumo: ${selectedInsumoToUpdate!!.nombreItem}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    Text("Stock actual: ${selectedInsumoToUpdate!!.cantidad} ${selectedInsumoToUpdate!!.unidadMedida}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = cantidadUsada,
                        onValueChange = { cantidadUsada = it },
                        placeholder = { Text("Cantidad a descontar") },
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
                        viewModel.updateInsumoStock(
                            itemId = selectedInsumoToUpdate!!.id!!,
                            cantidadUsada = cantidadUsada.toDoubleOrNull() ?: 0.0
                        ) {
                            selectedInsumoToUpdate = null
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Descontar Localmente", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInsumoToUpdate = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }

    if (selectedInsumoToEdit != null) {
        var nombre by remember { mutableStateOf(selectedInsumoToEdit!!.nombreItem) }
        var cantidad by remember { mutableStateOf(selectedInsumoToEdit!!.cantidad.toString()) }
        var unidad by remember { mutableStateOf(selectedInsumoToEdit!!.unidadMedida) }
        
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { selectedInsumoToEdit = null },
            title = { Text("Editar Ítem", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(
                        value = nombre, 
                        onValueChange = { nombre = it }, 
                        placeholder = { Text("Nombre del insumo") },
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
                        viewModel.editInsumo(
                            itemId = selectedInsumoToEdit!!.id!!,
                            nombre = nombre,
                            cantidad = cantidad.toDoubleOrNull() ?: 0.0,
                            unidad = unidad
                        ) {
                            selectedInsumoToEdit = null
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Actualizar Localmente", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInsumoToEdit = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }
}
