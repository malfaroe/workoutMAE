package com.mae.workoutmae.data.db.dao

import androidx.room.*
import com.mae.workoutmae.data.db.entity.MedidaCorporal
import kotlinx.coroutines.flow.Flow

@Dao
interface MedidaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(medida: MedidaCorporal): Long

    @Update
    suspend fun actualizar(medida: MedidaCorporal)

    @Delete
    suspend fun eliminar(medida: MedidaCorporal)

    @Query("SELECT * FROM medidas_corporales ORDER BY fecha DESC")
    fun todas(): Flow<List<MedidaCorporal>>

    @Query("SELECT * FROM medidas_corporales ORDER BY fecha DESC LIMIT 1")
    suspend fun ultima(): MedidaCorporal?

    @Query("SELECT * FROM medidas_corporales ORDER BY fecha DESC LIMIT 1")
    fun ultimaFlow(): Flow<MedidaCorporal?>
}
