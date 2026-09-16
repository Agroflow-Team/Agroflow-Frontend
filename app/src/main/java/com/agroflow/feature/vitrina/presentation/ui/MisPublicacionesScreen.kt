package com.agroflow.feature.vitrina.presentation.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.agroflow.feature.vitrina.data.CreatePublicacionRequest
import com.agroflow.feature.vitrina.data.Publicacion
import com.agroflow.feature.vitrina.presentation.VitrinaViewModel

private val AgroFlowGreen = Color(0xFF2C7A4B)
private val AgroFlowBackground = Color(0xFFF3EFE7)
private val AgroFlowSurface = Color(0xFFFFFFFF)
private val AppleDarkGrey = Color(0xFF1C1C1E)
private val AppleTextSecondary = Color(0xFF8E8E93)
private val AppleRed = Color(0xFFFF453A)
private val AppleBlue = Color(0xFF0A84FF)
private val AppleGreen = Color(0xFF30D158)

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
                    containerColor = AgroFlowGreen
                ) {
                    Text("+", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }
            }
        },
        containerColor = AgroFlowBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (fincaId == null) {
                Text(
                    text = "Selecciona una finca en la pestaña Fincas para ver tus publicaciones.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppleDarkGrey.copy(alpha = 0.7f)
                )
            } else if (isLoading && publicaciones.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AgroFlowGreen)
            } else if (!error.isNullOrEmpty()) {
                Text(
                    text = error ?: "",
                    color = AppleRed,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (publicaciones.isEmpty()) {
                Text(
                    text = "No tienes publicaciones en ${selectedFinca?.nombre ?: "esta finca"}",
                    modifier = Modifier.align(Alignment.Center),
                    color = AppleDarkGrey
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
                            onToggleState = { isActiva ->
                                val newState = if (isActiva) "ACTIVA" else "VENDIDA"
                                viewModel.updateEstado(pub.id, newState, fincaId)
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

    val context = LocalContext.current
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
                    Text("Eliminar", color = AppleRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { publicacionToDelete = null }) {
                    Text("Cancelar", color = AppleDarkGrey)
                }
            },
            containerColor = AgroFlowSurface
        )
    }
}

@Composable
fun MiPublicacionItem(pub: Publicacion, onToggleState: (Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    
    val phoneRegex = Regex("""(?:📞\s*Tel:?|Contacto:?)\s*([0-9+\s-]+)""")
    val locationRegex = Regex("""(?:📍\s*Ubicación:?)\s*([^\n\r]+)""")
    
    val fullDesc = pub.descripcion ?: ""
    val phoneMatch = phoneRegex.find(fullDesc)
    val phone = phoneMatch?.groupValues?.get(1)?.trim()
    
    val locationMatch = locationRegex.find(fullDesc)
    val location = locationMatch?.groupValues?.get(1)?.trim() ?: "Finca"
    
    val cleanDesc = fullDesc.replace(phoneRegex, "").replace(locationRegex, "").trim()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AgroFlowSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!pub.imagenUrl.isNullOrBlank()) {
                AsyncImage(
                    model = pub.imagenUrl,
                    contentDescription = "Imagen del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(AgroFlowBackground, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌾", style = MaterialTheme.typography.headlineLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pub.tituloProducto,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppleDarkGrey
                    )
                    Text(
                        text = "\$${pub.precio}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AgroFlowGreen
                    )
                    Text(
                        text = "Disponibles: ${pub.cantidadDisponible}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppleTextSecondary
                    )
                    if (phone != null) {
                        Text(
                            text = "📞 $phone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppleDarkGrey
                        )
                    }
                }
            }

            if (cleanDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = cleanDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppleDarkGrey.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (pub.estadoPublicacion == "ACTIVA") "ACTIVA" else "VENDIDO",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (pub.estadoPublicacion == "ACTIVA") AgroFlowGreen else AppleTextSecondary
                    )
                    Switch(
                        checked = pub.estadoPublicacion == "ACTIVA",
                        onCheckedChange = { onToggleState(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AgroFlowGreen,
                            checkedTrackColor = AgroFlowGreen.copy(alpha = 0.3f)
                        )
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.background(AgroFlowBackground, RoundedCornerShape(8.dp)).size(40.dp)) {
                        Text("✏️")
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp)).size(40.dp)) {
                        Text("🗑️")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📍 Ver Ubicación", color = Color.White)
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

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppleDarkGrey,
        unfocusedTextColor = AppleDarkGrey,
        focusedContainerColor = AgroFlowSurface,
        unfocusedContainerColor = AgroFlowSurface,
        focusedBorderColor = AgroFlowGreen,
        unfocusedBorderColor = AppleTextSecondary,
        cursorColor = AgroFlowGreen,
        focusedLabelColor = AgroFlowGreen,
        unfocusedLabelColor = AppleTextSecondary
    )

    AlertDialog(
        shape = RoundedCornerShape(24.dp),
        containerColor = AgroFlowBackground,
        onDismissRequest = onDismiss,
        title = { Text("Nueva Publicación", color = AppleDarkGrey, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        label = { Text("Precio (\$)", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono / WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación en Google Maps") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )
                val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    imageUri = uri
                }

                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AgroFlowGreen),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(if (imageUri != null) "Imagen seleccionada ✅" else "📸 Seleccionar Foto")
                }
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
                                imagenUrl = null 
                            ),
                            imageUri 
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgroFlowGreen),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Publicar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = AppleTextSecondary)
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
    val context = LocalContext.current
    
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

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppleDarkGrey,
        unfocusedTextColor = AppleDarkGrey,
        focusedContainerColor = AgroFlowSurface,
        unfocusedContainerColor = AgroFlowSurface,
        focusedBorderColor = AgroFlowGreen,
        unfocusedBorderColor = AppleTextSecondary,
        cursorColor = AgroFlowGreen,
        focusedLabelColor = AgroFlowGreen,
        unfocusedLabelColor = AppleTextSecondary
    )

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
        shape = RoundedCornerShape(24.dp),
        containerColor = AgroFlowBackground,
        onDismissRequest = onDismiss,
        title = { Text("Editar Publicación", color = AppleDarkGrey, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = precio,
                        onValueChange = { precio = it },
                        label = { Text("Precio (\$)", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad", maxLines = 1) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono / WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    shape = RoundedCornerShape(14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroFlowGreen),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("📷 Cambiar Foto", color = Color.White)
                    }
                    if (selectedImagePath != null) {
                        Text("✅", color = AppleGreen, style = MaterialTheme.typography.bodyLarge)
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
                colors = ButtonDefaults.buttonColors(containerColor = AgroFlowGreen),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = AppleTextSecondary)
            }
        }
    )
}
