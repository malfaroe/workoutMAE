package com.mae.workoutmae.data.repository

import com.mae.workoutmae.data.db.dao.MedidaDao
import com.mae.workoutmae.data.db.entity.MedidaCorporal
import kotlinx.coroutines.flow.Flow

class MedidaRepository(private val dao: MedidaDao) {

    fun todas(): Flow<List<MedidaCorporal>> = dao.todas()
    fun ultimaFlow(): Flow<MedidaCorporal?> = dao.ultimaFlow()

    suspend fun insertar(medida: MedidaCorporal): Long = dao.insertar(medida)
    suspend fun actualizar(medida: MedidaCorporal) = dao.actualizar(medida)
    suspend fun eliminar(medida: MedidaCorporal) = dao.eliminar(medida)
    suspend fun ultima(): MedidaCorporal? = dao.ultima()
}
