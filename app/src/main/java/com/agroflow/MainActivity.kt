package com.agroflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import com.agroflow.core.theme.AgroFlowTheme
import com.agroflow.core.session.SessionManager
import com.agroflow.feature.dashboard.presentation.ui.DashboardScreen
import com.agroflow.feature.auth.presentation.ui.LoginScreen
import com.agroflow.feature.auth.presentation.ui.RecoverPasswordScreen

enum class NavScreen { LANDING, LOGIN, RECOVER_PASSWORD, REGISTRO_CLIENTE, DASHBOARD, ADMIN_MANAGE_USERS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgroFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        var currentScreen by remember { 
                            mutableStateOf(if (SessionManager.isLoggedIn()) NavScreen.DASHBOARD else NavScreen.LANDING) 
                        }

                        // Force navigate to LANDING when session is cleared
                        val isLoggedIn = SessionManager.isLoggedIn()
                        LaunchedEffect(isLoggedIn) {
                            if (!isLoggedIn && currentScreen != NavScreen.LANDING && currentScreen != NavScreen.LOGIN && currentScreen != NavScreen.REGISTRO_CLIENTE && currentScreen != NavScreen.RECOVER_PASSWORD) {
                                currentScreen = NavScreen.LANDING
                            }
                        }
                        
                        when (currentScreen) {
                            NavScreen.LANDING -> {
                                com.agroflow.feature.auth.presentation.ui.LandingScreen(
                                    onNavigateToLogin = { currentScreen = NavScreen.LOGIN },
                                    onNavigateToRegistroCliente = { currentScreen = NavScreen.REGISTRO_CLIENTE }
                                )
                            }
                            NavScreen.REGISTRO_CLIENTE -> {
                                com.agroflow.feature.auth.presentation.ui.RegistroClienteScreen(
                                    onNavigateToLogin = { currentScreen = NavScreen.LOGIN }
                                )
                            }
                            NavScreen.DASHBOARD -> {
                                val roleId = SessionManager.roleId?.lowercase() ?: ""
                                val isEmpleado = roleId == SessionManager.ROLE_TRABAJADOR.lowercase() || roleId.contains("empleado") || roleId.contains("trabajador") || roleId == "2"
                                val isCliente = roleId == SessionManager.ROLE_CLIENTE.lowercase() || roleId.contains("cliente") || roleId == "3"
                                val isAdmin = roleId == SessionManager.ROLE_ADMIN.lowercase()
                                
                                if (isAdmin) {
                                    com.agroflow.feature.admin.presentation.ui.AdminDashboardScreen(
                                        onNavigateToManageUsers = { currentScreen = NavScreen.ADMIN_MANAGE_USERS },
                                        onLogout = { 
                                            SessionManager.clearSession()
                                            val intent = android.content.Intent(this@MainActivity, MainActivity::class.java)
                                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                    )
                                } else if (isEmpleado) {
                                    com.agroflow.feature.empleado.presentation.ui.EmpleadoDashboardScreen(
                                        onLogout = { 
                                            SessionManager.clearSession()
                                            val intent = android.content.Intent(this@MainActivity, MainActivity::class.java)
                                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                    )
                                } else if (isCliente) {
                                    com.agroflow.feature.vitrina.presentation.ui.VitrinaScreen(
                                        onLogout = { 
                                            SessionManager.clearSession()
                                            val intent = android.content.Intent(this@MainActivity, MainActivity::class.java)
                                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                    )
                                } else {
                                    DashboardScreen(
                                        onLogout = { 
                                            SessionManager.clearSession()
                                            val intent = android.content.Intent(this@MainActivity, MainActivity::class.java)
                                            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
                                    )
                                }
                            }
                            NavScreen.LOGIN -> {
                                LoginScreen(
                                    onLoginSuccess = {
                                        currentScreen = NavScreen.DASHBOARD
                                        android.widget.Toast.makeText(
                                            this@MainActivity,
                                            "Bienvenido a AgroFlow",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    onRecoverPasswordClick = {
                                        currentScreen = NavScreen.RECOVER_PASSWORD
                                    }
                                )
                            }
                            NavScreen.RECOVER_PASSWORD -> {
                                RecoverPasswordScreen(
                                    onBackToLogin = {
                                        currentScreen = NavScreen.LOGIN
                                    }
                                )
                            }
                            NavScreen.ADMIN_MANAGE_USERS -> {
                                com.agroflow.feature.admin.presentation.ui.AdminManageUsersScreen(
                                    onBack = {
                                        currentScreen = NavScreen.DASHBOARD
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}