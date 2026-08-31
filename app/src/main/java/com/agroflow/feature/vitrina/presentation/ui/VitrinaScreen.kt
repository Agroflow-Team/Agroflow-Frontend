package com.agroflow.feature.vitrina.presentation.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.core.theme.AppleDarkGrey
import com.agroflow.core.theme.AppleGreen
import com.agroflow.feature.vitrina.data.Publicacion
import com.agroflow.feature.vitrina.presentation.VitrinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitrinaScreen(
    onLogout: () -> Unit,
    viewModel: VitrinaViewModel = viewModel()
) {
    val publicaciones by viewModel.publicacionesActivas.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadPublicacionesActivas()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vitrina Comercial") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Text("Salir")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && publicaciones.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!error.isNullOrEmpty()) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (publicaciones.isEmpty()) {
                Text(
                    text = "No hay publicaciones activas",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(publicaciones) { pub ->
                        PublicacionItem(pub, context)
                    }
                }
            }
        }
    }
}

@Composable
fun PublicacionItem(pub: Publicacion, context: Context) {
    val desc = pub.descripcion.orEmpty()
    val phoneRegex = Regex("""(?:📞\s*Tel:?|Contacto:?)\s*([0-9+\s-]+)""")
    val phoneMatch = phoneRegex.find(desc)
    val extractedPhone = phoneMatch?.groupValues?.get(1)?.trim()?.replace(" ", "")?.replace("-", "")

    val locationRegex = Regex("""(?:📍\s*Ubicación:?)\s*([^\n\r]+)""")
    val locationMatch = locationRegex.find(desc)
    val extractedLocation = locationMatch?.groupValues?.get(1)?.trim()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppleDarkGrey)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Text("🌾 Imagen", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = pub.tituloProducto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$${pub.precio}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppleGreen
                )
                Text(
                    text = "Disponibles: ${pub.cantidadDisponible}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Texto corregido para evitar nulos y con la coma faltante
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val msg = "Hola, me interesa tu producto '${pub.tituloProducto}' que vi en AgroFlow."
                        val phoneUrl = if (!extractedPhone.isNullOrBlank()) {
                            "https://wa.me/$extractedPhone?text=${Uri.encode(msg)}"
                        } else {
                            "https://wa.me/?text=${Uri.encode(msg)}"
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(phoneUrl))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleGreen)
                ) {
                    Text("💬 WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                val mapQuery = when {
                    pub.latitud != null && pub.longitud != null -> "${pub.latitud},${pub.longitud}"
                    !extractedLocation.isNullOrBlank() -> extractedLocation
                    else -> "Colombia"
                }

                OutlinedButton(
                    onClick = {
                        val mapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(mapQuery)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📍 Google Maps", color = Color.White)
                }
            }
            }
        }
    }
}