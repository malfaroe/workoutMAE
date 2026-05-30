package com.mae.workoutmae.data.db.dao

import androidx.room.*
import com.mae.workoutmae.data.db.entity.Sesion
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(sesion: Sesion): Long

    @Update
    suspend fun actualizar(sesion: Sesion)

    @Delete
    suspend fun eliminar(sesion: Sesion)

    @Query("SELECT * FROM sesiones ORDER BY fecha DESC, creadoEn DESC")
    fun todas(): Flow<List<Sesion>>

    @Query("SELECT * FROM sesiones WHERE tipo = :tipo ORDER BY fecha DESC")
    fun porTipo(tipo: String): Flow<List<Sesion>>

    @Query("SELECT * FROM sesiones WHERE fecha BETWEEN :desde AND :hasta ORDER BY fecha DESC")
    fun porRango(desde: String, hasta: String): Flow<List<Sesion>>

    @Query("SELECT * FROM sesiones WHERE id = :id")
    suspend fun porId(id: Int): Sesion?

    @Query("SELECT * FROM sesiones ORDER BY fecha DESC LIMIT :limite")
    fun ultimas(limite: Int): Flow<List<Sesion>>

    @Query("SELECT COUNT(*) FROM sesiones")
    fun totalSesiones(): Flow<Int>

    @Query("SELECT AVG(dolorDurante) FROM sesiones WHERE fecha >= :desde")
    fun dolorPromedioDurante(desde: String): Flow<Float?>

    @Query("SELECT MAX(stepupReps) FROM sesiones WHERE tipo = 'piernas' AND stepupReps IS NOT NULL")
    fun mejorStepup(): Flow<Int?>

    @Query("SELECT * FROM sesiones ORDER BY fecha DESC LIMIT 1")
    suspend fun ultima(): Sesion?
}
