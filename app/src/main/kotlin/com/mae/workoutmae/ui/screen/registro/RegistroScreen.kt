package com.mae.workoutmae.ui.screen.registro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mae.workoutmae.data.db.entity.Sesion
import com.mae.workoutmae.data.repository.SesionRepository
import com.mae.workoutmae.ui.component.DolorSlider
import com.mae.workoutmae.ui.component.StarRating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    sesionId: Int,
    sesionRepository: SesionRepository,
    onGuardado: () -> Unit,
    onCancelar: () -> Unit,
) {
    val vm: RegistroViewModel = viewModel(factory = RegistroViewModel.factory(sesionRepository))
    val s by vm.sesion.collectAsStateWithLifecycle()
    val guardado by vm.guardado.collectAsStateWithLifecycle()
    val puedeEditarDolorPost by vm.puedeEditarDolorPost.collectAsStateWithLifecycle()

    LaunchedEffect(sesionId) { vm.cargar(sesionId) }
    LaunchedEffect(guardado) { if (guardado) onGuardado() }

    var expandDolor by remember { mutableStateOf(true) }
    var expandRendimiento by remember { mutableStateOf(true) }
    var expandSensaciones by remember { mutableStateOf(true) }
    var expandSuple by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (sesionId == 0) "Nueva sesión" else "Editar sesión") },
                navigationIcon = {
                    IconButton(onClick = onCancelar) { Icon(Icons.Filled.Close, null) }
                },
                actions = {
                    TextButton(onClick = { vm.guardar() }) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            // ── Datos generales ──────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Text("Datos generales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                // Tipo de sesión
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("piernas" to "Piernas + Push-ups", "escalada" to "Escalada").forEach { (tipo, label) ->
                        FilterChip(
                            selected = s.tipo == tipo,
                            onClick = { vm.update(s.copy(tipo = tipo)) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // ── Dolor ────────────────────────────────────────────────────
            item {
                SeccionHeader("Dolor tendón patelar", expandDolor) { expandDolor = !expandDolor }
            }
            item {
                AnimatedVisibility(expandDolor) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DolorSlider(
                            valor = s.dolorDurante,
                            onValorChange = { vm.update(s.copy(dolorDurante = it)) },
                            label = "Durante el entrenamiento",
                        )
                        DolorSlider(
                            valor = s.dolorPost ?: 0,
                            onValorChange = { vm.update(s.copy(dolorPost = it)) },
                            label = "Post / mañana siguiente",
                            enabled = puedeEditarDolorPost,
                        )
                        if (!puedeEditarDolorPost) {
                            Text(
                                "🔒 Bloqueado — más de 48h desde el registro",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedTextField(
                            value = s.descripcionDolor ?: "",
                            onValueChange = { vm.update(s.copy(descripcionDolor = it.takeIf { it.isNotBlank() })) },
                            label = { Text("Descripción del dolor (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                        )
                    }
                }
            }

            // ── Rendimiento Piernas ──────────────────────────────────────
            if (s.tipo == "piernas") {
                item {
                    SeccionHeader("Ejercicios — Piernas", expandRendimiento) { expandRendimiento = !expandRendimiento }
                }
                item {
                    AnimatedVisibility(expandRendimiento) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            // Step-ups — KPI principal
                            KPIField(
                                label = "Step-ups hasta falla",
                                valor = s.stepupReps?.toString() ?: "",
                                onValorChange = { vm.update(s.copy(stepupReps = it.toIntOrNull())) },
                                unidad = "reps",
                                destacado = true,
                            )

                            // Wall Sit — KPI principal
                            KPIField(
                                label = "Wall Sit",
                                valor = s.wallsitSegundos?.toString() ?: "",
                                onValorChange = { vm.update(s.copy(wallsitSegundos = it.toIntOrNull())) },
                                unidad = "seg",
                                destacado = true,
                            )

                            // Slant Board
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KPIField(
                                    label = "Slant Board",
                                    valor = s.slantSeries?.toString() ?: "",
                                    onValorChange = { vm.update(s.copy(slantSeries = it.toIntOrNull())) },
                                    unidad = "series",
                                    modifier = Modifier.weight(1f),
                                )
                                SensacionSelector(
                                    valor = s.slantSensacion,
                                    onValorChange = { vm.update(s.copy(slantSensacion = it)) },
                                    label = "Sensación",
                                    opciones = listOf("normal", "pesado", "muy_pesado"),
                                    etiquetas = listOf("Normal", "Pesado", "Muy pesado"),
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            // Spanish Squat
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KPIField(
                                    label = "Spanish Squat",
                                    valor = s.spanishSeries?.toString() ?: "",
                                    onValorChange = { vm.update(s.copy(spanishSeries = it.toIntOrNull())) },
                                    unidad = "series",
                                    modifier = Modifier.weight(1f),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tempo 5-X-2", style = MaterialTheme.typography.labelSmall)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = s.spanishTempo == true,
                                            onCheckedChange = { vm.update(s.copy(spanishTempo = it)) },
                                        )
                                        Text("Logrado")
                                    }
                                }
                            }

                            // Bulgarian
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KPIField(
                                    label = "Bulgarian Split",
                                    valor = s.bulgSeries?.toString() ?: "",
                                    onValorChange = { vm.update(s.copy(bulgSeries = it.toIntOrNull())) },
                                    unidad = "series",
                                    modifier = Modifier.weight(1f),
                                )
                                SensacionSelector(
                                    valor = s.bulgTensionDorsal,
                                    onValorChange = { vm.update(s.copy(bulgTensionDorsal = it)) },
                                    label = "Tensión dorsal",
                                    opciones = listOf("ninguna", "leve", "moderada", "alta"),
                                    etiquetas = listOf("Ninguna", "Leve", "Mod.", "Alta"),
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            // Otros
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                KPIField("Calf Raises", s.calfSeries?.toString() ?: "", { vm.update(s.copy(calfSeries = it.toIntOrNull())) }, "series", modifier = Modifier.weight(1f))
                                KPIField("Flutter Kicks", s.flutterSeries?.toString() ?: "", { vm.update(s.copy(flutterSeries = it.toIntOrNull())) }, "series", modifier = Modifier.weight(1f))
                            }
                            KPIField("Push-ups totales", s.pushups?.toString() ?: "", { vm.update(s.copy(pushups = it.toIntOrNull())) }, "reps")
                        }
                    }
                }
            }

            // ── Rendimiento Escalada ──────────────────────────────────────
            if (s.tipo == "escalada") {
                item {
                    SeccionHeader("Escalada", expandRendimiento) { expandRendimiento = !expandRendimiento }
                }
                item {
                    AnimatedVisibility(expandRendimiento) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            KPIField("Duración sesión", s.escaladaDuracion?.toString() ?: "", { vm.update(s.copy(escaladaDuracion = it.toIntOrNull())) }, "min")

                            // Tipo escalada
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("resistencia" to "Resistencia", "boulder" to "Boulder", "mixto" to "Mixto").forEach { (tipo, label) ->
                                    FilterChip(
                                        selected = s.escaladaTipo == tipo,
                                        onClick = { vm.update(s.copy(escaladaTipo = tipo)) },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    )
                                }
                            }

                            // Grado máximo
                            var gradoMenuAbierto by remember { mutableStateOf(false) }
                            val grados = (listOf("5a", "5b", "5c", "6a", "6a+", "6b", "6b+", "6c", "6c+", "7a", "7a+", "7b", "7b+", "7c", "7c+", "8a", "8a+", "8b", "8b+", "8c", "8c+", "9a", "9b", "9c"))
                            ExposedDropdownMenuBox(expanded = gradoMenuAbierto, onExpandedChange = { gradoMenuAbierto = it }) {
                                OutlinedTextField(
                                    value = s.escaladaGradoMax ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Grado máximo") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(gradoMenuAbierto) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                )
                                ExposedDropdownMenu(expanded = gradoMenuAbierto, onDismissRequest = { gradoMenuAbierto = false }) {
                                    grados.forEach { g ->
                                        DropdownMenuItem(text = { Text(g) }, onClick = {
                                            vm.update(s.copy(escaladaGradoMax = g))
                                            gradoMenuAbierto = false
                                        })
                                    }
                                }
                            }

                            DolorSlider(s.escaladaDolorRodilla ?: 0, { vm.update(s.copy(escaladaDolorRodilla = it)) }, label = "Sensación rodilla derecha")
                            DolorSlider(s.escaladaDolorTendon ?: 0, { vm.update(s.copy(escaladaDolorTendon = it)) }, label = "Sensación tendón en gancho")
                            OutlinedTextField(
                                value = s.escaladaNotas ?: "",
                                onValueChange = { vm.update(s.copy(escaladaNotas = it.takeIf { it.isNotBlank() })) },
                                label = { Text("Notas escalada") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                            )
                        }
                    }
                }
            }

            // ── Sensaciones generales ────────────────────────────────────
            item {
                SeccionHeader("Sensaciones generales", expandSensaciones) { expandSensaciones = !expandSensaciones }
            }
            item {
                AnimatedVisibility(expandSensaciones) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StarRating(s.energiaGeneral ?: 0, { vm.update(s.copy(energiaGeneral = it)) }, label = "Energía general")
                        StarRating(s.calidadSuenio ?: 0, { vm.update(s.copy(calidadSuenio = it)) }, label = "Calidad del sueño")

                        SensacionSelector(
                            valor = tensionToStr(s.tensionDorsal),
                            onValorChange = { vm.update(s.copy(tensionDorsal = strToTension(it))) },
                            label = "Tensión dorsal Bulgarian",
                            opciones = listOf("ninguna", "leve", "moderada", "alta"),
                            etiquetas = listOf("Ninguna", "Leve", "Moderada", "Alta"),
                        )
                        SensacionSelector(
                            valor = pinchazoToStr(s.pinchazoRotula),
                            onValorChange = { vm.update(s.copy(pinchazoRotula = strToPinchazo(it))) },
                            label = "Pinchazo rótula",
                            opciones = listOf("ninguno", "leve", "moderado", "alto"),
                            etiquetas = listOf("Ninguno", "Leve", "Moderado", "Alto"),
                        )

                        OutlinedTextField(
                            value = s.notas ?: "",
                            onValueChange = {
                                if (it.length <= 300) vm.update(s.copy(notas = it.takeIf { it.isNotBlank() }))
                            },
                            label = { Text("Notas libres (máx. 300 chars)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            supportingText = { Text("${s.notas?.length ?: 0}/300") },
                        )
                    }
                }
            }

            // ── Suplementación ───────────────────────────────────────────
            item {
                SeccionHeader("Suplementación", expandSuple) { expandSuple = !expandSuple }
            }
            item {
                AnimatedVisibility(expandSuple) {
                    Column {
                        listOf(
                            Triple("Colágeno 10g (ayunas)", s.colageno) { v: Boolean -> vm.update(s.copy(colageno = v)) },
                            Triple("Vitamina C (nocturna)", s.vitaminaC) { v: Boolean -> vm.update(s.copy(vitaminaC = v)) },
                            Triple("Creatina post-entrenamiento", s.creatina) { v: Boolean -> vm.update(s.copy(creatina = v)) },
                        ).forEach { (label, checked, onChange) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Checkbox(checked = checked, onCheckedChange = onChange)
                                Text(label)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.guardar() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(Icons.Filled.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar sesión ✓", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Helpers UI ──────────────────────────────────────────────────────────────

@Composable
private fun SeccionHeader(titulo: String, expandido: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onClick) {
            Icon(
                if (expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun KPIField(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    unidad: String,
    destacado: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(if (destacado) "★ $label" else label) },
        suffix = { if (unidad.isNotBlank()) Text(unidad) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier,
        colors = if (destacado) OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primary,
        ) else OutlinedTextFieldDefaults.colors(),
    )
}

@Composable
private fun SensacionSelector(
    valor: String?,
    onValorChange: (String) -> Unit,
    label: String,
    opciones: List<String>,
    etiquetas: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            opciones.zip(etiquetas).forEach { (op, et) ->
                FilterChip(
                    selected = valor == op,
                    onClick = { onValorChange(op) },
                    label = { Text(et, style = MaterialTheme.typography.labelSmall) },
                )
            }
        }
    }
}

private fun tensionToStr(v: Int?) = when (v) { 1 -> "leve"; 2 -> "moderada"; 3 -> "alta"; else -> "ninguna" }
private fun strToTension(s: String) = when (s) { "leve" -> 1; "moderada" -> 2; "alta" -> 3; else -> 0 }
private fun pinchazoToStr(v: Int?) = when (v) { 1 -> "leve"; 2 -> "moderado"; 3 -> "alto"; else -> "ninguno" }
private fun strToPinchazo(s: String) = when (s) { "leve" -> 1; "moderado" -> 2; "alto" -> 3; else -> 0 }
