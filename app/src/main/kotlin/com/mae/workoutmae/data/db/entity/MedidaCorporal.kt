package com.mae.workoutmae.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.abs

@Entity(tableName = "medidas_corporales")
data class MedidaCorporal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: String,                          // yyyy-MM-dd
    val peso: Float? = null,
    val musloDerechoCm: Float? = null,
    val musloIzquierdoCm: Float? = null,
    val pantorrillaDerechaCm: Float? = null,
    val pantorrillaIzquierdaCm: Float? = null,
    val notas: String? = null,
) {
    val asimetriaCm: Float?
        get() = if (musloDerechoCm != null && musloIzquierdoCm != null)
            abs(musloDerechoCm - musloIzquierdoCm) else null

    val asimetriaPct: Float?
        get() = if (musloDerechoCm != null && musloIzquierdoCm != null && musloIzquierdoCm > 0)
            abs(musloDerechoCm - musloIzquierdoCm) / musloIzquierdoCm * 100f else null
}
