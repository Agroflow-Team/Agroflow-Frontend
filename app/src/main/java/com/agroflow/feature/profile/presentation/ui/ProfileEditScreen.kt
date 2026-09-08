package com.agroflow.feature.profile.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agroflow.core.session.SessionManager

@Composable
fun ProfileEditScreen(onLogout: () -> Unit) {
    val scrollState = rememberScrollState()

    var nombre by remember { mutableStateOf(SessionManager.userEmail?.substringBefore("@") ?: "Agricultor") }
    var email by remember { mutableStateOf(SessionManager.userEmail ?: "") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF3EFE7),
        unfocusedContainerColor = Color(0xFFF3EFE7).copy(alpha = 0.7f),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = Color(0xFF2C7A4B),
        focusedTextColor = Color(0xFF1C1C1E),
        unfocusedTextColor = Color(0xFF1C1C1E),
        focusedPlaceholderColor = Color(0xFF8E8E93),
        unfocusedPlaceholderColor = Color(0xFF8E8E93)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Photo
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C7A4B).copy(alpha = 0.15f))
                .clickable { /* TODO: Open gallery picker */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👤",
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Toca para cambiar foto",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF2C7A4B)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Name Field
        Text(
            text = "Nombre",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF1C1C1E),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = { Text("Tu nombre completo") },
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Email Field
        Text(
            text = "Correo electrónico",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF1C1C1E),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("tucorreo@email.com") },
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Phone Field
        Text(
            text = "Teléfono",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF1C1C1E),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = telefono,
            onValueChange = { telefono = it },
            placeholder = { Text("300 123 4567") },
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Address Field
        Text(
            text = "Dirección",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF1C1C1E),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = direccion,
            onValueChange = { direccion = it },
            placeholder = { Text("Tu dirección o vereda") },
            singleLine = false,
            maxLines = 3,
            colors = textFieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Info cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EFE7))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rol", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                    Text(
                        text = when {
                            SessionManager.roleId == SessionManager.ROLE_AGRICULTOR -> "Agricultor"
                            SessionManager.roleId == SessionManager.ROLE_TRABAJADOR -> "Trabajador"
                            SessionManager.roleId == SessionManager.ROLE_CLIENTE -> "Cliente"
                            SessionManager.roleId == SessionManager.ROLE_ADMIN -> "Administrador"
                            else -> "Usuario"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF2C7A4B)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ID Usuario", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF8E8E93))
                    Text(
                        text = SessionManager.userId?.take(8)?.plus("...") ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Success message
        savedMessage?.let {
            Text(
                text = it,
                color = Color(0xFF2C7A4B),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Save Button
        Button(
            onClick = {
                // Save locally for now
                savedMessage = "✅ Perfil actualizado correctamente"
            },
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C7A4B)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Guardar Cambios",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        OutlinedButton(
            onClick = {
                SessionManager.clearSession()
                onLogout()
            },
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFFF453A)
            ),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF453A)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Cerrar Sesión",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFF453A)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
