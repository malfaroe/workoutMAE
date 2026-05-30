package com.mae.workoutmae.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.mae.workoutmae.data.preferences.PreferencesManager
import com.mae.workoutmae.data.repository.EjercicioRepository
import com.mae.workoutmae.data.repository.MedidaRepository
import com.mae.workoutmae.data.repository.SesionRepository
import com.mae.workoutmae.ui.screen.dashboard.DashboardScreen
import com.mae.workoutmae.ui.screen.ejercicios.EjerciciosScreen
import com.mae.workoutmae.ui.screen.historial.HistorialScreen
import com.mae.workoutmae.ui.screen.medidas.MedidasScreen
import com.mae.workoutmae.ui.screen.export.ExportScreen
import com.mae.workoutmae.ui.screen.onboarding.OnboardingScreen
import com.mae.workoutmae.ui.screen.progreso.ProgresoScreen
import com.mae.workoutmae.ui.screen.registro.RegistroScreen
import com.mae.workoutmae.ui.screen.settings.SettingsScreen

sealed class Ruta(val path: String, val label: String, val icon: ImageVector) {
    object Dashboard : Ruta("dashboard", "Inicio", Icons.Filled.Home)
    object Progreso  : Ruta("progreso",  "Progreso", Icons.Filled.ShowChart)
    object Historial : Ruta("historial", "Historial", Icons.Filled.List)
    object Mas       : Ruta("mas",       "Más", Icons.Filled.MoreHoriz)
    object Registro  : Ruta("registro/{sesionId}", "Registrar", Icons.Filled.Add)
    object Medidas   : Ruta("medidas", "Medidas", Icons.Filled.FitnessCenter)
    object Ejercicios: Ruta("ejercicios", "Ejercicios", Icons.Filled.SportsGymnastics)
    object Exportar  : Ruta("exportar", "Exportar", Icons.Filled.Share)
    object Settings  : Ruta("settings", "Configuración", Icons.Filled.Settings)
}

private val bottomNavItems = listOf(Ruta.Dashboard, Ruta.Progreso, Ruta.Historial, Ruta.Mas)

@Composable
fun AppNavigation(
    sesionRepository: SesionRepository,
    medidaRepository: MedidaRepository,
    ejercicioRepository: EjercicioRepository,
    preferencesManager: PreferencesManager,
) {
    val onboardingOk by preferencesManager.onboardingCompletado.collectAsStateWithLifecycle(false)

    if (!onboardingOk) {
        OnboardingScreen(preferencesManager = preferencesManager)
    } else {
        MainNavigation(sesionRepository, medidaRepository, ejercicioRepository, preferencesManager)
    }
}

@Composable
private fun MainNavigation(
    sesionRepository: SesionRepository,
    medidaRepository: MedidaRepository,
    ejercicioRepository: EjercicioRepository,
    preferencesManager: PreferencesManager,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val rutaActual = backStack?.destination?.route

    val showBottomBar = bottomNavItems.any { it.path == rutaActual }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { ruta ->
                        NavigationBarItem(
                            selected = rutaActual == ruta.path,
                            onClick = {
                                navController.navigate(ruta.path) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(ruta.icon, contentDescription = ruta.label) },
                            label = { Text(ruta.label) },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Ruta.Dashboard.path,
            modifier = Modifier.padding(padding),
        ) {
            composable(Ruta.Dashboard.path) {
                DashboardScreen(
                    sesionRepository = sesionRepository,
                    medidaRepository = medidaRepository,
                    preferencesManager = preferencesManager,
                    onRegistrar = { navController.navigate("registro/0") },
                )
            }
            composable("registro/{sesionId}") { back ->
                val sesionId = back.arguments?.getString("sesionId")?.toIntOrNull() ?: 0
                RegistroScreen(
                    sesionId = sesionId,
                    sesionRepository = sesionRepository,
                    onGuardado = { navController.popBackStack() },
                    onCancelar = { navController.popBackStack() },
                )
            }
            composable(Ruta.Progreso.path) {
                ProgresoScreen(sesionRepository = sesionRepository, medidaRepository = medidaRepository)
            }
            composable(Ruta.Historial.path) {
                HistorialScreen(
                    sesionRepository = sesionRepository,
                    onEditarSesion = { id -> navController.navigate("registro/$id") },
                )
            }
            composable(Ruta.Mas.path) {
                MasScreen(
                    onMedidas    = { navController.navigate(Ruta.Medidas.path) },
                    onEjercicios = { navController.navigate(Ruta.Ejercicios.path) },
                    onExportar   = { navController.navigate(Ruta.Exportar.path) },
                    onSettings   = { navController.navigate(Ruta.Settings.path) },
                )
            }
            composable(Ruta.Medidas.path) {
                MedidasScreen(medidaRepository = medidaRepository, onBack = { navController.popBackStack() })
            }
            composable(Ruta.Ejercicios.path) {
                EjerciciosScreen(ejercicioRepository = ejercicioRepository, onBack = { navController.popBackStack() })
            }
            composable(Ruta.Exportar.path) {
                ExportScreen(
                    sesionRepository = sesionRepository,
                    medidaRepository = medidaRepository,
                    preferencesManager = preferencesManager,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Ruta.Settings.path) {
                SettingsScreen(preferencesManager = preferencesManager, ejercicioRepository = ejercicioRepository, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun MasScreen(
    onMedidas: () -> Unit,
    onEjercicios: () -> Unit,
    onExportar: () -> Unit,
    onSettings: () -> Unit,
) {
    val items = listOf(
        Triple("Medidas corporales",   Icons.Filled.FitnessCenter,     onMedidas),
        Triple("Gestionar ejercicios", Icons.Filled.SportsGymnastics,  onEjercicios),
        Triple("Exportar datos",       Icons.Filled.Share,             onExportar),
        Triple("Configuración",        Icons.Filled.Settings,          onSettings),
    )
    androidx.compose.foundation.lazy.LazyColumn {
        item { ListItem(headlineContent = { Text("Más opciones", style = MaterialTheme.typography.titleLarge) }) }
        items.forEach { (label, icon, action) ->
            item {
                ListItem(
                    headlineContent = { Text(label) },
                    leadingContent = { Icon(icon, null) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, null) },
                    modifier = androidx.compose.ui.Modifier.clickable(onClick = action),
                )
                HorizontalDivider()
            }
        }
    }
}

private fun androidx.compose.ui.Modifier.clickable(onClick: () -> Unit) =
    this.then(androidx.compose.ui.Modifier.clickable(onClick = onClick))
