package com.agroflow.feature.empleado.presentation.ui

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoDashboardScreen(
    onLogout: () -> Unit
) {
    val viewModel: EmpleadoViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

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
                    Text(text = "worker@agroflow.com", fontWeight = FontWeight.Bold)
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
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("AgroFlow Worker") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Notifications */ }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
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

