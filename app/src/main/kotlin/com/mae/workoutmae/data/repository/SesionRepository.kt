package com.mae.workoutmae.data.repository

import com.mae.workoutmae.data.db.dao.SesionDao
import com.mae.workoutmae.data.db.entity.Sesion
import kotlinx.coroutines.flow.Flow

class SesionRepository(private val dao: SesionDao) {

    fun todas(): Flow<List<Sesion>> = dao.todas()
    fun porTipo(tipo: String): Flow<List<Sesion>> = dao.porTipo(tipo)
    fun ultimas(n: Int): Flow<List<Sesion>> = dao.ultimas(n)
    fun totalSesiones(): Flow<Int> = dao.totalSesiones()
    fun dolorPromedioDurante(desde: String): Flow<Float?> = dao.dolorPromedioDurante(desde)
    fun mejorStepup(): Flow<Int?> = dao.mejorStepup()

    suspend fun insertar(sesion: Sesion): Long = dao.insertar(sesion)
    suspend fun actualizar(sesion: Sesion) = dao.actualizar(sesion)
    suspend fun eliminar(sesion: Sesion) = dao.eliminar(sesion)
    suspend fun porId(id: Int): Sesion? = dao.porId(id)
    suspend fun ultima(): Sesion? = dao.ultima()
}
