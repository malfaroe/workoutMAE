package com.mae.workoutmae.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ejercicios")
data class Ejercicio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val categoria: String,              // "piernas" | "escalada" | "general"
    val metricaPrincipal: String,       // ver MetricaTipo
    val unidadPrincipal: String,
    val metricaSecundaria: String? = null,
    val unidadSecundaria: String? = null,
    val notas: String? = null,
    val activo: Boolean = true,
    val esPersonalizado: Boolean = false,
    val orden: Int = 0,
)

object MetricaTipo {
    const val SERIES_X_REPS     = "series_x_reps"
    const val SERIES_FALLA      = "series_hasta_falla"
    const val REPS_FALLA        = "reps_hasta_falla"
    const val TIEMPO_SEGUNDOS   = "tiempo_segundos"
    const val TOTAL_REPS        = "total_reps"
    const val ESCALA_SENSACION  = "escala_sensacion"
    const val ESCALA_DOLOR      = "escala_dolor"
    const val PESO_KG           = "peso_kg"
    const val DISTANCIA_CM      = "distancia_cm"
    const val BOOLEANO          = "booleano"
    const val TEXTO_LIBRE       = "texto_libre"
}
