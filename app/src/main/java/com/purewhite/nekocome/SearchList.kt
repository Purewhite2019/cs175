package com.purewhite.nekocome

import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.View.SCROLLBARS_INSIDE_INSET
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(num : Int, clickListener: (num: Int) -> Unit, text : String) {
//        (itemView as ImageCaption).onBind(drawable, text)
        (itemView as TextView).text = text
        (itemView as TextView).movementMethod = ScrollingMovementMethod.getInstance();
        itemView.setOnClickListener{clickListener(num)}
    }
}

class SearchAdapter(val clickListener: (num: Int) -> Unit) :
    RecyclerView.Adapter<SearchViewHolder>() {

    private val items = arrayListOf<Int>()
    private var catInfo : MainActivity.CatInfo? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(TextView(parent.context))
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        catInfo?.cat_names?.get(items[position])?.let {
            catInfo?.cat_descriptions?.get(items[position])?.let { it1 ->
                holder.bind(items[position], clickListener,
                    "$it $it1"
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(items: List<Int>, catInfo: MainActivity.CatInfo) {
        Log.d("TAG", "SearchList: updateItems() running")
        this.items.clear();
        this.items.addAll(items)
        this.catInfo = catInfo
        notifyDataSetChanged()
        Log.d("TAG", "SearchList: updateItems() OK")

    }
}