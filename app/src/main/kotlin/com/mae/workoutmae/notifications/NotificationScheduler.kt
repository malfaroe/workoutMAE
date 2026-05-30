package com.mae.workoutmae.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object NotificationScheduler {

    private const val REQUEST_CODE = 1001

    fun schedule(context: Context, horaMin: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildIntent(context)

        val hora = horaMin / 60
        val min  = horaMin % 60
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }

        // On Android 12+ exact alarms require user-granted permission; fall back to inexact if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildIntent(context))
    }

    private fun buildIntent(context: Context) = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
