package com.example.smartreminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer
import java.util.TimerTask

class WeatherService : Service() {

    private val channelId = "WeatherServiceChannel"
    private var lastWeatherUpdate: String = "Fetching weather updates..."

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("WeatherService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smart Reminder")
            .setContentText(lastWeatherUpdate)
            .setSmallIcon(R.drawable.ic_weather)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
        Log.d("WeatherService", "Foreground service started")

        startWeatherUpdates()

        // Display a toast message indicating the service has started
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@WeatherService, "Weather service started", Toast.LENGTH_SHORT).show()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId,
            "Weather Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
        Log.d("WeatherService", "Notification channel created")
    }

    private fun startWeatherUpdates() {
        val timer = Timer()
        val timerTask = object : TimerTask() {
            override fun run() {
                fetchWeather()
            }
        }
        timer.schedule(timerTask, 0, 60000) // Update every 60 seconds
        Log.d("WeatherService", "Weather updates started")
    }

    private fun fetchWeather() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = ""
                val city = "Bucharest"
                val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&APPID=$apiKey&units=metric"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val weatherData = JSONObject(response)
                    val weatherDescription = weatherData.getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description")
                    val temperature = weatherData.getJSONObject("main").getDouble("temp")

                    lastWeatherUpdate = "$city: $weatherDescription, $temperature°C"
                    updateNotification(lastWeatherUpdate)
                    Log.d("WeatherService", "Weather updated: $lastWeatherUpdate")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("WeatherService", "Error fetching weather: ${e.message}")
            }
        }
    }

    private fun updateNotification(contentText: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smart Reminder")
            .setContentText(lastWeatherUpdate)
            .setSmallIcon(R.drawable.ic_weather) // Ensure this references the correct drawable
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
        Log.d("WeatherService", "Notification updated: $contentText")
    }
}
