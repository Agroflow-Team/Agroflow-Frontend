package com.agroflow.feature.dashboard.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.inventory.presentation.ui.InventoryScreen
import com.agroflow.feature.personnel.presentation.PersonnelViewModel
import com.agroflow.feature.personnel.presentation.ui.PersonnelScreen
import com.agroflow.feature.tasks.presentation.ui.TasksScreen

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val personnelViewModel: PersonnelViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("🧑") },
                    label = { Text("Fincas") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Text("📋") },
                    label = { Text("Tareas") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Text("📦") },
                    label = { Text("Inventario") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Text("💰") },
                    label = { Text("Finanzas") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Text("🛒") },
                    label = { Text("Vitrina") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
                NavigationBarItem(
                    icon = { Text("⚙️") },
                    label = { Text("Perfil") },
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> PersonnelScreen(personnelViewModel)
                1 -> TasksScreen(personnelViewModel)
                2 -> InventoryScreen(personnelViewModel)
                3 -> com.agroflow.feature.finance.presentation.ui.FinanceScreen(personnelViewModel)
                4 -> com.agroflow.feature.vitrina.presentation.ui.MisPublicacionesScreen()
                5 -> ProfileScreen(onLogout)
            }
        }
    }
}

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Perfil y Configuración", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Email: ${SessionManager.userEmail}")
        Text("Rol ID: ${SessionManager.roleId}")
        Text("Token: ${SessionManager.token}")
        
        Spacer(Modifier.height(32.dp))
        
        Button(onClick = {
            SessionManager.clearSession()
            onLogout()
        }) {
            Text("Cerrar Sesión")
        }
    }
}
