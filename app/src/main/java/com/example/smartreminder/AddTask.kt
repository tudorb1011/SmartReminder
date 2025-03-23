package com.example.smartreminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class AddTask : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val database: Database
        database = Database(this)

        var selectedDate = ""
        var selectedTime = ""

        val taskEditText: EditText = findViewById(R.id.editTextTask)
        val addButton: Button = findViewById(R.id.buttonAddTask)
        val selectDateButton: Button = findViewById(R.id.buttonSelectDate)
        val selectTimeButton: Button = findViewById(R.id.buttonSelectTime)
        val textViewSelectedDate: TextView = findViewById(R.id.textViewSelectedDate)
        val textViewSelectedTime: TextView = findViewById(R.id.textViewSelectedTime)

        // Set up Date Picker
        selectDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    if(month>9) {
                        if(dayOfMonth>9)
                        {
                            selectedDate = "$year/${month + 1}/$dayOfMonth"
                        }
                        else{
                            selectedDate = "$year/${month + 1}/0$dayOfMonth"
                        }
                    }
                    else{
                        if(dayOfMonth>9)
                        {
                            selectedDate = "$year/0${month + 1}/$dayOfMonth"
                        }
                        else{
                            selectedDate = "$year/0${month + 1}/0$dayOfMonth"
                        }
                    }
                    textViewSelectedDate.text = "Selected Date: $selectedDate"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Set up Time Picker
        selectTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    if(hourOfDay>9) {
                        selectedTime = "$hourOfDay:$minute"
                    }
                    else{
                        selectedTime = "0$hourOfDay:$minute"
                    }
                    textViewSelectedTime.text = "Selected Time: $selectedTime"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        // Add Task Button Click
        addButton.setOnClickListener {
            val task = taskEditText.text.toString()
            if (task.isNotEmpty() && selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                database.addTask(task, selectedDate, selectedTime)
                Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show()
                finish() // Close activity after adding task
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }


        // back button
        val buttonBack: Button = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener {
            // Navigate to the Home screen (MainActivity)
            startActivity(Intent(this, TasksActivity::class.java))
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