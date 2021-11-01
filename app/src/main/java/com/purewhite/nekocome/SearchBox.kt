package com.purewhite.nekocome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import android.widget.FrameLayout

class SearchBox @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        inflate(context, R.layout.activity_search_box, this)
    }

    fun addTextChangedListener(listener: Listener) {
        val edit = findViewById<EditText>(R.id.input)
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                listener.onChanged(s.toString())
            }

        })
    }

    interface Listener {
        fun onChanged(content: String)
    }
}