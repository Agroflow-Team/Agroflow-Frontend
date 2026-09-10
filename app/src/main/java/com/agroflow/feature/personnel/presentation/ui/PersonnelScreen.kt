package com.agroflow.feature.personnel.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    Scaffold(
        floatingActionButton = {
            if (viewModel.selectedFinca != null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = Color(0xFF2C7A4B),
                    contentColor = Color.White
                ) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }
        },
        containerColor = Color(0xFFF3EFE7)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text(
                text = "Fincas y Trabajadores", 
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Selecciona una Finca", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = { showCreateFincaDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("+ Finca", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }
            Spacer(Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }
            
            LaunchedEffect(viewModel.fincas) {
                if (viewModel.fincas.isNotEmpty() && viewModel.selectedFinca == null) {
                    val initialFinca = viewModel.fincas.first()
                    viewModel.selectedFinca = initialFinca
                    viewModel.loadTrabajadores(initialFinca.id)
                }
            }

            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                TextField(
                    value = viewModel.selectedFinca?.nombre ?: "Selecciona Finca",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    viewModel.fincas.forEach { finca ->
                        DropdownMenuItem(
                            text = { Text(finca.nombre) },
                            onClick = {
                                viewModel.selectedFinca = finca
                                viewModel.selectedTrabajador = null
                                viewModel.loadTrabajadores(finca.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (viewModel.selectedFinca != null) {
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Trabajadores", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(viewModel.trabajadores) { trabajador ->
                        val isSelected = viewModel.selectedTrabajador?.id == trabajador.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectedTrabajador = trabajador },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar
                                Surface(
                                    modifier = Modifier.size(60.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF2C7A4B).copy(alpha=0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("👤", style = MaterialTheme.typography.headlineMedium)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = trabajador.nombreCompleto.split(" ").firstOrNull() ?: trabajador.nombreCompleto, 
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = trabajador.documento, 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    IconButton(
                                        onClick = { trabajadorToEdit = trabajador; showEditDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("✏️")
                                    }
                                    IconButton(
                                        onClick = { trabajadorToDelete = trabajador; showDeleteDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("🗑️")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF3EFE7),
        unfocusedContainerColor = Color(0xFFF3EFE7).copy(alpha = 0.7f),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        cursorColor = Color(0xFF2C7A4B),
        focusedTextColor = Color(0xFF1C1C1E),
        unfocusedTextColor = Color(0xFF1C1C1E),
        focusedPlaceholderColor = Color(0xFF8E8E93),
        unfocusedPlaceholderColor = Color(0xFF8E8E93)
    )

    if (showCreateFincaDialog) {
        var nombreFinca by remember { mutableStateOf("") }

        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCreateFincaDialog = false },
            title = { Text("Registrar Nueva Finca", color = Color(0xFF1C1C1E), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column {
                    TextField(
                        value = nombreFinca, 
                        onValueChange = { nombreFinca = it }, 
                        placeholder = { Text("Nombre de la finca (ej. Villa Verde)") }, 
                        singleLine = true, 
                        colors = textFieldColors, 
                        shape = RoundedCornerShape(14.dp), 
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C7A4B))
                ) { Text("Registrar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFincaDialog = false }) { Text("Cancelar", color = Color(0xFF8E8E93)) }
            }
        )
    }

    if (showCreateDialog) {
        var nombre by remember { mutableStateOf("") }
        var documento by remember { mutableStateOf("") }
        var correo by remember { mutableStateOf("") }
        var clave by remember { mutableStateOf("") }
        var tarifaHora by remember { mutableStateOf("") }

        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nuevo Trabajador", color = Color(0xFF1C1C1E), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column {
                    TextField(value = nombre, onValueChange = { nombre = it }, placeholder = { Text("Nombre Completo") }, singleLine = true, colors = textFieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = documento, onValueChange = { documento = it }, placeholder = { Text("Documento") }, singleLine = true, colors = textFieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = correo, onValueChange = { correo = it }, placeholder = { Text("Correo") }, singleLine = true, colors = textFieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = clave, onValueChange = { clave = it }, placeholder = { Text("Contraseña") }, singleLine = true, colors = textFieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = tarifaHora, onValueChange = { tarifaHora = it }, placeholder = { Text("Tarifa / Hora (ej. 10000)") }, singleLine = true, colors = textFieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C7A4B))
                ) { Text("Crear", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar", color = Color(0xFF8E8E93)) }
            }
        )
    }

    if (showEditDialog && trabajadorToEdit != null) {
        var nombre by remember { mutableStateOf(trabajadorToEdit!!.nombreCompleto) }
        var documento by remember { mutableStateOf(trabajadorToEdit!!.documento) }
        var tarifaHora by remember { mutableStateOf(trabajadorToEdit!!.tarifaHora.toString()) }

        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Trabajador", color = Color(0xFF1C1C1E), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column {
                    TextField(value = nombre, onValueChange = { nombre = it }, placeholder = { Text("Nombre Completo") }, singleLine = true, colors = textFieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = documento, onValueChange = { documento = it }, placeholder = { Text("Documento") }, singleLine = true, colors = textFieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    TextField(value = tarifaHora, onValueChange = { tarifaHora = it }, placeholder = { Text("Tarifa / Hora") }, singleLine = true, colors = textFieldColors, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTrabajador(trabajadorToEdit!!.id, nombre, documento, tarifaHora.toDoubleOrNull() ?: 0.0)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C7A4B))
                ) { Text("Guardar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar", color = Color(0xFF8E8E93)) }
            }
        )
    }

    if (showDeleteDialog && trabajadorToDelete != null) {
        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Trabajador", color = Color(0xFF1C1C1E), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("¿Eliminar a ${trabajadorToDelete!!.nombreCompleto}?", color = Color(0xFF1C1C1E)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTrabajador(trabajadorToDelete!!.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar", color = Color(0xFF8E8E93)) }
            }
        )
    }
}
