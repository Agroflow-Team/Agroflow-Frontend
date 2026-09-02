package com.agroflow.feature.admin.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agroflow.feature.admin.presentation.AdminManageUsersViewModel
import com.agroflow.feature.admin.presentation.ManageUsersUiState

@Composable
fun AdminManageUsersScreen(
    onBack: () -> Unit,
    viewModel: AdminManageUsersViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Administrador") }
    val roles = listOf("Administrador", "Agricultor")

    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear Nuevo Usuario", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Rol", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Column(modifier = Modifier.fillMaxWidth()) {
            roles.forEach { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = (role == selectedRole),
                        onClick = { selectedRole = role }
                    )
                    Text(text = role)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        when (uiState) {
            is ManageUsersUiState.Loading -> CircularProgressIndicator()
            is ManageUsersUiState.Success -> {
                Text("Usuario creado exitosamente", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    viewModel.resetState()
                    onBack()
                }) {
                    Text("Volver")
                }
            }
            is ManageUsersUiState.Error -> {
                Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
            }
            else -> {}
        }

        if (uiState !is ManageUsersUiState.Loading && uiState !is ManageUsersUiState.Success) {
            Button(
                onClick = { viewModel.createUser(name, email, password, selectedRole) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear Usuario")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
