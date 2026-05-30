package com.mae.workoutmae.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mae.workoutmae.data.db.entity.MedidaCorporal
import com.mae.workoutmae.data.db.entity.Sesion
import com.mae.workoutmae.data.preferences.PreferencesManager
import com.mae.workoutmae.data.repository.MedidaRepository
import com.mae.workoutmae.data.repository.SesionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DashboardViewModel(
    private val sesionRepo: SesionRepository,
    private val medidaRepo: MedidaRepository,
    private val prefs: PreferencesManager,
) : ViewModel() {

    val nombrePaciente: StateFlow<String> = prefs.nombrePaciente
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val fechaInicio: StateFlow<String> = prefs.fechaInicio
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val diasDesdeInicio: StateFlow<Int> = prefs.fechaInicio.map { fecha ->
        if (fecha.isBlank()) 0
        else ChronoUnit.DAYS.between(LocalDate.parse(fecha), LocalDate.now()).toInt().coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSesiones: StateFlow<Int> = sesionRepo.totalSesiones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mejorStepup: StateFlow<Int?> = sesionRepo.mejorStepup()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val ultimaMedida: StateFlow<MedidaCorporal?> = medidaRepo.ultimaFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dolorPromedio: StateFlow<Float?> = sesionRepo.dolorPromedioDurante(
        LocalDate.now().minusDays(14).toString()
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val ultimas10Sesiones: StateFlow<List<Sesion>> = sesionRepo.ultimas(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diasSinMedicion: StateFlow<Int?> = medidaRepo.ultimaFlow().map { medida ->
        if (medida == null) null
        else ChronoUnit.DAYS.between(LocalDate.parse(medida.fecha), LocalDate.now()).toInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rachaActual: StateFlow<Int> = sesionRepo.ultimas(60).map { sesiones ->
        if (sesiones.isEmpty()) return@map 0
        val fechas = sesiones.map { LocalDate.parse(it.fecha) }.toSortedSet().reversed()
        var racha = 0
        var dia = LocalDate.now()
        for (fecha in fechas) {
            if (fecha == dia || fecha == dia.minusDays(1)) {
                racha++
                dia = fecha
            } else break
        }
        racha
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    companion object {
        fun factory(
            sesionRepo: SesionRepository,
            medidaRepo: MedidaRepository,
            prefs: PreferencesManager,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { DashboardViewModel(sesionRepo, medidaRepo, prefs) }
        }
    }
}
