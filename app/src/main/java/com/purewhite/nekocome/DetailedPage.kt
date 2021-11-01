package com.purewhite.nekocome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import org.w3c.dom.Text

class DetailedPage constructor() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_page)

        val num = intent.extras?.getInt("num")
        Log.d("TAG", "DetailedPage::onCreate: $num")

        intent.extras?.getInt("cat_drawable")?.let {
            findViewById<ImageView>(R.id.cat_image).setImageResource(
                it
            )
        }
        findViewById<TextView>(R.id.cat_number).text = "No. " + intent.extras?.getInt("num").toString()
        findViewById<TextView>(R.id.cat_name).text = intent.extras?.getString("cat_name")
        findViewById<TextView>(R.id.cat_gender).text = intent.extras?.getString("cat_gender")
        findViewById<TextView>(R.id.cat_age).text = intent.extras?.getString("cat_age")
        findViewById<TextView>(R.id.cat_color).text = intent.extras?.getString("cat_color")
        findViewById<TextView>(R.id.cat_description).text = intent.extras?.getString("cat_description")
    }
}