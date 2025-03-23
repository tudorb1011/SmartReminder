package com.example.smartreminder

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class TasksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val database: Database
        database = Database(this)

        // add task button implementation
        val buttonAddTask: Button = findViewById(R.id.button_addtask)
        buttonAddTask.setOnClickListener {
            startActivity(Intent(this, AddTask::class.java))
        }

        val tasksListView: ListView = findViewById(R.id.listViewTasks)
        val tasks = database.getAllTasks()?.toMutableList()
        if (tasks != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks)
            tasksListView.adapter = adapter

            // Long press to delete task
            tasksListView.setOnItemLongClickListener { _, _, position, _ ->
                val task = tasks[position]
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Delete Task")
                builder.setMessage("Are you sure you want to delete this task?")
                builder.setPositiveButton("Yes") { dialog, _ ->
                    database.deleteTask(task)
                    tasks.removeAt(position)
                    adapter.notifyDataSetChanged()
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                val alertDialog = builder.create()
                alertDialog.show()
                true
            }
        } else {
            tasksListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf("No tasks"))
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
        buttonAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
