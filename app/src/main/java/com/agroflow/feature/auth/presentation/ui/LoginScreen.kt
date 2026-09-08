package com.agroflow.feature.auth.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.R
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.auth.presentation.AuthViewModel
import com.agroflow.feature.auth.presentation.LoginUiState

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onRecoverPasswordClick: () -> Unit = {}
) {
    val uiState = viewModel.uiState
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Full-screen background
        Image(
            painter = painterResource(id = R.drawable.campo),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // 3. Animated truck with vegetables
        val infiniteTransition = rememberInfiniteTransition(label = "TruckTransition")
        val animatedX by infiniteTransition.animateFloat(
            initialValue = -300f,
            targetValue = 400f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "TruckX"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Row(modifier = Modifier.offset(x = animatedX.dp)) {
                Text("🚛", fontSize = 40.sp)
                Text("🥬🍅🥕🌽🥦", fontSize = 24.sp, modifier = Modifier.align(Alignment.Bottom))
            }
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // 4. Centered login card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.92f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.logo_agroflow),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C7A4B)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val textFieldColors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = Color(0xFF1C1C1E),
                        unfocusedTextColor = Color(0xFF1C1C1E)
                    )

                    // Email TextField
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Correo electrónico", color = Color(0xFF8E8E93)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password TextField
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Contraseña", color = Color(0xFF8E8E93)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = "Toggle password visibility")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState is LoginUiState.Error) {
                        Text(
                            text = (uiState as LoginUiState.Error).message,
                            color = Color(0xFFFF453A),
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(color = Color(0xFF2C7A4B))
                    } else {
                        Button(
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C7A4B))
                        ) {
                            Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateToRegister) {
                        Text("Registrarse", color = Color(0xFF2C7A4B), fontWeight = FontWeight.Bold)
                    }

                    TextButton(onClick = onRecoverPasswordClick) {
                        Text("¿Olvidaste tu contraseña?", color = Color(0xFF8E8E93))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}