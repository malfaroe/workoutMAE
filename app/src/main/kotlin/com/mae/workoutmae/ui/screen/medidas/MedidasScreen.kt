package com.mae.workoutmae.ui.screen.medidas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mae.workoutmae.data.db.entity.MedidaCorporal
import com.mae.workoutmae.data.repository.MedidaRepository
import com.mae.workoutmae.ui.theme.AsimetriaAlta
import com.mae.workoutmae.ui.theme.AsimetriaMedia
import com.mae.workoutmae.ui.theme.AsimetriaOk
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedidasScreen(medidaRepository: MedidaRepository, onBack: () -> Unit) {

    val vm: MedidasViewModel = viewModel(factory = MedidasViewModel.factory(medidaRepository))
    val medidas by vm.medidas.collectAsStateWithLifecycle()
    val form by vm.form.collectAsStateWithLifecycle()
    val guardado by vm.guardado.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var mostrarFormulario by remember { mutableStateOf(false) }
    var medidaAEliminar by remember { mutableStateOf<MedidaCorporal?>(null) }

    // Cerrar formulario y mostrar confirmación al guardar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(guardado) {
        if (guardado) {
            mostrarFormulario = false
            snackbarHostState.showSnackbar("Medición guardada ✓")
            vm.resetGuardado()
        }
    }

    medidaAEliminar?.let { m ->
        AlertDialog(
            onDismissRequest = { medidaAEliminar = null },
            title = { Text("Eliminar medición") },
            text = { Text("¿Eliminar la medición del ${m.fecha}?") },
            confirmButton = {
                TextButton(onClick = { vm.eliminar(m); medidaAEliminar = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { medidaAEliminar = null }) { Text("Cancelar") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medidas corporales") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
            )
        },
        floatingActionButton = {
            if (!mostrarFormulario) {
                FloatingActionButton(onClick = { vm.resetForm(); mostrarFormulario = true }) {
                    Icon(Icons.Filled.Add, "Nueva medición")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Formulario nuevo / edición ───────────────────────────────
            item {
                AnimatedVisibility(mostrarFormulario) {
                    FormularioMedida(
                        form = form,
                        onUpdate = vm::updateForm,
                        onGuardar = { vm.guardar() },
                        onCancelar = { mostrarFormulario = false; vm.resetForm() },
                    )
                }
            }

            // ── Encabezado historial ─────────────────────────────────────
            if (medidas.isEmpty() && !mostrarFormulario) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.FitnessCenter, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Text("Sin mediciones registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Toca + para agregar la primera", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (medidas.isNotEmpty()) {
                item {
                    Text(
                        "Historial (${medidas.size} mediciones)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // ── Cards historial ──────────────────────────────────────────
            itemsIndexed(medidas, key = { _, m -> m.id }) { index, medida ->
                val delta = vm.delta(medidas, index)
                MedidaCard(
                    medida = medida,
                    delta = delta,
                    onEditar = {
                        vm.cargarParaEditar(medida)
                        mostrarFormulario = true
                    },
                    onEliminar = { medidaAEliminar = medida },
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

// ── Formulario ───────────────────────────────────────────────────────────────

@Composable
private fun FormularioMedida(
    form: MedidaFormState,
    onUpdate: (MedidaFormState) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (form.id == 0) "Nueva medición" else "Editar medición",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            OutlinedTextField(
                value = form.fecha,
                onValueChange = { onUpdate(form.copy(fecha = it)) },
                label = { Text("Fecha (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Peso
            MedidaField("Peso corporal", form.peso, { onUpdate(form.copy(peso = it)) }, "kg")

            HorizontalDivider()

            // Muslos
            Text("Muslo (15cm sobre borde rótula)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedidaField("Derecho", form.musloD, { onUpdate(form.copy(musloD = it)) }, "cm", Modifier.weight(1f))
                MedidaField("Izquierdo", form.musloI, { onUpdate(form.copy(musloI = it)) }, "cm", Modifier.weight(1f))
            }

            // Asimetría en tiempo real
            form.asimetriaCmPreview?.let { asim ->
                AsimetriaIndicador(asim, animado = true)
            }

            HorizontalDivider()

            // Pantorrillas
            Text("Pantorrilla (punto más ancho)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedidaField("Derecha", form.pantorrillaD, { onUpdate(form.copy(pantorrillaD = it)) }, "cm", Modifier.weight(1f))
                MedidaField("Izquierda", form.pantorrillaI, { onUpdate(form.copy(pantorrillaI = it)) }, "cm", Modifier.weight(1f))
            }

            OutlinedTextField(
                value = form.notas,
                onValueChange = { onUpdate(form.copy(notas = it)) },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancelar, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = onGuardar, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Check, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Guardar")
                }
            }
        }
    }
}

// ── Card historial ────────────────────────────────────────────────────────────

@Composable
private fun MedidaCard(
    medida: MedidaCorporal,
    delta: MedidaDelta?,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    var menuAbierto by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Fecha + menú
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text(medida.fecha, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { menuAbierto = true }, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.MoreVert, null, modifier = Modifier.size(18.dp)) }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        DropdownMenuItem(text = { Text("Editar") }, leadingIcon = { Icon(Icons.Filled.Edit, null) }, onClick = { menuAbierto = false; onEditar() })
                        DropdownMenuItem(text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) }, onClick = { menuAbierto = false; onEliminar() })
                    }
                }
            }

            // Peso
            medida.peso?.let { peso ->
                FilaMetrica(
                    label = "Peso",
                    valor = "%.1f kg".format(peso),
                    delta = delta?.pesoDelta,
                    unidad = "kg",
                    positicoEsMejor = null, // neutro
                )
            }

            HorizontalDivider()

            // Muslos
            if (medida.musloDerechoCm != null || medida.musloIzquierdoCm != null) {
                Text("Muslo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    medida.musloDerechoCm?.let { v ->
                        FilaMetrica("Derecho", "%.1f cm".format(v), delta?.musloDDelta, "cm", null, Modifier.weight(1f))
                    }
                    medida.musloIzquierdoCm?.let { v ->
                        FilaMetrica("Izquierdo", "%.1f cm".format(v), delta?.musloIDelta, "cm", null, Modifier.weight(1f))
                    }
                }

                // Asimetría
                medida.asimetriaCm?.let { asim ->
                    AsimetriaIndicador(
                        asim = asim,
                        delta = delta?.asimetriaDelta,
                    )
                }
            }

            // Pantorrillas
            if (medida.pantorrillaDerechaCm != null || medida.pantorrillaIzquierdaCm != null) {
                HorizontalDivider()
                Text("Pantorrilla", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    medida.pantorrillaDerechaCm?.let { v ->
                        FilaMetrica("Derecha", "%.1f cm".format(v), delta?.pantorrillaDDelta, "cm", null, Modifier.weight(1f))
                    }
                    medida.pantorrillaIzquierdaCm?.let { v ->
                        FilaMetrica("Izquierda", "%.1f cm".format(v), delta?.pantorrillaIDelta, "cm", null, Modifier.weight(1f))
                    }
                }
            }

            medida.notas?.let { notas ->
                HorizontalDivider()
                Text(notas, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Componentes auxiliares ────────────────────────────────────────────────────

@Composable
private fun AsimetriaIndicador(
    asim: Float,
    delta: Float? = null,
    animado: Boolean = false,
) {
    val color = when {
        asim <= 1f -> AsimetriaOk
        asim <= 2f -> AsimetriaMedia
        else       -> AsimetriaAlta
    }
    val label = when {
        asim <= 1f -> "Asimetría OK"
        asim <= 2f -> "Asimetría moderada"
        else       -> "Asimetría alta — consulta kinesiólogo"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = if (asim > 2f) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
                Text("Diferencia muslo D/I", style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("%.1f cm".format(asim), style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
            delta?.let { d ->
                // Delta asimetría: negativo = mejoró (bajó), positivo = empeoró (subió)
                val deltaColor = if (d <= 0f) AsimetriaOk else AsimetriaAlta
                Text(
                    text = "${if (d <= 0f) "▼" else "▲"} ${"%.1f".format(kotlin.math.abs(d))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = deltaColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun FilaMetrica(
    label: String,
    valor: String,
    delta: Float?,
    unidad: String,
    positicoEsMejor: Boolean?,   // true=↑ verde, false=↓ verde, null=neutro
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Baseline, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(valor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            delta?.let { d ->
                val subiendo = d > 0f
                val deltaColor = when (positicoEsMejor) {
                    true  -> if (subiendo) AsimetriaOk else AsimetriaAlta
                    false -> if (!subiendo) AsimetriaOk else AsimetriaAlta
                    null  -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    text = "${if (subiendo) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(d))}",
                    fontSize = 11.sp,
                    color = deltaColor,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun MedidaField(
    label: String,
    valor: String,
    onValorChange: (String) -> Unit,
    unidad: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(label) },
        suffix = { Text(unidad) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier,
    )
}
