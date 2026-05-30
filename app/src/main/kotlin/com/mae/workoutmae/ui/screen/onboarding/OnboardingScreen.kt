package com.mae.workoutmae.ui.screen.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mae.workoutmae.data.preferences.PreferencesManager
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(preferencesManager: PreferencesManager) {
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf(LocalDate.now().toString()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // DatePicker returns UTC midnight; convert to local date
                        val epoch = java.time.Instant.ofEpochMilli(millis)
                        fechaInicio = epoch.atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))

            // Logo / branding
            Text(
                text = "WorkoutMAE",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Rehabilitación tendinopatía patelar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            Text(
                text = "¡Bienvenido!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Configuremos tu perfil antes de empezar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    error = ""
                },
                label = { Text("Tu nombre") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error.isNotEmpty(),
                supportingText = if (error.isNotEmpty()) ({ Text(error) }) else null,
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = fechaInicio,
                onValueChange = {},
                readOnly = true,
                label = { Text("Inicio del protocolo de rehab") },
                leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Esta fecha se usará para calcular la semana de tu protocolo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    if (nombre.isBlank()) {
                        error = "Ingresa tu nombre para continuar"
                        return@Button
                    }
                    scope.launch {
                        preferencesManager.setNombre(nombre.trim())
                        preferencesManager.setFechaInicio(fechaInicio)
                        preferencesManager.setOnboardingCompletado()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text("Empezar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
