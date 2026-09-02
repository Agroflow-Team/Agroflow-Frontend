package com.agroflow.feature.auth.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agroflow.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IndexScreen(
    onNatigateToLogin: () -> Unit
){
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFA5D6A7))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = com.agroflow.R.drawable.logo_agroflow),
                        contentDescription = "Logo pequeño AgroFlow",
                        modifier = Modifier
                            .size(65.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentScale = ContentScale.Crop
                    )
                }
                TextButton(
                    onClick = { onNatigateToLogin() }
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bienvenido a AgroFlow",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            val pagerState = rememberPagerState(pageCount = { 9 })

            LaunchedEffect(pagerState) {
                while (true) {
                    delay(3000)
                    val nextPage = (pagerState.currentPage + 1) % 9
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) { page ->
                CarouselItem(page = page)
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ){
                repeat(9) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            InfoCard(
                title = "¿Para Agricultores?",
                description = "Gestiona tu inventario, tareas de empleados y finanzas en un solo lugar. Trabaja offline y sincroniza cuando tengas conexión.",
                imageRes = R.drawable.campo // Pasamos la imagen directamente a la tarjeta
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoCard(
                title = "¿Para Clientes?",
                description = "Explora los catálogos de cosechas directamente de las manos de los agricultores y asegura los mejores precios.",
                imageRes = R.drawable.tomates // Pasamos la imagen directamente a la tarjeta
            )
        }
    }
}

@Composable
fun CarouselItem(page: Int){
    val (title, imageRes) = when (page) {
        0 -> "" to R.drawable.campo
        1 -> "" to R.drawable.campito
        2 -> "" to R.drawable.campos
        3 -> "" to R.drawable.arandanos
        4 -> "" to R.drawable.cebollas
        5 -> "" to R.drawable.moras
        6 -> "" to R.drawable.papa
        7 -> "" to R.drawable.papa_criolla
        8 -> "" to R.drawable.tomates
        else -> "" to R.drawable.zanahorias
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Box(modifier = Modifier.fillMaxSize()){
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )
        }
    }
}


@Composable
fun InfoCard(title: String, description: String, imageRes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))


            val infiniteTransition = rememberInfiniteTransition(label = "animacion_flotante")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "movimiento_y"
            )


            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .size(90.dp)
                    .offset(y = offsetY.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}