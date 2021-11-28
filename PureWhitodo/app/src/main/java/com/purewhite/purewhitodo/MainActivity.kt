package com.purewhite.purewhitodo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.purewhite.purewhitodo.db.TodoDao
import com.purewhite.purewhitodo.db.TodoDatabase

class MainActivity : AppCompatActivity() {
    private var db : TodoDatabase? = null
    private var todoDao : TodoDao? = null

    private var recyclerView : RecyclerView? = null
    private var todoAdapter : TodoAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            TodoDatabase::class.java, "database-todo"
        ).build()
        todoDao = db!!.todoDao()
        todoAdapter = TodoAdapter(todoDao!!, this)

//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        recyclerView = findViewById(R.id.list_todo)

//        setSupportActionBar(toolbar)
        fab.setOnClickListener {
            val intent = Intent(this, AddTodoActivity::class.java)
            startActivity(intent)
        }
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView?.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView?.adapter = todoAdapter
        todoAdapter!!.refresh()

    }

    override fun onRestart() {
        super.onRestart()
        todoAdapter?.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        db?.close()
    }
}