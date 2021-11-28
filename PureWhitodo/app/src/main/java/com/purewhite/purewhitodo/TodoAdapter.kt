package com.purewhite.purewhitodo

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.purewhite.purewhitodo.db.TodoDao
import com.purewhite.purewhitodo.db.TodoEntry
import kotlin.concurrent.thread

class TodoAdapter(private val todoDao: TodoDao, private val activity: Activity) : RecyclerView.Adapter<TodoViewHolder>() {
    private var todoList : MutableList<TodoEntry> = mutableListOf()

    fun refresh() {
        thread {
            todoList.clear()
            val res = todoDao.getAll()
            todoList.addAll(res)
            todoList.sortWith { v1, v2 ->
                if ((v1.priority < v2.priority) or ((v1.priority == v2.priority) and (v1.id > v2.id))) 1
                else -1
            }
            activity.runOnUiThread{
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val todoView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(todoView, todoDao)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(todoList[position], this)
    }

    override fun getItemCount(): Int {
        return todoList.size
    }
}