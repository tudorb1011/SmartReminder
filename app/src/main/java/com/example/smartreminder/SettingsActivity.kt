package com.example.smartreminder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val startServiceButton: Button = findViewById(R.id.button_start_service)
        val stopServiceButton: Button = findViewById(R.id.button_stop_service)

        startServiceButton.setOnClickListener {
            val startServiceIntent = Intent(this, WeatherService::class.java)
            startService(startServiceIntent)
        }

        stopServiceButton.setOnClickListener {
            val stopServiceIntent = Intent(this, WeatherService::class.java)
            stopService(stopServiceIntent)
        }


        val themeSwitch: Switch = findViewById(R.id.switch_theme)
        themeSwitch.isChecked = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        var currentNightMode: Int = AppCompatDelegate.getDefaultNightMode()

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val newNightMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            if (newNightMode != currentNightMode) {
                AppCompatDelegate.setDefaultNightMode(newNightMode)
                currentNightMode = newNightMode
            }
        }


        //bottom menu implementation
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
        buttonSettings.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }


}