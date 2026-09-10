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

    // Finca startup check
    LaunchedEffect(personnelViewModel.selectedFinca, personnelViewModel.fincas) {
        if (personnelViewModel.fincas.isNotEmpty() && personnelViewModel.selectedFinca == null) {
            // Wait, we need to force selection. But if fincas is empty, they must create one.
            // Let's just route them to Fincas screen if they don't have a finca selected
            if (selectedTab != 8) {
                // Ensure they go to Fincas
                selectedTab = 8
            }
        }
    }

    val menuItems = listOf(
        "📊 Inicio / Dashboard Principal" to 0,
        "🌱 Mis Lotes / Cultivos" to 9,
        "📡 Sensores e IoT" to 10,
        "📦 Inventario" to 1,
        "📈 Finanzas y Reportes" to 6,
        "👥 Gestión de Empleados" to 2,
        "✅ Gestión de Tareas" to 3,
        "🏷️ Catálogo" to 4,
        "💰 Ventas" to 5,
        "🏡 Fincas" to 8
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF2C7A4B)
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
                            .background(Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 48.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = SessionManager.userEmail ?: "Agricultor",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    TextButton(
                        onClick = {
                            selectedTab = 7
                            scope.launch { drawerState.close() }
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Mi Perfil", color = Color.White.copy(alpha = 0.8f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.3f))
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
                                selectedContainerColor = Color.White.copy(alpha = 0.15f),
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = Color.White,
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        val title = menuItems.find { it.second == selectedTab }?.first ?: if(selectedTab == 7) "Mi Perfil" else "AgroFlow"
                        Text(title)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showNotifications = true }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                            }
                            DropdownMenu(
                                expanded = showNotifications,
                                onDismissRequest = { showNotifications = false },
                                modifier = Modifier.width(300.dp).background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Se regó el cultivo de tomate", fontWeight = FontWeight.Bold) },
                                    onClick = { showNotifications = false }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Nueva tarea completada: Revisar cercas", fontWeight = FontWeight.Bold) },
                                    onClick = { showNotifications = false }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Insumo Urea bajo en stock", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                    onClick = { showNotifications = false }
                                )
                            }
                        }
                    }
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
        
        // Startup Finca Dialog
        if (personnelViewModel.selectedFinca == null && selectedTab != 8) {
            AlertDialog(
                onDismissRequest = {}, // Force selection
                confirmButton = {
                    Button(onClick = { selectedTab = 8 }) {
                        Text("Ir a Fincas")
                    }
                },
                title = { Text("¡Bienvenido a AgroFlow!") },
                text = { Text("Para empezar a usar el sistema, debes crear o seleccionar una finca activa.") }
            )
        }
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
