package com.mae.workoutmae.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mae.workoutmae.WorkoutMAEApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as WorkoutMAEApplication
        CoroutineScope(Dispatchers.IO).launch {
            val activo = app.preferencesManager.recordatorioActivo.first()
            val horaMin = app.preferencesManager.recordatorioHora.first()
            if (activo) NotificationScheduler.schedule(context, horaMin)
        }
    }
}
