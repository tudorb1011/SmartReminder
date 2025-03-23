package com.example.smartreminder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //quote widget
        val textQuote: TextView = findViewById(R.id.textQuote)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://zenquotes.io/api/random"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonArray = JSONArray(response)
                    val jsonObject = jsonArray.getJSONObject(0)

                    val content = jsonObject.getString("q") // Quote text
                    val author = jsonObject.getString("a")  // Author name

                    runOnUiThread {
                        textQuote.text = "\"$content\" \n - $author"
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Error fetching quote: ${connection.responseMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        //weather widget
        val weatherTextView: TextView = findViewById(R.id.textViewWeather)
        val weatherTextViewTitle: TextView = findViewById(R.id.textViewWeathertitle)

        // fetch and display weather
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = "" // Your OpenWeather API key
                val city = "Bucharest" // Desired city
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

                    runOnUiThread {
                        weatherTextViewTitle.text = "WEATHER"
                        weatherTextView.text = "$city: \n$weatherDescription, $temperature°C"
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${connection.responseMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace() // log the exception for debugging
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to fetch weather: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // database association to the main activity
        val database: Database = Database(this)

        val taskListLayout: LinearLayout = findViewById(R.id.task_list)

        // today's tasks
        val dayToday = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
        val monthToday = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
        val yearToday = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        var today = ""

        if (monthToday.toInt() > 9) {
            if (dayToday.toInt() > 9) {
                today = "$yearToday/$monthToday/$dayToday"
            } else {
                today = "$yearToday/$monthToday/0$dayToday"
            }
        } else {
            if (dayToday.toInt() > 9) {
                today = "$yearToday/0$monthToday/$dayToday"
            } else {
                today = "$yearToday/0$monthToday/0$dayToday"
            }
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        today = dateFormat.format(Date())

        val todayTasks = database.getTodayTasks(today)
        var k = 0 // we use k to check if we have tasks

        //display tasks from today
        for (task in todayTasks) {
            k=1
            // Create a TextView for the task name
            val taskNameView = TextView(this).apply {
                text = "Task: ${task.name} \n Deadline: ${task.time}"
                textSize = 16f
                setPadding(16, 8, 16, 4)
            }
            taskListLayout.addView(taskNameView)
        }

        //check if we have tasks or not (using k)
        if (k == 0) {
            val taskView = TextView(this).apply {
                text = "No tasks today"
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            taskListLayout.addView(taskView)
        }


        //notifications for due tasks

        val allTasks = database.getAllTasks()

        val deadlineNotifier = DeadlineNotifier(this)

        for (task in allTasks) {
            val taskDateStr = task.date
            val taskTimeStr = task.time
            val taskDateTimeStr = "$taskDateStr $taskTimeStr"
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

            try {
                val taskDateTime = sdf.parse(taskDateTimeStr)
                val taskDeadlineMillis = taskDateTime.time

                val nowMillis = System.currentTimeMillis()
                val nowCalendar = Calendar.getInstance()
                nowCalendar.timeInMillis = nowMillis

                // Get task date and time as a calendar object
                val taskCalendar = Calendar.getInstance()
                if (taskDateTime != null) {
                    taskCalendar.time = taskDateTime
                }

                // Calculate times for notifications
                val twentyFourHoursBefore = taskDeadlineMillis - 86400000 // 24 hours in milliseconds
                val oneHourBefore = taskDeadlineMillis - 3600000 // 1 hour in milliseconds

                // Check and schedule 24-hour notification
                if (twentyFourHoursBefore > nowMillis) {
                    val isDayBefore =
                        nowCalendar.get(Calendar.DAY_OF_YEAR) == taskCalendar.get(Calendar.DAY_OF_YEAR) - 1
                    val isSameTime = nowCalendar.get(Calendar.HOUR_OF_DAY) == taskCalendar.get(Calendar.HOUR_OF_DAY)

                    if (isDayBefore || twentyFourHoursBefore >= nowMillis) {
                        deadlineNotifier.scheduleNotification(
                            "${task.name} is due in 24 hours",
                            twentyFourHoursBefore
                        )
                    }
                }

                // Check and schedule 1-hour notification
                if (oneHourBefore > nowMillis) {
                    val isSameDay = nowCalendar.get(Calendar.DAY_OF_YEAR) == taskCalendar.get(Calendar.DAY_OF_YEAR)
                    val isOneHourBefore = nowCalendar.get(Calendar.HOUR_OF_DAY) + 1 == taskCalendar.get(Calendar.HOUR_OF_DAY)

                    if (isSameDay || oneHourBefore >= nowMillis) {
                        deadlineNotifier.scheduleNotification(
                            "${task.name} is due in 1 hour",
                            oneHourBefore
                        )
                    }
                }

                // Schedule exact-time notification
                if (taskDeadlineMillis > nowMillis) {
                    deadlineNotifier.scheduleNotification(
                        "${task.name} is due now",
                        taskDeadlineMillis
                    )
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error scheduling notifications for task: ${task.name}", e)
            }
        }



        // Share tasks
        val buttonShare: Button = findViewById(R.id.button_share)
        buttonShare.setOnClickListener {
            val taskListText = buildString {
                append("Tasks for today:\n\n")
                for (task in todayTasks) {
                    append("Task: ${task.name}\nDeadline: ${task.time}\n\n")
                }
                if (todayTasks.isEmpty()) {
                    append("No tasks today")
                }
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, taskListText)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share your tasks via"))
        }

        // Bottom menu implementation
        // Home button
        val buttonHome: Button = findViewById(R.id.button_home)
        buttonHome.setOnClickListener {
            // Navigate to the Home screen (MainActivity)
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Tasks button
        val buttonAdd: Button = findViewById(R.id.button_add)
        buttonAdd.setOnClickListener {
            startActivity(Intent(this, TasksActivity::class.java))
        }

        // Settings button
        val buttonSettings: Button = findViewById(R.id.button_settings)
        buttonSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // About button
        val buttonAbout: Button = findViewById(R.id.button_about)
        buttonAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for task deadlines"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
