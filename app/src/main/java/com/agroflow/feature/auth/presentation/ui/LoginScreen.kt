package com.agroflow.feature.auth.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5A714C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_agroflow),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = "Control Every\nField with Ease",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 42.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle
            Text(
                text = "Smart Farming brings modern technology into agriculture, helping farmers manage their fields with greater efficiency.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF6A8256),
                unfocusedContainerColor = Color(0xFF6A8256),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                cursorColor = Color.White,
                focusedTrailingIconColor = Color.White.copy(alpha = 0.8f),
                unfocusedTrailingIconColor = Color.White.copy(alpha = 0.8f)
            )

            // Email TextField
            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password TextField
            TextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

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
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    RunningPlantAnimation()
                }
            } else {
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4E245))
                ) {
                    Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextButton(onClick = onNavigateToRegister) {
                    Text("Registrarse", color = Color.White, fontWeight = FontWeight.Bold)
                }
    
                TextButton(onClick = onRecoverPasswordClick) {
                    Text("Olvidé mi contraseña", color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = { /* TODO */ }) {
                    Text("Configuración de Servidor", color = Color.White.copy(alpha = 0.6f))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
@Composable
fun RunningPlantAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val legSwing by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier
        .size(100.dp)
        .offset(y = offsetY.dp)
    ) {
        val strokeWidth = 5f
        
        // Draw Legs
        rotate(legSwing, pivot = Offset(size.width * 0.4f, size.height * 0.75f)) {
            drawLine(
                color = Color.Black,
                start = Offset(size.width * 0.4f, size.height * 0.75f),
                end = Offset(size.width * 0.4f, size.height * 0.95f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }
        rotate(-legSwing, pivot = Offset(size.width * 0.6f, size.height * 0.75f)) {
            drawLine(
                color = Color.Black,
                start = Offset(size.width * 0.6f, size.height * 0.75f),
                end = Offset(size.width * 0.6f, size.height * 0.95f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        // Leaves
        val leafColor = Color(0xFF8BC34A)
        
        fun drawLeaf(path: Path) {
            drawPath(path = path, color = leafColor)
            drawPath(path = path, color = Color.Black, style = Stroke(strokeWidth, join = StrokeJoin.Round))
        }

        // Center Leaf
        drawLeaf(Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.45f)
            quadraticBezierTo(size.width * 0.35f, size.height * 0.2f, size.width * 0.5f, size.height * 0.1f)
            quadraticBezierTo(size.width * 0.65f, size.height * 0.2f, size.width * 0.5f, size.height * 0.45f)
        })

        // Left Leaf
        drawLeaf(Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.45f)
            quadraticBezierTo(size.width * 0.2f, size.height * 0.4f, size.width * 0.2f, size.height * 0.25f)
            quadraticBezierTo(size.width * 0.4f, size.height * 0.25f, size.width * 0.5f, size.height * 0.45f)
        })

        // Right Leaf
        drawLeaf(Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.45f)
            quadraticBezierTo(size.width * 0.8f, size.height * 0.4f, size.width * 0.8f, size.height * 0.25f)
            quadraticBezierTo(size.width * 0.6f, size.height * 0.25f, size.width * 0.5f, size.height * 0.45f)
        })

        // Top Left Leaf
        drawLeaf(Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.45f)
            quadraticBezierTo(size.width * 0.2f, size.height * 0.2f, size.width * 0.35f, size.height * 0.15f)
            quadraticBezierTo(size.width * 0.4f, size.height * 0.25f, size.width * 0.5f, size.height * 0.45f)
        })

        // Top Right Leaf
        drawLeaf(Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.45f)
            quadraticBezierTo(size.width * 0.8f, size.height * 0.2f, size.width * 0.65f, size.height * 0.15f)
            quadraticBezierTo(size.width * 0.6f, size.height * 0.25f, size.width * 0.5f, size.height * 0.45f)
        })

        // Pot
        val potPath = Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.45f)
            lineTo(size.width * 0.7f, size.height * 0.45f)
            lineTo(size.width * 0.65f, size.height * 0.75f)
            lineTo(size.width * 0.35f, size.height * 0.75f)
            close()
        }
        drawPath(path = potPath, color = Color(0xFFF3EFE7))
        drawPath(path = potPath, color = Color.Black, style = Stroke(strokeWidth, join = StrokeJoin.Round))

        // Pot Rim
        val rimRect = androidx.compose.ui.geometry.Rect(
            Offset(size.width * 0.25f, size.height * 0.43f),
            Size(size.width * 0.5f, size.height * 0.06f)
        )
        val rimPath = Path().apply { addRoundRect(androidx.compose.ui.geometry.RoundRect(rimRect, CornerRadius(8f, 8f))) }
        drawPath(path = rimPath, color = Color(0xFFF3EFE7))
        drawPath(path = rimPath, color = Color.Black, style = Stroke(strokeWidth, join = StrokeJoin.Round))

        // Face
        drawCircle(color = Color.Black, radius = 4f, center = Offset(size.width * 0.42f, size.height * 0.6f))
        drawCircle(color = Color.Black, radius = 4f, center = Offset(size.width * 0.58f, size.height * 0.6f))
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.47f, size.height * 0.65f)
                quadraticBezierTo(size.width * 0.5f, size.height * 0.68f, size.width * 0.53f, size.height * 0.65f)
            },
            color = Color.Black,
            style = Stroke(3f, cap = StrokeCap.Round)
        )
    }
}
