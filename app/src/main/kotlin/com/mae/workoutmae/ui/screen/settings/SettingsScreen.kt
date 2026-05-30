package com.mae.workoutmae.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mae.workoutmae.data.preferences.PreferencesManager
import com.mae.workoutmae.data.repository.EjercicioRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    ejercicioRepository: EjercicioRepository,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val nombre by preferencesManager.nombrePaciente.collectAsStateWithLifecycle("")
    val fechaInicio by preferencesManager.fechaInicio.collectAsStateWithLifecycle("")

    var editNombre by remember(nombre) { mutableStateOf(nombre) }
    var editFecha by remember(fechaInicio) { mutableStateOf(fechaInicio) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                Text("Paciente", style = MaterialTheme.typography.titleMedium)
            }
            item {
                OutlinedTextField(
                    value = editNombre,
                    onValueChange = { editNombre = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            item {
                OutlinedTextField(
                    value = editFecha,
                    onValueChange = { editFecha = it },
                    label = { Text("Fecha inicio protocolo (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ej. 2026-05-29") },
                    singleLine = true,
                )
            }
            item {
                Button(
                    onClick = {
                        scope.launch {
                            preferencesManager.setNombre(editNombre)
                            preferencesManager.setFechaInicio(editFecha)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Guardar") }
            }

            item { HorizontalDivider() }

            item {
                Text("Ejercicios", style = MaterialTheme.typography.titleMedium)
            }
            item {
                OutlinedButton(
                    onClick = { scope.launch { ejercicioRepository.restaurarEjerciciosBase() } },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Restaurar ejercicios base") }
            }

            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text(
                    "WorkoutMAE v1.0.0 · Rehabilitación tendinopatía patelar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
