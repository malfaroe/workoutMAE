package com.mae.workoutmae.ui.screen.historial

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mae.workoutmae.data.db.entity.Sesion
import com.mae.workoutmae.data.repository.SesionRepository
import com.mae.workoutmae.ui.theme.dolorColor
import kotlinx.coroutines.launch

@Composable
fun HistorialScreen(
    sesionRepository: SesionRepository,
    onEditarSesion: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sesiones by sesionRepository.todas().collectAsStateWithLifecycle(emptyList())
    var filtroTipo by remember { mutableStateOf("todas") }
    var sesionAEliminar by remember { mutableStateOf<Sesion?>(null) }

    val filtradas = when (filtroTipo) {
        "piernas"  -> sesiones.filter { it.tipo == "piernas" }
        "escalada" -> sesiones.filter { it.tipo == "escalada" }
        else       -> sesiones
    }

    sesionAEliminar?.let { s ->
        AlertDialog(
            onDismissRequest = { sesionAEliminar = null },
            title = { Text("Eliminar sesión") },
            text = { Text("¿Eliminar la sesión del ${s.fecha}?") },
            confirmButton = {
                TextButton(onClick = { scope.launch { sesionRepository.eliminar(s) }; sesionAEliminar = null }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { sesionAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filtros
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("todas" to "Todas", "piernas" to "Piernas", "escalada" to "Escalada").forEach { (val_, label) ->
                FilterChip(selected = filtroTipo == val_, onClick = { filtroTipo = val_ }, label = { Text(label) })
            }
        }

        if (filtradas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin sesiones registradas", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtradas, key = { it.id }) { s ->
                    SesionItem(s, onEditar = { onEditarSesion(s.id) }, onEliminar = { sesionAEliminar = s })
                }
            }
        }
    }
}

@Composable
private fun SesionItem(s: Sesion, onEditar: () -> Unit, onEliminar: () -> Unit) {
    var menuAbierto by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditar() },
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Indicador dolor
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = dolorColor(s.dolorDurante).copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${s.dolorDurante}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = dolorColor(s.dolorDurante))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(s.fecha, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    if (s.tipo == "piernas") "Piernas + Push-ups" else "Escalada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (s.dolorPost != null) {
                    Text("Post: ${s.dolorPost}", style = MaterialTheme.typography.labelSmall, color = dolorColor(s.dolorPost))
                }
            }
            if (s.tipo == "piernas") {
                Column(horizontalAlignment = Alignment.End) {
                    s.stepupReps?.let { Text("↑ $it reps", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    s.wallsitSegundos?.let { Text("⏱ ${it}s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
            Box {
                IconButton(onClick = { menuAbierto = true }) { Icon(Icons.Filled.MoreVert, null) }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    DropdownMenuItem(text = { Text("Editar") }, leadingIcon = { Icon(Icons.Filled.Edit, null) }, onClick = { menuAbierto = false; onEditar() })
                    DropdownMenuItem(text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) }, onClick = { menuAbierto = false; onEliminar() })
                }
            }
        }
    }
}
