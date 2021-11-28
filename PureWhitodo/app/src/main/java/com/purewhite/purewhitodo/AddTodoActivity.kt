package com.purewhite.purewhitodo

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.purewhite.purewhitodo.db.TodoDao
import com.purewhite.purewhitodo.db.TodoDatabase
import com.purewhite.purewhitodo.db.TodoEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class AddTodoActivity : AppCompatActivity(){
    private var db : TodoDatabase? = null
    private var todoDao : TodoDao? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_todo)
        title = "Add a todo-entry"

        db = Room.databaseBuilder(
            applicationContext,
            TodoDatabase::class.java, "database-todo"
        ).build()
        todoDao = db!!.todoDao()

        val editText = findViewById<EditText>(R.id.edit_text)
        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        val lowRadio = findViewById<RadioButton>(R.id.btn_low)
        val addButton = findViewById<Button>(R.id.btn_add)

        editText.focusable = View.FOCUSABLE
        editText.requestFocus()
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputManager?.showSoftInput(editText, 0)

        lowRadio.isChecked = true

        addButton.setOnClickListener {
            val content = editText.text
            if (content.isEmpty()) {
                Toast.makeText(this, "No content to add", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            insert(content.toString(), gerPriority(radioGroup))
            this.finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        db?.close()
    }

    private fun gerPriority(radioGroup : RadioGroup) : Int =
        when (radioGroup.checkedRadioButtonId) {
        R.id.btn_high -> 2
        R.id.btn_medium -> 1
        else -> 0
    }

    private fun insert(content: String, priority: Int) {
        val todoEntry = TodoEntry(
            state = false,
            content = content,
            date = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH).format(Date(System.currentTimeMillis())).toString(),
            priority = priority,
            id = System.currentTimeMillis())

        thread {
            todoDao!!.insert(todoEntry)
//            Log.d("Insert", id.toString())
//            if (id != -1) {
//                Toast.makeText(
//                    this,
//                    "Todo-entry added", Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                Toast.makeText(
//                    this,
//                    "Error", Toast.LENGTH_SHORT
//                ).show()
//            }
        }

    }

}