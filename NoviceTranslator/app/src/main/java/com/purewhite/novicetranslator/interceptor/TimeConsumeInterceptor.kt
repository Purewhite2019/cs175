package com.purewhite.novicetranslator.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class TimeConsumeInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val startTime = System.currentTimeMillis()
        val resp = chain.proceed(chain.request())
        val endTime = System.currentTimeMillis()
        val url = chain.request().url.toString()
        Log.e("LogInterceptor", "request: $url, time consumption: ${endTime-startTime}")
        return resp
    }
}