package com.agroflow.feature.personnel.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import com.agroflow.feature.personnel.presentation.PersonnelUiState
import com.agroflow.feature.personnel.data.Trabajador

@Composable
fun PersonnelScreen(viewModel: PersonnelViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadFincas()
    }

    var showCreateFincaDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var trabajadorToEdit by remember { mutableStateOf<Trabajador?>(null) }
    var trabajadorToDelete by remember { mutableStateOf<Trabajador?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Fincas y Trabajadores", 
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(20.dp))
        
        if (viewModel.uiState is PersonnelUiState.Loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
        }
        
        if (viewModel.uiState is PersonnelUiState.Error) {
            Text(
                (viewModel.uiState as PersonnelUiState.Error).message, 
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Selecciona una Finca", style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = { showCreateFincaDialog = true },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("+ Finca", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(viewModel.fincas) { finca ->
                val isSelected = viewModel.selectedFinca?.id == finca.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable {
                            viewModel.selectedFinca = finca
                            viewModel.selectedTrabajador = null
                            viewModel.loadTrabajadores(finca.id)
                        },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "🏡 ${finca.nombre}", 
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        }

        if (viewModel.selectedFinca != null) {
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = "Trabajadores de ${viewModel.selectedFinca?.nombre}", 
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { showCreateDialog = true },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("+ Trabajador", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                }
            }
            Spacer(Modifier.height(8.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.trabajadores) { trabajador ->
                    val isSelected = viewModel.selectedTrabajador?.id == trabajador.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                viewModel.selectedTrabajador = trabajador
                            },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text(
                                    text = trabajador.nombreCompleto, 
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                                Row {
                                    TextButton(onClick = { trabajadorToEdit = trabajador; showEditDialog = true }) {
                                        Text("Editar", color = androidx.compose.ui.graphics.Color(0xFF0A84FF), style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium))
                                    }
                                    TextButton(onClick = { trabajadorToDelete = trabajador; showDeleteDialog = true }) {
                                        Text("Eliminar", color = androidx.compose.ui.graphics.Color(0xFFFF453A), style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium))
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Documento: ${trabajador.documento}", 
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateFincaDialog) {
        var nombreFinca by remember { mutableStateOf("") }
        val textFieldColors = TextFieldDefaults.colors(
            unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            focusedContainerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            cursorColor = androidx.compose.ui.graphics.Color(0xFF30D158),
            unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
            focusedTextColor = androidx.compose.ui.graphics.Color.White,
            unfocusedPlaceholderColor = androidx.compose.ui.graphics.Color(0xFF8E8E93),
            focusedPlaceholderColor = androidx.compose.ui.graphics.Color(0xFF8E8E93)
        )

        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            onDismissRequest = { showCreateFincaDialog = false },
            title = { Text("Registrar Nueva Finca", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(
                        value = nombreFinca, 
                        onValueChange = { nombreFinca = it }, 
                        placeholder = { Text("Nombre de la finca (ej. Villa Verde)") }, 
                        singleLine = true, 
                        colors = textFieldColors, 
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), 
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreFinca.isNotBlank()) {
                            viewModel.createFinca(nombreFinca.trim())
                            showCreateFincaDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF30D158))
                ) { Text("Registrar", color = androidx.compose.ui.graphics.Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFincaDialog = false }) { Text("Cancelar", color = androidx.compose.ui.graphics.Color(0xFF8E8E93)) }
            }
        )
    }

    if (showCreateDialog) {
        var nombre by remember { mutableStateOf("") }
        var documento by remember { mutableStateOf("") }
        var correo by remember { mutableStateOf("") }
        var clave by remember { mutableStateOf("") }
        var tarifaHora by remember { mutableStateOf("") }

        val textFieldColors = TextFieldDefaults.colors(
            unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            focusedContainerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            cursorColor = androidx.compose.ui.graphics.Color(0xFF30D158),
            unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
            focusedTextColor = androidx.compose.ui.graphics.Color.White,
            unfocusedPlaceholderColor = androidx.compose.ui.graphics.Color(0xFF8E8E93),
            focusedPlaceholderColor = androidx.compose.ui.graphics.Color(0xFF8E8E93)
        )

        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nuevo Trabajador", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(value = nombre, onValueChange = { nombre = it }, placeholder = { Text("Nombre Completo") }, singleLine = true, colors = textFieldColors, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = documento, onValueChange = { documento = it }, placeholder = { Text("Documento") }, singleLine = true, colors = textFieldColors, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = correo, onValueChange = { correo = it }, placeholder = { Text("Correo") }, singleLine = true, colors = textFieldColors, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = clave, onValueChange = { clave = it }, placeholder = { Text("Contraseña") }, singleLine = true, colors = textFieldColors, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = tarifaHora, onValueChange = { tarifaHora = it }, placeholder = { Text("Tarifa / Hora (ej. 10000)") }, singleLine = true, colors = textFieldColors, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombre.isNotBlank() && correo.isNotBlank() && clave.isNotBlank()) {
                            viewModel.createTrabajador(viewModel.selectedFinca!!.id, nombre, documento, tarifaHora.toDoubleOrNull() ?: 0.0, correo, clave)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF30D158))
                ) { Text("Crear", color = androidx.compose.ui.graphics.Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar", color = androidx.compose.ui.graphics.Color(0xFF8E8E93)) }
            }
        )
    }

    if (showEditDialog && trabajadorToEdit != null) {
        var nombre by remember { mutableStateOf(trabajadorToEdit!!.nombreCompleto) }
        var documento by remember { mutableStateOf(trabajadorToEdit!!.documento) }
        var tarifaHora by remember { mutableStateOf(trabajadorToEdit!!.tarifaHora.toString()) }

        val textFieldColors = TextFieldDefaults.colors(
            unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            focusedContainerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            cursorColor = androidx.compose.ui.graphics.Color(0xFF30D158),
            unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
            focusedTextColor = androidx.compose.ui.graphics.Color.White,
            unfocusedPlaceholderColor = androidx.compose.ui.graphics.Color(0xFF8E8E93),
            focusedPlaceholderColor = androidx.compose.ui.graphics.Color(0xFF8E8E93)
        )

        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Trabajador", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(value = nombre, onValueChange = { nombre = it }, placeholder = { Text("Nombre Completo") }, singleLine = true, colors = textFieldColors, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = documento, onValueChange = { documento = it }, placeholder = { Text("Documento") }, singleLine = true, colors = textFieldColors, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = tarifaHora, onValueChange = { tarifaHora = it }, placeholder = { Text("Tarifa / Hora") }, singleLine = true, colors = textFieldColors, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTrabajador(trabajadorToEdit!!.id, nombre, documento, tarifaHora.toDoubleOrNull() ?: 0.0)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF30D158))
                ) { Text("Guardar", color = androidx.compose.ui.graphics.Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar", color = androidx.compose.ui.graphics.Color(0xFF8E8E93)) }
            }
        )
    }

    if (showDeleteDialog && trabajadorToDelete != null) {
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = androidx.compose.ui.graphics.Color(0xFF1C1C1E),
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Trabajador", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = { Text("¿Eliminar a ${trabajadorToDelete!!.nombreCompleto}?", color = androidx.compose.ui.graphics.Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTrabajador(trabajadorToDelete!!.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFF453A))
                ) { Text("Eliminar", color = androidx.compose.ui.graphics.Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = androidx.compose.ui.graphics.Color(0xFF8E8E93)) }
            }
        )
    }
}
