package com.mae.workoutmae.ui.screen.progreso

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mae.workoutmae.data.repository.MedidaRepository
import com.mae.workoutmae.data.repository.SesionRepository
import com.mae.workoutmae.ui.theme.*

@Composable
fun ProgresoScreen(
    sesionRepository: SesionRepository,
    medidaRepository: MedidaRepository,
) {
    val vm: ProgresoViewModel = viewModel(
        factory = ProgresoViewModel.factory(sesionRepository, medidaRepository)
    )
    val periodo by vm.periodo.collectAsStateWithLifecycle()
    val sesiones by vm.sesiones.collectAsStateWithLifecycle()
    val alertas by vm.alertas.collectAsStateWithLifecycle()
    val chartData by vm.chartData.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // ── Selector de período ──────────────────────────────────────────
        item {
            Text("Progreso y análisis", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Periodo.entries.forEach { p ->
                    item {
                        FilterChip(
                            selected = periodo == p,
                            onClick = { vm.setPeriodo(p) },
                            label = { Text(p.label) },
                        )
                    }
                }
            }
        }

        // ── Sin datos ────────────────────────────────────────────────────
        if (sesiones.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Filled.ShowChart, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                    Text("Sin datos para este período", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Registra sesiones para ver el progreso.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {

            // ── Alertas clínicas ─────────────────────────────────────────
            if (alertas.isNotEmpty()) {
                item {
                    Text("Alertas clínicas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                alertas.forEach { alerta ->
                    item { AlertaCard(alerta) }
                }
            }

            // ── KPIs destacados ──────────────────────────────────────────
            item {
                Text("KPIs principales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val mejorStepup = sesiones.filter { it.tipo == "piernas" }.maxOfOrNull { it.stepupReps ?: 0 }
                    val mejorWallsit = sesiones.filter { it.tipo == "piernas" }.maxOfOrNull { it.wallsitSegundos ?: 0 }
                    KpiDestacado("Step-ups", mejorStepup?.toString() ?: "–", "reps (mejor)", Icons.Filled.TrendingUp, Modifier.weight(1f))
                    KpiDestacado("Wall Sit", mejorWallsit?.toString() ?: "–", "seg (mejor)", Icons.Filled.Timer, Modifier.weight(1f))
                }
            }

            // ── Gráfico dolor ────────────────────────────────────────────
            if (chartData.dolorDurante.isNotEmpty()) {
                item {
                    GraficoCard(titulo = "Tendencia dolor tendón", subtitulo = "● Durante  ● Post/mañana") {
                        GraficoLineaDoble(
                            serie1 = chartData.dolorDurante,
                            serie2 = chartData.dolorPost,
                            maxY = 10f,
                            color1 = TealGreen,
                            color2 = DolorNaranja,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LeyendaItem(TealGreen, "Durante")
                            LeyendaItem(DolorNaranja, "Post")
                            Spacer(Modifier.weight(1f))
                            Text("Objetivo: 0", style = MaterialTheme.typography.labelSmall, color = DolorVerde, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // ── Step-ups por semana ──────────────────────────────────────
            if (chartData.stepupsPorSemana.isNotEmpty()) {
                item {
                    GraficoCard(titulo = "★ Step-ups — máximo semanal", subtitulo = "Reps hasta falla") {
                        GraficoBarras(
                            datos = chartData.stepupsPorSemana,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                        )
                    }
                }
            }

            // ── Wall Sit por sesión ──────────────────────────────────────
            if (chartData.wallsitPorSesion.isNotEmpty()) {
                item {
                    GraficoCard(titulo = "★ Wall Sit — progresión", subtitulo = "Segundos por sesión") {
                        GraficoLinea(
                            datos = chartData.wallsitPorSesion,
                            maxY = (chartData.wallsitPorSesion.maxOfOrNull { it.valor } ?: 120f) * 1.2f,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                        )
                    }
                }
            }

            // ── Distribución sesiones ────────────────────────────────────
            if (chartData.sesionesXTipo.values.sum() > 0) {
                item {
                    GraficoCard(titulo = "Distribución de sesiones", subtitulo = "${sesiones.size} sesiones en el período") {
                        DistribucionSesiones(chartData.sesionesXTipo)
                    }
                }
            }

            // ── Adherencia suplementación ────────────────────────────────
            item {
                GraficoCard(titulo = "Adherencia colágeno", subtitulo = "Días registrados / días totales") {
                    AdherenciaBar(chartData.adherenciaColageno)
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

// ── Alertas ──────────────────────────────────────────────────────────────────

@Composable
private fun AlertaCard(alerta: Alerta) {
    val (bgColor, iconColor, icon) = when (alerta.nivel) {
        NivelAlerta.CRITICA      -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, Icons.Filled.Error)
        NivelAlerta.ADVERTENCIA  -> Triple(Color(0xFFFFF3CD), Color(0xFFB45309), Icons.Filled.Warning)
        NivelAlerta.INFO         -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, Icons.Filled.Info)
    }
    Card(colors = CardDefaults.cardColors(containerColor = bgColor), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp).padding(top = 2.dp))
            Text(alerta.mensaje, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        }
    }
}

// ── Gráficos Canvas ───────────────────────────────────────────────────────────

@Composable
private fun GraficoLineaDoble(
    serie1: List<PuntoGrafico>,
    serie2: List<PuntoGrafico>,
    maxY: Float,
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier,
) {
    val padLeft = 32f
    val padBottom = 28f
    val padTop = 12f

    Canvas(modifier = modifier) {
        val chartW = size.width - padLeft - 8f
        val chartH = size.height - padBottom - padTop

        // Líneas de referencia horizontales
        for (y in listOf(0, 2, 5, 7, 10)) {
            val yPos = padTop + chartH - (y / maxY) * chartH
            drawLine(Color.Gray.copy(alpha = 0.18f), Offset(padLeft, yPos), Offset(size.width - 4f, yPos), 1f)
        }

        // Línea objetivo en 0 (verde prominente)
        val y0 = padTop + chartH
        drawLine(DolorVerde, Offset(padLeft, y0), Offset(size.width - 4f, y0), strokeWidth = 2.5f, cap = StrokeCap.Round)

        // Función para dibujar una serie
        fun dibujarSerie(puntos: List<PuntoGrafico>, color: Color) {
            if (puntos.size < 2) return
            val stepX = if (puntos.size > 1) chartW / (puntos.size - 1) else chartW
            val path = Path()
            puntos.forEachIndexed { i, p ->
                val x = padLeft + i * stepX
                val y = padTop + chartH - (p.valor.coerceIn(0f, maxY) / maxY) * chartH
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = color, style = Stroke(width = 3f, cap = StrokeCap.Round))
            // Puntos
            puntos.forEachIndexed { i, p ->
                val x = padLeft + i * stepX
                val y = padTop + chartH - (p.valor.coerceIn(0f, maxY) / maxY) * chartH
                drawCircle(color, radius = 5f, center = Offset(x, y))
                drawCircle(Color.White, radius = 2.5f, center = Offset(x, y))
            }
        }

        dibujarSerie(serie2, color2)
        dibujarSerie(serie1, color1)

        // Etiquetas Y
        for (y in listOf(0, 5, 10)) {
            val yPos = padTop + chartH - (y / maxY) * chartH
            // No podemos dibujar texto en Canvas directamente con SP; omitimos etiquetas complejas
        }
    }
}

@Composable
private fun GraficoLinea(
    datos: List<PuntoGrafico>,
    maxY: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (datos.isEmpty()) return
    val padLeft = 8f
    val padBottom = 24f
    val padTop = 8f

    Canvas(modifier = modifier) {
        if (datos.size < 2) return@Canvas
        val chartW = size.width - padLeft - 8f
        val chartH = size.height - padBottom - padTop
        val stepX = chartW / (datos.size - 1)

        // Fondo degradado sutil
        val path = Path()
        datos.forEachIndexed { i, p ->
            val x = padLeft + i * stepX
            val y = padTop + chartH - (p.valor.coerceIn(0f, maxY) / maxY) * chartH
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        // Línea principal
        drawPath(path, color, style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Puntos
        datos.forEachIndexed { i, p ->
            val x = padLeft + i * stepX
            val y = padTop + chartH - (p.valor.coerceIn(0f, maxY) / maxY) * chartH
            drawCircle(color, radius = 5f, center = Offset(x, y))
            drawCircle(Color.White, radius = 2.5f, center = Offset(x, y))
        }

        // Línea base
        drawLine(Color.Gray.copy(alpha = 0.3f), Offset(padLeft, padTop + chartH), Offset(size.width - 4f, padTop + chartH), 1.5f)
    }
}

@Composable
private fun GraficoBarras(
    datos: List<PuntoGrafico>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (datos.isEmpty()) return
    val maxY = datos.maxOf { it.valor } * 1.15f
    val padLeft = 8f
    val padBottom = 24f
    val padTop = 8f
    val gap = 4f

    Canvas(modifier = modifier) {
        if (datos.isEmpty()) return@Canvas
        val chartW = size.width - padLeft - 8f
        val chartH = size.height - padBottom - padTop
        val barWidth = (chartW / datos.size) - gap

        datos.forEachIndexed { i, p ->
            val barH = (p.valor.coerceIn(0f, maxY) / maxY) * chartH
            val x = padLeft + i * (barWidth + gap)
            val y = padTop + chartH - barH
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth.coerceAtLeast(4f), barH.coerceAtLeast(2f)),
                cornerRadius = CornerRadius(4f, 4f),
            )
        }

        // Línea base
        drawLine(Color.Gray.copy(alpha = 0.3f), Offset(padLeft, padTop + chartH), Offset(size.width - 4f, padTop + chartH), 1.5f)
    }
}

// ── Componentes de resumen ────────────────────────────────────────────────────

@Composable
private fun DistribucionSesiones(dist: Map<String, Int>) {
    val total = dist.values.sum()
    if (total == 0) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        dist.forEach { (tipo, count) ->
            val fraccion = count.toFloat() / total
            val color = if (tipo == "piernas") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            val label = if (tipo == "piernas") "Piernas / Fuerza" else "Escalada"
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(120.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.15f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraccion)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color),
                    )
                }
                Text("$count", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun AdherenciaBar(fraccion: Float) {
    val pct = (fraccion * 100).toInt()
    val color = when {
        pct >= 80 -> DolorVerde
        pct >= 50 -> DolorAmarillo
        else      -> DolorRojo
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("$pct% de sesiones con colágeno registrado", style = MaterialTheme.typography.bodyMedium)
            Text(if (pct >= 80) "✓ Excelente" else if (pct >= 50) "Regular" else "Mejorar", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { fraccion.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
        )
    }
}

// ── Helpers UI ────────────────────────────────────────────────────────────────

@Composable
private fun GraficoCard(titulo: String, subtitulo: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun KpiDestacado(label: String, valor: String, unidad: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(unidad, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LeyendaItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
