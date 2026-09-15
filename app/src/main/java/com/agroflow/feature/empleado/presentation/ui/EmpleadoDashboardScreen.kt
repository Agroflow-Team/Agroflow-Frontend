package com.agroflow.feature.empleado.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.agroflow.feature.empleado.presentation.EmpleadoViewModel
import com.agroflow.feature.profile.presentation.ui.ProfileEditScreen
import kotlinx.coroutines.launch
import com.agroflow.core.session.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoDashboardScreen(
    onLogout: () -> Unit
) {
    val viewModel: EmpleadoViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    var showNotifications by remember { mutableStateOf(false) }

    // Cargar automáticamente las tareas, la finca y el inventario del trabajador
    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFA5D6A7),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 40.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.nombreTrabajador ?: SessionManager.userEmail ?: "Trabajador",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SessionManager.userEmail ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Mi Perfil",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            selectedTab = 4
                            scope.launch { drawerState.close() }
                        }
                    )
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    label = { Text("📊 Dashboard") },
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("✅ Gestión de Tareas") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("📦 Inventario") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("💰 Información (Pagos)") },
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        scope.launch { drawerState.close() }
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Logout Button
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF453A))
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        val title = when(selectedTab) {
                            0 -> "Dashboard"
                            1 -> "Gestión de Tareas"
                            2 -> "Inventario"
                            3 -> "Pagos"
                            4 -> "Mi Perfil"
                            else -> "AgroFlow Worker"
                        }
                        Column {
                            Text(title)
                            // Display Finca
                            val fincaText = viewModel.currentFincaNombre ?: if (viewModel.currentFincaId != null) "Finca Asignada" else "Sin finca asignada"
                            val fincaColor = if (viewModel.currentFincaId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            Text("🏡 $fincaText", style = MaterialTheme.typography.bodySmall, color = fincaColor)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showNotifications = true }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                            DropdownMenu(
                                expanded = showNotifications,
                                onDismissRequest = { showNotifications = false },
                                modifier = Modifier.width(300.dp).background(MaterialTheme.colorScheme.surface)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nuevas tareas asignadas", fontWeight = FontWeight.Bold) },
                                    onClick = { showNotifications = false }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Se aprobó tu reporte de horas", fontWeight = FontWeight.Bold) },
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
                    0 -> WorkerHomeScreen(viewModel)
                    1 -> WorkerTasksScreen(viewModel)
                    2 -> WorkerInventoryScreen(viewModel)
                    3 -> WorkerInfoScreen(viewModel)
                    4 -> ProfileEditScreen(onLogout = onLogout)
                }
            }
        }
    }
}
