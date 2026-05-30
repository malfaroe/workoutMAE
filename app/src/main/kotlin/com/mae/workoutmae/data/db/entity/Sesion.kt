package com.mae.workoutmae.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sesiones")
data class Sesion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: String,                      // ISO 8601 yyyy-MM-dd
    val tipo: String,                       // "piernas" | "escalada"
    val horaInicio: String? = null,
    val horaFin: String? = null,

    // Dolor tendón patelar
    val dolorDurante: Int = 0,              // 0-10
    val dolorPost: Int? = null,             // 0-10, editable 48h después
    val descripcionDolor: String? = null,

    // Piernas — ejercicios
    val slantSeries: Int? = null,
    val slantSensacion: String? = null,     // "normal" | "pesado" | "muy_pesado"
    val stepupReps: Int? = null,
    val spanishSeries: Int? = null,
    val spanishTempo: Boolean? = null,
    val wallsitSegundos: Int? = null,
    val bulgSeries: Int? = null,
    val bulgTensionDorsal: String? = null,  // "ninguna" | "leve" | "moderada" | "alta"
    val calfSeries: Int? = null,
    val flutterSeries: Int? = null,
    val pushups: Int? = null,

    // Escalada
    val escaladaDuracion: Int? = null,      // minutos
    val escaladaTipo: String? = null,       // "resistencia" | "boulder" | "mixto"
    val escaladaGradoMax: String? = null,
    val escaladaDolorRodilla: Int? = null,  // 0-10
    val escaladaDolorTendon: Int? = null,   // 0-10
    val escaladaNotas: String? = null,

    // Sensaciones generales
    val energiaGeneral: Int? = null,        // 1-5
    val calidadSuenio: Int? = null,         // 1-5
    val tensionDorsal: Int? = null,         // 0-3
    val pinchazoRotula: Int? = null,        // 0-3
    val notas: String? = null,

    // Suplementación
    val colageno: Boolean = false,
    val vitaminaC: Boolean = false,
    val creatina: Boolean = false,

    val creadoEn: Long = System.currentTimeMillis(),
)
