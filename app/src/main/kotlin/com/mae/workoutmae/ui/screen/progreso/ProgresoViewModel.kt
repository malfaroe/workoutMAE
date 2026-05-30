package com.mae.workoutmae.ui.screen.progreso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mae.workoutmae.data.db.entity.MedidaCorporal
import com.mae.workoutmae.data.db.entity.Sesion
import com.mae.workoutmae.data.repository.MedidaRepository
import com.mae.workoutmae.data.repository.SesionRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

// ── Modelos de UI ─────────────────────────────────────────────────────────────

enum class Periodo(val label: String) {
    DOS_SEMANAS("2 sem"),
    UN_MES("1 mes"),
    TRES_MESES("3 meses"),
    TODO("Todo"),
}

data class Alerta(val mensaje: String, val nivel: NivelAlerta)

enum class NivelAlerta { INFO, ADVERTENCIA, CRITICA }

data class PuntoGrafico(val etiqueta: String, val valor: Float)

data class ProgresoChartData(
    val dolorDurante: List<PuntoGrafico> = emptyList(),
    val dolorPost: List<PuntoGrafico> = emptyList(),
    val stepupsPorSemana: List<PuntoGrafico> = emptyList(),
    val wallsitPorSesion: List<PuntoGrafico> = emptyList(),
    val sesionesXTipo: Map<String, Int> = emptyMap(),    // "piernas", "escalada"
    val adherenciaColageno: Float = 0f,                  // 0-1
)

// ── ViewModel ────────────────────────────────────────────────────────────────

class ProgresoViewModel(
    private val sesionRepo: SesionRepository,
    private val medidaRepo: MedidaRepository,
) : ViewModel() {

    val periodo = MutableStateFlow(Periodo.UN_MES)

    private val todasSesiones = sesionRepo.todas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medidas: StateFlow<List<MedidaCorporal>> = medidaRepo.todas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sesiones: StateFlow<List<Sesion>> = combine(todasSesiones, periodo) { todas, p ->
        val desde = fechaDesde(p) ?: return@combine todas
        todas.filter { it.fecha >= desde }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertas: StateFlow<List<Alerta>> = combine(todasSesiones, medidas) { ses, med ->
        calcularAlertas(ses, med)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chartData: StateFlow<ProgresoChartData> = sesiones.map { ses ->
        computeChartData(ses)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgresoChartData())

    fun setPeriodo(p: Periodo) { periodo.value = p }

    // ── Procesamiento de datos ──────────────────────────────────────────────

    private fun computeChartData(ses: List<Sesion>): ProgresoChartData {
        val ordenadas = ses.sortedBy { it.fecha }

        // Dolor durante (todas las sesiones)
        val dolorDurante = ordenadas.mapIndexed { i, s ->
            PuntoGrafico(s.fecha.takeLast(5), s.dolorDurante.toFloat())
        }

        // Dolor post (solo las que tienen valor)
        val dolorPost = ordenadas.filter { it.dolorPost != null }.mapIndexed { i, s ->
            PuntoGrafico(s.fecha.takeLast(5), s.dolorPost!!.toFloat())
        }

        // Step-ups: máximo por semana
        val weekFields = WeekFields.of(Locale.getDefault())
        val stepupsSemana = ordenadas
            .filter { it.tipo == "piernas" && it.stepupReps != null }
            .groupBy { sesion ->
                val f = LocalDate.parse(sesion.fecha)
                "S${f.get(weekFields.weekOfWeekBasedYear())}"
            }
            .mapValues { (_, grupo) -> grupo.maxOf { it.stepupReps!! }.toFloat() }
            .entries.sortedBy { it.key }
            .map { (sem, max) -> PuntoGrafico(sem, max) }

        // Wall sit: por sesión
        val wallsit = ordenadas
            .filter { it.tipo == "piernas" && it.wallsitSegundos != null }
            .map { s -> PuntoGrafico(s.fecha.takeLast(5), s.wallsitSegundos!!.toFloat()) }

        // Distribución por tipo
        val dist = mapOf(
            "piernas" to ses.count { it.tipo == "piernas" },
            "escalada" to ses.count { it.tipo == "escalada" },
        )

        // Adherencia colágeno
        val adherencia = if (ses.isEmpty()) 0f
        else ses.count { it.colageno }.toFloat() / ses.size

        return ProgresoChartData(
            dolorDurante = dolorDurante,
            dolorPost = dolorPost,
            stepupsPorSemana = stepupsSemana,
            wallsitPorSesion = wallsit,
            sesionesXTipo = dist,
            adherenciaColageno = adherencia,
        )
    }

    private fun calcularAlertas(ses: List<Sesion>, med: List<MedidaCorporal>): List<Alerta> {
        val alertas = mutableListOf<Alerta>()

        // Dolor post > 3 en dos sesiones recientes con valor
        val conPost = ses.filter { it.dolorPost != null }.take(3)
        if (conPost.size >= 2 && conPost.take(2).all { it.dolorPost!! > 3 }) {
            alertas += Alerta(
                "Dolor post > 3/10 en dos sesiones consecutivas. Considera reducir volumen esta semana.",
                NivelAlerta.ADVERTENCIA,
            )
        }

        // Asimetría muslo > 2 cm
        med.firstOrNull()?.asimetriaCm?.let { asim ->
            if (asim > 2f) alertas += Alerta(
                "Diferencia muscular significativa (${"%.1f".format(asim)} cm). Consulta con tu kinesiólogo.",
                NivelAlerta.CRITICA,
            )
        }

        // Más de 5 días sin registro
        ses.firstOrNull()?.let { ultima ->
            val dias = ChronoUnit.DAYS.between(LocalDate.parse(ultima.fecha), LocalDate.now()).toInt()
            if (dias > 5) alertas += Alerta("Han pasado $dias días sin registro.", NivelAlerta.INFO)
        }

        // Pinchazo rótula alto en dos sesiones seguidas
        if (ses.take(3).count { it.pinchazoRotula == 3 } >= 2) {
            alertas += Alerta(
                "Pinchazo rótula alto repetido. Revisar técnica step-ups.",
                NivelAlerta.ADVERTENCIA,
            )
        }

        // Tensión dorsal alta en tres sesiones
        if (ses.take(5).count { it.tensionDorsal == 3 } >= 3) {
            alertas += Alerta(
                "Tensión dorsal persistente en Bulgarian. Revisar posición.",
                NivelAlerta.ADVERTENCIA,
            )
        }

        return alertas
    }

    private fun fechaDesde(p: Periodo): String? = when (p) {
        Periodo.DOS_SEMANAS -> LocalDate.now().minusWeeks(2).toString()
        Periodo.UN_MES      -> LocalDate.now().minusMonths(1).toString()
        Periodo.TRES_MESES  -> LocalDate.now().minusMonths(3).toString()
        Periodo.TODO        -> null
    }

    companion object {
        fun factory(sesionRepo: SesionRepository, medidaRepo: MedidaRepository) =
            viewModelFactory { initializer { ProgresoViewModel(sesionRepo, medidaRepo) } }
    }
}
