package com.agroflow.feature.tasks.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.personnel.data.Trabajador
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import com.agroflow.feature.tasks.data.CreateTaskRequest
import com.agroflow.feature.tasks.data.Task
import com.agroflow.feature.tasks.data.TaskStatus
import com.agroflow.feature.tasks.data.UpdateProgressRequest
import com.agroflow.feature.tasks.presentation.TaskUiState
import com.agroflow.feature.tasks.presentation.TaskViewModel
import kotlin.random.Random

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(personnelViewModel: PersonnelViewModel, taskViewModel: TaskViewModel = viewModel()) {
    val finca = personnelViewModel.selectedFinca ?: personnelViewModel.fincas.firstOrNull()
    val isTrabajador = SessionManager.roleId?.equals(SessionManager.ROLE_TRABAJADOR, ignoreCase = true) == true

    // Auto-seleccionar finca si aún no está seleccionada pero existen en la lista
    LaunchedEffect(personnelViewModel.fincas) {
        if (personnelViewModel.selectedFinca == null && personnelViewModel.fincas.isNotEmpty()) {
            personnelViewModel.selectedFinca = personnelViewModel.fincas.first()
        }
    }

    // Cargar tareas y trabajadores al entrar a la pantalla
    LaunchedEffect(finca) {
        if (finca != null) {
            taskViewModel.loadTasksByFinca(finca.id)
            personnelViewModel.loadTrabajadores(finca.id)
        } else {
            personnelViewModel.loadTrabajadores()
        }
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (finca == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🏡 Por favor, selecciona una finca para ver sus tareas.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    if (!isTrabajador) {
                        Button(onClick = { personnelViewModel.loadFincas() }) {
                            Text("Cargar Fincas")
                        }
                    }
                }
            }
            return@Column
        }

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
                    text = "Finca: ${finca.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (!isTrabajador) {
                Button(
                    onClick = { 
                        personnelViewModel.loadTrabajadores(finca.id)
                        showCreateDialog = true 
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Nueva Tarea", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (taskViewModel.uiState is TaskUiState.Loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
        }

        val allTasks = taskViewModel.tasks
        val pendingTasks = allTasks.filter { it.estado == TaskStatus.PENDIENTE }
        val inProgressTasks = allTasks.filter { it.estado == TaskStatus.EN_PROGRESO }
        val completedTasks = allTasks.filter { it.estado == TaskStatus.COMPLETADA }

        // Tablero Kanban con soporte Drag & Drop
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Columna 1: Pendiente
            InteractiveTaskColumn(
                title = "📋 Pendiente",
                columnStatus = TaskStatus.PENDIENTE,
                headerColor = Color(0xFFFF9F0A),
                tasks = pendingTasks,
                trabajadores = personnelViewModel.trabajadores,
                onStatusChange = { task, newStatus ->
                    taskViewModel.updateProgress(
                        task.id!!,
                        UpdateProgressRequest(task.trabajadorId, task.horasReales ?: 0.0, task.novedades ?: "", task.severidadNovedad, newStatus)
                    ) {
                        taskViewModel.loadTasksByFinca(finca.id)
                    }
                }
            )

            // Columna 2: En Proceso
            InteractiveTaskColumn(
                title = "🔄 En Proceso",
                columnStatus = TaskStatus.EN_PROGRESO,
                headerColor = Color(0xFF0A84FF),
                tasks = inProgressTasks,
                trabajadores = personnelViewModel.trabajadores,
                onStatusChange = { task, newStatus ->
                    taskViewModel.updateProgress(
                        task.id!!,
                        UpdateProgressRequest(task.trabajadorId, task.horasReales ?: 0.0, task.novedades ?: "", task.severidadNovedad, newStatus)
                    ) {
                        taskViewModel.loadTasksByFinca(finca.id)
                    }
                }
            )

            // Columna 3: Terminado
            InteractiveTaskColumn(
                title = "✅ Terminado",
                columnStatus = TaskStatus.COMPLETADA,
                headerColor = Color(0xFF30D158),
                tasks = completedTasks,
                trabajadores = personnelViewModel.trabajadores,
                onStatusChange = { task, newStatus ->
                    taskViewModel.updateProgress(
                        task.id!!,
                        UpdateProgressRequest(task.trabajadorId, task.horasReales ?: 0.0, task.novedades ?: "", task.severidadNovedad, newStatus)
                    ) {
                        taskViewModel.loadTasksByFinca(finca.id)
                    }
                }
            )
        }
    }

    // Diálogo para crear nueva tarea
    if (showCreateDialog) {
        var titulo by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        var trabajador by remember { mutableStateOf<Trabajador?>(null) }
        val listaTrabajadores = personnelViewModel.trabajadores

        AlertDialog(
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Asignar Nueva Tarea", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column {
                    Text(
                        text = "Selecciona el trabajador y completa los detalles de la labor:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(12.dp))

                    // Selector de trabajador
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        TextField(
                            value = trabajador?.let { "👤 ${it.nombreCompleto}" } ?: if (listaTrabajadores.isEmpty()) "Sin trabajadores registrados" else "Seleccionar Trabajador",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedContainerColor = Color(0xFFF3EFE7),
                                unfocusedContainerColor = Color(0xFFF3EFE7).copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (listaTrabajadores.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No hay trabajadores. Pulsa para recargar", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        personnelViewModel.loadTrabajadores(finca?.id)
                                    }
                                )
                            } else {
                                listaTrabajadores.forEach { t ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("👤 ${t.nombreCompleto}", fontWeight = FontWeight.Bold)
                                                Text("Doc: ${t.documento} • Tarifa: $${t.tarifaHora}/h", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            trabajador = t
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    TextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        placeholder = { Text("Título de la tarea (Ej: Podar lote 2)") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3EFE7),
                            unfocusedContainerColor = Color(0xFFF3EFE7).copy(alpha = 0.6f),
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
                        placeholder = { Text("Descripción de la labor a realizar...") },
                        maxLines = 3,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF3EFE7),
                            unfocusedContainerColor = Color(0xFFF3EFE7).copy(alpha = 0.6f),
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
                        val fincaId = finca?.id ?: t?.fincaId
                        if (t != null && titulo.isNotBlank() && fincaId != null) {
                            val request = CreateTaskRequest(
                                fincaId = fincaId,
                                trabajadorId = t.id,
                                loteId = null,
                                titulo = titulo,
                                descripcion = descripcion,
                                estado = TaskStatus.PENDIENTE
                            )
                            taskViewModel.createTask(request) {
                                taskViewModel.loadTasksByFinca(fincaId)
                                showCreateDialog = false
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = trabajador != null && titulo.isNotBlank()
                ) {
                    Text("Asignar y Notificar", color = MaterialTheme.colorScheme.onPrimary)
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
fun InteractiveTaskColumn(
    title: String,
    columnStatus: TaskStatus,
    headerColor: Color,
    tasks: List<Task>,
    trabajadores: List<Trabajador>,
    onStatusChange: (Task, TaskStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .width(290.dp)
            .fillMaxHeight()
            .background(Color(0xFFE8F5E9), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        // Encabezado de Columna
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Arrastra tareas aquí",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks, key = { it.id ?: it.titulo }) { task ->
                    val workerName = trabajadores.find { it.id == task.trabajadorId || it.usuarioId == task.trabajadorId }?.nombreCompleto ?: "Trabajador"
                    
                    DraggableKanbanCard(
                        task = task,
                        workerName = workerName,
                        currentStatus = columnStatus,
                        onMoveStatus = { newStatus ->
                            onStatusChange(task, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableKanbanCard(
    task: Task,
    workerName: String,
    currentStatus: TaskStatus,
    onMoveStatus: (TaskStatus) -> Unit
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
                            onMoveStatus(targetStatusRight)
                        } else if (offsetX < -dragThreshold && targetStatusLeft != null) {
                            onMoveStatus(targetStatusLeft)
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
                    // Limitar rango de arrastre
                    offsetX = newOffset.coerceIn(-180f, 180f)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                offsetX > dragThreshold -> Color(0xFFE8F5E9)
                offsetX < -dragThreshold -> Color(0xFFFFF3E0)
                else -> Color.White
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Indicador visual de arrastre
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

            Spacer(Modifier.height(8.dp))

            // Info de trabajador y horas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 $workerName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (task.horasEfectivas > 0.0) {
                    Text(
                        text = "⏱ ${task.horasEfectivas}h",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
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

            // Botones de acción rápida entre columnas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (targetStatusLeft != null) {
                    AssistChip(
                        onClick = { onMoveStatus(targetStatusLeft) },
                        label = { Text("◀ ${if (targetStatusLeft == TaskStatus.PENDIENTE) "Pendiente" else "En Proceso"}", fontSize = 11.sp, color = Color(0xFF1B5E20)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF1F8E9))
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (targetStatusRight != null) {
                    AssistChip(
                        onClick = { onMoveStatus(targetStatusRight) },
                        label = { Text("${if (targetStatusRight == TaskStatus.EN_PROGRESO) "Iniciar ▶" else "Completar ✅"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE8F5E9))
                    )
                }
            }
        }
    }
}
