package com.tymo.kalendarz

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Reminders use inexact alarms (no special permission needed). Only the next 3 days are
 * scheduled; a daily refresh alarm and app start keep the window up to date.
 */
object Reminders {
    private const val ACTION_REMIND = "com.tymo.kalendarz.REMIND"
    private const val ACTION_REFRESH = "com.tymo.kalendarz.REFRESH"
    private const val CHANNEL = "events"
    private const val PREF_CODES = "alarm_codes"
    private const val REFRESH_CODE = 1

    private fun pending(context: Context, code: Int, action: String, extras: Intent.() -> Unit = {}, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(action).apply(extras)
        return PendingIntent.getBroadcast(context, code, intent, flags or PendingIntent.FLAG_IMMUTABLE)
    }

    fun scheduleAll(context: Context) {
        val data = Repository(context).load() ?: return
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val prefs = context.getSharedPreferences("alarms", Context.MODE_PRIVATE)

        // Cancel what was scheduled before.
        prefs.getStringSet(PREF_CODES, emptySet()).orEmpty().forEach { c ->
            pending(context, c.toInt(), ACTION_REMIND, flags = PendingIntent.FLAG_NO_CREATE)?.let { am.cancel(it); it.cancel() }
        }

        val now = LocalDateTime.now()
        val until = now.plusDays(3)
        val zone = ZoneId.systemDefault()
        val codes = mutableSetOf<String>()
        occurrences(data.events.filter { it.reminder != null }, now.toLocalDate().minusDays(1), until.toLocalDate().plusDays(1), holidays = false)
            .mapNotNull { o ->
                val base = if (o.event.allDay) o.start.toLocalDate().atTime(9, 0) else o.start
                val trigger = base.minusMinutes((o.event.reminder ?: 0).toLong())
                if (trigger.isAfter(now) && !trigger.isAfter(until)) o to trigger else null
            }
            .take(60)
            .forEach { (o, trigger) ->
                val code = (o.event.id * 31 + o.start.atZone(zone).toEpochSecond() / 60).hashCode()
                val pi = pending(context, code, ACTION_REMIND, {
                    putExtra("title", o.event.title)
                    putExtra("text", describe(o))
                    putExtra("id", code)
                }) ?: return@forEach
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.atZone(zone).toInstant().toEpochMilli(), pi)
                codes += code.toString()
            }
        prefs.edit().putStringSet(PREF_CODES, codes).apply()

        // Refresh the 3-day window once a day.
        pending(context, REFRESH_CODE, ACTION_REFRESH)?.let {
            am.set(AlarmManager.RTC, System.currentTimeMillis() + 24L * 3600 * 1000, it)
        }
    }

    private fun describe(o: Occurrence): String {
        val day = when (o.start.toLocalDate()) {
            LocalDate.now() -> "Dziś"
            LocalDate.now().plusDays(1) -> "Jutro"
            else -> o.start.format(DateTimeFormatter.ofPattern("d MMM", PL))
        }
        val time = if (o.event.allDay) "cały dzień" else o.start.format(DateTimeFormatter.ofPattern("HH:mm")) + "–" + o.end.format(DateTimeFormatter.ofPattern("HH:mm"))
        return listOf("$day, $time", o.event.location).filter { it.isNotBlank() }.joinToString(" · ")
    }

    fun onAlarm(context: Context, intent: Intent) {
        if (intent.action == ACTION_REFRESH) {
            scheduleAll(context)
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, "Przypomnienia o wydarzeniach", NotificationManager.IMPORTANCE_HIGH))
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent.getStringExtra("title") ?: "Wydarzenie")
            .setContentText(intent.getStringExtra("text") ?: "")
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(intent.getIntExtra("id", 2), n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Reminders.onAlarm(context, intent)
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) Reminders.scheduleAll(context)
    }
}
