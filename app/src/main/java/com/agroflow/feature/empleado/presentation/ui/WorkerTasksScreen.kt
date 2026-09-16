package com.agroflow.feature.empleado.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroflow.feature.empleado.presentation.EmpleadoViewModel
import com.agroflow.feature.tasks.data.Task
import com.agroflow.feature.tasks.data.TaskStatus
import com.agroflow.core.session.SessionManager

@Composable
fun WorkerTasksScreen(viewModel: EmpleadoViewModel) {
    val tasks = viewModel.tasks
    val pendingTasks = tasks.filter { it.estado == TaskStatus.PENDIENTE }
    val inProgressTasks = tasks.filter { it.estado == TaskStatus.EN_PROGRESO }
    val completedTasks = tasks.filter { it.estado == TaskStatus.COMPLETADA }

    var targetStatus by remember { mutableStateOf<TaskStatus?>(null) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gestión de Tareas",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tablero Kanban",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(rememberScrollState())
        ) {
            // Columna Pendiente
            TaskColumnWorker(
                title = "📋 Pendiente",
                headerColor = Color(0xFFFF9F0A),
                tasks = pendingTasks,
                onMoveStatus = { t, s -> 
                    selectedTask = t
                    targetStatus = s
                    showDialog = true
                }
            )

            // Columna En Progreso
            TaskColumnWorker(
                title = "🔄 En Proceso",
                headerColor = Color(0xFF0A84FF),
                tasks = inProgressTasks,
                onMoveStatus = { t, s -> 
                    selectedTask = t
                    targetStatus = s
                    showDialog = true
                }
            )

            // Columna Completada
            TaskColumnWorker(
                title = "✅ Terminado",
                headerColor = Color(0xFF30D158),
                tasks = completedTasks,
                onMoveStatus = { t, s -> 
                    selectedTask = t
                    targetStatus = s
                    showDialog = true
                }
            )
        }
    }

    if (showDialog && selectedTask != null && targetStatus != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Mover Tarea") },
            text = { Text("¿Deseas mover '${selectedTask?.titulo}' a ${targetStatus?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = if (targetStatus == TaskStatus.COMPLETADA) 2.0 else 0.0 // Default or logic
                        viewModel.updateTaskProgress(selectedTask!!.id!!, hours, "Actualizado por trabajador", null, targetStatus!!) {
                            viewModel.loadTasks() // recargar
                        }
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C7A4B))
                ) {
                    Text("Mover")
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
fun TaskColumnWorker(
    title: String,
    headerColor: Color,
    tasks: List<Task>,
    onMoveStatus: (Task, TaskStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .padding(end = 16.dp)
            .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp)) // Premium Green background
    ) {
        Surface(
            color = headerColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = headerColor
                )
                Surface(
                    shape = CircleShape,
                    color = headerColor,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(tasks.size.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->
                TaskCardWorker(task = task, onMoveStatus = { newStatus -> onMoveStatus(task, newStatus) })
            }
        }
    }
}

@Composable
fun TaskCardWorker(
    task: Task,
    onMoveStatus: (TaskStatus) -> Unit
) {
    val currentStatus = task.estado
    val targetStatusLeft = when (currentStatus) {
        TaskStatus.EN_PROGRESO -> TaskStatus.PENDIENTE
        TaskStatus.COMPLETADA -> TaskStatus.EN_PROGRESO
        else -> null
    }
    val targetStatusRight = when (currentStatus) {
        TaskStatus.PENDIENTE -> TaskStatus.EN_PROGRESO
        TaskStatus.EN_PROGRESO -> TaskStatus.COMPLETADA
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.titulo,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1C1C1E)
            )
            if (!task.descripcion.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = task.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1C1C1E).copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Mostrar el tiempo si está completada
            if (currentStatus == TaskStatus.COMPLETADA) {
                Spacer(Modifier.height(8.dp))
                val hours = task.horasEfectivas.toInt()
                val minutes = ((task.horasEfectivas - hours) * 60).toInt()
                Text(
                    text = "Demoró: ${hours}h ${minutes}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2C7A4B),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (targetStatusLeft != null) {
                    AssistChip(
                        onClick = { onMoveStatus(targetStatusLeft) },
                        label = { Text("← ${if (targetStatusLeft == TaskStatus.PENDIENTE) "Pendiente" else "En Proceso"}", fontSize = 11.sp, color = Color(0xFF1B5E20)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF1F8E9))
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (targetStatusRight != null) {
                    AssistChip(
                        onClick = { onMoveStatus(targetStatusRight) },
                        label = { Text("${if (targetStatusRight == TaskStatus.EN_PROGRESO) "Iniciar →" else "Completar ✓"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE8F5E9))
                    )
                }
            }
        }
    }
}
