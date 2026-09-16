package com.agroflow.feature.empleado.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroflow.feature.empleado.presentation.EmpleadoViewModel
import com.agroflow.feature.tasks.data.TaskStatus
import com.agroflow.core.session.SessionManager

// Colors based on AgroFlow Theme instructions
private val AgroFlowGreen = Color(0xFF2C7A4B)
private val AppleDarkGrey = Color(0xFF1C1C1E)
private val AgroFlowBackground = Color(0xFFF1F8E9)

@Composable
fun WorkerHomeScreen(viewModel: EmpleadoViewModel, onNavigateToTasks: () -> Unit = {}) {
    val userName = viewModel.nombreTrabajador ?: SessionManager.userEmail ?: "Trabajador"
    val misTareas = viewModel.tasks
    val pendingTasks = misTareas.filter { it.estado == TaskStatus.PENDIENTE }
    val totalHoras = viewModel.totalHorasTrabajadas

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AgroFlowBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("¡Hola, $userName!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppleDarkGrey)
                    Text("Bienvenido a tu panel de control.", fontSize = 16.sp, color = Color.Gray)
                    
                    if (viewModel.lastError != null) {
                        Text(viewModel.lastError!!, fontSize = 12.sp, color = Color.Red)
                    }
                }
                IconButton(onClick = { viewModel.loadTasks() }) {
                    Icon(androidx.compose.material.icons.Icons.Default.Refresh, contentDescription = "Refrescar", tint = AgroFlowGreen)
                }
            }
        }

        // Tareas Pendientes Alert
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = "Alerta", tint = Color(0xFFFF9500), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Alertas de Tareas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
                        if (pendingTasks.isNotEmpty()) {
                            Text("Tienes ${pendingTasks.size} tareas pendientes.", color = Color.Gray, fontSize = 14.sp)
                        } else {
                            Text("No hay tareas urgentes.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Horas reportadas 
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Horas Reportadas", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${totalHoras}h", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AgroFlowGreen)
                    }
                }
            }
        }

        // Mis Tareas Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTasks() },
                colors = CardDefaults.cardColors(containerColor = AgroFlowGreen),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Mis Tareas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${misTareas.size} asignadas en total", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Ir", tint = Color.White)
                }
            }
        }

        // Progreso Semanal Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Progreso Semanal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppleDarkGrey
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val completedCount = misTareas.count { it.estado == TaskStatus.COMPLETADA }
                    val inProgressCount = misTareas.count { it.estado == TaskStatus.EN_PROGRESO }
                    val pendingCount = pendingTasks.size
                    
                    val completedTasksData = if (misTareas.isEmpty()) {
                        listOf(0, 0, 0, 0, 0, 0, 0)
                    } else {
                        listOf(pendingCount, inProgressCount, completedCount, 0, 0, 0, 0)
                    }
                    
                    WeeklyProgressCanvasChart(data = completedTasksData)
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressCanvasChart(data: List<Int>) {
    val onSurfaceColor = AppleDarkGrey.toArgb()
    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        val maxVal = data.maxOrNull() ?: 1
        val safeMaxVal = if (maxVal > 0) maxVal else 1

        val barWidth = (width * 0.5f) / data.size
        val spacing = (width * 0.5f) / (data.size + 1)

        val paint = android.graphics.Paint().apply {
            color = onSurfaceColor
            textSize = 12.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }

        data.forEachIndexed { index, value ->
            val factor = (value.toFloat() / safeMaxVal)
            val barHeight = (height * 0.7f) * factor
            val x = spacing + (index * (barWidth + spacing))
            val y = (height * 0.75f) - barHeight

            if (barHeight > 0) {
                drawRoundRect(
                    color = AgroFlowGreen,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            drawContext.canvas.nativeCanvas.drawText(
                days[index],
                x + barWidth / 2,
                height * 0.95f,
                paint
            )
        }
    }
}
