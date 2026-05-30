package com.mae.workoutmae.ui.screen.ejercicios

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mae.workoutmae.data.db.entity.Ejercicio
import com.mae.workoutmae.data.repository.EjercicioRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EjerciciosScreen(ejercicioRepository: EjercicioRepository, onBack: () -> Unit) {
    val vm: EjerciciosViewModel = viewModel(factory = EjerciciosViewModel.factory(ejercicioRepository))

    val ejercicios by vm.ejerciciosFiltrados.collectAsState()
    val todosEjercicios by vm.ejercicios.collectAsState()
    val filtro by vm.filtroCategoria.collectAsState()
    val showForm by vm.showForm.collectAsState()
    val form by vm.form.collectAsState()
    val eliminandoId by vm.eliminandoId.collectAsState()

    val ejercicioEliminando = remember(eliminandoId, todosEjercicios) {
        todosEjercicios.firstOrNull { it.id == eliminandoId }
    }
    val agrupados = remember(ejercicios) { ejercicios.groupBy { it.categoria } }

    val snackState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) },
        topBar = {
            TopAppBar(
                title = { Text("Ejercicios") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    IconButton(onClick = {
                        vm.restaurarBase()
                        scope.launch { snackState.showSnackbar("Ejercicios base restaurados") }
                    }) {
                        Icon(Icons.Filled.Restore, contentDescription = "Restaurar base")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.abrirFormNuevo() },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Nuevo ejercicio") },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Category filter chips
            FiltroCategoriasRow(filtro = filtro, onFiltro = { vm.setFiltro(it) })

            if (ejercicios.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sin ejercicios en esta categoría",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp),
                ) {
                    if (filtro == null) {
                        CATEGORIAS.forEach { cat ->
                            val grupo = agrupados[cat] ?: emptyList()
                            if (grupo.isNotEmpty()) {
                                stickyHeader(key = "header_$cat") {
                                    CategoriaHeader(cat)
                                }
                                items(grupo, key = { it.id }) { ej ->
                                    EjercicioItem(
                                        ejercicio = ej,
                                        onToggle = { vm.toggleActivo(ej) },
                                        onEditar = { vm.abrirFormEditar(ej) },
                                        onEliminar = { vm.pedirEliminacion(ej.id) },
                                    )
                                }
                            }
                        }
                    } else {
                        items(ejercicios, key = { it.id }) { ej ->
                            EjercicioItem(
                                ejercicio = ej,
                                onToggle = { vm.toggleActivo(ej) },
                                onEditar = { vm.abrirFormEditar(ej) },
                                onEliminar = { vm.pedirEliminacion(ej.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    // Form dialog
    if (showForm) {
        EjercicioFormDialog(
            state = form,
            onUpdate = { vm.updateForm(it) },
            onGuardar = { vm.guardar() },
            onCancelar = { vm.cerrarForm() },
        )
    }

    // Delete confirmation dialog
    if (ejercicioEliminando != null) {
        AlertDialog(
            onDismissRequest = { vm.cancelarEliminacion() },
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar ejercicio") },
            text = {
                Text("¿Eliminar «${ejercicioEliminando.nombre}»?\nEsta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = { vm.confirmarEliminacion() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.cancelarEliminacion() }) { Text("Cancelar") }
            },
        )
    }
}

// ── Componentes ──────────────────────────────────────────────────────────────

@Composable
private fun FiltroCategoriasRow(filtro: String?, onFiltro: (String?) -> Unit) {
    val opciones = listOf(null) + CATEGORIAS
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        opciones.forEach { cat ->
            FilterChip(
                selected = filtro == cat,
                onClick = { onFiltro(if (filtro == cat) null else cat) },
                label = { Text(if (cat == null) "Todos" else cat.replaceFirstChar { it.uppercase() }) },
            )
        }
    }
}

@Composable
private fun CategoriaHeader(categoria: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (categoria) {
                    "piernas"  -> Icons.Filled.DirectionsRun
                    "escalada" -> Icons.Filled.Terrain
                    else       -> Icons.Filled.FitnessCenter
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = categoria.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EjercicioItem(
    ejercicio: Ejercicio,
    onToggle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = ejercicio.nombre,
                fontWeight = if (ejercicio.activo) FontWeight.SemiBold else FontWeight.Normal,
                color = if (ejercicio.activo) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = (METRICAS_LABELS[ejercicio.metricaPrincipal] ?: ejercicio.metricaPrincipal) +
                           " · ${ejercicio.unidadPrincipal}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (ejercicio.esPersonalizado) {
                    Text(
                        text = "Personalizado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (ejercicio.esPersonalizado) {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Switch(checked = ejercicio.activo, onCheckedChange = { onToggle() })
            }
        },
    )
    HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
}

// ── Formulario ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EjercicioFormDialog(
    state: EjercicioFormState,
    onUpdate: (EjercicioFormState) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
) {
    val titulo = if (state.id == 0) "Nuevo ejercicio" else "Editar ejercicio"

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Nombre
                OutlinedTextField(
                    value = state.nombre,
                    onValueChange = { onUpdate(state.copy(nombre = it)) },
                    label = { Text("Nombre del ejercicio *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.nombre.isNotEmpty() && !state.nombreValido,
                )

                // Categoría
                CampoDropdown(
                    label = "Categoría *",
                    opciones = CATEGORIAS,
                    seleccionado = state.categoria,
                    etiqueta = { it.replaceFirstChar { c -> c.uppercase() } },
                    onSeleccionar = { onUpdate(state.copy(categoria = it)) },
                )

                // Métrica principal
                CampoDropdown(
                    label = "Métrica principal *",
                    opciones = METRICAS_LABELS.keys.toList(),
                    seleccionado = state.metricaPrincipal,
                    etiqueta = { METRICAS_LABELS[it] ?: it },
                    onSeleccionar = { onUpdate(state.copy(metricaPrincipal = it)) },
                )

                OutlinedTextField(
                    value = state.unidadPrincipal,
                    onValueChange = { onUpdate(state.copy(unidadPrincipal = it)) },
                    label = { Text("Unidad principal * (ej: series, reps, seg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.unidadPrincipal.isNotEmpty() && !state.unidadValida,
                )

                Text("Métrica secundaria (opcional)", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                CampoDropdown(
                    label = "Métrica secundaria",
                    opciones = listOf("") + METRICAS_LABELS.keys.toList(),
                    seleccionado = state.metricaSecundaria,
                    etiqueta = { if (it.isEmpty()) "— ninguna —" else (METRICAS_LABELS[it] ?: it) },
                    onSeleccionar = { onUpdate(state.copy(metricaSecundaria = it)) },
                )

                if (state.metricaSecundaria.isNotEmpty()) {
                    OutlinedTextField(
                        value = state.unidadSecundaria,
                        onValueChange = { onUpdate(state.copy(unidadSecundaria = it)) },
                        label = { Text("Unidad secundaria (ej: sensación, kg)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = state.notas,
                    onValueChange = { onUpdate(state.copy(notas = it)) },
                    label = { Text("Notas / instrucciones (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGuardar,
                enabled = state.nombreValido && state.unidadValida,
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> CampoDropdown(
    label: String,
    opciones: List<T>,
    seleccionado: T,
    etiqueta: (T) -> String,
    onSeleccionar: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = etiqueta(seleccionado),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(etiqueta(opcion)) },
                    onClick = {
                        onSeleccionar(opcion)
                        expanded = false
                    },
                )
            }
        }
    }
}
