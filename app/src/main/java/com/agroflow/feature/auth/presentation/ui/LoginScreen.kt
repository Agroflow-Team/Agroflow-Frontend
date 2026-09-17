package com.agroflow.feature.auth.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo general
        Image(
            painter = painterResource(id = R.drawable.bg_butterflies),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2C4A22).copy(alpha = 0.85f))
        )

        // Mariposas animadas flotando
        AnimatedButterflies()

        // Contenedor central translúcido
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Bienvenido a AgroFlow",
                    color = Color(0xFF1B5E20),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                val textFieldColors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.8f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF1B5E20),
                    unfocusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                    cursorColor = Color(0xFF1B5E20),
                    focusedTrailingIconColor = Color(0xFF1B5E20),
                    unfocusedTrailingIconColor = Color.Gray
                )

                // Correo electrónico
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = textFieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contraseña
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Contraseña") },
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

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState is LoginUiState.Error) {
                    Text(
                        text = uiState.message,
                        color = Color(0xFFFF453A), // AppleRed
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState is LoginUiState.Loading) {
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        RunningPlantAnimation()
                    }
                } else {
                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(25.dp), // Forma de píldora
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF558B2F)) // Verde oliva/hoja
                    ) {
                        Text("Iniciar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text("Registrarse", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onRecoverPasswordClick) {
                    Text("Olvidé mi contraseña", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AnimatedButterflies() {
    val infiniteTransition = rememberInfiniteTransition(label = "butterflies")

    val b1Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label = "b1"
    )
    val b2Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing)),
        label = "b2"
    )
    val b3Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing)),
        label = "b3"
    )
    val b4Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "b4"
    )
    val b5Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "b5"
    )
    val b6Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(17000, easing = LinearEasing)),
        label = "b6"
    )
    val b7Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing)),
        label = "b7"
    )
    val b8Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(19000, easing = LinearEasing)),
        label = "b8"
    )
    
    val flap by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(150, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "flap"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val x1 = (b1Phase / (2f * PI.toFloat())) * (w + 400f) - 200f
        val y1 = h * 0.2f + sin(b1Phase.toDouble()).toFloat() * 300f
        val rot1 = 20f * cos(b1Phase.toDouble()).toFloat() + 45f 
        
        val x2 = w + 200f - (b2Phase / (2f * PI.toFloat())) * (w + 400f)
        val y2 = h * 0.7f + cos(b2Phase.toDouble()).toFloat() * 400f
        val rot2 = -20f * sin(b2Phase.toDouble()).toFloat() - 45f

        val x3 = (b3Phase / (2f * PI.toFloat())) * (w + 400f) - 200f
        val y3 = h * 0.85f - sin((b3Phase * 2f).toDouble()).toFloat() * 250f
        val rot3 = 15f * cos(b3Phase.toDouble()).toFloat() + 30f

        val x4 = w + 200f - (b4Phase / (2f * PI.toFloat())) * (w + 400f)
        val y4 = h * 0.3f - cos(b4Phase.toDouble()).toFloat() * 250f
        val rot4 = -15f * cos(b4Phase.toDouble()).toFloat() - 30f

        val x5 = (b5Phase / (2f * PI.toFloat())) * (w + 400f) - 200f
        val y5 = h * 0.5f + sin((b5Phase * 1.5f).toDouble()).toFloat() * 350f
        val rot5 = 25f * cos(b5Phase.toDouble()).toFloat() + 60f 
        
        val x6 = w + 200f - (b6Phase / (2f * PI.toFloat())) * (w + 400f)
        val y6 = h * 0.1f + cos((b6Phase * 1.2f).toDouble()).toFloat() * 200f
        val rot6 = -25f * sin(b6Phase.toDouble()).toFloat() - 60f

        val x7 = (b7Phase / (2f * PI.toFloat())) * (w + 400f) - 200f
        val y7 = h * 0.9f - sin(b7Phase.toDouble()).toFloat() * 300f
        val rot7 = 10f * cos(b7Phase.toDouble()).toFloat() + 15f

        val x8 = w + 200f - (b8Phase / (2f * PI.toFloat())) * (w + 400f)
        val y8 = h * 0.6f - cos((b8Phase * 1.8f).toDouble()).toFloat() * 300f
        val rot8 = -10f * cos(b8Phase.toDouble()).toFloat() - 15f

        fun drawButterfly(color: Color, scaleVal: Float, x: Float, y: Float, rotationVal: Float) {
            withTransform({
                translate(left = x, top = y)
                rotate(rotationVal)
                val baseScale = scaleVal * 3.5f 
                scale(baseScale, baseScale)
            }) {
                withTransform({ scale(flap, 1f, Offset(0f, 0f)) }) {
                    drawOval(color = color, topLeft = Offset(-15f, -20f), size = Size(15f, 25f))
                    drawOval(color = color, topLeft = Offset(0f, -20f), size = Size(15f, 25f))
                    drawOval(color = color.copy(alpha = 0.8f), topLeft = Offset(-12f, 2f), size = Size(12f, 18f))
                    drawOval(color = color.copy(alpha = 0.8f), topLeft = Offset(0f, 2f), size = Size(12f, 18f))
                }
                drawLine(color = Color.DarkGray, start = Offset(0f, -12f), end = Offset(0f, 15f), strokeWidth = 3f, cap = StrokeCap.Round)
            }
        }
        
        drawButterfly(Color(0xFF1B5E20), 1.2f, x1, y1, rot1)
        drawButterfly(Color(0xFF2E7D32), 0.9f, x2, y2, rot2)
        drawButterfly(Color(0xFF388E3C), 1f, x3, y3, rot3)
        drawButterfly(Color(0xFF4CAF50), 0.7f, x4, y4, rot4)
        drawButterfly(Color(0xFF66BB6A), 1.1f, x5, y5, rot5)
        drawButterfly(Color(0xFF81C784), 0.8f, x6, y6, rot6)
        drawButterfly(Color(0xFFA5D6A7), 1.3f, x7, y7, rot7)
        drawButterfly(Color(0xFF1B5E20), 0.95f, x8, y8, rot8)
    }
}

@Composable
fun RunningPlantAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "plant_animation")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    val legSwing by infiniteTransition.animateFloat(
        initialValue = -25f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "legSwing"
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
