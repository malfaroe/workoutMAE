package com.mae.workoutmae.ui.screen.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mae.workoutmae.data.export.ExportManager
import com.mae.workoutmae.data.preferences.PreferencesManager
import com.mae.workoutmae.data.repository.MedidaRepository
import com.mae.workoutmae.data.repository.SesionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    sesionRepository: SesionRepository,
    medidaRepository: MedidaRepository,
    preferencesManager: PreferencesManager,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sesiones by sesionRepository.todas().collectAsStateWithLifecycle(emptyList())
    val medidas by medidaRepository.todas().collectAsStateWithLifecycle(emptyList())
    val nombre by preferencesManager.nombrePaciente.collectAsStateWithLifecycle("")
    val fechaInicio by preferencesManager.fechaInicio.collectAsStateWithLifecycle("")

    var cargando by remember { mutableStateOf(false) }
    val snackState = remember { SnackbarHostState() }

    fun exportar(accion: suspend () -> Unit) {
        scope.launch {
            cargando = true
            try {
                withContext(Dispatchers.IO) { accion() }
            } catch (e: Exception) {
                snackState.showSnackbar("Error al exportar: ${e.localizedMessage}")
            } finally {
                cargando = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) },
        topBar = {
            TopAppBar(
                title = { Text("Exportar datos") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                },
            )
        },
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Comparte tus datos con tu kinesiólogo o guárdalos como respaldo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                ExportCard(
                    icon = Icons.Filled.FitnessCenter,
                    titulo = "Historial de sesiones",
                    descripcion = "${sesiones.size} sesiones registradas",
                    formato = "CSV",
                    habilitado = sesiones.isNotEmpty(),
                    onExportar = {
                        exportar {
                            ExportManager.compartirCsv(
                                context,
                                ExportManager.sesionesACsv(sesiones),
                                "workoutmae_sesiones.csv",
                            )
                        }
                    },
                )
            }

            item {
                ExportCard(
                    icon = Icons.Filled.Straighten,
                    titulo = "Medidas corporales",
                    descripcion = "${medidas.size} registros de medidas",
                    formato = "CSV",
                    habilitado = medidas.isNotEmpty(),
                    onExportar = {
                        exportar {
                            ExportManager.compartirCsv(
                                context,
                                ExportManager.medidasACsv(medidas),
                                "workoutmae_medidas.csv",
                            )
                        }
                    },
                )
            }

            item {
                ExportCard(
                    icon = Icons.Filled.PictureAsPdf,
                    titulo = "Reporte completo",
                    descripcion = "Sesiones + medidas + resumen de progreso",
                    formato = "PDF",
                    habilitado = sesiones.isNotEmpty() || medidas.isNotEmpty(),
                    onExportar = {
                        exportar {
                            ExportManager.compartirPdf(
                                context,
                                sesiones,
                                medidas,
                                nombre,
                                fechaInicio,
                            )
                        }
                    },
                )
            }

            item {
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    "Los archivos CSV se pueden abrir en Excel o Google Sheets. " +
                    "El PDF incluye tablas con todos los registros y un resumen clínico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Tarjeta de exportación ────────────────────────────────────────────────────

@Composable
private fun ExportCard(
    icon: ImageVector,
    titulo: String,
    descripcion: String,
    formato: String,
    habilitado: Boolean,
    onExportar: () -> Unit,
) {
    val formatoColor = if (formato == "PDF") MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = formatoColor.copy(alpha = 0.12f),
                    ) {
                        Text(
                            formato,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = formatoColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onExportar,
                enabled = habilitado,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Filled.Share, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Exportar")
            }
        }
    }
}
