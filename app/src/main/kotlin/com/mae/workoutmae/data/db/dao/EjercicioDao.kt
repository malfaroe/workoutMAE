package com.mae.workoutmae.data.db.dao

import androidx.room.*
import com.mae.workoutmae.data.db.entity.Ejercicio
import com.mae.workoutmae.data.db.entity.RegistroEjercicio
import kotlinx.coroutines.flow.Flow

@Dao
interface EjercicioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(ejercicio: Ejercicio): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(ejercicios: List<Ejercicio>)

    @Update
    suspend fun actualizar(ejercicio: Ejercicio)

    @Delete
    suspend fun eliminar(ejercicio: Ejercicio)

    @Query("SELECT * FROM ejercicios ORDER BY categoria, orden, nombre")
    fun todos(): Flow<List<Ejercicio>>

    @Query("SELECT * FROM ejercicios WHERE activo = 1 AND categoria = :categoria ORDER BY orden, nombre")
    fun activosPorCategoria(categoria: String): Flow<List<Ejercicio>>

    @Query("SELECT * FROM ejercicios WHERE activo = 1 ORDER BY categoria, orden, nombre")
    fun todosActivos(): Flow<List<Ejercicio>>

    @Query("SELECT COUNT(*) FROM ejercicios WHERE id = :id")
    suspend fun existe(id: Int): Int

    // Registros dinámicos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistro(registro: RegistroEjercicio): Long

    @Query("SELECT * FROM registros_ejercicio WHERE sesionId = :sesionId")
    suspend fun registrosDeSesion(sesionId: Int): List<RegistroEjercicio>

    @Query("SELECT COUNT(*) FROM registros_ejercicio WHERE ejercicioId = :ejercicioId")
    suspend fun contarRegistros(ejercicioId: Int): Int

    @Query("DELETE FROM registros_ejercicio WHERE sesionId = :sesionId")
    suspend fun eliminarRegistrosDeSesion(sesionId: Int)

    @Query("UPDATE ejercicios SET activo = 1 WHERE esPersonalizado = 0")
    suspend fun restaurarEjerciciosBase()
}
