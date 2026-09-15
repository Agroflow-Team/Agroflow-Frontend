package com.agroflow.feature.tasks.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import com.agroflow.feature.tasks.presentation.TaskViewModel
import com.agroflow.feature.tasks.presentation.TaskUiState
import com.agroflow.feature.tasks.data.TaskStatus
import com.agroflow.feature.tasks.data.CreateTaskRequest
import com.agroflow.feature.tasks.data.UpdateProgressRequest
import com.agroflow.feature.tasks.data.Task
import com.agroflow.feature.personnel.data.Trabajador

@Composable
fun StatusBadge(status: TaskStatus) {
    val (bgColor, textColor, label) = when (status) {
        TaskStatus.PENDIENTE -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Pendiente")
        TaskStatus.COMPLETADA -> Triple(Color(0xFFE8F8EE), Color(0xFF30D158), "Completada")
        TaskStatus.CANCELADA -> Triple(Color(0xFFFFEBEE), Color(0xFFFF453A), "Cancelada")
        else -> Triple(Color(0xFFC8E6C9), Color(0xFF1B5E20), "En Progreso")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SeverityBadge(severity: String) {
    val (bgColor, textColor, label) = when (severity.uppercase()) {
        "BAJO" -> Triple(Color(0xFFE8F8EE), Color(0xFF30D158), "Bajo")
        "MEDIO" -> Triple(Color(0xFFFFF8E1), Color(0xFFFFD60A), "Medio")
        "ALTO" -> Triple(Color(0xFFFFEBEE), Color(0xFFFF453A), "Alto")
        else -> Triple(Color(0xFFF5F5F5), Color(0xFF8E8E93), severity)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TasksScreen(personnelViewModel: PersonnelViewModel, taskViewModel: TaskViewModel = viewModel()) {
    val finca = personnelViewModel.selectedFinca
    
    LaunchedEffect(finca) {
        if (finca != null) {
            taskViewModel.loadTasksByFinca(finca.id)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (finca != null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Tarea")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)) {
            
            if (finca == null) {
                Text(
                    text = "Por favor, selecciona una finca en la pestaña Personal.", 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Text(
                text = "Gestión de Tareas", 
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(Modifier.height(20.dp))

            if (taskViewModel.uiState is TaskUiState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
            }

            val allTasks = taskViewModel.tasks
            val pendingTasks = allTasks.filter { it.estado == TaskStatus.PENDIENTE }
            val inProgressTasks = allTasks.filter { it.estado == TaskStatus.EN_PROGRESO }
            val completedTasks = allTasks.filter { it.estado == TaskStatus.COMPLETADA }

            // Kanban Board with LazyRow
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TaskColumn(
                        title = "📋 Pendiente",
                        headerColor = Color(0xFF2E7D32),
                        tasks = pendingTasks,
                        actionText = "▶ Mover a En Progreso",
                        onActionClick = { task -> 
                            taskViewModel.updateProgress(
                                task.id!!,
                                UpdateProgressRequest(task.trabajadorId, task.horasReales ?: 0.0, task.novedades ?: "", task.severidadNovedad, TaskStatus.EN_PROGRESO)
                            ) {}
                        }
                    )
                }
                item {
                    TaskColumn(
                        title = "🔄 En Progreso",
                        headerColor = Color(0xFF1B5E20),
                        tasks = inProgressTasks,
                        actionText = "▶ Mover a Completado",
                        onActionClick = { task -> 
                            taskViewModel.updateProgress(
                                task.id!!,
                                UpdateProgressRequest(task.trabajadorId, task.horasReales ?: 0.0, task.novedades ?: "", task.severidadNovedad, TaskStatus.COMPLETADA)
                            ) {}
                        }
                    )
                }
                item {
                    TaskColumn(
                        title = "✅ Completado",
                        headerColor = Color(0xFF388E3C),
                        tasks = completedTasks,
                        actionText = null,
                        onActionClick = null
                    )
                }
            }
        }

        if (showCreateDialog) {
            var titulo by remember { mutableStateOf("") }
            var descripcion by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }
            var trabajador by remember { mutableStateOf<Trabajador?>(null) }
            
            AlertDialog(
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Crear Tarea", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                text = {
                    Column {
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            TextField(
                                value = trabajador?.nombreCompleto ?: "Seleccionar Empleado",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = ExposedDropdownMenuDefaults.textFieldColors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                personnelViewModel.trabajadores.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.nombreCompleto) },
                                        onClick = {
                                            trabajador = t
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        TextField(
                            value = titulo, 
                            onValueChange = { titulo = it }, 
                            placeholder = { Text("Título de la tarea") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        TextField(
                            value = descripcion, 
                            onValueChange = { descripcion = it }, 
                            placeholder = { Text("Descripción") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val t = trabajador
                            if (t != null && titulo.isNotBlank()) {
                                val request = CreateTaskRequest(
                                    fincaId = finca!!.id,
                                    trabajadorId = t.id,
                                    loteId = null,
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    estado = TaskStatus.PENDIENTE
                                )
                                taskViewModel.createTask(request) {
                                    showCreateDialog = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = trabajador != null
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
    }
}

@Composable
fun TaskColumn(
    title: String,
    headerColor: Color,
    tasks: List<Task>,
    actionText: String?,
    onActionClick: ((Task) -> Unit)?
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Surface(
            color = headerColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                color = headerColor,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = task.titulo,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = task.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (task.severidadNovedad != null) {
                                SeverityBadge(task.severidadNovedad)
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                        }

                        if (actionText != null && onActionClick != null) {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { onActionClick(task) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = actionText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
