package com.agroflow.feature.personnel.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agroflow.feature.personnel.presentation.PersonnelViewModel

@Composable
fun FincaManagementScreen(viewModel: PersonnelViewModel) {
    var showCreateFincaDialog by remember { mutableStateOf(false) }
    var fincaToDelete by remember { mutableStateOf<com.agroflow.feature.personnel.data.Finca?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFincas()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tus Fincas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { showCreateFincaDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Nueva Finca")
            }
        }

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
                        // Delete button when a finca is selected
                        if (isSelected) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.deleteFinca(finca.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = finca.nombre,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Text("✅ Seleccionada", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Seleccionar", color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { fincaToDelete = finca }) {
                                Text("🗑️")
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
