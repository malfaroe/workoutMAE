package com.mae.workoutmae.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "workoutmae_reminder"
        nm.createNotificationChannel(
            NotificationChannel(channelId, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("WorkoutMAE")
            .setContentText("¿Registraste tu sesión de hoy?")
            .setAutoCancel(true)
            .build()
        nm.notify(1, notif)
    }
}
