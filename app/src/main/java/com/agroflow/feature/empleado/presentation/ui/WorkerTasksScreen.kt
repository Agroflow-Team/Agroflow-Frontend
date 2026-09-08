package com.agroflow.feature.empleado.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroflow.feature.empleado.presentation.EmpleadoViewModel
import com.agroflow.feature.tasks.data.Task
import com.agroflow.feature.tasks.data.TaskStatus
import com.agroflow.feature.tasks.presentation.ui.SeverityBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkerTasksScreen(viewModel: EmpleadoViewModel) {
    val scrollState = rememberScrollState()
    val tasks = viewModel.tasks

    val pendingTasks = tasks.filter { it.estado == TaskStatus.PENDIENTE }
    val inProgressTasks = tasks.filter { it.estado == TaskStatus.EN_PROGRESO }
    val completedTasks = tasks.filter { it.estado == TaskStatus.COMPLETADA }

    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var targetStatus by remember { mutableStateOf<TaskStatus?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Tablero Kanban",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Column 1: Pendiente
            WorkerTaskColumn(
                title = "📋 Pendiente",
                headerColor = Color(0xFFFF9F0A),
                tasks = pendingTasks,
                onMoveToInProgress = { task -> 
                    selectedTask = task
                    targetStatus = TaskStatus.EN_PROGRESO
                    showDialog = true
                },
                onMoveToCompleted = null
            )
            // Column 2: En Proceso
            WorkerTaskColumn(
                title = "🔄 En Proceso",
                headerColor = Color(0xFF0A84FF),
                tasks = inProgressTasks,
                onMoveToInProgress = null,
                onMoveToCompleted = { task -> 
                    selectedTask = task
                    targetStatus = TaskStatus.COMPLETADA
                    showDialog = true
                }
            )
            // Column 3: Terminado
            WorkerTaskColumn(
                title = "✅ Terminado",
                headerColor = Color(0xFF30D158),
                tasks = completedTasks,
                onMoveToInProgress = null,
                onMoveToCompleted = null
            )
        }
    }

    if (showDialog && selectedTask != null && targetStatus != null) {
        var hours by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Actualizar Tarea", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Hora de registro: $currentTime", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("Horas Trabajadas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Novedades / Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val floatHours = hours.toDoubleOrNull() ?: 0.0
                    viewModel.updateTaskProgress(
                        taskId = selectedTask!!.id!!,
                        nuevasHoras = floatHours,
                        novedades = notes,
                        severidad = selectedTask!!.severidadNovedad,
                        estado = targetStatus!!
                    ) {
                        showDialog = false
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun WorkerTaskColumn(
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
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
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Horas Reportadas: ${task.horasEfectivas}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
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

                            if (onMoveToInProgress != null) {
                                TextButton(
                                    onClick = { onMoveToInProgress(task) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("→ Proceso", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0A84FF))
                                }
                            }

                            if (onMoveToCompleted != null) {
                                TextButton(
                                    onClick = { onMoveToCompleted(task) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("→ Terminado", style = MaterialTheme.typography.bodySmall, color = Color(0xFF30D158))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
