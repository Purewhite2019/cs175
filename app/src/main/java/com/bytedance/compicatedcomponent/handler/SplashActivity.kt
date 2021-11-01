package com.bytedance.compicatedcomponent.handler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import com.bytedance.compicatedcomponent.R

/**
 *  author : neo
 *  time   : 2021/10/30
 *  desc   :
 */
class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val skipBtn = findViewById<TextView>(R.id.skip)
        val handler = Handler()
        val runnable = Runnable { //跳转到首页
            jumpToHomepage()
        }
        handler.postDelayed(runnable, 3000)
        skipBtn.setOnClickListener {
            handler.removeCallbacks(runnable)
            jumpToHomepage()
        }
    }

    private fun jumpToHomepage() {
        startActivity(Intent(this, HomepageActivity::class.java))
        finish()
    }
}