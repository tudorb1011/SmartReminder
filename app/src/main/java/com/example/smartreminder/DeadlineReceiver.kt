package com.example.smartreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat

class DeadlineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val taskName = intent?.getStringExtra("task_name") ?: "Unknown Task"
            Log.d("DeadlineReceiver", "Received alarm for task: $taskName")

            val notificationManager =
                it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification = NotificationCompat.Builder(it, "DeadlineChannel")
                .setContentTitle("Task Deadline")
                .setContentText("Task: $taskName")
                .setSmallIcon(R.drawable.ic_task_deadline) // Ensure you have this drawable resource
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(taskName.hashCode(), notification)
        }
    }
}
