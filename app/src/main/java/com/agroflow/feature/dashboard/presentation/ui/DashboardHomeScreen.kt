package com.agroflow.feature.dashboard.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.R
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.finance.data.BalanceResponse
import com.agroflow.feature.finance.presentation.FinanceViewModel
import com.agroflow.feature.inventory.data.InventoryItem
import com.agroflow.feature.inventory.presentation.InventoryViewModel
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import com.agroflow.feature.personnel.data.Finca

// Colors based on AgroFlow Theme instructions
private val NeonYellow = Color(0xFFF4E245)
private val AppleGreen = Color(0xFF30D158)
private val AppleRed = Color(0xFFFF453A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHomeScreen(
    financeViewModel: FinanceViewModel = viewModel(),
    inventoryViewModel: InventoryViewModel = viewModel(),
    personnelViewModel: PersonnelViewModel = viewModel(),
    onNavigateToTab: (Int) -> Unit = {}
) {
    val userName = SessionManager.userName ?: SessionManager.userEmail ?: "Productor"
    val selectedFinca = personnelViewModel.selectedFinca

    LaunchedEffect(selectedFinca) {
        selectedFinca?.id?.let { fincaId ->
            financeViewModel.loadBalance(fincaId)
            inventoryViewModel.loadInventory(fincaId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Header
        item {
            HeaderRow(userName = userName)
        }

        // B. Card de Fincas
        item {
            FincasSelectionCard(personnelViewModel = personnelViewModel)
        }

        // C. Card de Empleados
        item {
            EmployeesCard(onNavigateToTab = onNavigateToTab)
        }

        // D. Gráfica de Finanzas (Ingresos vs Egresos)
        item {
            FinanceChartCard(balance = financeViewModel.balance)
        }

        // E. Gráfica de Productos (Top 5)
        item {
            InventoryChartCard(items = inventoryViewModel.items)
        }
    }
}

@Composable
fun HeaderRow(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola $userName",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bienvenido a tu panel de control",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FincasSelectionCard(personnelViewModel: PersonnelViewModel) {
    val fincas = personnelViewModel.fincas
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(fincas) {
        if (personnelViewModel.selectedFinca == null && fincas.isNotEmpty()) {
            personnelViewModel.selectedFinca = fincas.first()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Finca Activa",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                TextField(
                    value = personnelViewModel.selectedFinca?.nombre ?: "Seleccione una finca",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (fincas.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay fincas disponibles") },
                            onClick = { expanded = false }
                        )
                    } else {
                        fincas.forEach { finca ->
                            DropdownMenuItem(
                                text = { Text(finca.nombre) },
                                onClick = {
                                    personnelViewModel.selectedFinca = finca
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeesCard(onNavigateToTab: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToTab(2) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Empleados",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Gestión de Empleados",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ver y administrar personal",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ir a empleados",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FinanceChartCard(balance: BalanceResponse?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Resumen Financiero",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (balance == null) {
                Text(
                    text = "No hay datos financieros para mostrar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ingresos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Text("$${balance.totalIngresos}", fontWeight = FontWeight.Bold, color = AppleGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Egresos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Text("$${balance.totalEgresos}", fontWeight = FontWeight.Bold, color = AppleRed)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                FinanceCanvasChart(ingresos = balance.totalIngresos, egresos = balance.totalEgresos)
            }
        }
    }
}

@Composable
fun FinanceCanvasChart(ingresos: Double, egresos: Double) {
    val total = ingresos + egresos
    val ingresosPct = if (total > 0) (ingresos / total).toFloat() else 0.5f
    val egresosPct = if (total > 0) (egresos / total).toFloat() else 0.5f

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width * 0.25f

        val paintLabels = android.graphics.Paint().apply {
            color = onSurfaceColor
            textSize = 12.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Ingresos Bar
        val maxBarHeight = height * 0.8f
        val ingresosHeight = if (total > 0) maxBarHeight * ingresosPct else maxBarHeight * 0.1f
        val xIngresos = width * 0.25f - barWidth / 2
        val yIngresos = height * 0.8f - ingresosHeight
        drawRoundRect(
            color = AppleGreen,
            topLeft = Offset(xIngresos, yIngresos),
            size = Size(barWidth, ingresosHeight),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        drawContext.canvas.nativeCanvas.drawText(
            "Ingresos",
            width * 0.25f,
            height,
            paintLabels
        )

        // Egresos Bar
        val egresosHeight = if (total > 0) maxBarHeight * egresosPct else maxBarHeight * 0.1f
        val xEgresos = width * 0.75f - barWidth / 2
        val yEgresos = height * 0.8f - egresosHeight
        drawRoundRect(
            color = AppleRed,
            topLeft = Offset(xEgresos, yEgresos),
            size = Size(barWidth, egresosHeight),
            cornerRadius = CornerRadius(8.dp.toPx())
        )
        drawContext.canvas.nativeCanvas.drawText(
            "Egresos",
            width * 0.75f,
            height,
            paintLabels
        )
        
        // Base line
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, height * 0.8f),
            end = Offset(width, height * 0.8f),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun InventoryChartCard(items: List<InventoryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Top 5 Productos en Inventario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                Text(
                    text = "No hay productos en el inventario.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            } else {
                val top5 = items.sortedByDescending { it.cantidad }.take(5)
                InventoryCanvasChart(top5 = top5)
            }
        }
    }
}

@Composable
fun InventoryCanvasChart(top5: List<InventoryItem>) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height

        val maxCantidad = top5.maxOfOrNull { it.cantidad } ?: 1.0
        val safeMaxCantidad = if (maxCantidad > 0) maxCantidad else 1.0

        val barWidth = (width * 0.6f) / top5.size
        val spacing = (width * 0.4f) / (top5.size + 1)

        val paintLabels = android.graphics.Paint().apply {
            color = onSurfaceColor
            textSize = 11.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val paintValues = android.graphics.Paint().apply {
            color = onSurfaceVariantColor
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }

        top5.forEachIndexed { index, item ->
            val factor = (item.cantidad / safeMaxCantidad).toFloat()
            val barHeight = (height * 0.7f) * factor
            val x = spacing + (index * (barWidth + spacing))
            val y = (height * 0.75f) - barHeight

            drawRoundRect(
                color = NeonYellow,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            // Name label (truncated)
            val displayName = if (item.nombreItem.length > 8) {
                item.nombreItem.substring(0, 6) + ".."
            } else {
                item.nombreItem
            }
            drawContext.canvas.nativeCanvas.drawText(
                displayName,
                x + barWidth / 2,
                height * 0.92f,
                paintLabels
            )

            // Value label
            drawContext.canvas.nativeCanvas.drawText(
                "${item.cantidad.toInt()} ${item.unidadMedida}",
                x + barWidth / 2,
                y - 6.dp.toPx(),
                paintValues
            )
        }

        // Base line
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, height * 0.75f),
            end = Offset(width, height * 0.75f),
            strokeWidth = 2.dp.toPx()
        )
    }
}
