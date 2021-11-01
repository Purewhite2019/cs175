package com.bytedance.compicatedcomponent.view

import android.app.Activity
import android.os.Bundle
import com.bytedance.compicatedcomponent.R

/**
 *  author : neo
 *  time   : 2021/10/24
 *  desc   :
 */
class CustomViewActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_custom_activity)
    }
}