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
import androidx.compose.ui.draw.clip
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
    personnelViewModel: com.agroflow.feature.personnel.presentation.PersonnelViewModel,
    viewModel: VitrinaViewModel = viewModel()
) {
    val publicaciones by viewModel.misPublicaciones.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var publicacionToEdit by remember { mutableStateOf<Publicacion?>(null) }
    var publicacionToDelete by remember { mutableStateOf<Publicacion?>(null) }

    val selectedFinca = personnelViewModel.selectedFinca
    val fincaId = selectedFinca?.id

    LaunchedEffect(fincaId) {
        if (fincaId != null) {
            viewModel.loadMisPublicaciones(fincaId)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (fincaId != null) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = AppleGreen
                ) {
                    Text("+", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (fincaId == null) {
                Text(
                    text = "Selecciona una finca en la pestaña Fincas para ver tus publicaciones.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            } else if (isLoading && publicaciones.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!error.isNullOrEmpty()) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (publicaciones.isEmpty()) {
                Text(
                    text = "No tienes publicaciones en ${selectedFinca?.nombre ?: "esta finca"}",
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
                            },
                            onEdit = {
                                publicacionToEdit = pub
                            },
                            onDelete = {
                                publicacionToDelete = pub
                            }
                        )
                    }
                }
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    if (showCreateDialog && fincaId != null) {
        CreatePublicacionDialog(
            fincaId = fincaId,
            onDismiss = { showCreateDialog = false },
            onCreate = { request, uri ->
                viewModel.createPublicacionWithImage(context, uri, request) {
                    showCreateDialog = false
                }
            }
        )
    }

    publicacionToEdit?.let { pub ->
        EditPublicacionDialog(
            publicacion = pub,
            fincaId = fincaId ?: "",
            onDismiss = { publicacionToEdit = null },
            onEdit = { request ->
                viewModel.updatePublicacion(pub.id, request) {
                    publicacionToEdit = null
                }
            }
        )
    }

    publicacionToDelete?.let { pub ->
        AlertDialog(
            onDismissRequest = { publicacionToDelete = null },
            title = { Text("Eliminar Publicación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta publicación?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePublicacion(pub.id, fincaId ?: "") {
                        publicacionToDelete = null
                    }
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { publicacionToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MiPublicacionItem(pub: Publicacion, onMarkSold: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppleDarkGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!pub.imagenUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = pub.imagenUrl,
                    contentDescription = "Imagen del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color(0xFF2C2C2E), shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌾", style = MaterialTheme.typography.headlineSmall)
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(4.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pub.tituloProducto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "\$${pub.precio}",
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
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = onMarkSold,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3C))
                        ) {
                            Text("Vender", color = Color.White)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                Text("✏️")
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                Text("🗑️")
                            }
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Text("🗑️")
                        }
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
    onCreate: (CreatePublicacionRequest, android.net.Uri?) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }

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
                val context = androidx.compose.ui.platform.LocalContext.current
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    imageUri = uri
                }

                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (imageUri != null) "Imagen seleccionada ✅" else "📸 Seleccionar Foto de Galería")
                }

                // Keep this hidden or pass it back via onCreate
                // We'll modify the callback to pass the URI
            }
        },
        confirmButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
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

                        // We pass the CreatePublicacionRequest along with the imageUri
                        // Since onCreate signature only takes Request, I'll invoke ViewModel directly or change signature.
                        // I will change onCreate signature in the replacement.

                        onCreate(
                            CreatePublicacionRequest(
                                fincaId = fincaId,
                                tituloProducto = titulo.trim(),
                                descripcion = fullDesc,
                                precio = p,
                                cantidadDisponible = c,
                                imagenUrl = null 
                            ),
                            imageUri 
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPublicacionDialog(
    publicacion: Publicacion,
    fincaId: String,
    onDismiss: () -> Unit,
    onEdit: (CreatePublicacionRequest) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val fullDesc = publicacion.descripcion.orEmpty()
    val phoneRegex = Regex("""(?:📞\s*Tel:?|Contacto:?)\s*([0-9+\s-]+)""")
    val locationRegex = Regex("""(?:📍\s*Ubicación:?)\s*([^\n\r]+)""")
    
    val phoneMatch = phoneRegex.find(fullDesc)
    val initialPhone = phoneMatch?.groupValues?.get(1)?.trim() ?: ""
    
    val locationMatch = locationRegex.find(fullDesc)
    val initialLocation = locationMatch?.groupValues?.get(1)?.trim() ?: ""
    
    val initialDesc = fullDesc
        .replace(phoneRegex, "")
        .replace(locationRegex, "")
        .trim()
    
    var titulo by remember { mutableStateOf(publicacion.tituloProducto) }
    var desc by remember { mutableStateOf(initialDesc) }
    var precio by remember { mutableStateOf(publicacion.precio.toString()) }
    var cantidad by remember { mutableStateOf(publicacion.cantidadDisponible.toString()) }
    var telefono by remember { mutableStateOf(initialPhone) }
    var ubicacion by remember { mutableStateOf(initialLocation) }
    var selectedImagePath by remember { mutableStateOf<String?>(publicacion.imagenUrl) }

    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val imagesDir = java.io.File(context.filesDir, "publicaciones_imagenes")
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val fileName = "pub_${System.currentTimeMillis()}.jpg"
                val destFile = java.io.File(imagesDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                selectedImagePath = destFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        containerColor = AppleDarkGrey,
        onDismissRequest = onDismiss,
        title = { Text("Editar Publicación", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del producto") },
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
                        label = { Text("Precio (\$)", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono / WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Dirección o link de Google Maps") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3C)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Text("📷 Cambiar Foto", color = Color.White)
                    }
                    if (selectedImagePath != null) {
                        Text("✅ Foto", color = AppleGreen, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = precio.toDoubleOrNull() ?: 0.0
                    val c = cantidad.toDoubleOrNull() ?: 0.0
                    if (titulo.isNotBlank() && p > 0 && c > 0) {
                        val finalDesc = buildString {
                            append(desc.trim())
                            if (telefono.isNotBlank()) {
                                append("\n\n📞 Tel: ${telefono.trim()}")
                            }
                            if (ubicacion.isNotBlank()) {
                                append("\n📍 Ubicación: ${ubicacion.trim()}")
                            }
                        }

                        onEdit(
                            CreatePublicacionRequest(
                                fincaId = fincaId,
                                tituloProducto = titulo.trim(),
                                descripcion = finalDesc,
                                precio = p,
                                cantidadDisponible = c,
                                imagenUrl = selectedImagePath
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.LightGray)
            }
        }
    )
}
