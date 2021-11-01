package com.purewhite.nekocome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log

class MainActivity : AppCompatActivity() {

    class CatInfo constructor(num : Int) {
        private val drawables : List<Int> = listOf(R.drawable.c1, R.drawable.c2, R.drawable.c3, R.drawable.c4, R.drawable.c5, R.drawable.c6, R.drawable.c7, R.drawable.c8)
        private val names : List<String> = listOf("Alice", "Bob", "Carol", "Dave", "Eve")
        private val colors : List<String> = listOf("Red", "Green", "Blue")

        val cat_drawables = arrayListOf<Int>()
        val cat_names = arrayListOf<String>()
        val cat_genders = arrayListOf<String>()
        val cat_ages = arrayListOf<String>()
        val cat_colors = arrayListOf<String>()
        val cat_descriptions = arrayListOf<String>()

        init {
            for (i in (0..1000)) {
                cat_drawables.add(drawables.random())
                cat_names.add(names.random())
                cat_genders.add(listOf("Male", "Female").random())
                cat_ages.add((0..5).random().toString() + " year(s) ")
                cat_colors.add(colors.random())
                cat_descriptions.add("absent")
            }
        }
    }

    val catInfo = CatInfo(1000)

    private fun itemClicked(num : Int) {
        val intent = Intent(this, DetailedPage::class.java)
        intent.putExtra("num", num)
        intent.putExtra("cat_drawable", catInfo.cat_drawables[num])
        intent.putExtra("cat_name", catInfo.cat_names[num])
        intent.putExtra("cat_gender", catInfo.cat_genders[num])
        intent.putExtra("cat_age", catInfo.cat_ages[num])
        intent.putExtra("cat_color", catInfo.cat_colors[num])
        intent.putExtra("cat_description", catInfo.cat_descriptions[num])
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = findViewById<RecyclerView>(R.id.list)
        val adapter = SearchAdapter { num: Int -> itemClicked(num) }

        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(this)

        val items =(0..1000).toList()
        Log.d("TAG", "MainActivity: Objects init OK")

        adapter.updateItems(items, catInfo)
        Log.d("TAG", "MainActivity: Adapter init OK")

        val searchBox = findViewById<SearchBox>(R.id.search_bar)
        searchBox.addTextChangedListener(object : SearchBox.Listener {
            override fun onChanged(content: String) {
                val filters = items.filter {
                    catInfo.cat_names[it].contains(content) or
                    catInfo.cat_ages[it].contains(content) or
                    catInfo.cat_ages[it].contains(content) or
                    catInfo.cat_colors[it].contains(content) or
                    catInfo.cat_descriptions[it].contains(content)
                }
                adapter.updateItems(filters, catInfo)
            }
        })
        Log.d("TAG", "MainActivity: SearchBox init OK")

    }
}