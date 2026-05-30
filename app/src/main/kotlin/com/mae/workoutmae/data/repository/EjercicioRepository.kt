package com.mae.workoutmae.data.repository

import com.mae.workoutmae.data.db.dao.EjercicioDao
import com.mae.workoutmae.data.db.entity.Ejercicio
import com.mae.workoutmae.data.db.entity.RegistroEjercicio
import kotlinx.coroutines.flow.Flow

class EjercicioRepository(private val dao: EjercicioDao) {

    fun todos(): Flow<List<Ejercicio>> = dao.todos()
    fun todosActivos(): Flow<List<Ejercicio>> = dao.todosActivos()
    fun activosPorCategoria(cat: String): Flow<List<Ejercicio>> = dao.activosPorCategoria(cat)

    suspend fun insertar(e: Ejercicio): Long = dao.insertar(e)
    suspend fun actualizar(e: Ejercicio) = dao.actualizar(e)
    suspend fun eliminar(e: Ejercicio) = dao.eliminar(e)
    suspend fun existe(id: Int): Boolean = dao.existe(id) > 0

    suspend fun insertarRegistro(r: RegistroEjercicio): Long = dao.insertarRegistro(r)
    suspend fun registrosDeSesion(sesionId: Int): List<RegistroEjercicio> = dao.registrosDeSesion(sesionId)
    suspend fun contarRegistros(ejercicioId: Int): Int = dao.contarRegistros(ejercicioId)
    suspend fun restaurarEjerciciosBase() = dao.restaurarEjerciciosBase()
}
