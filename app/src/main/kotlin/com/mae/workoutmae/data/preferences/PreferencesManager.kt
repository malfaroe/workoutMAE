package com.mae.workoutmae.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("workoutmae_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val NOMBRE_PACIENTE       = stringPreferencesKey("nombre_paciente")
        val FECHA_INICIO          = stringPreferencesKey("fecha_inicio_protocolo")
        val RECORDATORIO_ACTIVO   = booleanPreferencesKey("recordatorio_activo")
        val RECORDATORIO_HORA     = intPreferencesKey("recordatorio_hora")     // minutos desde 00:00
        val ONBOARDING_COMPLETADO = booleanPreferencesKey("onboarding_ok")
    }

    val nombrePaciente: Flow<String> = context.dataStore.data.map { it[NOMBRE_PACIENTE] ?: "" }
    val fechaInicio: Flow<String> = context.dataStore.data.map { it[FECHA_INICIO] ?: "" }
    val recordatorioActivo: Flow<Boolean> = context.dataStore.data.map { it[RECORDATORIO_ACTIVO] ?: false }
    val recordatorioHora: Flow<Int> = context.dataStore.data.map { it[RECORDATORIO_HORA] ?: (8 * 60 + 0) }
    val onboardingCompletado: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETADO] ?: false }

    suspend fun setNombre(v: String) = context.dataStore.edit { it[NOMBRE_PACIENTE] = v }
    suspend fun setFechaInicio(v: String) = context.dataStore.edit { it[FECHA_INICIO] = v }
    suspend fun setRecordatorio(activo: Boolean, horaMin: Int) = context.dataStore.edit {
        it[RECORDATORIO_ACTIVO] = activo
        it[RECORDATORIO_HORA] = horaMin
    }
    suspend fun setOnboardingCompletado() = context.dataStore.edit { it[ONBOARDING_COMPLETADO] = true }
}
