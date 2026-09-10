package com.agroflow.feature.tasks.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        TaskStatus.PENDIENTE -> Triple(Color(0xFFFFF4E5), Color(0xFFFF9F0A), "Pendiente")
        TaskStatus.COMPLETADA -> Triple(Color(0xFFE8F8EE), Color(0xFF30D158), "Completada")
        TaskStatus.CANCELADA -> Triple(Color(0xFFFFEBEE), Color(0xFFFF453A), "Cancelada")
        else -> Triple(Color(0xFFE3F2FD), Color(0xFF0A84FF), "En Progreso")
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (finca == null) {
            Text(
                text = "Por favor, selecciona una finca en la pestaña Personal.", 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Gestión de Tareas", 
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Nueva Tarea", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        
        Spacer(Modifier.height(20.dp))

        if (taskViewModel.uiState is TaskUiState.Loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
        }

        val allTasks = taskViewModel.tasks
        val pendingTasks = allTasks.filter { it.estado == TaskStatus.PENDIENTE }
        val inProgressTasks = allTasks.filter { it.estado == TaskStatus.EN_PROGRESO }
        val completedTasks = allTasks.filter { it.estado == TaskStatus.COMPLETADA }

        // Kanban Board
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskColumn(
                title = "📋 Pendiente",
                headerColor = Color(0xFFFF9F0A),
                tasks = pendingTasks,
                onMoveToInProgress = { task -> 
                    taskViewModel.updateProgress(
                        task.id!!,
                        UpdateProgressRequest(task.trabajadorId, task.horasReales ?: 0.0, task.novedades ?: "", task.severidadNovedad, TaskStatus.EN_PROGRESO)
                    ) {}
                },
                onMoveToCompleted = null
            )
            TaskColumn(
                title = "🔄 En Proceso",
                headerColor = Color(0xFF0A84FF),
                tasks = inProgressTasks,
                onMoveToInProgress = null,
                onMoveToCompleted = { task -> 
                    taskViewModel.updateProgress(
                        task.id!!,
                        UpdateProgressRequest(task.trabajadorId, task.horasReales ?: 0.0, task.novedades ?: "", task.severidadNovedad, TaskStatus.COMPLETADA)
                    ) {}
                }
            )
            TaskColumn(
                title = "✅ Terminado",
                headerColor = Color(0xFF30D158),
                tasks = completedTasks,
                onMoveToInProgress = null,
                onMoveToCompleted = null
            )
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
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        TextField(
                            value = trabajador?.nombreCompleto ?: "Seleccionar Empleado",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7),
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
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
                            focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7),
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7).copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = descripcion, 
                        onValueChange = { descripcion = it }, 
                        placeholder = { Text("Descripción") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7),
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF3EFE7).copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(20.dp),
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

@Composable
fun TaskColumn(
    title: String,
    headerColor: Color,
    tasks: List<Task>,
    onMoveToInProgress: ((Task) -> Unit)?,
    onMoveToCompleted: ((Task) -> Unit)?
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color(0xFFF3EFE7).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        // Header
        Surface(
            color = headerColor.copy(alpha = 0.1f),
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
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks) { task ->
                var offsetX by remember { mutableStateOf(0f) }
                val threshold = 150f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(offsetX.toInt(), 0) }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX > threshold && onMoveToInProgress != null) {
                                        onMoveToInProgress(task)
                                    } else if (offsetX > threshold && onMoveToCompleted != null) {
                                        onMoveToCompleted(task)
                                    } else if (offsetX < -threshold && onMoveToInProgress != null) {
                                        // Optional: move back to progress if needed
                                    }
                                    offsetX = 0f
                                }
                            ) { change, dragAmount ->
                                offsetX += dragAmount
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = task.titulo,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = task.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
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
                            
                            // Visual hint for drag
                            Text(
                                text = "↔ Arrastra para mover",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
