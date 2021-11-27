package com.purewhite.novicetranslator

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.TrafficStats
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.purewhite.novicetranslator.api.YoudaoRootBean
import com.purewhite.novicetranslator.interceptor.TimeConsumeInterceptor
import okhttp3.*
import okhttp3.EventListener
import java.io.IOException
import java.util.*
import android.view.ViewGroup

class TextPagerAdapter(vlist: MutableList<View>, tabs : MutableList<String>) : PagerAdapter() {
    private var textViewList : MutableList<View> = vlist
    private var tabList : MutableList<String> = tabs


    override fun getCount(): Int {
        return textViewList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(textViewList[position])
        return textViewList[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(textViewList[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabList[position]
    }
}

class MainActivity : AppCompatActivity() {
    private var tabs : MutableList<String> = mutableListOf()
    private var ViewList : MutableList<View> = mutableListOf()
    private var uid = -1

    private var inputButton : Button? = null
    private var inputEditText : EditText? = null
    private var tabLayout : TabLayout? = null
    private var viewPager : ViewPager? = null

    private val cache : MutableMap<String, YoudaoRootBean> = mutableMapOf()
    private val textHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val mBundle = msg.data
            mBundle?.getString("msg")?.let {
                val textView = ViewList.first().findViewById<TextView>(R.id.text_view)
                textView.text = "${textView.text}\n${it}"
//                viewPager?.adapter?.notifyDataSetChanged()
                Log.e("handleMessage", it)
            }
            mBundle?.getString("body")?.let { it ->
                val youdaoBean = gson.fromJson(it, YoudaoRootBean::class.java)
                cache[youdaoBean?.meta!!.input] = youdaoBean
                update(youdaoBean)
            }
        }
    }

    private val okHttpListener = object : EventListener() {
        override fun dnsStart(call: Call, domainName: String) {
            super.dnsStart(call, domainName)

            val msg = Message.obtain()
            msg.data = Bundle().apply { putString("msg", "DNS Search: $domainName") }
            textHandler.sendMessage(msg)
        }

        override fun responseBodyStart(call: Call) {
            super.responseBodyStart(call)

            val msg = Message.obtain()
            msg.data = Bundle().apply { putString("msg", "Response Start") }
            textHandler.sendMessage(msg)
        }
    }

    private val client : OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(TimeConsumeInterceptor())
        .eventListener(okHttpListener)
        .build()

    private val gson = GsonBuilder().create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pm = applicationContext.packageManager
        val ai = pm.getApplicationInfo("com.purewhite.novicetranslator", PackageManager.GET_META_DATA)
        uid = ai.uid

        inputButton = findViewById<Button>(R.id.input_button)
        inputEditText = findViewById<EditText>(R.id.input)
        tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        viewPager = findViewById<ViewPager>(R.id.view_pager)

        viewPager?.adapter = TextPagerAdapter(ViewList, tabs)
        tabLayout?.setupWithViewPager(viewPager, false)

        addPage("日志", "Init OK")
        addPage("网络翻译", "")
        addPage("维基百科", "")
        inputButton?.setOnClickListener {
            val key = inputEditText?.text.toString()

//            while (ViewList.size > 1) {
//                tabLayout?.removeTabAt(1)
//                tabs.removeAt(1)
//                ViewList.removeAt(1)
//
//                viewPager?.adapter?.notifyDataSetChanged()
//                viewPager?.currentItem = 0
//            }
            val textView = ViewList.first().findViewById<TextView>(R.id.text_view)
            textView.text = "${textView.text}\nQuerying for ${key}"
//            viewPager?.adapter?.notifyDataSetChanged()

            if (cache[key] == null){
                textView.text = "${textView.text}\nCache miss for ${key}"
                requestTranslation(key)

            }
            else {
                textView.text = "${textView.text}\nCache hit for ${key}"
                cache[key]?.let { update(it) }
            }
        }
    }

    private fun request(url : String, callback : Callback) {
        val request : Request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(callback)
    }

    private fun requestTranslation(input : String) {
        val url = "https://dict.youdao.com/jsonapi?q=$input"
        request(url, object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()

                val msg = Message.obtain()
                msg.data = Bundle().apply {
                    putString("body", bodyString)
                }
                textHandler.sendMessage(msg)
            }

            override fun onFailure(call: Call, e: IOException) {
                val msg = Message.obtain()
                msg.data = Bundle().apply { putString("msg", e.message) }
                textHandler.sendMessage(msg)
            }
        })
    }

    private fun addPage(title : String, content : String) {
        tabs.add(title)
        tabLayout?.addTab(tabLayout!!.newTab().setText(title))

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.view, null)
        val textView = view.findViewById<TextView>(R.id.text_view)
        textView.text = content

        ViewList.add(view)
        viewPager?.adapter?.notifyDataSetChanged()
        viewPager?.currentItem = 0
    }

    private fun update(youdaoBean : YoudaoRootBean) {
        var tv = ViewList[1].findViewById<TextView>(R.id.text_view)
        tv.text = ""
        youdaoBean.web_trans.web_translation?.let{
            var web_translation : String = ""
            for (items in it) {
                web_translation += "\t${items.key}\n"
                for (trans in items.trans)
                    web_translation += "\t\t${trans.value}\n"
            }
//            addPage("网络翻译", web_translation)
            tv.text = web_translation
//            viewPager?.adapter?.notifyDataSetChanged()
        }

        tv = ViewList[2].findViewById<TextView>(R.id.text_view)
        tv.text = ""
        youdaoBean.wikipedia_digest?.let {
            var wikipedia_digest : String = ""
            for (summary in it.summarys) {
                wikipedia_digest += "${summary.key} : ${summary.summary}"
            }
//            addPage("维基百科", wikipedia_digest)
            tv.text = wikipedia_digest
        }

        val rx = TrafficStats.getUidRxBytes(uid)
        val tx = TrafficStats.getUidTxBytes(uid)
        tv = ViewList[0].findViewById<TextView>(R.id.text_view)
        tv.text = "${tv.text}\nTranffic Stats: \n\tSend: ${tx}, Receive:${rx}"
    }
}