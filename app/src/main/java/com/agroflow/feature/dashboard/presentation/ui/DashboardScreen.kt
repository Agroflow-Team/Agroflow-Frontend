package com.agroflow.feature.dashboard.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

import com.agroflow.core.session.SessionManager
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import com.agroflow.feature.personnel.presentation.ui.FincaManagementScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    val personnelViewModel: PersonnelViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    
    var showNotifications by remember { mutableStateOf(false) }

    // El usuario solicitó que abra directamente el dashboard, así que omitimos el redireccionamiento a Fincas

    val menuItems = listOf(
        "📊 Dashboard" to 0,
        "📦 Inventario" to 1,
        "👥 Empleados" to 2,
        "✅ Tareas" to 3,
        "🏷️ Catálogo" to 4,
        "💰 Ventas" to 5,
        "📈 Finanzas" to 6,
        "🏡 Fincas" to 8
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF1B5E20)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header section
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // TODO: Use real photo URI when Profile logic is complete
                        Text("🧑‍🌾", fontSize = 48.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = SessionManager.userEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Agricultor",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = SessionManager.userEmail ?: "correo@ejemplo.com",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            selectedTab = 7
                            scope.launch { drawerState.close() }
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text("Ver Perfil", color = Color(0xFFF4E245), fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Menu items
                    menuItems.forEach { (label, index) ->
                        NavigationDrawerItem(
                            label = { Text(label) },
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFFF4E245).copy(alpha = 0.15f),
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = Color(0xFFF4E245),
                                unselectedTextColor = Color.White.copy(alpha = 0.8f)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Logout
                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF453A))
                    ) {
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        
        LaunchedEffect(Unit) {
            // Delay 2 seconds to simulate receiving a notification
            kotlinx.coroutines.delay(2000)
            snackbarHostState.showSnackbar(
                message = "🔔 Nueva notificación: Se regó el cultivo de tomate",
                duration = SnackbarDuration.Long // Will be around 4-10s
            )
        }

        Scaffold(
            containerColor = Color(0xFFF1F8E9), // Light background for entire app
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        val title = menuItems.find { it.second == selectedTab }?.first ?: if(selectedTab == 7) "Mi Perfil" else "AgroFlow"
                        Text(title, color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showNotifications = true }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                            }
                            DropdownMenu(
                                expanded = showNotifications,
                                onDismissRequest = { showNotifications = false },
                                modifier = Modifier
                                    .width(320.dp)
                                    .background(Color.White) // White Dropdown
                                    .padding(8.dp)
                            ) {
                                Text("Notificaciones", color = Color(0xFF1C1C1E), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                                
                                val notifications = listOf(
                                    "Se regó el cultivo de tomate" to false,
                                    "Nueva tarea completada: Revisar cercas" to false,
                                    "Insumo Urea bajo en stock" to true
                                )
                                
                                notifications.forEach { (text, isAlert) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = if(isAlert) Color(0xFFFF453A) else Color(0xFFF4E245),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text, color = Color(0xFF1C1C1E), fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20)) // Dark Green Header
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (selectedTab) {
                    0 -> DashboardHomeScreen(
                        onNavigateToTab = { tabIndex ->
                            selectedTab = tabIndex
                        }
                    )
                    1 -> com.agroflow.feature.inventory.presentation.ui.InventoryScreen(personnelViewModel)
                    2 -> com.agroflow.feature.personnel.presentation.ui.PersonnelScreen(personnelViewModel)
                    3 -> com.agroflow.feature.tasks.presentation.ui.TasksScreen(personnelViewModel)
                    4 -> com.agroflow.feature.vitrina.presentation.ui.MisPublicacionesScreen(personnelViewModel)
                    5 -> com.agroflow.feature.sales.presentation.ui.SalesScreen(personnelViewModel)
                    6 -> com.agroflow.feature.finance.presentation.ui.FinanceScreen(personnelViewModel)
                    7 -> com.agroflow.feature.profile.presentation.ui.ProfileEditScreen(onLogout)
                    8 -> FincaManagementScreen(personnelViewModel)
                    9 -> PlaceholderScreen("Mis Lotes / Cultivos", "Aquí podrás gestionar los lotes, ver qué cultivos tienes sembrados en cada uno y su ciclo de crecimiento.")
                    10 -> PlaceholderScreen("Sensores e IoT", "Aquí podrás monitorear la humedad, clima y activar sistemas de riego de forma remota.")
                }
            }
        }
        
        // Dialog removed as per user request (open Dashboard directly)
    }
}


@Composable
fun PlaceholderScreen(title: String, description: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("🚧", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2C7A4B))
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray)
        }
    }
}
