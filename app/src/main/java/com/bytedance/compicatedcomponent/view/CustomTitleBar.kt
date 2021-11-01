package com.bytedance.compicatedcomponent.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.compicatedcomponent.R

/**
 *  author : neo
 *  time   : 2021/10/24
 *  desc   :
 */
class CustomTitleBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val titleView: TextView by lazy {
        findViewById(R.id.tv_title)
    }

    private val leftIconView: ImageView by lazy {
        findViewById(R.id.iv_left_icon)
    }

    private val rightIconView: ImageView by lazy {
        findViewById(R.id.iv_right_icon)
    }


    init {
        val typeArray: TypedArray = context.obtainStyledAttributes(attrs,
            R.styleable.CustomTitleBar
        )
        val title = typeArray.getString(R.styleable.CustomTitleBar_title_text)
        val leftIcon = typeArray.getDrawable(R.styleable.CustomTitleBar_left_icon)
        val rightIcon = typeArray.getDrawable(R.styleable.CustomTitleBar_right_icon)
        typeArray.recycle()

        val view = LayoutInflater.from(context).inflate(R.layout.layout_title_bar_view, this, false)
        addView(view)

        titleView.text = title
        leftIconView.setImageDrawable(leftIcon)
        rightIconView.setImageDrawable(rightIcon)
    }
}