package com.mae.workoutmae.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registros_ejercicio",
    foreignKeys = [
        ForeignKey(entity = Sesion::class, parentColumns = ["id"], childColumns = ["sesionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Ejercicio::class, parentColumns = ["id"], childColumns = ["ejercicioId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("sesionId"), Index("ejercicioId")],
)
data class RegistroEjercicio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sesionId: Int,
    val ejercicioId: Int,
    val valorPrincipal: Float? = null,
    val valorSecundario: Float? = null,
    val valorTexto: String? = null,
    val notas: String? = null,
)
