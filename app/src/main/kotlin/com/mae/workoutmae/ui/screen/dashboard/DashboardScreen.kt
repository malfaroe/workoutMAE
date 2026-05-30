package com.mae.workoutmae.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mae.workoutmae.data.preferences.PreferencesManager
import com.mae.workoutmae.data.repository.MedidaRepository
import com.mae.workoutmae.data.repository.SesionRepository
import com.mae.workoutmae.ui.theme.AsimetriaAlta
import com.mae.workoutmae.ui.theme.AsimetriaMedia
import com.mae.workoutmae.ui.theme.AsimetriaOk
import com.mae.workoutmae.ui.theme.dolorColor

@Composable
fun DashboardScreen(
    sesionRepository: SesionRepository,
    medidaRepository: MedidaRepository,
    preferencesManager: PreferencesManager,
    onRegistrar: () -> Unit,
) {
    val vm: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(sesionRepository, medidaRepository, preferencesManager)
    )

    val nombre by vm.nombrePaciente.collectAsStateWithLifecycle()
    val diasInicio by vm.diasDesdeInicio.collectAsStateWithLifecycle()
    val total by vm.totalSesiones.collectAsStateWithLifecycle()
    val mejorStepup by vm.mejorStepup.collectAsStateWithLifecycle()
    val dolorProm by vm.dolorPromedio.collectAsStateWithLifecycle()
    val ultimas by vm.ultimas10Sesiones.collectAsStateWithLifecycle()
    val ultimaMedida by vm.ultimaMedida.collectAsStateWithLifecycle()
    val diasSinMedicion by vm.diasSinMedicion.collectAsStateWithLifecycle()
    val racha by vm.rachaActual.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = if (nombre.isBlank()) "WorkoutMAE" else "Hola, $nombre",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (diasInicio > 0) {
                        Text(
                            text = "Día $diasInicio del protocolo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                // Racha
                if (racha > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🔥", fontSize = 22.sp)
                        }
                        Text("$racha días", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Botón principal
        item {
            Button(
                onClick = onRegistrar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Registrar sesión de hoy", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Alerta medición corporal
        if (diasSinMedicion != null && diasSinMedicion!! >= 28) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            "Han pasado $diasSinMedicion días sin medir. Registra tus medidas.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }

        // Métricas clave
        item {
            Text("Métricas clave", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    MetricCard(
                        label = "Sesiones",
                        valor = "$total",
                        icon = Icons.Filled.FitnessCenter,
                    )
                }
                item {
                    MetricCard(
                        label = "Dolor prom. 2s",
                        valor = dolorProm?.let { "%.1f".format(it) } ?: "–",
                        icon = Icons.Filled.MonitorHeart,
                        color = dolorProm?.let { dolorColor(it.toInt()) },
                    )
                }
                item {
                    MetricCard(
                        label = "Mejor step-up",
                        valor = mejorStepup?.toString() ?: "–",
                        unidad = "reps",
                        icon = Icons.Filled.TrendingUp,
                        destacado = true,
                    )
                }
                ultimaMedida?.asimetriaCm?.let { asim ->
                    item {
                        MetricCard(
                            label = "Asimetría muslo",
                            valor = "%.1f".format(asim),
                            unidad = "cm",
                            icon = Icons.Filled.Accessibility,
                            color = when {
                                asim <= 1f -> AsimetriaOk
                                asim <= 2f -> AsimetriaMedia
                                else       -> AsimetriaAlta
                            },
                            destacado = true,
                        )
                    }
                }
            }
        }

        // Mini gráfico dolor últimas 10 sesiones
        if (ultimas.isNotEmpty()) {
            item {
                Text("Dolor últimas 10 sesiones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                MiniDolorChart(sesiones = ultimas)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun MetricCard(
    label: String,
    valor: String,
    unidad: String = "",
    icon: ImageVector,
    color: Color? = null,
    destacado: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.width(130.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (destacado) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color ?: MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = valor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color ?: MaterialTheme.colorScheme.onSurface,
                )
                if (unidad.isNotBlank()) {
                    Text(unidad, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MiniDolorChart(sesiones: List<com.mae.workoutmae.data.db.entity.Sesion>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(60.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            sesiones.reversed().forEach { s ->
                val altura = (s.dolorDurante / 10f).coerceIn(0.05f, 1f)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(altura)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(dolorColor(s.dolorDurante)),
                    )
                }
            }
        }
    }
}
