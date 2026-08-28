package com.agroflow.feature.tasks.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import com.agroflow.feature.tasks.presentation.TaskViewModel
import com.agroflow.feature.tasks.presentation.TaskUiState
import com.agroflow.feature.tasks.data.TaskStatus
import com.agroflow.feature.tasks.data.CreateTaskRequest
import com.agroflow.feature.tasks.data.UpdateProgressRequest

@Composable
fun StatusBadge(status: TaskStatus) {
    val (bgColor, textColor, label) = when (status) {
        TaskStatus.PENDIENTE -> Triple(androidx.compose.ui.graphics.Color(0xFF3A2D0F), androidx.compose.ui.graphics.Color(0xFFFF9F0A), "Pendiente")
        TaskStatus.COMPLETADA -> Triple(androidx.compose.ui.graphics.Color(0xFF143F24), androidx.compose.ui.graphics.Color(0xFF30D158), "Completada")
        TaskStatus.CANCELADA -> Triple(androidx.compose.ui.graphics.Color(0xFF3A1E1E), androidx.compose.ui.graphics.Color(0xFFFF453A), "Cancelada")
        else -> Triple(androidx.compose.ui.graphics.Color(0xFF0F2D5E), androidx.compose.ui.graphics.Color(0xFF0A84FF), "En Progreso")
    }

    Surface(
        color = bgColor,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        modifier = androidx.compose.ui.Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
            modifier = androidx.compose.ui.Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TasksScreen(personnelViewModel: PersonnelViewModel, taskViewModel: TaskViewModel = viewModel()) {
    val trabajador = personnelViewModel.selectedTrabajador

    LaunchedEffect(trabajador) {
        if (trabajador != null) {
            taskViewModel.loadTasksForWorker(trabajador.id)
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (trabajador == null) {
            Text(
                text = "Por favor, selecciona un trabajador en la pestaña Fincas/Personal.", 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = "Tareas de ${trabajador.nombreCompleto.split(" ").firstOrNull() ?: trabajador.nombreCompleto}", 
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { showCreateDialog = true },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Nueva Tarea", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
            }
        }
        
        Spacer(Modifier.height(20.dp))

        if (taskViewModel.uiState is TaskUiState.Loading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
        }

        LazyColumn {
            items(taskViewModel.tasks) { task ->
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
                            Text(task.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!task.novedades.isNullOrBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text("Novedades: ${task.novedades}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog && trabajador != null) {
        var titulo by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        
        AlertDialog(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear Tarea", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) },
            text = {
                Column {
                    TextField(
                        value = titulo, 
                        onValueChange = { titulo = it }, 
                        placeholder = { Text("Título de la tarea") },
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
                        value = descripcion, 
                        onValueChange = { descripcion = it }, 
                        placeholder = { Text("Descripción") },
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
                        val request = CreateTaskRequest(
                            fincaId = personnelViewModel.selectedFinca!!.id,
                            trabajadorId = trabajador.id,
                            loteId = null,
                            titulo = titulo,
                            descripcion = descripcion,
                            estado = TaskStatus.PENDIENTE
                        )
                        taskViewModel.createTask(request) {
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
}
