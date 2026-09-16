package com.agroflow.feature.personnel.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.agroflow.feature.personnel.presentation.PersonnelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FincaManagementScreen(viewModel: PersonnelViewModel) {
    var showCreateFincaDialog by remember { mutableStateOf(false) }
    var showEditFincaDialog by remember { mutableStateOf<com.agroflow.feature.personnel.data.Finca?>(null) }
    var fincaToDelete by remember { mutableStateOf<com.agroflow.feature.personnel.data.Finca?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFincas()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateFincaDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva Finca")
            }
        },
        containerColor = Color(0xFFF1F8E9) // Light pastel green background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Tus Fincas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.fincas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes fincas registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.fincas) { finca ->
                        val isSelected = viewModel.selectedFinca?.id == finca.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectedFinca = finca },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = finca.nombre,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Text("✅ Seleccionada", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Toca para seleccionar", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                
                                Row {
                                    IconButton(onClick = { showEditFincaDialog = finca }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { fincaToDelete = finca }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog for creating a new finca
        if (showCreateFincaDialog) {
            var nombre by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateFincaDialog = false },
                title = { Text("Nueva Finca") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre de la finca") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.createFinca(nombre)
                        showCreateFincaDialog = false
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFincaDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Dialog for editing a finca
        showEditFincaDialog?.let { fincaToEdit ->
            var nombre by remember { mutableStateOf(fincaToEdit.nombre) }
            AlertDialog(
                onDismissRequest = { showEditFincaDialog = null },
                title = { Text("Editar Finca") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre de la finca") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Normally viewModel.updateFinca(fincaToEdit.id, nombre)
                        showEditFincaDialog = null
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditFincaDialog = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Dialog for deleting a finca
        fincaToDelete?.let { finca ->
            AlertDialog(
                onDismissRequest = { fincaToDelete = null },
                title = { Text("Eliminar Finca") },
                text = { Text("¿Estás seguro de que deseas eliminar la finca '${finca.nombre}'? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteFinca(finca.id)
                            fincaToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fincaToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (viewModel.deleteError != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearDeleteError() },
                title = { Text("Error al eliminar", color = MaterialTheme.colorScheme.error) },
                text = { Text(viewModel.deleteError!!) },
                confirmButton = {
                    Button(onClick = { viewModel.clearDeleteError() }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}
