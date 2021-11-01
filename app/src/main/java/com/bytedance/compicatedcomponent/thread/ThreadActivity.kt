package com.bytedance.compicatedcomponent.thread

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.bytedance.compicatedcomponent.R
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 *  author : neo
 *  time   : 2021/10/23
 *  desc   :
 */
class ThreadActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_thread_activity)

        // 1
//        Thread {
//            // do something...
//            countdown()
//        }.start()

        // 2
//        val t = SampleThread()
//        t.start()

        // 3
        val threadPoolExecutor = createThreadPoolExecutor()
        threadPoolExecutor.submit {
            countdown()
        }
    }
}

private fun countdown() {
    var count = 9
    Log.i("Countdown", "countdown: start...")
    while (count >= 0) {
        Log.i("Countdown", "countdown: current is ${count --}, thread name is ${Thread.currentThread().name}")
        if (count == 5) {
            Thread.currentThread().interrupt()
        }
        Thread.sleep(1000)
    }
    Log.i("Countdown", "countdown: end...")
}

class SampleThread : Thread() {
    override fun run() {
        super.run()
        countdown()
    }
}

fun createThreadPoolExecutor() = ThreadPoolExecutor(
    3, // 核心线程数
    5, // 最大线程数
    10L, // 线程存活时间
    TimeUnit.SECONDS, // 时间单位
    PriorityBlockingQueue() // 缓冲队列
)
