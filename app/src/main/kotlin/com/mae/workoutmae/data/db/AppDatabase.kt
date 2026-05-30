package com.mae.workoutmae.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mae.workoutmae.data.db.dao.EjercicioDao
import com.mae.workoutmae.data.db.dao.MedidaDao
import com.mae.workoutmae.data.db.dao.SesionDao
import com.mae.workoutmae.data.db.entity.Ejercicio
import com.mae.workoutmae.data.db.entity.MedidaCorporal
import com.mae.workoutmae.data.db.entity.MetricaTipo
import com.mae.workoutmae.data.db.entity.RegistroEjercicio
import com.mae.workoutmae.data.db.entity.Sesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Sesion::class, MedidaCorporal::class, Ejercicio::class, RegistroEjercicio::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sesionDao(): SesionDao
    abstract fun medidaDao(): MedidaDao
    abstract fun ejercicioDao(): EjercicioDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workoutmae.db",
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.ejercicioDao().insertarTodos(ejerciciosBase())
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }

        private fun ejerciciosBase() = listOf(
            Ejercicio(nombre = "Slant Board", categoria = "piernas", metricaPrincipal = MetricaTipo.SERIES_FALLA, unidadPrincipal = "series", metricaSecundaria = MetricaTipo.ESCALA_SENSACION, unidadSecundaria = "sensación", notas = "Declive 25°. Rodilla no pasa punta pie.", orden = 1),
            Ejercicio(nombre = "Step-ups", categoria = "piernas", metricaPrincipal = MetricaTipo.REPS_FALLA, unidadPrincipal = "reps", notas = "Hasta falla. KPI principal.", orden = 2),
            Ejercicio(nombre = "Spanish Squat", categoria = "piernas", metricaPrincipal = MetricaTipo.SERIES_FALLA, unidadPrincipal = "series", metricaSecundaria = MetricaTipo.BOOLEANO, unidadSecundaria = "tempo 5-X-2", notas = "Tempo 5-X-2. Cuerda o poste.", orden = 3),
            Ejercicio(nombre = "Wall Sit", categoria = "piernas", metricaPrincipal = MetricaTipo.TIEMPO_SEGUNDOS, unidadPrincipal = "segundos", notas = "Rodillas 90°. KPI principal.", orden = 4),
            Ejercicio(nombre = "Bulgarian Split Squat", categoria = "piernas", metricaPrincipal = MetricaTipo.SERIES_X_REPS, unidadPrincipal = "series", metricaSecundaria = MetricaTipo.ESCALA_SENSACION, unidadSecundaria = "tensión dorsal", notas = "Pie trasero elevado. Revisar posición si tensión alta.", orden = 5),
            Ejercicio(nombre = "Calf Raises", categoria = "piernas", metricaPrincipal = MetricaTipo.SERIES_FALLA, unidadPrincipal = "series", notas = "Hasta falla por serie.", orden = 6),
            Ejercicio(nombre = "Flutter Kicks", categoria = "piernas", metricaPrincipal = MetricaTipo.SERIES_FALLA, unidadPrincipal = "series", orden = 7),
            Ejercicio(nombre = "Push-ups", categoria = "piernas", metricaPrincipal = MetricaTipo.TOTAL_REPS, unidadPrincipal = "reps totales", notas = "Total acumulado de la sesión.", orden = 8),
        )
    }
}
