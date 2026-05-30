package com.mae.workoutmae.ui.theme

import androidx.compose.ui.graphics.Color

// Teal primario adaptado a MD3
val Teal10 = Color(0xFF002117)
val Teal20 = Color(0xFF00382A)
val Teal30 = Color(0xFF00513E)
val Teal40 = Color(0xFF006B52)
val Teal80 = Color(0xFF5EDBB8)
val Teal90 = Color(0xFF7FF8D3)
val TealGreen = Color(0xFF1D9E75)

// Escala de dolor — usada en sliders y tarjetas
val DolorVerde = Color(0xFF2E7D32)
val DolorAmarillo = Color(0xFFF9A825)
val DolorNaranja = Color(0xFFE65100)
val DolorRojo = Color(0xFFB71C1C)

fun dolorColor(valor: Int) = when (valor) {
    in 0..2  -> DolorVerde
    in 3..5  -> DolorAmarillo
    in 6..7  -> DolorNaranja
    else     -> DolorRojo
}

// Asimetría muscular
val AsimetriaOk     = Color(0xFF2E7D32)
val AsimetriaMedia  = Color(0xFFF9A825)
val AsimetriaAlta   = Color(0xFFB71C1C)
