package com.mae.workoutmae.ui.screen.medidas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mae.workoutmae.data.db.entity.MedidaCorporal
import com.mae.workoutmae.data.repository.MedidaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MedidaFormState(
    val id: Int = 0,
    val fecha: String = LocalDate.now().toString(),
    val peso: String = "",
    val musloD: String = "",
    val musloI: String = "",
    val pantorrillaD: String = "",
    val pantorrillaI: String = "",
    val notas: String = "",
) {
    // Asimetría calculada en tiempo real desde el formulario
    val asimetriaCmPreview: Float?
        get() {
            val d = musloD.toFloatOrNull() ?: return null
            val i = musloI.toFloatOrNull() ?: return null
            return kotlin.math.abs(d - i)
        }
}

data class MedidaDelta(
    val pesoDelta: Float?,
    val musloDDelta: Float?,
    val musloIDelta: Float?,
    val asimetriaDelta: Float?,   // negativo = mejoró (asimetría bajó)
    val pantorrillaDDelta: Float?,
    val pantorrillaIDelta: Float?,
)

class MedidasViewModel(private val repo: MedidaRepository) : ViewModel() {

    val medidas = repo.todas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _form = MutableStateFlow(MedidaFormState())
    val form = _form.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado = _guardado.asStateFlow()

    fun updateForm(f: MedidaFormState) { _form.value = f }

    fun resetGuardado() { _guardado.value = false }

    fun cargarParaEditar(m: MedidaCorporal) {
        _form.value = MedidaFormState(
            id = m.id,
            fecha = m.fecha,
            peso = m.peso?.toString() ?: "",
            musloD = m.musloDerechoCm?.toString() ?: "",
            musloI = m.musloIzquierdoCm?.toString() ?: "",
            pantorrillaD = m.pantorrillaDerechaCm?.toString() ?: "",
            pantorrillaI = m.pantorrillaIzquierdaCm?.toString() ?: "",
            notas = m.notas ?: "",
        )
    }

    fun resetForm() { _form.value = MedidaFormState() }

    fun guardar() {
        viewModelScope.launch {
            val f = _form.value
            val m = MedidaCorporal(
                id = f.id,
                fecha = f.fecha,
                peso = f.peso.toFloatOrNull(),
                musloDerechoCm = f.musloD.toFloatOrNull(),
                musloIzquierdoCm = f.musloI.toFloatOrNull(),
                pantorrillaDerechaCm = f.pantorrillaD.toFloatOrNull(),
                pantorrillaIzquierdaCm = f.pantorrillaI.toFloatOrNull(),
                notas = f.notas.takeIf { it.isNotBlank() },
            )
            if (f.id == 0) repo.insertar(m) else repo.actualizar(m)
            resetForm()
            _guardado.value = true
        }
    }

    fun eliminar(m: MedidaCorporal) {
        viewModelScope.launch { repo.eliminar(m) }
    }

    // Calcula el delta entre la medición en [index] y la anterior (index+1 en lista DESC)
    fun delta(lista: List<MedidaCorporal>, index: Int): MedidaDelta? {
        if (index >= lista.size - 1) return null
        val actual = lista[index]
        val prev = lista[index + 1]
        return MedidaDelta(
            pesoDelta = diff(actual.peso, prev.peso),
            musloDDelta = diff(actual.musloDerechoCm, prev.musloDerechoCm),
            musloIDelta = diff(actual.musloIzquierdoCm, prev.musloIzquierdoCm),
            asimetriaDelta = diff(actual.asimetriaCm, prev.asimetriaCm),
            pantorrillaDDelta = diff(actual.pantorrillaDerechaCm, prev.pantorrillaDerechaCm),
            pantorrillaIDelta = diff(actual.pantorrillaIzquierdaCm, prev.pantorrillaIzquierdaCm),
        )
    }

    private fun diff(a: Float?, b: Float?) = if (a != null && b != null) a - b else null

    companion object {
        fun factory(repo: MedidaRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { MedidasViewModel(repo) }
        }
    }
}
