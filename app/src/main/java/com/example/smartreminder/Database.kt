package com.example.smartreminder

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

const val DATABASE_NAME = "smartreminder_database"
const val DATABASE_VERSION = 1
const val TABLE_NAME = "tasks"
const val ID = "id"
const val TASK = "task"
const val DEADLINE_DATE = "deadline_date"
const val DEADLINE_TIME = "deadline_time"

data class Task( val id: Int, val name: String, val date: String, val time: String ) { override fun toString(): String { return "$name (Due: $date at $time)" } }



class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID + " INTEGER PRIMARY KEY, " +
                TASK + " TEXT," +
                DEADLINE_DATE + " TEXT," + DEADLINE_TIME + " TEXT" + ")")
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun addTask(task: String, deadlineDate: String, deadlineTime: String) {
        val values = ContentValues().apply {
            put(TASK, task)
            put(DEADLINE_DATE, deadlineDate)
            put(DEADLINE_TIME, deadlineTime)
        }

        val db = writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun deleteTask(task: Task) {
        val db = writableDatabase
        val id = task.id
        db.delete(TABLE_NAME, "$ID=?", arrayOf(id.toString()))
        db.close()
    }

    fun editTask(id: Int, task: String, deadlineDate: String, deadlineTime: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET $TASK = '$task', $DEADLINE_DATE = '$deadlineDate', $DEADLINE_TIME = '$deadlineTime' WHERE $ID = $id")
        db.close()
    }

    fun getAllTasks(): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val query = db.rawQuery(
            """
            SELECT $ID, $TASK, $DEADLINE_DATE, $DEADLINE_TIME
            FROM $TABLE_NAME
            ORDER BY $DEADLINE_DATE ASC, $DEADLINE_TIME ASC
            """.trimIndent(),
            null
        )
        if (query.moveToFirst()) {
            do {
                val task = Task(
                    id = query.getInt(query.getColumnIndexOrThrow(ID)),
                    name = query.getString(query.getColumnIndexOrThrow(TASK)),
                    date = query.getString(query.getColumnIndexOrThrow(DEADLINE_DATE)),
                    time = query.getString(query.getColumnIndexOrThrow(DEADLINE_TIME))
                )
                tasks.add(task)
            } while (query.moveToNext())
        }
        query.close()
        db.close()
        return tasks
    }

    fun getTodayTasks(today: String): List<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val query = db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $DEADLINE_DATE = ? ORDER BY $DEADLINE_TIME ASC", arrayOf(today))
        if (query.moveToFirst()) {
            do {
                val task = Task(
                    id = query.getInt(query.getColumnIndexOrThrow(ID)),
                    name = query.getString(query.getColumnIndexOrThrow(TASK)),
                    date = query.getString(query.getColumnIndexOrThrow(DEADLINE_DATE)),
                    time = query.getString(query.getColumnIndexOrThrow(DEADLINE_TIME))
                )
                tasks.add(task)
            } while (query.moveToNext())
        }
        query.close()
        db.close()
        return tasks
    }
}
