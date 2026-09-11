package com.agroflow.feature.dashboard.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroflow.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colors
private val OliveGreenBg = Color(0xFF5A714C)
private val OliveGreenCard = Color(0xFF6A8256)
private val NeonYellow = Color(0xFFF4E245)
private val WhiteMain = Color.White
private val WhiteSecondary = Color.White.copy(alpha = 0.7f)
private val AppleRed = Color(0xFFFF453A)
private val AppleGreen = Color(0xFF30D158)
private val WaterBlue = Color(0xFF82B1FF)

@Composable
fun DashboardHomeScreen(onNavigateToTab: (Int) -> Unit = {}) {
    val currentDate = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale("es", "ES")).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OliveGreenBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // A. Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "¡Hola, Productor!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteMain
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📍", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Estancia El Ombú, Buenos Aires",
                            fontSize = 14.sp,
                            color = WhiteSecondary
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp),
                    color = OliveGreenCard
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_logo),
                        contentDescription = "Perfil del Productor",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // B. Weather Card
        item {
            WeatherCard()
        }

        // C. Categories / Filter Pills
        item {
            CategoriesRow()
        }

        // D. Mis Lotes / Campos Card
        item {
            SectionCard(title = "Mis Lotes / Campos") {
                ParcelMapCanvas()
            }
        }

        // E. Existing Visual Modules (Tasks, Resources, Yield)
        item {
            SectionCard(title = "Gestión de Tareas") {
                DailyTasksList()
            }
        }

        item {
            SectionCard(title = "Consumo de Recursos") {
                ResourceConsumptionChart()
            }
        }

        item {
            SectionCard(title = "Rendimiento de Cultivos") {
                CropYieldChart()
            }
        }
    }
}

@Composable
fun WeatherCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OliveGreenCard),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "34°",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhiteMain,
                        lineHeight = 64.sp
                    )
                    Text(
                        text = "Parcialmente nublado",
                        fontSize = 16.sp,
                        color = WhiteSecondary
                    )
                }
                Text(text = "⛅", fontSize = 72.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherStat(icon = "💨", label = "Viento", value = "12 km/h")
                WeatherStat(icon = "💧", label = "Humedad", value = "45%")
                WeatherStat(icon = "🌧️", label = "Lluvia", value = "0 mm")
                WeatherStat(icon = "🌡️", label = "Sens.", value = "36°")
            }
        }
    }
}

@Composable
fun WeatherStat(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WhiteMain)
        Text(text = label, fontSize = 12.sp, color = WhiteSecondary)
    }
}

@Composable
fun CategoriesRow() {
    val categories = listOf("Todos", "Frutas", "Cereales", "Hortalizas")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            val isSelected = category == "Todos"
            val bgColor = if (isSelected) NeonYellow else Color.White.copy(alpha = 0.2f)
            val textColor = if (isSelected) Color.Black else WhiteMain
            
            Surface(
                color = bgColor,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = category,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OliveGreenCard),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteMain
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ParcelMapCanvas() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height

        val lotAWidth = width * 0.45f
        val lotAHeight = height * 0.8f
        
        // Lote A (Verde - Saludable)
        drawRoundRect(
            color = AppleGreen,
            topLeft = Offset(width * 0.05f, height * 0.1f),
            size = Size(lotAWidth, lotAHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        // Lote B (Rojo - Peste/Enfermedad)
        drawRoundRect(
            color = AppleRed,
            topLeft = Offset(width * 0.55f, height * 0.1f),
            size = Size(width * 0.4f, height * 0.35f),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        // Lote C (Amarillo Neón - Estrés Hídrico)
        drawRoundRect(
            color = NeonYellow,
            topLeft = Offset(width * 0.55f, height * 0.55f),
            size = Size(width * 0.4f, height * 0.35f),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        val paintWhite = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 14.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
        
        val paintBlack = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }

        drawContext.canvas.nativeCanvas.drawText(
            "Lote A",
            width * 0.05f + lotAWidth / 2,
            height * 0.1f + lotAHeight / 2 + 14.sp.toPx() / 3,
            paintWhite
        )

        drawContext.canvas.nativeCanvas.drawText(
            "Lote B",
            width * 0.55f + (width * 0.4f) / 2,
            height * 0.1f + (height * 0.35f) / 2 + 14.sp.toPx() / 3,
            paintWhite
        )

        drawContext.canvas.nativeCanvas.drawText(
            "Lote C",
            width * 0.55f + (width * 0.4f) / 2,
            height * 0.55f + (height * 0.35f) / 2 + 14.sp.toPx() / 3,
            paintBlack // Use black on neon yellow for better contrast
        )
    }
}

@Composable
fun ResourceConsumptionChart() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val pointCount = 4
        
        val aguaColor = WaterBlue
        val fertColor = NeonYellow

        val aguaPoints = listOf(0.3f, 0.5f, 0.4f, 0.7f)
        val fertPoints = listOf(0.4f, 0.4f, 0.6f, 0.5f)

        val stepX = width / (pointCount - 1)

        val aguaPath = Path()
        val fertPath = Path()

        aguaPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value * height)
            if (index == 0) {
                aguaPath.moveTo(x, y)
            } else {
                aguaPath.lineTo(x, y)
            }
            drawCircle(
                color = aguaColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        fertPoints.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value * height)
            if (index == 0) {
                fertPath.moveTo(x, y)
            } else {
                fertPath.lineTo(x, y)
            }
            drawCircle(
                color = fertColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path = aguaPath,
            color = aguaColor,
            style = Stroke(width = 2.dp.toPx())
        )

        drawPath(
            path = fertPath,
            color = fertColor,
            style = Stroke(width = 2.dp.toPx())
        )

        drawLine(
            color = WhiteSecondary.copy(alpha = 0.3f),
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .size(12.dp)
            .background(WaterBlue, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Agua", fontSize = 12.sp, color = WhiteSecondary)
        Spacer(modifier = Modifier.width(16.dp))
        Box(modifier = Modifier
            .size(12.dp)
            .background(NeonYellow, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Fertilizante", fontSize = 12.sp, color = WhiteSecondary)
    }
}

@Composable
fun DailyTasksList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskItem(text = "Fumigación Lote A - Juan Pérez", initialChecked = true)
        TaskItem(text = "Revisión de Sensores Lote B - Ana Gómez", initialChecked = false)
        TaskItem(text = "Mantenimiento Sistema Riego", initialChecked = false)
        TaskItem(text = "Aplicación Fertilizante Lote C", initialChecked = false)
    }
}

@Composable
fun TaskItem(text: String, initialChecked: Boolean) {
    var checked by remember { mutableStateOf(initialChecked) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = CheckboxDefaults.colors(
                checkedColor = NeonYellow,
                checkmarkColor = Color.Black,
                uncheckedColor = WhiteMain
            )
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (checked) WhiteSecondary else WhiteMain,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun CropYieldChart() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        
        val crops = listOf("Maíz", "Frijol", "Tomate")
        val values = listOf(0.8f, 0.6f, 0.9f)
        
        val barWidth = width * 0.15f
        val spacing = (width - (barWidth * crops.size)) / (crops.size + 1)
        
        val paintLabels = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            alpha = (255 * 0.7f).toInt()
            textSize = 12.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
        }
        
        val paintValues = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 12.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }
        
        crops.forEachIndexed { index, name ->
            val value = values[index]
            val barHeight = height * 0.8f * value
            
            val x = spacing + (index * (barWidth + spacing))
            val y = height * 0.8f - barHeight
            
            drawRoundRect(
                color = NeonYellow,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            drawContext.canvas.nativeCanvas.drawText(
                name,
                x + barWidth / 2,
                height,
                paintLabels
            )
            
            drawContext.canvas.nativeCanvas.drawText(
                "${(value * 100).toInt()}%",
                x + barWidth / 2,
                y - 4.dp.toPx(),
                paintValues
            )
        }
        
        drawLine(
            color = WhiteSecondary.copy(alpha = 0.3f),
            start = Offset(0f, height * 0.8f),
            end = Offset(width, height * 0.8f),
            strokeWidth = 1.dp.toPx()
        )
    }
}
