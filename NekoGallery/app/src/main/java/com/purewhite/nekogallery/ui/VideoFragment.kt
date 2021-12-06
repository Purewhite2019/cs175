package com.purewhite.nekogallery.ui

import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.purewhite.nekogallery.R
import java.io.IOException
import java.util.*

class VideoFragment : Fragment() {
    lateinit var progressBar : SeekBar
    lateinit var progressText : TextView

    private val player : MediaPlayer by lazy { MediaPlayer() }
    private var holder : SurfaceHolder? = null

    private val imgStart by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.ic_start)
    }
    private val imgPause by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.ic_pause)
    }

    private val timer = Timer()
    private var timerPaused = true
    private val progressUpdateTask = object : TimerTask() {
        override fun run() {
            Log.d("VideoFragment", "Timer: $timerPaused")
            if (!timerPaused) {
                progressBar.progress = player.currentPosition * 100 / player.duration
                requireActivity().runOnUiThread{
                    progressText.text = "${player.currentPosition/1000}/${player.duration/1000}"
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video, container, false)

        val surfaceView = view.findViewById<SurfaceView>(R.id.surface_view)
        val btn = view.findViewById<ImageButton>(R.id.btn)
        progressBar = view.findViewById(R.id.progress_bar)
        progressText = view.findViewById(R.id.progress_text)

        timer.schedule(progressUpdateTask, 0,500)

        try {
            player.setDataSource(resources.openRawResourceFd(R.raw.big_buck_bunny))
            holder = surfaceView.holder
            holder?.setFormat(PixelFormat.TRANSPARENT)
            holder?.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    player.setDisplay(holder)
                }
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {}
                override fun surfaceDestroyed(holder: SurfaceHolder) {}
            })
            player.prepare()
            player.setOnBufferingUpdateListener{_, percent -> println(percent)}
            player.setOnCompletionListener {
                timerPaused = true
                btn.setImageBitmap(imgStart)
            }
            progressText.text = "${player.currentPosition/1000}/${player.duration/1000}"

        } catch (e : IOException) {
            e.printStackTrace()
        }

        btn.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                timerPaused = true
                btn.setImageBitmap(imgStart)
            } else {
                player.start()
                timerPaused = false
                btn.setImageBitmap(imgPause)
            }
        }

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player.seekTo(player.duration * progress / 100)
                    progressText.text = "${player.currentPosition/1000}/${player.duration/1000}"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                player.start()
                player.pause()
                timerPaused = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                player.start()
                timerPaused = false
                btn.setImageBitmap(imgPause)
            }
        })

        return view
    }

    override fun onDestroy() {
        timer.cancel()
        Log.d("VideoFragment", "onDestroy")
        super.onDestroy()
    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Log.d("VideoFragment", "orientation: ORIENTATION_LANDSCAPE")
//        } else {
//            Log.d("VideoFragment", "orientation: not ORIENTATION_LANDSCAPE")
//        }
//    }

}