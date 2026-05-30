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
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class RegistroViewModel(private val repo: SesionRepository) : ViewModel() {

    private val _sesion = MutableStateFlow(Sesion(fecha = LocalDate.now().toString(), tipo = "piernas"))
    val sesion = _sesion.asStateFlow()

    private val _guardado = MutableStateFlow(false)
    val guardado = _guardado.asStateFlow()

    // dolorPost is only editable within 48h of session creation
    private val _puedeEditarDolorPost = MutableStateFlow(true)
    val puedeEditarDolorPost = _puedeEditarDolorPost.asStateFlow()

    fun cargar(id: Int) {
        if (id == 0) return
        viewModelScope.launch {
            repo.porId(id)?.let {
                _sesion.value = it
                val horas = ChronoUnit.HOURS.between(Instant.ofEpochMilli(it.creadoEn), Instant.now())
                _puedeEditarDolorPost.value = horas < 48
            }
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
