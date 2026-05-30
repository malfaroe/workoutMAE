package com.mae.workoutmae.ui.screen.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mae.workoutmae.data.db.entity.Sesion
import com.mae.workoutmae.data.repository.SesionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class RegistroViewModel(private val repo: SesionRepository) : ViewModel() {

    private val _sesion = MutableStateFlow(Sesion(fecha = LocalDate.now().toString(), tipo = "piernas"))
    val sesion = _sesion.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado = _guardado.asStateFlow()

    fun cargar(id: Int) {
        if (id == 0) return
        viewModelScope.launch {
            repo.porId(id)?.let { _sesion.value = it }
        }
    }

    fun update(s: Sesion) { _sesion.value = s }

    fun guardar() {
        viewModelScope.launch {
            val s = _sesion.value
            if (s.id == 0) repo.insertar(s) else repo.actualizar(s)
            _guardado.value = true
        }
    }

    companion object {
        fun factory(repo: SesionRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { RegistroViewModel(repo) }
        }
    }
}
