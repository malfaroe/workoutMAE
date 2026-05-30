package com.mae.workoutmae.ui.screen.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mae.workoutmae.data.preferences.PreferencesManager
import com.mae.workoutmae.data.repository.EjercicioRepository
import com.mae.workoutmae.notifications.NotificationScheduler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    ejercicioRepository: EjercicioRepository,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val nombre by preferencesManager.nombrePaciente.collectAsStateWithLifecycle("")
    val fechaInicio by preferencesManager.fechaInicio.collectAsStateWithLifecycle("")
    val recordatorioActivo by preferencesManager.recordatorioActivo.collectAsStateWithLifecycle(false)
    val recordatorioHora by preferencesManager.recordatorioHora.collectAsStateWithLifecycle(8 * 60)

    var editNombre by remember(nombre) { mutableStateOf(nombre) }
    var editFecha by remember(fechaInicio) { mutableStateOf(fechaInicio) }
    var showTimePicker by remember { mutableStateOf(false) }

    val horaDisplay = remember(recordatorioHora) {
        "%02d:%02d".format(recordatorioHora / 60, recordatorioHora % 60)
    }

    // Runtime POST_NOTIFICATIONS permission (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                preferencesManager.setRecordatorio(true, recordatorioHora)
                NotificationScheduler.schedule(context, recordatorioHora)
            }
        }
    }

    fun toggleRecordatorio(activo: Boolean) {
        scope.launch {
            if (activo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    preferencesManager.setRecordatorio(true, recordatorioHora)
                    NotificationScheduler.schedule(context, recordatorioHora)
                }
            } else {
                preferencesManager.setRecordatorio(false, recordatorioHora)
                NotificationScheduler.cancel(context)
            }
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = recordatorioHora / 60,
            initialMinute = recordatorioHora % 60,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Hora del recordatorio") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimeInput(state = timeState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val nuevaHoraMin = timeState.hour * 60 + timeState.minute
                    scope.launch {
                        preferencesManager.setRecordatorio(recordatorioActivo, nuevaHoraMin)
                        if (recordatorioActivo) NotificationScheduler.schedule(context, nuevaHoraMin)
                    }
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── Paciente ──────────────────────────────────────────────────────
            item { Text("Paciente", style = MaterialTheme.typography.titleMedium) }
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
                ) { Text("Guardar datos") }
            }

            item { HorizontalDivider() }

            // ── Recordatorio diario ───────────────────────────────────────────
            item { Text("Recordatorio diario", style = MaterialTheme.typography.titleMedium) }
            item {
                ListItem(
                    headlineContent = { Text("Recordatorio diario") },
                    supportingContent = {
                        Text(
                            if (recordatorioActivo) "Activo a las $horaDisplay"
                            else "Desactivado"
                        )
                    },
                    leadingContent = { Icon(Icons.Filled.Notifications, null) },
                    trailingContent = {
                        Switch(
                            checked = recordatorioActivo,
                            onCheckedChange = { toggleRecordatorio(it) },
                        )
                    },
                )
            }
            if (recordatorioActivo) {
                item {
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Schedule, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cambiar hora ($horaDisplay)")
                    }
                }
            }

            item { HorizontalDivider() }

            // ── Ejercicios ────────────────────────────────────────────────────
            item { Text("Ejercicios", style = MaterialTheme.typography.titleMedium) }
            item {
                OutlinedButton(
                    onClick = { scope.launch { ejercicioRepository.restaurarEjerciciosBase() } },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Restaurar ejercicios base") }
            }

            item { HorizontalDivider() }

            // ── Acerca de ─────────────────────────────────────────────────────
            item { Spacer(Modifier.height(4.dp)) }
            item {
                Text(
                    "WorkoutMAE v1.0.0 · Rehabilitación tendinopatía patelar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
