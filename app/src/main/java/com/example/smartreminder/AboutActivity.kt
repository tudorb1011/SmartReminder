package com.example.smartreminder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)



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
        buttonAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }


}