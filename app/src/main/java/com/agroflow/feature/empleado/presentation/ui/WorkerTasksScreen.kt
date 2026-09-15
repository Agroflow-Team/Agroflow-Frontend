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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
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
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tablero Kanban de Tareas",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Arrastra o pulsa los botones para mover",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Columna 1: Pendiente
            WorkerInteractiveColumn(
                title = "📋 Pendiente",
                columnStatus = TaskStatus.PENDIENTE,
                headerColor = Color(0xFFFF9F0A),
                tasks = pendingTasks,
                onRequestMove = { task, newStatus ->
                    selectedTask = task
                    targetStatus = newStatus
                    showDialog = true
                }
            )

            // Columna 2: En Proceso
            WorkerInteractiveColumn(
                title = "🔄 En Proceso",
                columnStatus = TaskStatus.EN_PROGRESO,
                headerColor = Color(0xFF0A84FF),
                tasks = inProgressTasks,
                onRequestMove = { task, newStatus ->
                    selectedTask = task
                    targetStatus = newStatus
                    showDialog = true
                }
            )

            // Columna 3: Terminado
            WorkerInteractiveColumn(
                title = "✅ Terminado",
                columnStatus = TaskStatus.COMPLETADA,
                headerColor = Color(0xFF30D158),
                tasks = completedTasks,
                onRequestMove = { task, newStatus ->
                    selectedTask = task
                    targetStatus = newStatus
                    showDialog = true
                }
            )
        }
    }

    if (showDialog && selectedTask != null && targetStatus != null) {
        var hours by remember { mutableStateOf(if (selectedTask!!.horasEfectivas > 0.0) selectedTask!!.horasEfectivas.toString() else "") }
        var notes by remember { mutableStateOf(selectedTask!!.novedades ?: "") }
        val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = { 
                Text(
                    text = "Mover a ${when(targetStatus!!) {
                        TaskStatus.PENDIENTE -> "Pendiente"
                        TaskStatus.EN_PROGRESO -> "En Proceso"
                        TaskStatus.COMPLETADA -> "Terminado"
                        else -> targetStatus!!.name
                    }}", 
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column {
                    Text("Tarea: ${selectedTask!!.titulo}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text("Hora actual: $currentTime", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("Horas Trabajadas / Invertidas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Novedades o Comentarios") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val floatHours = hours.toDoubleOrNull() ?: selectedTask!!.horasEfectivas
                        viewModel.updateTaskProgress(
                            taskId = selectedTask!!.id!!,
                            nuevasHoras = floatHours,
                            novedades = notes,
                            severidad = selectedTask!!.severidadNovedad,
                            estado = targetStatus!!
                        ) {
                            showDialog = false
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar Avance")
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
fun WorkerInteractiveColumn(
    title: String,
    columnStatus: TaskStatus,
    headerColor: Color,
    tasks: List<Task>,
    onRequestMove: (Task, TaskStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .width(290.dp)
            .fillMaxHeight()
            .background(Color(0xFFF7F5F0), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Surface(
            color = headerColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = headerColor,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = CircleShape,
                    color = headerColor,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${tasks.size}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay tareas en este estado", style = MaterialTheme.typography.bodySmall, color = Color.Gray.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks, key = { it.id ?: it.titulo }) { task ->
                    WorkerDraggableCard(
                        task = task,
                        currentStatus = columnStatus,
                        onRequestMove = { newStatus -> onRequestMove(task, newStatus) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkerDraggableCard(
    task: Task,
    currentStatus: TaskStatus,
    onRequestMove: (TaskStatus) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val dragThreshold = 90f

    val targetStatusRight = when (currentStatus) {
        TaskStatus.PENDIENTE -> TaskStatus.EN_PROGRESO
        TaskStatus.EN_PROGRESO -> TaskStatus.COMPLETADA
        TaskStatus.COMPLETADA -> null
        TaskStatus.CANCELADA -> null
    }

    val targetStatusLeft = when (currentStatus) {
        TaskStatus.COMPLETADA -> TaskStatus.EN_PROGRESO
        TaskStatus.EN_PROGRESO -> TaskStatus.PENDIENTE
        TaskStatus.PENDIENTE -> null
        TaskStatus.CANCELADA -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.toInt(), 0) }
            .pointerInput(currentStatus) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        if (offsetX > dragThreshold && targetStatusRight != null) {
                            onRequestMove(targetStatusRight)
                        } else if (offsetX < -dragThreshold && targetStatusLeft != null) {
                            onRequestMove(targetStatusLeft)
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetX = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val newOffset = offsetX + dragAmount
                    offsetX = newOffset.coerceIn(-180f, 180f)
                }
            }
            .shadow(
                elevation = if (isDragging) 8.dp else 2.dp,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                offsetX > dragThreshold -> Color(0xFFE8F5E9)
                offsetX < -dragThreshold -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (isDragging) {
                val hintText = if (offsetX > 0 && targetStatusRight != null) {
                    "▶ Mover a ${targetStatusRight.name}"
                } else if (offsetX < 0 && targetStatusLeft != null) {
                    "◀ Mover a ${targetStatusLeft.name}"
                } else {
                    "↔ Arrastra para cambiar estado"
                }
                Text(
                    text = hintText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Text(
                text = task.titulo,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!task.descripcion.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = task.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

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

                Text(
                    text = "⏱ Horas: ${task.horasEfectivas}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            // Botones de acción rápida
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (targetStatusLeft != null) {
                    AssistChip(
                        onClick = { onRequestMove(targetStatusLeft) },
                        label = { Text("◀ ${if (targetStatusLeft == TaskStatus.PENDIENTE) "Pendiente" else "En Proceso"}", fontSize = 11.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF0F0F0))
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (targetStatusRight != null) {
                    AssistChip(
                        onClick = { onRequestMove(targetStatusRight) },
                        label = { Text("${if (targetStatusRight == TaskStatus.EN_PROGRESO) "Iniciar ▶" else "Completar ✅"}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), labelColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
