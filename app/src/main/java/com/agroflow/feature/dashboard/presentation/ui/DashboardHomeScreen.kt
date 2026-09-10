package com.agroflow.feature.dashboard.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardHomeScreen(onNavigateToTab: (Int) -> Unit = {}) {
    val financeViewModel: com.agroflow.feature.finance.presentation.FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val balance = financeViewModel.balance

    LaunchedEffect(com.agroflow.core.session.SessionManager.fincaId) {
        val fId = com.agroflow.core.session.SessionManager.fincaId
        if (fId != null) {
            financeViewModel.loadBalance(fId)
        }
    }

    val currentDate = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "ES")).format(Date())
    }

    val summaryCards = remember {
        listOf(
            Triple("📦", "Productos", "124"),
            Triple("👥", "Empleados", "12"),
            Triple("✅", "Tareas Pendientes", "5"),
            Triple("💰", "Balance", "$4,500")
        )
    }

    val promotions = remember {
        listOf(
            "Semillas de Maíz" to "20%",
            "Fertilizante NPK" to "15%",
            "Tractor Rental" to "10%",
            "Sistema de Riego" to "25%"
        )
    }

    val notifications = remember {
        listOf(
            "Nuevo empleado registrado" to "Hace 10 min",
            "Cosecha de tomate completada" to "Hace 2 horas",
            "Inventario de fertilizantes bajo" to "Hace 5 horas",
            "Pago recibido por 100 kg papa" to "Ayer",
            "Tarea 'Riego sector A' asignada" to "Ayer"
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3EFE7))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Welcome header
        item {
            Column {
                Text(
                    text = "Bienvenido a AgroFlow",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E)
                )
                Text(
                    text = currentDate.replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        }

        // 2. Summary cards row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(modifier = Modifier.weight(1f), icon = summaryCards[0].first, title = summaryCards[0].second, value = summaryCards[0].third, onClick = { onNavigateToTab(4) })
                    SummaryCard(modifier = Modifier.weight(1f), icon = summaryCards[1].first, title = summaryCards[1].second, value = summaryCards[1].third, onClick = { onNavigateToTab(2) })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(modifier = Modifier.weight(1f), icon = summaryCards[2].first, title = summaryCards[2].second, value = summaryCards[2].third, onClick = { onNavigateToTab(3) })
                    SummaryCard(modifier = Modifier.weight(1f), icon = summaryCards[3].first, title = summaryCards[3].second, value = summaryCards[3].third, onClick = { onNavigateToTab(6) })
                }
            }
        }


        // 3. Consumption chart section
        item {
            Column {
                Text(
                    text = "Consumo Semanal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        val transactions = balance?.transacciones ?: emptyList()
                        val chartData = FloatArray(7) { 0f }
                        // Basic mock logic: fill the chart based on recent transactions amounts
                        if (transactions.isNotEmpty()) {
                            val maxAmount = transactions.maxOf { it.montoTotal }.toFloat().coerceAtLeast(1f)
                            transactions.take(7).forEachIndexed { index, t ->
                                chartData[index % 7] += (t.montoTotal.toFloat() / maxAmount).coerceAtMost(1f)
                            }
                        } else {
                            // If no data, show empty
                        }
                        WeeklyConsumptionChart(chartData.toList().map { it.coerceIn(0.1f, 1f) }.takeIf { transactions.isNotEmpty() } ?: listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f))
                    }
                }
            }
        }

        // 4. Promotions section
        item {
            Column {
                Text(
                    text = "Promociones",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                val vitrinaViewModel: com.agroflow.feature.vitrina.presentation.VitrinaViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val publicaciones by vitrinaViewModel.publicacionesActivas.collectAsState()

                LaunchedEffect(Unit) {
                    vitrinaViewModel.loadPublicacionesActivas()
                }

                if (publicaciones.isEmpty()) {
                    Text("No hay productos en el catálogo actualmente.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(publicaciones) { pub ->
                            Card(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(130.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C7A4B))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = pub.tituloProducto,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$${pub.precio}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color(0xFFFFD60A),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Disp: ${pub.cantidadDisponible}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryCard(modifier: Modifier = Modifier, icon: String, title: String, value: String, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 14.sp, color = Color(0xFF8E8E93))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1C1E))
        }
    }
}

@Composable
fun PromotionCard(title: String, discount: String) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2C7A4B), Color(0xFFA5D6A7))
    )
    Box(
        modifier = Modifier
            .width(200.dp)
            .background(brush = gradient, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$discount Dcto",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun NotificationCard(message: String, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🔔", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = message, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1C1C1E))
                Text(text = time, fontSize = 12.sp, color = Color(0xFF8E8E93))
            }
        }
    }
}

@Composable
fun WeeklyConsumptionChart(data: List<Float> = listOf(0.4f, 0.7f, 0.3f, 0.8f, 0.5f, 0.9f, 0.6f)) {
    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val barWidth = size.width / (data.size * 2)
            val maxBarHeight = size.height - 30.dp.toPx()

            data.forEachIndexed { index, value ->
                val barHeight = maxBarHeight * value
                val x = index * (size.width / data.size) + (size.width / data.size - barWidth) / 2
                val y = maxBarHeight - barHeight

                drawRoundRect(
                    color = Color(0xFF2C7A4B),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
