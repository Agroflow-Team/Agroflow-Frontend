package com.agroflow.feature.empleado.presentation.ui

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agroflow.feature.empleado.presentation.EmpleadoViewModel
import com.agroflow.feature.tasks.data.TaskStatus
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerInfoScreen(viewModel: EmpleadoViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Hoy, 1: Semana, 2: Mes

    val completedTasks = viewModel.tasks.filter { it.estado == TaskStatus.COMPLETADA }
    val today = LocalDate.now()

    val filteredTasks = when (selectedTab) {
        0 -> completedTasks.filter { 
            val d = try { LocalDate.parse(it.fechaCompletada?.take(10) ?: "") } catch(e: Exception) { today }
            d.isEqual(today) 
        }
        1 -> completedTasks.filter { 
            val d = try { LocalDate.parse(it.fechaCompletada?.take(10) ?: "") } catch(e: Exception) { today.minusDays(10) }
            d.isAfter(today.minusWeeks(1)) || d.isEqual(today.minusWeeks(1))
        }
        2 -> completedTasks.filter { 
            val d = try { LocalDate.parse(it.fechaCompletada?.take(10) ?: "") } catch(e: Exception) { today.minusDays(40) }
            d.isAfter(today.minusMonths(1)) || d.isEqual(today.minusMonths(1))
        }
        else -> completedTasks
    }

    val horasEnFiltro = filteredTasks.sumOf { it.horasEfectivas }
    val pagoEnFiltro = horasEnFiltro * 5000.0 // 5000 por hora como ejemplo

    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Información Financiera",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = {
                    try {
                        val pdfDocument = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
                        val page = pdfDocument.startPage(pageInfo)
                        val canvas = page.canvas
                        val paint = Paint()

                        paint.textSize = 16f
                        paint.isFakeBoldText = true
                        canvas.drawText("Recibo de Pago AgroFlow", 50f, 50f, paint)

                        paint.textSize = 12f
                        paint.isFakeBoldText = false
                        canvas.drawText("Trabajador: Operario", 20f, 100f, paint)
                        canvas.drawText("Filtro: ${listOf("Hoy", "Semana", "Mes")[selectedTab]}", 20f, 130f, paint)
                        canvas.drawText("Horas Totales: $horasEnFiltro", 20f, 160f, paint)
                        
                        paint.isFakeBoldText = true
                        canvas.drawText("Total a Pagar: ${formatter.format(pagoEnFiltro)}", 20f, 200f, paint)

                        pdfDocument.finishPage(page)

                        val file = File(context.cacheDir, "recibo_pago_${System.currentTimeMillis()}.pdf")
                        pdfDocument.writeTo(FileOutputStream(file))
                        pdfDocument.close()

                        Toast.makeText(context, "PDF guardado en descargas (cache)", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            ) {
                Text("Descargar PDF")
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            listOf("Hoy", "Esta Semana", "Este Mes").forEachIndexed { index, label ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(label) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen ($horasEnFiltro horas)", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Pago Estimado: ${formatter.format(pagoEnFiltro)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text("Tareas Completadas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn {
            items(filteredTasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(task.titulo, fontWeight = FontWeight.Bold)
                            Text(task.fechaCompletada?.take(10) ?: "Fecha no disponible", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${task.horasEfectivas} hrs", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
