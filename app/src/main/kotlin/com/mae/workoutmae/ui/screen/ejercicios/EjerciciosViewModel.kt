package com.mae.workoutmae.ui.screen.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mae.workoutmae.data.db.entity.Ejercicio
import com.mae.workoutmae.data.db.entity.MetricaTipo
import com.mae.workoutmae.data.repository.EjercicioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EjercicioFormState(
    val id: Int = 0,
    val nombre: String = "",
    val categoria: String = "piernas",
    val metricaPrincipal: String = MetricaTipo.SERIES_X_REPS,
    val unidadPrincipal: String = "",
    val metricaSecundaria: String = "",
    val unidadSecundaria: String = "",
    val notas: String = "",
    val esPersonalizado: Boolean = true,
) {
    val nombreValido get() = nombre.isNotBlank()
    val unidadValida get() = unidadPrincipal.isNotBlank()
}

val CATEGORIAS = listOf("piernas", "escalada", "general")

val METRICAS_LABELS = mapOf(
    MetricaTipo.SERIES_X_REPS    to "Series × Reps",
    MetricaTipo.SERIES_FALLA     to "Series hasta falla",
    MetricaTipo.REPS_FALLA       to "Reps hasta falla",
    MetricaTipo.TIEMPO_SEGUNDOS  to "Tiempo (segundos)",
    MetricaTipo.TOTAL_REPS       to "Total reps",
    MetricaTipo.ESCALA_SENSACION to "Escala sensación (1-3)",
    MetricaTipo.ESCALA_DOLOR     to "Escala dolor (0-10)",
    MetricaTipo.PESO_KG          to "Peso (kg)",
    MetricaTipo.DISTANCIA_CM     to "Distancia (cm)",
    MetricaTipo.BOOLEANO         to "Booleano (Sí/No)",
    MetricaTipo.TEXTO_LIBRE      to "Texto libre",
)

class EjerciciosViewModel(private val repo: EjercicioRepository) : ViewModel() {

    val ejercicios: StateFlow<List<Ejercicio>> = repo.todos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filtroCategoria = MutableStateFlow<String?>(null)
    val filtroCategoria = _filtroCategoria.asStateFlow()

    val ejerciciosFiltrados: StateFlow<List<Ejercicio>> = combine(ejercicios, filtroCategoria) { lista, cat ->
        if (cat == null) lista else lista.filter { it.categoria == cat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(EjercicioFormState())
    val form = _form.asStateFlow()

    private val _showForm = MutableStateFlow(false)
    val showForm = _showForm.asStateFlow()

    private val _eliminandoId = MutableStateFlow<Int?>(null)
    val eliminandoId = _eliminandoId.asStateFlow()

    fun setFiltro(cat: String?) { _filtroCategoria.value = cat }

    fun abrirFormNuevo() {
        _form.value = EjercicioFormState()
        _showForm.value = true
    }

    fun abrirFormEditar(e: Ejercicio) {
        _form.value = EjercicioFormState(
            id = e.id,
            nombre = e.nombre,
            categoria = e.categoria,
            metricaPrincipal = e.metricaPrincipal,
            unidadPrincipal = e.unidadPrincipal,
            metricaSecundaria = e.metricaSecundaria ?: "",
            unidadSecundaria = e.unidadSecundaria ?: "",
            notas = e.notas ?: "",
            esPersonalizado = e.esPersonalizado,
        )
        _showForm.value = true
    }

    fun cerrarForm() {
        _showForm.value = false
        _form.value = EjercicioFormState()
    }

    fun updateForm(f: EjercicioFormState) { _form.value = f }

    fun guardar() {
        viewModelScope.launch {
            val f = _form.value
            if (!f.nombreValido || !f.unidadValida) return@launch
            val e = Ejercicio(
                id = f.id,
                nombre = f.nombre.trim(),
                categoria = f.categoria,
                metricaPrincipal = f.metricaPrincipal,
                unidadPrincipal = f.unidadPrincipal.trim(),
                metricaSecundaria = f.metricaSecundaria.takeIf { it.isNotBlank() },
                unidadSecundaria = f.unidadSecundaria.takeIf { it.isNotBlank() },
                notas = f.notas.takeIf { it.isNotBlank() },
                esPersonalizado = true,
            )
            if (f.id == 0) repo.insertar(e) else repo.actualizar(e)
            cerrarForm()
        }
    }

    fun toggleActivo(e: Ejercicio) {
        viewModelScope.launch { repo.actualizar(e.copy(activo = !e.activo)) }
    }

    fun pedirEliminacion(id: Int) { _eliminandoId.value = id }
    fun cancelarEliminacion() { _eliminandoId.value = null }

    fun confirmarEliminacion() {
        val id = _eliminandoId.value ?: return
        viewModelScope.launch {
            ejercicios.value.firstOrNull { it.id == id }?.let { repo.eliminar(it) }
            _eliminandoId.value = null
        }
    }

    fun restaurarBase() {
        viewModelScope.launch { repo.restaurarEjerciciosBase() }
    }

    companion object {
        fun factory(repo: EjercicioRepository) = viewModelFactory {
            initializer { EjerciciosViewModel(repo) }
        }
    }
}
