package com.agroflow.feature.auth.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen() {
    // 1. Estados (State): Aquí guardamos lo que el usuario va escribiendo
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 2. Surface: Es el "lienzo" o fondo de nuestra pantalla
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 3. Column: Organiza los elementos de arriba hacia abajo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Título
            Text(
                text = "AgroFlow 🌾",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp)) // Espacio en blanco

            // Campo de Correo
            OutlinedTextField(
                value = email,
                onValueChange = { email = it }, // Actualiza el estado cuando el usuario escribe
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(), // Oculta el texto con asteriscos
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Ingreso
            Button(
                onClick = { /* TODO: Aquí conectaremos el ViewModel más adelante */ },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Iniciar Sesión", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// 4. Preview: ¡Esto le dice a Android Studio que dibuje la pantalla aquí mismo!
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}