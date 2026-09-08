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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agroflow.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IndexScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegistroCliente: () -> Unit
){
    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState(pageCount = { 9 })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % 9
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){
        // Hero Carousel Section (Top half)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp) // Large dynamic header
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                HeroCarouselItem(page = page)
            }

            // Dark gradient overlay to make text readable
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Title and dots at the bottom of the hero section
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp), // Space for the overlapping surface
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido a AgroFlow",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Pager dots
                Row(
                    horizontalArrangement = Arrangement.Center
                ){
                    repeat(9) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)
                        val width = if (isSelected) 24.dp else 8.dp
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        }

        // Information Section (Bottom half overlapping the hero)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(380.dp)) // Push content down to overlap just a bit

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    InfoCard(
                        title = "¿Para Agricultores?",
                        description = "Gestiona tu inventario, tareas de empleados y finanzas en un solo lugar. Trabaja offline y sincroniza cuando tengas conexión.",
                        imageRes = R.drawable.campo
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    InfoCard(
                        title = "¿Para Clientes?",
                        description = "Explora los catálogos de cosechas directamente de las manos de los agricultores y asegura los mejores precios.",
                        imageRes = R.drawable.tomates
                    )


                }
            }
        }

        // Top Bar Overlay (Placed at the end of Box so it draws on top and receives touches)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_agroflow),
                contentDescription = "Logo AgroFlow",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Button(
                onClick = { onNavigateToLogin() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Iniciar Sesión",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HeroCarouselItem(page: Int){
    val imageRes = when (page) {
        0 -> R.drawable.campo
        1 -> R.drawable.campito
        2 -> R.drawable.campos
        3 -> R.drawable.arandanos
        4 -> R.drawable.cebollas
        5 -> R.drawable.moras
        6 -> R.drawable.papa
        7 -> R.drawable.papa_criolla
        else -> R.drawable.zanahorias
    }

    // Add a slow zoom-in effect for each image
    val infiniteTransition = rememberInfiniteTransition(label = "zoom_effect")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize()){
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale),
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun InfoCard(title: String, description: String, imageRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
