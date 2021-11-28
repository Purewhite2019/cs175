package com.purewhite.purewhitodo

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.purewhite.purewhitodo.db.TodoDao
import com.purewhite.purewhitodo.db.TodoEntry
import kotlin.concurrent.thread

class TodoViewHolder(itemView: View, private var todoDao: TodoDao) : RecyclerView.ViewHolder(itemView) {
    private var checkBox : CheckBox = itemView.findViewById(R.id.checkbox)
    private var contentText : TextView = itemView.findViewById(R.id.text_content)
    private var dateText : TextView = itemView.findViewById(R.id.text_date)
    private var deleteButton : View = itemView.findViewById(R.id.button_delete)

    private val colorMap = mapOf(0 to Color.WHITE, 1 to Color.GREEN, 2 to Color.RED)

    fun bind(todoEntry : TodoEntry, todoAdapter: TodoAdapter) {
        contentText.text = todoEntry.content
        dateText.text = todoEntry.date

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            todoEntry.state = isChecked
            if (isChecked) {
                contentText.setTextColor(Color.GRAY)
                contentText.paintFlags = contentText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                contentText.setTextColor(Color.BLACK)
                contentText.paintFlags = contentText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            thread {
                todoDao.update(todoEntry)
            }
        }
        if (todoEntry.state) {
            contentText.setTextColor(Color.GRAY)
            contentText.paintFlags = contentText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            contentText.setTextColor(Color.BLACK)
            contentText.paintFlags = contentText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        deleteButton.setOnClickListener {
            thread {
                todoDao.delete(todoEntry)
                todoAdapter.refresh()
            }
        }

        itemView.setBackgroundColor(colorMap[todoEntry.priority]?: Color.WHITE)
    }
}
