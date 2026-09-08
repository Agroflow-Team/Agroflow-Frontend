package com.agroflow.feature.dashboard.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    val personnelViewModel: PersonnelViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }

    val menuItems = listOf(
        "📊 Dashboard" to 0,
        "📦 Inventario" to 1,
        "👥 Gestión de Empleados" to 2,
        "✅ Gestión de Tareas" to 3,
        "🏷️ Catálogo" to 4,
        "💰 Ventas" to 5,
        "📈 Balance Financiero" to 6
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
                        Text("Mi Perfil", color = Color.White)
                    }
                    
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    // Menu items
                    menuItems.forEach { (title, index) ->
                        NavigationDrawerItem(
                            label = { Text(title) },
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
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Logout button
                    TextButton(
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            when(selectedTab) {
                                0 -> "Dashboard"
                                1 -> "Inventario"
                                2 -> "Empleados"
                                3 -> "Tareas"
                                4 -> "Catálogo"
                                5 -> "Ventas"
                                6 -> "Balance Financiero"
                                7 -> "Mi Perfil"
                                else -> ""
                            }
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Show notifications */ }) {
                            Icon(androidx.compose.material.icons.Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF3EFE7)
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when(selectedTab) {
                    0 -> DashboardHomeScreen()
                    1 -> com.agroflow.feature.inventory.presentation.ui.InventoryScreen(personnelViewModel)
                    2 -> com.agroflow.feature.personnel.presentation.ui.PersonnelScreen(personnelViewModel)
                    3 -> com.agroflow.feature.tasks.presentation.ui.TasksScreen(personnelViewModel)
                    4 -> com.agroflow.feature.vitrina.presentation.ui.MisPublicacionesScreen(personnelViewModel)
                    5 -> com.agroflow.feature.sales.presentation.ui.SalesScreen(personnelViewModel)
                    6 -> com.agroflow.feature.finance.presentation.ui.FinanceScreen(personnelViewModel)
                    7 -> com.agroflow.feature.profile.presentation.ui.ProfileEditScreen(onLogout)
                }
            }
        }
    }
}
