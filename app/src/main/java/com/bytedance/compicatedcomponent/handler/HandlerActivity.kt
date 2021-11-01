package com.bytedance.compicatedcomponent.handler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.bytedance.compicatedcomponent.R

class HandlerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_handler_activity)

//        val handler = Handler(Looper.getMainLooper())
//        handler.postDelayed({
//            jump2MainPage()
//        }, 3000)



//        val skipButton = findViewById<View>(R.id.tv_skip)
//        skipButton.setOnClickListener {
//            handler.removeCallbacksAndMessages(null)
//            jump2MainPage()
//        }

        val handler = Handler(Looper.getMainLooper()) { msg ->
            if (msg.what == 1) {
                jump2MainPage()
            }
            true
        }
        val msg = Message.obtain(handler, 1)
        handler.sendMessageDelayed(msg, 3000)

    }

    private fun jump2MainPage() {
        startActivity(Intent(this, HomepageActivity::class.java))
    }
}