package com.agroflow.feature.vitrina.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.core.session.SessionManager
import com.agroflow.core.theme.AppleDarkGrey
import com.agroflow.core.theme.AppleGreen
import com.agroflow.feature.vitrina.data.CreatePublicacionRequest
import com.agroflow.feature.vitrina.data.Publicacion
import com.agroflow.feature.vitrina.presentation.VitrinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPublicacionesScreen(
    viewModel: VitrinaViewModel = viewModel()
) {
    val publicaciones by viewModel.misPublicaciones.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // Assuming we have a fincaId in session or we just use a default one for now
    val fincaId = SessionManager.fincaId ?: "00000000-0000-0000-0000-000000000000"

    LaunchedEffect(fincaId) {
        viewModel.loadMisPublicaciones(fincaId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = AppleGreen
            ) {
                Text("+", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            }
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
                    text = "No tienes publicaciones",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(publicaciones) { pub ->
                        MiPublicacionItem(
                            pub = pub,
                            onMarkSold = {
                                viewModel.markAsVendida(pub.id, fincaId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePublicacionDialog(
            fincaId = fincaId,
            onDismiss = { showCreateDialog = false },
            onCreate = { request ->
                viewModel.createPublicacion(request) {
                    showCreateDialog = false
                }
            }
        )
    }
}

@Composable
fun MiPublicacionItem(pub: Publicacion, onMarkSold: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppleDarkGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFF2C2C2E), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌾", style = MaterialTheme.typography.headlineSmall)
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pub.tituloProducto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$${pub.precio}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppleGreen
                    )
                    Text(
                        text = "Disponibles: ${pub.cantidadDisponible}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Estado: ${pub.estadoPublicacion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (pub.estadoPublicacion == "ACTIVA") AppleGreen else Color.Red
                    )
                }
                
                if (pub.estadoPublicacion == "ACTIVA") {
                    Button(
                        onClick = onMarkSold,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3C))
                    ) {
                        Text("Vender", color = Color.White)
                    }
                }
            }

            if (!pub.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = pub.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublicacionDialog(
    fincaId: String,
    onDismiss: () -> Unit,
    onCreate: (CreatePublicacionRequest) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }

    AlertDialog(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        containerColor = AppleDarkGrey,
        onDismissRequest = onDismiss,
        title = { Text("Nueva Publicación", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del producto (ej. Tomates frescos)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción del producto") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        label = { Text("Precio ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono / WhatsApp (ej. 3001234567)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación en Google Maps (ej. Vereda Roble o 4.71,-74.07)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imagenUrl,
                    onValueChange = { imagenUrl = it },
                    label = { Text("URL de la Foto (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = precio.toDoubleOrNull() ?: 0.0
                    val c = cantidad.toDoubleOrNull() ?: 0.0
                    if (titulo.isNotBlank() && p > 0 && c > 0) {
                        val fullDesc = buildString {
                            append(desc.trim())
                            if (telefono.isNotBlank()) {
                                append("\n\n📞 Tel: ${telefono.trim()}")
                            }
                            if (ubicacion.isNotBlank()) {
                                append("\n📍 Ubicación: ${ubicacion.trim()}")
                            }
                        }

                        onCreate(
                            CreatePublicacionRequest(
                                fincaId = fincaId,
                                tituloProducto = titulo.trim(),
                                descripcion = fullDesc,
                                precio = p,
                                cantidadDisponible = c,
                                imagenUrl = if (imagenUrl.isNotBlank()) imagenUrl.trim() else null
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Text("Publicar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.LightGray)
            }
        }
    )
}
