package com.mae.workoutmae

import android.app.Application
import com.mae.workoutmae.data.db.AppDatabase
import com.mae.workoutmae.data.preferences.PreferencesManager
import com.mae.workoutmae.data.repository.EjercicioRepository
import com.mae.workoutmae.data.repository.MedidaRepository
import com.mae.workoutmae.data.repository.SesionRepository

class WorkoutMAEApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val sesionRepository by lazy { SesionRepository(database.sesionDao()) }
    val medidaRepository by lazy { MedidaRepository(database.medidaDao()) }
    val ejercicioRepository by lazy { EjercicioRepository(database.ejercicioDao()) }

    val preferencesManager by lazy { PreferencesManager(this) }
}
