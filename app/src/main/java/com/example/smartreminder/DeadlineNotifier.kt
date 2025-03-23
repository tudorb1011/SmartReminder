package com.example.smartreminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast

class DeadlineNotifier(private val context: Context) {

    private val channelId = "DeadlineChannel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Deadline Notifications"
            val descriptionText = "Notifications for task deadlines"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(taskName: String, taskTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            }
            context.startActivity(intent)
        }

        // Schedule notification 1 minute before the task deadline
        val notificationTime = taskTime - 60 * 1000

        val intent = Intent(context, DeadlineReceiver::class.java).apply {
            putExtra("task_name", taskName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, taskName.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent)
            Log.d("DeadlineNotifier", "Scheduled alarm for task: $taskName at $notificationTime")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Exact alarm scheduling not permitted", Toast.LENGTH_SHORT).show()
        }
    }

}
