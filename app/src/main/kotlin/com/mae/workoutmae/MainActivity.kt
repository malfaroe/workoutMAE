package com.mae.workoutmae

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mae.workoutmae.ui.navigation.AppNavigation
import com.mae.workoutmae.ui.theme.WorkoutMAETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as WorkoutMAEApplication
        setContent {
            WorkoutMAETheme {
                AppNavigation(
                    sesionRepository = app.sesionRepository,
                    medidaRepository = app.medidaRepository,
                    ejercicioRepository = app.ejercicioRepository,
                    preferencesManager = app.preferencesManager,
                )
            }
        }
    }
}
